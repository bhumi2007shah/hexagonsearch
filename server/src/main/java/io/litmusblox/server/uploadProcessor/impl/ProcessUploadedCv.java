/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.ValidationException;
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
        String[] s = fileName.split("_");
        Map headerInformation = LoggedInUserInfoUtil.getLoggedInUserJobInformation(Long.parseLong(s[1]));
        AtomicReference<CvParserResponseBean> cvParserResponseBean = new AtomicReference<CvParserResponseBean>();
        Map<String, List<String>> neighbourSkillMap = new HashMap<>();
        long PythonStartTime = System.currentTimeMillis();
        StringBuffer queryString = new StringBuffer(environment.getProperty("parserBaseUrl")+environment.getProperty("pythonParseCv"));
        queryString.append("?file=");
        queryString.append(environment.getProperty(IConstant.FILE_STORAGE_URL)+tempFolderName+"/"+ fileName);
        try {
            List<JobSkillsAttributes> jdKeySkills = jobSkillsAttributesRepository.findByJobId(Long.parseLong(s[1]));
            if (jdKeySkills.size() == 0)
                log.error("Found no key skills for jobId: {}.  Not making api call to rate CV.", Long.parseLong(s[1]));
            else {
                jdKeySkills.forEach(jobSkillsAttributes -> {
                    if (null != jobSkillsAttributes.getSkillId())
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
            Long jcmId = addCandidate(cvParserResponseBean.get(), cvParsingDetails.get(),  Long.parseLong(s[1]), filePath.toString(), Long.parseLong(s[0]), candidateSource, statusCode.get());
        }catch (Exception exception){
            File file = new File(String.valueOf(filePath));
            try {
                StoreFileUtil.storeFile(Util.createMultipartFile(file), Long.parseLong(s[1]), environment.getProperty(IConstant.REPO_LOCATION), IConstant.ERROR_FILES, null, userRepository.findById(Long.parseLong(s[0])).orElse(null));
            } catch (Exception e) { e.printStackTrace(); }
            file.delete();
            log.error("Error in add candidate : {}",exception.getMessage());
        }
        log.info("Completed processing " + filePath.toString());
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
                            jobCandidateMapping.setCvSkillRatingJson(responseFromPython.getCvRatingResponseWrapper().cvRatingResponse.toString());
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
                    }
                    if (cvToRate.getCvRatingApiCallTRetryCount().equals(3)) {
                        cvToRate.setCvRatingApiFlag(true);
                        cvToRate.setCvRatingApiResponseTime(cvRatingApiProcessingTime);
                        cvParsingDetailsRepository.save(cvToRate);
                    }
                }
                else {
                    log.error("JCM Id not set for CV Parsing details record with id: " + cvToRate.getId());
                }
            }catch(Exception ex) {
                log.error("Error processing record to rate cv with jcmId: " + cvToRate.getJobCandidateMappingId().getId() + "\n" + ex.getMessage());
            }
        });
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