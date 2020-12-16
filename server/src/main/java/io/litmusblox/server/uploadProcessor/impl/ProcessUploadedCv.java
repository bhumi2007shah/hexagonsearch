/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.CvParserResponseBean;
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.service.impl.CvRatingRequestBean;
import io.litmusblox.server.uploadProcessor.IProcessUploadedCV;
import io.litmusblox.server.utils.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class that has the methods to process the files and that will be triggered by scheduler
 *
 * @author : Shital Raval
 * Date : 21/8/19
 * Time : 1:09 PM
 * Class Name : ProcessUploadedCv
 * Project Name : server
 */
@Log4j2
@Service
public class ProcessUploadedCv implements IProcessUploadedCV {

    @Autowired
    Environment environment;

    @Resource
    CvParsingDetailsRepository cvParsingDetailsRepository;

    @Resource
    JobSkillsAttributesRepository jobSkillsAttributesRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    UserRepository userRepository;

    @Resource
    JobRepository jobRepository;

    @Resource
    CandidateRepository candidateRepository;

    @Resource
    AsyncOperationsErrorRecordsRepository asyncOperationsErrorRecordsRepository;

    @Autowired
    IJobCandidateMappingService jobCandidateMappingService;

    private static final String PARSING_RESPONSE_JSON = "PARSING_RESPONSE_JSON";
    private static final String PARSING_RESPONSE_PYTHON = "PARSING_RESPONSE_PYTHON";
    private static final String PARSING_RESPONSE_ML = "PARSING_RESPONSE_ML";

    /**
     * Method that will be called by scheduler
     * process all cv from temp folder (DragAndDrop, MassMail, JObPosting)
     *
     * @throws Exception
     */

    @Override
    public void processCv() {
        try{
            File directoryPath = new File(environment.getProperty(IConstant.TEMP_REPO_LOCATION));
            File filesList[] = directoryPath.listFiles();
            String candidateSource = null;
            String tempFolderName = null;
            for (File file : filesList){
                if(IConstant.JOB_POSTING.equals(file.getName())){
                    candidateSource = IConstant.CandidateSource.NaukriJobPosting.getValue();
                    tempFolderName = IConstant.JOB_POSTING;
                } else if(IConstant.MASS_MAIL.equals(file.getName())) {
                    candidateSource = IConstant.CandidateSource.NaukriMassMail.getValue();
                    tempFolderName = IConstant.MASS_MAIL;
                } else if(IConstant.DRAG_AND_DROP.equals(file.getName())) {
                    candidateSource = IConstant.CandidateSource.DragDropCv.getValue();
                    tempFolderName = IConstant.DRAG_AND_DROP;
                } else if(IConstant.GENERIC_EMAIL.equals(file.getName())){
                    candidateSource = IConstant.CandidateSource.GenericEmail.getValue();
                    tempFolderName = IConstant.GENERIC_EMAIL;
                }

                Stream<Path> filePathStream= Files.walk(Paths.get(file.getAbsolutePath()));
                String finalCandidateSource = candidateSource;
                String finalTempFolderName = tempFolderName;
                filePathStream.forEach(filePath -> {
                            if (Files.isRegularFile(filePath)) {
                                processSingleCv(filePath, finalCandidateSource, finalTempFolderName);
                            }
                });
            }

        } catch (Exception e) {
            log.info("Error while processing temp location files : "+e.getMessage());
        }
    }

    /**
     * Process cv by python API
     * @param filePath cv file path
     * @param candidateSource from which source candidate upload(DraDrop, MassMail or JobPosting)
     */
    private void processSingleCv(Path filePath, String candidateSource, String tempFolderName) {
        log.info("Inside processSingleCv");
        log.info("Temp folder Cv path : " + filePath.getFileName());
        RestClient rest = RestClient.getInstance();
        AtomicInteger statusCode = new AtomicInteger();
        AtomicReference<String> pythonResponse = new AtomicReference<>();
        AtomicReference<CvParsingDetails> cvParsingDetails = new AtomicReference<>();
        String fileName = filePath.toString().substring(filePath.toString().lastIndexOf(File.separator) + 1);
        Long jobIdFromFileName = null, userIdFromFileName = null;
        User user = null;
        try {
            String[] fileNameParts = fileName.split("_");
            jobIdFromFileName = Long.parseLong(fileNameParts[1]);
            userIdFromFileName = Long.parseLong(fileNameParts[0]);
            user = userRepository.findById(userIdFromFileName).orElse(null);
            if(null == user) {
                log.error("No user id found for "+userIdFromFileName+" moving "+fileName+" to "+environment.getProperty(IConstant.REPO_LOCATION)+IConstant.ERROR_FILES+"/fileNameWithoutJobId");
                throw new WebException(IErrorMessages.USER_NOT_FOUND + userIdFromFileName, HttpStatus.UNPROCESSABLE_ENTITY);
            }
            if(null == jobRepository.findById(jobIdFromFileName)) {
                log.error("No job id found for "+jobIdFromFileName+" moving "+fileName+" to "+environment.getProperty(IConstant.REPO_LOCATION)+IConstant.ERROR_FILES+"/fileNameWithoutJobId");
                throw new WebException(IErrorMessages.JOB_NOT_FOUND + jobIdFromFileName, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } catch (Exception e){
            moveFile(jobIdFromFileName, user, filePath, "fileNameWithoutJobId/");
        }
        Map headerInformation = LoggedInUserInfoUtil.getLoggedInUserJobInformation(jobIdFromFileName);
        AtomicReference<CvParserResponseBean> cvParserResponseBean = new AtomicReference<CvParserResponseBean>();
        Map<String, List<String>> neighbourSkillMap = new HashMap<>();
        long PythonStartTime = System.currentTimeMillis();
        StringBuffer queryString = new StringBuffer(environment.getProperty("parserBaseUrl")+environment.getProperty("pythonParseCv"));
        queryString.append("?file=");
        queryString.append(environment.getProperty(IConstant.FILE_STORAGE_URL)+tempFolderName+"/"+ fileName);
        try {
            List<JobSkillsAttributes> jdKeySkills = jobSkillsAttributesRepository.findByJobId(jobIdFromFileName);
            if (jdKeySkills.size() != 0) {
                jdKeySkills.forEach(jobSkillsAttributes -> {
                    if (null != jobSkillsAttributes.getSkillId())
                        if(jobSkillsAttributes.isSelected())
                            neighbourSkillMap.put(jobSkillsAttributes.getSkillId().getSkillName(), (null != jobSkillsAttributes.getNeighbourSkills()) ? Arrays.asList(jobSkillsAttributes.getNeighbourSkills()) : new ArrayList<>());
                });
            }
            CvRatingRequestBean cvRatingRequestBean = new CvRatingRequestBean(neighbourSkillMap);
            ObjectMapper objectMapper = new ObjectMapper();
            RestClientResponseBean restResponseBean = rest.consumeRestApi(objectMapper.writeValueAsString(cvRatingRequestBean), queryString.toString(), HttpMethod.POST, null,null, java.util.Optional.of(IConstant.REST_READ_TIME_OUT_FOR_CV_TEXT), Optional.of(headerInformation));
            statusCode.set(restResponseBean.getStatusCode());
            cvParserResponseBean.set(objectMapper.readValue(restResponseBean.getResponseBody(), CvParserResponseBean.class));
            pythonResponse.set(String.valueOf(cvParserResponseBean.get().getCandidate()));
            log.info("Python parser response : {}",pythonResponse.get());
            if(HttpStatus.OK.value() != statusCode.get()){
                asyncOperationsErrorRecordsRepository.save(new AsyncOperationsErrorRecords(jobIdFromFileName, null, null, null, null, pythonResponse.get(), IConstant.ASYNC_OPERATIONS.DragDrop.name(), user, new Date(), fileName));
                moveFile(jobIdFromFileName, user, filePath, "");
            }
            if(null == cvParsingDetails.get()){
                cvParsingDetails.set(new CvParsingDetails());
            }
            cvParsingDetails.get().setParsingResponsePython(pythonResponse.get());
            cvParsingDetails.get().setProcessingTime(System.currentTimeMillis() - PythonStartTime);
            log.info("Received response from Python parser in {}ms.", cvParsingDetails.get().getProcessingTime());
        } catch (Exception e) {
            log.error("Error while parse resume by Python parser : {}", Util.getStackTrace(e));
        }
        try{
            Long jcmId = addCandidate(cvParserResponseBean.get(), cvParsingDetails.get(), jobIdFromFileName, filePath.toString(), userIdFromFileName, candidateSource, statusCode.get());
        }catch (Exception exception){
            moveFile(jobIdFromFileName, user, filePath, "");
            log.error("Error in add candidate : {}",exception.getMessage());
        }
        log.info("Completed processing " + filePath.toString());
    }

    private void moveFile(Long jobId, User user,Path filePath,String location){
        File file = new File(String.valueOf(filePath));
        try {
            StoreFileUtil.storeFile(Util.createMultipartFile(file), jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.ERROR_FILES+"/"+location, null, user);
        } catch (Exception e) { e.printStackTrace(); }
        file.delete();
    }


    /**
     * Private method to convert python response string candidate and upload candidate
     * @param responseFromPython candidate object
     * @param jobId In which job we upload candidate
     * @param filePath cv file path
     * @param userId user whose upload this cv
     * @param candidateSource from which source candidate upload(DraDrop, MassMail or JobPosting)
     */
    private Long addCandidate(CvParserResponseBean responseFromPython, CvParsingDetails cvParsingDetails, Long jobId, String filePath, Long userId, String candidateSource, int statusCode){
        log.info("Inside addCandidate");
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        JobCandidateMapping jobCandidateMapping = null;
        if (null != cvParsingDetails) {

            UploadResponseBean uploadResponseBean = null;
            Long candidateId = null;
            String errorMessage = null;
            Map<String, String> breadCrumb = new HashMap<>();
            if(HttpStatus.OK.value() == statusCode){
                try {
                    Job jobFromDb = jobRepository.getOne(jobId);
                    breadCrumb.put("JobId", jobFromDb.getId().toString());
                    breadCrumb.put("UserId", userId.toString());
                    breadCrumb.put("FileName", fileName);
                    breadCrumb.put("CandidateSource", candidateSource);
                    responseFromPython.getCandidate().setCandidateSource(candidateSource);
                    if (Util.isNull(responseFromPython.getCandidate().getEmail()) || !Util.isValidateEmail(responseFromPython.getCandidate().getEmail(), Optional.of(responseFromPython.getCandidate())))
                        responseFromPython.getCandidate().setEmail("notavailable" + new Date().getTime() + IConstant.NOT_AVAILABLE_EMAIL);

                    if (null == responseFromPython.getCandidate().getCandidateName())
                        responseFromPython.getCandidate().setCandidateName(IConstant.NOT_AVAILABLE);

                    if (null == responseFromPython.getCandidate().getFirstName() || !Util.validateName(responseFromPython.getCandidate().getFirstName()))
                        responseFromPython.getCandidate().setFirstName(IConstant.NOT_FIRST_NAME);

                    if (null == responseFromPython.getCandidate().getLastName() || !Util.validateName(responseFromPython.getCandidate().getLastName()))
                        responseFromPython.getCandidate().setLastName(IConstant.NOT_LAST_NAME);

                    if(Util.isNotNull(responseFromPython.getCandidate().getMobile())){
                        String validMobile = Util.indianMobileConvertor(responseFromPython.getCandidate().getMobile(), jobFromDb.getCompanyId().getCountryId().getCountryCode());
                        if(!Util.validateMobile(validMobile, jobFromDb.getCompanyId().getCountryId().getCountryCode(), Optional.of(responseFromPython.getCandidate())))
                            responseFromPython.getCandidate().setMobile(null);
                    }

                    if (null != responseFromPython.getCandidate().getAlternateMobile() && responseFromPython.getCandidate().getAlternateMobile().length() == 0)
                        responseFromPython.getCandidate().setAlternateMobile(null);

                    uploadResponseBean = jobCandidateMappingService.uploadIndividualCandidate(Arrays.asList(responseFromPython.getCandidate()), jobId, ((null != responseFromPython.getCandidate().getMobile())?false:true), userRepository.findById(userId), true);
                    if(uploadResponseBean.getStatus().equals(IConstant.UPLOAD_STATUS.Success.name())){
                        candidateId = uploadResponseBean.getSuccessfulCandidates().get(0).getId();
                    }else if(uploadResponseBean.getStatus().equals(IConstant.UPLOAD_STATUS.Failure.name()) && null != uploadResponseBean.getFailedCandidates().get(0).getId()){
                        candidateId = uploadResponseBean.getFailedCandidates().get(0).getId();
                    }
                } catch (Exception e) {
                    log.info(Util.getStackTrace(e));
                    errorMessage = ((ValidationException) e).getErrorMessage();
                    log.error("Error while upload candidate via python response : " + e.getMessage());
                }
            }else{
                errorMessage = "File parsing had an error";
                cvParsingDetails.setParsingResponsePython(null);
                SentryUtil.logWithStaticAPI(null, errorMessage, breadCrumb);
            }
            try {
                cvParsingDetails.setCvFileName(fileName);
                cvParsingDetails.setProcessedOn(new Date());
                File file = new File(filePath);

                if (null != candidateId) {
                    jobCandidateMapping = jobCandidateMappingRepository.findByJobIdAndCandidateId(jobId, candidateId);
                    jobCandidateMapping.setCvFileType("." + Util.getFileExtension(fileName));
                    if(null != responseFromPython.getCvRatingResponseWrapper()) {
                        jobCandidateMapping.setOverallRating(responseFromPython.getCvRatingResponseWrapper().overallRating);
                        if (null != (responseFromPython.getCvRatingResponseWrapper().cvRatingResponse))
                           jobCandidateMapping.setCvSkillRatingJson(responseFromPython.getCvRatingResponseWrapper().cvRatingResponse);
                    }
                    jobCandidateMappingRepository.save(jobCandidateMapping);
                    cvParsingDetails.setProcessingStatus(IConstant.UPLOAD_STATUS.Success.name());
                    cvParsingDetails.setCandidateId(candidateId);
                    cvParsingDetails.setJobCandidateMappingId(jobCandidateMapping);

                    //StringBuffer errorFile = new StringBuffer(environment.getProperty(IConstant.REPO_LOCATION));
                    //errorFile.append(File.separator).append(IConstant.ERROR_FILES_REPO_LOCATION).append(File.separator).append(fileName);
                    Candidate candidate = Candidate.builder().id(candidateId).build();
                    StoreFileUtil.storeFile(Util.createMultipartFile(file), jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(), candidate, null);
                }else {
                    cvParsingDetails.setProcessingStatus(IConstant.UPLOAD_STATUS.Failure.name());
                    if(null != uploadResponseBean && null != uploadResponseBean.getFailedCandidates() && uploadResponseBean.getFailedCandidates().size()>0 && null != uploadResponseBean.getFailedCandidates().get(0).getUploadErrorMessage())
                        cvParsingDetails.setErrorMessage(uploadResponseBean.getFailedCandidates().get(0).getUploadErrorMessage());
                    else
                        cvParsingDetails.setErrorMessage(errorMessage);

                    if(null != uploadResponseBean && null != uploadResponseBean.getFailedCandidates() && uploadResponseBean.getFailedCandidates().size()>0 && null != uploadResponseBean.getFailedCandidates().get(0).getId())
                        StoreFileUtil.storeFile(Util.createMultipartFile(file), jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(), uploadResponseBean.getFailedCandidates().get(0), null);
                    else
                        StoreFileUtil.storeFile(Util.createMultipartFile(file), jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.ERROR_FILES, null, userRepository.findById(userId).orElse(null));
                }
                file.delete();
            }catch (Exception e){
                log.info(Util.getStackTrace(e));
                log.error("Error while save candidate cv : " + e.getMessage());
            }
            cvParsingDetailsRepository.save(cvParsingDetails);
        }
        return null != jobCandidateMapping?jobCandidateMapping.getId():0;
    }
    @Override
    public void updateCvRating() {
        List<CvParsingDetails> cvToRateList = cvParsingDetailsRepository.getDataForUpdateCvRating();
        cvToRateList.stream().forEach(cvToRate -> {
            try {
                boolean processingError = false;
                long cvRatingApiProcessingTime = -1;
                if(null != cvToRate.getJobCandidateMappingId()) {
                    Map<String, List<String>> neighbourSkillMap = new HashMap<>();
                    //call rest api with the text part of cv
                    log.info("Processing CV for job id: " + cvToRate.getJobCandidateMappingId().getJob().getId() + " and candidate id: " + cvToRate.getJobCandidateMappingId().getCandidate().getId());
                    List<JobSkillsAttributes> jdKeySkills = jobSkillsAttributesRepository.findByJobId(cvToRate.getJobCandidateMappingId().getJob().getId());
                    if (jdKeySkills.size() == 0)
                        log.error("Found no key skills for jobId: {}.  Not making api call to rate CV.", cvToRate.getJobCandidateMappingId().getJob().getId());
                    else {
                        jdKeySkills.forEach(jobSkillsAttributes -> {
                            if(null != jobSkillsAttributes.getSkillId())
                                neighbourSkillMap.put(jobSkillsAttributes.getSkillId().getSkillName(), (null != jobSkillsAttributes.getNeighbourSkills())?Arrays.asList(jobSkillsAttributes.getNeighbourSkills()):new ArrayList<>());
                        });
                        try {
                            cvRatingApiProcessingTime = callCvRatingApi(cvToRate, new CvRatingRequestBean(neighbourSkillMap));

                        } catch (Exception e) {
                            log.info("Error while performing CV rating operation " + Util.getStackTrace(e));
                            cvToRate.setCvRatingApiCallTRetryCount(cvToRate.getCvRatingApiCallTRetryCount()+1);
                            processingError = true;
                        }
                    }
                    if (!processingError || cvToRate.getCvRatingApiCallTRetryCount().equals(3)) {
                        cvToRate.setCvRatingApiFlag(true);
                    }
                    cvToRate.setCvRatingApiResponseTime(cvRatingApiProcessingTime);
                    cvParsingDetailsRepository.save(cvToRate);  
                }
                else {
                    log.error("JCM Id not set for CV Parsing details record with id: " + cvToRate.getId());
                }
            }catch(Exception ex) {
                log.error("Error processing record to rate cv with jcmId: " + cvToRate.getJobCandidateMappingId().getId() + "\n" + ex.getMessage());
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private long callCvRatingApi(CvParsingDetails cvParsingDetails, CvRatingRequestBean requestBean) throws Exception {
        AtomicInteger statusCode = new AtomicInteger();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        long jobId = cvParsingDetails.getJobCandidateMappingId().getJob().getId();
        long jcmId = cvParsingDetails.getJobCandidateMappingId().getId();
        Map headerInformation = LoggedInUserInfoUtil.getLoggedInUserJobInformation(jobId);
        StringBuffer queryString = new StringBuffer(environment.getProperty("parserBaseUrl")+environment.getProperty("pythonParseCv"));
        queryString.append("?file=");
        queryString.append(environment.getProperty(IConstant.CV_STORAGE_LOCATION)).append(cvParsingDetails.getJobCandidateMappingId().getJob().getId()).append("/").append(cvParsingDetails.getCandidateId()).append(cvParsingDetails.getJobCandidateMappingId().getCvFileType());
        long apiCallStartTime = System.currentTimeMillis();
        RestClientResponseBean restResponseBean = RestClient.getInstance().consumeRestApi(objectMapper.writeValueAsString(requestBean), queryString.toString(), HttpMethod.POST, null,null,java.util.Optional.of(IConstant.REST_READ_TIME_OUT_FOR_CV_TEXT),Optional.of(headerInformation));
        statusCode.set(restResponseBean.getStatusCode());
        if(HttpStatus.OK.value() != statusCode.get()) {
            cvParsingDetails.setCvRatingApiCallTRetryCount(Long.parseLong("3"));
            cvParsingDetails.setCvRatingApiFlag(true);
            cvParsingDetails.setErrorMessage(restResponseBean.getResponseBody());
            cvParsingDetailsRepository.save(cvParsingDetails);
        }
        String cvRatingResponse = restResponseBean.getResponseBody();
        log.info("Response received from CV Rating Api : {}, JcmId : {}", cvRatingResponse, jcmId);
        long apiCallEndTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        CvParserResponseBean responseBean = objectMapper.readValue(cvRatingResponse, CvParserResponseBean.class);
        Candidate candidate = responseBean.getCandidate();
        CvRatingResponseWrapper cvRatingResponseWrapper = responseBean.getCvRatingResponseWrapper();
        JobCandidateMapping jcmObj = jobCandidateMappingRepository.findById(jcmId).orElse(null);
        log.info("Update cv_rating for jcm id : "+jcmId);
        log.info("Old cv_rating : "+jcmObj.getOverallRating()+", New Rating : "+cvRatingResponseWrapper.overallRating);
        jcmObj.setOverallRating(cvRatingResponseWrapper.overallRating);
        jcmObj.setCvSkillRatingJson(cvRatingResponseWrapper.cvRatingResponse);
        jobCandidateMappingRepository.save(jcmObj);
        log.info("Time taken to update cv rating data " + (System.currentTimeMillis() - startTime) + "ms.");
        updateCandidateInfo(cvParsingDetails, candidate);
        return (apiCallEndTime - apiCallStartTime);
    }


    private void updateCandidateInfo(CvParsingDetails cvParsingDetailsFromDb, Candidate candidateFromPython){
        long startTime = System.currentTimeMillis();
        if (candidateFromPython.getCandidateSkillDetails().size()>0){
            Set<String> skills = candidateFromPython.getCandidateSkillDetails().stream().map(CandidateSkillDetails::getSkill).collect(Collectors.toSet());
            List<CandidateSkillDetails> candidateSkillDetails = new ArrayList<>();
            //Update candidate skills
            if(null != cvParsingDetailsFromDb.getJobCandidateMappingId().getCandidate() && null == cvParsingDetailsFromDb.getJobCandidateMappingId().getCandidate().getCandidateSkillDetails()){
                Candidate candidate = cvParsingDetailsFromDb.getJobCandidateMappingId().getCandidate();
                skills.forEach(candidateSkill ->{
                    CandidateSkillDetails candidateSkillDetail = new CandidateSkillDetails();
                    candidateSkillDetail.setSkill(candidateSkill);
                    candidateSkillDetail.setCandidateId(candidate.getId());
                    candidateSkillDetails.add(candidateSkillDetail);
                });
                candidate.setCandidateSkillDetails(candidateSkillDetails);
                candidateRepository.save(candidate);
            }
        }
        //If existing user have mail with @notavailable.io and mobile is null then call edit candidate to update email and mobile
        if(cvParsingDetailsFromDb.getJobCandidateMappingId().getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL) || null == cvParsingDetailsFromDb.getJobCandidateMappingId().getMobile()){
            String validMobile = null;
            boolean isEditCandidate = false;
            JobCandidateMapping jcmFromDb = cvParsingDetailsFromDb.getJobCandidateMappingId();
            log.info("Update edit candidate for candidateId : {}", cvParsingDetailsFromDb.getCandidateId());

            //Check if existing candidate email not available then set python response email
            if(null != candidateFromPython && cvParsingDetailsFromDb.getJobCandidateMappingId().getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL) && Util.isNotNull(candidateFromPython.getEmail()) && Util.isValidateEmail(candidateFromPython.getEmail(), Optional.of(candidateFromPython))){
                log.info("candidate old email : {}, python response email : {}", jcmFromDb.getEmail(), candidateFromPython.getEmail());
                cvParsingDetailsFromDb.getJobCandidateMappingId().setEmail(candidateFromPython.getEmail());
                isEditCandidate = true;
            }
            //Check if existing candidate mobile is null then set python response mobile
            if(null != candidateFromPython && Util.isNull(jcmFromDb.getMobile()) && Util.isNotNull(candidateFromPython.getMobile())){
                validMobile = Util.indianMobileConvertor(candidateFromPython.getMobile(), cvParsingDetailsFromDb.getJobCandidateMappingId().getCountryCode());
                if(Util.validateMobile(validMobile, cvParsingDetailsFromDb.getJobCandidateMappingId().getCountryCode(),Optional.of(candidateFromPython))){
                    log.info("candidate old mobile : {}, python response mobile : {}, For JcmId : {}", jcmFromDb.getMobile(), candidateFromPython.getMobile(), jcmFromDb.getId());
                    cvParsingDetailsFromDb.getJobCandidateMappingId().setMobile(validMobile);
                    isEditCandidate = true;
                }
            }

            //If flag isEditCandidate is true then call update email and mobile
            if(isEditCandidate)
                jobCandidateMappingService.updateOrCreateEmailMobile(cvParsingDetailsFromDb.getJobCandidateMappingId(), jcmFromDb, jcmFromDb.getCreatedBy());

            cvParsingDetailsRepository.save(cvParsingDetailsFromDb);
        }
        log.info("Time taken to update candidate info in " + (System.currentTimeMillis() - startTime) + "ms.");
    }
}