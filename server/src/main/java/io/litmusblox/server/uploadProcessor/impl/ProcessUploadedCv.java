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
import io.litmusblox.server.service.MasterDataBean;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.service.impl.CvRatingRequestBean;
import io.litmusblox.server.uploadProcessor.IProcessUploadedCV;
import io.litmusblox.server.uploadProcessor.RChilliCvProcessor;
import io.litmusblox.server.utils.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    RChilliCvProcessor rChilliCvProcessor;

    @Autowired
    Environment environment;

    @Value("${mlCvRatingUrl}")
    private String mlCvRatingUrl;

    @Resource
    CvParsingDetailsRepository cvParsingDetailsRepository;

    @Resource
    JobKeySkillsRepository jobKeySkillsRepository;

    @Resource
    CvRatingRepository cvRatingRepository;

    @Resource
    CvRatingSkillKeywordDetailsRepository cvRatingSkillKeywordDetailsRepository;

    @Resource
    CvParsingApiDetailsRepository cvParsingApiDetailsRepository;

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
     * Process cv by Rchilli, python and ml API
     * @param filePath cv file path
     * @param candidateSource from which source candidate upload(DraDrop, MassMail or JobPosting)
     */
    private void processSingleCv(Path filePath, String candidateSource, String tempFolderName) {
        log.info("Inside processSingleCv");
        log.info("Temp folder Cv path : " + filePath.getFileName());
        AtomicReference<Candidate> candidate = new AtomicReference<>();
        RestClient rest = RestClient.getInstance();
        AtomicInteger statusCode = new AtomicInteger();
        AtomicReference<String> pythonResponse = new AtomicReference<>();
        String fileName = filePath.toString().substring(filePath.toString().lastIndexOf(File.separator) + 1);
        String[] s = fileName.split("_");
        Map headerInformation = LoggedInUserInfoUtil.getLoggedInUserJobInformation(Long.parseLong(s[1]));
        AtomicReference<CvParserResponseBean> cvParserResponseBean = null;
        cvParsingApiDetailsRepository.findAllByActiveOrderByApiSequenceAsc(true).forEach(cvParsingApiDetails -> {
            Map<String, List<String>> neighbourSkillMap = new HashMap<>();
            switch (cvParsingApiDetails.getColumnToUpdate()) {
                case PARSING_RESPONSE_JSON:
                    candidate.set(rChilliCvProcessor.processFile(filePath.toString(), null, cvParsingApiDetails));
                    break;
                case PARSING_RESPONSE_PYTHON:
                    long PythonStartTime = System.currentTimeMillis();
                    StringBuffer queryString = new StringBuffer(cvParsingApiDetails.getApiUrl());
                    queryString.append("?file=");
                    queryString.append(environment.getProperty(IConstant.FILE_STORAGE_URL)+tempFolderName+"/"+ fileName);
                    try {
                        List<JobKeySkills> jdKeySkills = jobKeySkillsRepository.findByJobId(Long.parseLong(s[1]));
                        if (jdKeySkills.size() == 0)
                            log.error("Found no key skills for jobId: {}.  Not making api call to rate CV.", Long.parseLong(s[1]));
                        else {
                            jdKeySkills.forEach(jobKeySkills -> {
                                if (null != jobKeySkills.getSkillId())
                                    neighbourSkillMap.put(jobKeySkills.getSkillId().getSkillName(), (null != jobKeySkills.getNeighbourSkills()) ? Arrays.asList(jobKeySkills.getNeighbourSkills()) : new ArrayList<>());
                            });
                        }
                        CvRatingRequestBean cvRatingRequestBean = new CvRatingRequestBean(neighbourSkillMap);
                        ObjectMapper objectMapper = new ObjectMapper();
                        RestClientResponseBean restResponseBean = rest.consumeRestApi(objectMapper.writeValueAsString(cvRatingRequestBean), queryString.toString(), HttpMethod.POST, null,null, java.util.Optional.of(IConstant.REST_READ_TIME_OUT_FOR_CV_TEXT), Optional.of(headerInformation));
                        statusCode.set(restResponseBean.getStatusCode());
                        cvParserResponseBean.set(objectMapper.readValue(restResponseBean.getResponseBody(), CvParserResponseBean.class));
                        pythonResponse.set(cvParserResponseBean.get().getCandidate().toString());
                        log.info("Python parser response : {}",pythonResponse.get());
                        if(null == candidate.get() || null == candidate.get().getCvParsingDetails()){
                            candidate.set(new Candidate());
                            candidate.get().setCvParsingDetails(new CvParsingDetails());
                        }
                        candidate.get().getCvParsingDetails().setParsingResponsePython(pythonResponse.get());
                        candidate.get().getCvParsingDetails().setProcessingTime(System.currentTimeMillis() - PythonStartTime);
                        log.info("Received response from Python parser in {}ms.", candidate.get().getCvParsingDetails().getProcessingTime());
                    } catch (Exception e) {
                        log.error("Error while parse resume by Python parser : {}", e.getMessage());
                    }
                    break;
                case PARSING_RESPONSE_ML:
                    long mlStartTime = System.currentTimeMillis();
                    StringBuffer queryObjectString = new StringBuffer("{");
                    queryObjectString.append("\"link\":");
                    queryObjectString.append("\"" + environment.getProperty(IConstant.FILE_STORAGE_URL)+tempFolderName+"/"+ fileName + "\"");
                    queryObjectString.append("}");
                    try {
                        candidate.get().getCvParsingDetails().setParsingResponseMl(rest.consumeRestApi(queryObjectString.toString(), cvParsingApiDetails.getApiUrl(), HttpMethod.POST, null,null,null,Optional.of(headerInformation)).getResponseBody());
                        log.info("Received response from ML parser in {}ms.", (System.currentTimeMillis() - mlStartTime));
                    } catch (Exception e) {
                        log.error("Error while parse resume by ML parser : {}", e.getMessage());
                    }
                    break;
            }
        });
        try{
            Long jcmId = addCandidate(candidate.get(), Long.parseLong(s[1]), filePath.toString(), Long.parseLong(s[0]), candidateSource, statusCode.get());
           //Add CV rating for candidate
            if(jcmId.equals(0) && null != cvParserResponseBean.get()){
                addCvRating(jcmId, cvParserResponseBean.get().getCvRatingResponseWrapper());
            }
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
     * @param candidate candidate object which have only cvParsing detail object
     * @param jobId In which job we upload candidate
     * @param filePath cv file path
     * @param userId user whose upload this cv
     * @param candidateSource from which source candidate upload(DraDrop, MassMail or JobPosting)
     */
    private Long addCandidate(Candidate candidate, Long jobId, String filePath, Long userId, String candidateSource, int statusCode){
        log.info("Inside addCandidate");
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        JobCandidateMapping jobCandidateMapping = null;
        if (null != candidate && null != candidate.getCvParsingDetails()) {

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
                    Candidate candidateFromPython = new ObjectMapper().readValue(candidate.getCvParsingDetails().getParsingResponsePython(), Candidate.class);
                    candidateFromPython.setCandidateSource(candidateSource);
                    if (Util.isNull(candidateFromPython.getEmail()) || !Util.isValidateEmail(candidateFromPython.getEmail(), Optional.of(candidateFromPython)))
                        candidateFromPython.setEmail("notavailable" + new Date().getTime() + IConstant.NOT_AVAILABLE_EMAIL);

                    if (null == candidateFromPython.getCandidateName())
                        candidateFromPython.setCandidateName(IConstant.NOT_AVAILABLE);

                    if (null == candidateFromPython.getFirstName() || !Util.validateName(candidateFromPython.getFirstName()))
                        candidateFromPython.setFirstName(IConstant.NOT_FIRST_NAME);

                    if (null == candidateFromPython.getLastName() || !Util.validateName(candidateFromPython.getLastName()))
                        candidateFromPython.setLastName(IConstant.NOT_LAST_NAME);

                    if(Util.isNotNull(candidateFromPython.getMobile())){
                        String validMobile = Util.indianMobileConvertor(candidateFromPython.getMobile(), jobFromDb.getCompanyId().getCountryId().getCountryCode());
                        if(!Util.validateMobile(validMobile, jobFromDb.getCompanyId().getCountryId().getCountryCode(), Optional.of(candidateFromPython)))
                            candidateFromPython.setMobile(null);
                    }

                    if (null != candidateFromPython.getAlternateMobile() && candidateFromPython.getAlternateMobile().length() == 0)
                        candidateFromPython.setAlternateMobile(null);

                    uploadResponseBean = jobCandidateMappingService.uploadIndividualCandidate(Arrays.asList(candidateFromPython), jobId, ((null != candidateFromPython.getMobile())?false:true), userRepository.findById(userId), true);
                    if(uploadResponseBean.getStatus().equals(IConstant.UPLOAD_STATUS.Success.name())){
                        candidateId = uploadResponseBean.getSuccessfulCandidates().get(0).getId();
                    }
                } catch (Exception e) {
                    log.info(Util.getStackTrace(e));
                    errorMessage = ((ValidationException) e).getErrorMessage();
                    log.error("Error while upload candidate via python response : " + e.getMessage());
                }
            }else{
                errorMessage = "File parsing had an error";
                candidate.getCvParsingDetails().setParsingResponsePython(null);
                SentryUtil.logWithStaticAPI(null, errorMessage, breadCrumb);
            }
            try {
                candidate.getCvParsingDetails().setCvFileName(fileName);
                candidate.getCvParsingDetails().setProcessedOn(new Date());
                File file = new File(filePath);

                if (null != candidateId) {
                    jobCandidateMapping = jobCandidateMappingRepository.findByJobIdAndCandidateId(jobId, candidateId);
                    jobCandidateMapping.setCvFileType("." + Util.getFileExtension(fileName));
                    jobCandidateMappingRepository.save(jobCandidateMapping);
                    candidate.getCvParsingDetails().setProcessingStatus(IConstant.UPLOAD_STATUS.Success.name());
                    candidate.getCvParsingDetails().setCandidateId(candidateId);
                    candidate.getCvParsingDetails().setJobCandidateMappingId(jobCandidateMapping);

                    //StringBuffer errorFile = new StringBuffer(environment.getProperty(IConstant.REPO_LOCATION));
                    //errorFile.append(File.separator).append(IConstant.ERROR_FILES_REPO_LOCATION).append(File.separator).append(fileName);
                    StoreFileUtil.storeFile(Util.createMultipartFile(file), jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(), uploadResponseBean.getSuccessfulCandidates().get(0), null);
                }else {
                    candidate.getCvParsingDetails().setProcessingStatus(IConstant.UPLOAD_STATUS.Failure.name());
                    if(null != uploadResponseBean && null != uploadResponseBean.getFailedCandidates() && uploadResponseBean.getFailedCandidates().size()>0 && null != uploadResponseBean.getFailedCandidates().get(0).getUploadErrorMessage())
                        candidate.getCvParsingDetails().setErrorMessage(uploadResponseBean.getFailedCandidates().get(0).getUploadErrorMessage());
                    else
                        candidate.getCvParsingDetails().setErrorMessage(errorMessage);

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
            cvParsingDetailsRepository.save(candidate.getCvParsingDetails());
        }
        return null != jobCandidateMapping?jobCandidateMapping.getId():0;
    }

    private void addCvRating(Long jcmId, CvRatingResponseWrapper responseWrapper){
        long startTime = System.currentTimeMillis();
        CvRating cvRatingFromDb = cvRatingRepository.findByJobCandidateMappingId(jcmId);
        if(null == cvRatingFromDb){
            cvRatingFromDb = cvRatingRepository.save(new CvRating(jcmId, responseWrapper.getCvRatingResponse().getOverallRating()));
            cvRatingSkillKeywordDetailsRepository.saveAll(convertToCvRatingSkillKeywordDetails(responseWrapper.getCvRatingResponse().getKeywords(), cvRatingFromDb.getId()));
        }else{
            log.info("Update cv_rating id : "+cvRatingFromDb.getId()+", For jcm id : "+jcmId);
            log.info("Old cv_rating : "+cvRatingFromDb.getOverallRating()+", New Rating : "+responseWrapper.getCvRatingResponse().getOverallRating());
            cvRatingFromDb.setOverallRating(responseWrapper.getCvRatingResponse().getOverallRating());
            cvRatingRepository.save(cvRatingFromDb);
        }
        log.info("Time taken to save cv rating data : " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    /**
     * Method that will be called by scheduler
     * All eligible records of CV will be run against CV rating api and rated
     */
   // @Transactional
    public void rateCv() {
        List<CvParsingDetails> cvToRateList = cvParsingDetailsRepository.findCvRatingRecordsToProcess(IConstant.CV_TEXT_API_RESPONSE_MIN_LENGTH);
        log.info("Found " + cvToRateList.size() + " records for CV rating process");

        cvToRateList.stream().forEach(cvToRate -> {
            try {
                boolean processingError = false;
                long cvRatingApiProcessingTime = -1;
                if(null != cvToRate.getJobCandidateMappingId()) {
                    Map<String, List<String>> neighbourSkillMap = new HashMap<>();
                    //call rest api with the text part of cv
                    log.info("Processing CV for job id: " + cvToRate.getJobCandidateMappingId().getJob().getId() + " and candidate id: " + cvToRate.getJobCandidateMappingId().getCandidate().getId());
                    List<JobKeySkills> jdKeySkills = jobKeySkillsRepository.findByJobId(cvToRate.getJobCandidateMappingId().getJob().getId());
                    if (jdKeySkills.size() == 0)
                        log.error("Found no key skills for jobId: {}.  Not making api call to rate CV.", cvToRate.getJobCandidateMappingId().getJob().getId());
                    else {
                        jdKeySkills.forEach(jobKeySkills -> {
                            if(null != jobKeySkills.getSkillId())
                                neighbourSkillMap.put(jobKeySkills.getSkillId().getSkillName(), (null != jobKeySkills.getNeighbourSkills())?Arrays.asList(jobKeySkills.getNeighbourSkills()):new ArrayList<>());
                        });

                        try {
                            if(null != cvToRate.getCandidateSkills()){}
                                // cvRatingApiProcessingTime = callCvRatingApi(new CvRatingRequestBean(neighbourSkillMap, Arrays.asList(cvToRate.getCandidateSkills()), cvToRate.getJobCandidateMappingId().getJob().getFunction().getFunction()), cvToRate.getJobCandidateMappingId().getId());

                        } catch (Exception e) {
                            log.info("Error while performing CV rating operation " + Util.getStackTrace(e));
                            cvToRate.setCvRatingApiCallTRetryCount(cvToRate.getCvRatingApiCallTRetryCount()+1);
                            processingError = true;
                        }
                    }
                    if (!processingError || cvToRate.getCvRatingApiCallTRetryCount().equals(3)) {
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

    /**
     * Method to convert cv file to cv text
     * In cv_parsing_detail if parsing_response_text is null then convert cv to text and save
     */
    public void cvToCvText() {
        log.info("inside CvToCvText");
        List<CvParsingDetails> cvParsingDetails = cvParsingDetailsRepository.getDataForConvertCvToCvText();
        if(null != cvParsingDetails && cvParsingDetails.size()>0){
            cvParsingDetails.forEach(cvParsingDetailsFromDb-> {
                cvParsingDetailsFromDb = processCvForCvToText(cvParsingDetailsFromDb);
                cvParsingDetailsFromDb.setCvConvertApiFlag(true);
                cvParsingDetailsRepository.save(cvParsingDetailsFromDb);
            });
        }
    }

    /**
     * Method to update cv rating for cv
     * In cv_parsing_detail if cv rating flag is false then get cv rating and save
     */
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
                    List<JobKeySkills> jdKeySkills = jobKeySkillsRepository.findByJobId(cvToRate.getJobCandidateMappingId().getJob().getId());
                    if (jdKeySkills.size() == 0)
                        log.error("Found no key skills for jobId: {}.  Not making api call to rate CV.", cvToRate.getJobCandidateMappingId().getJob().getId());
                    else {
                        jdKeySkills.forEach(jobKeySkills -> {
                            if(null != jobKeySkills.getSkillId())
                                neighbourSkillMap.put(jobKeySkills.getSkillId().getSkillName(), (null != jobKeySkills.getNeighbourSkills())?Arrays.asList(jobKeySkills.getNeighbourSkills()):new ArrayList<>());
                        });

                        try {
                            if(null != cvToRate.getCandidateSkills())
                                cvRatingApiProcessingTime = callCvRatingApi(cvToRate, new CvRatingRequestBean(neighbourSkillMap, Arrays.asList(cvToRate.getCandidateSkills()), cvToRate.getJobCandidateMappingId().getJob().getFunction().getFunction()));

                        } catch (Exception e) {
                            log.info("Error while performing CV rating operation " + Util.getStackTrace(e));
                            cvToRate.setCvRatingApiCallTRetryCount(cvToRate.getCvRatingApiCallTRetryCount()+1);
                            processingError = true;
                        }
                    }
                    if (!processingError || cvToRate.getCvRatingApiCallTRetryCount().equals(3)) {
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

    @Transactional
    private CvParsingDetails processCvForCvToText(CvParsingDetails cvParsingDetailsFromDb){
        log.info("Inside processCvForCvToText");
        String cvToTextResponse = null;
        Map<String, String> cvToTextMap = new HashMap<>();
        Candidate candidateFromPython = null;
        long responseTime = 0L;
        long jobId = (cvParsingDetailsFromDb.getJobCandidateMappingId().getJob().getId());
        Map headerInformation = LoggedInUserInfoUtil.getLoggedInUserJobInformation(jobId);
        Map<String, String> queryParameters = new HashMap<>();
        Map<String, String> breadCrumb = new HashMap<>();
        breadCrumb.put("cvParsingDetailsId", cvParsingDetailsFromDb.getId().toString());
        breadCrumb.put("Jcm id", cvParsingDetailsFromDb.getJobCandidateMappingId().getId().toString());
        try {
            queryParameters.put("file", environment.getProperty(IConstant.CV_STORAGE_LOCATION) + cvParsingDetailsFromDb.getJobCandidateMappingId().getJob().getId() + "/" + cvParsingDetailsFromDb.getCandidateId() + cvParsingDetailsFromDb.getJobCandidateMappingId().getCvFileType());
            log.info("Cv storage file path : {}", queryParameters.get("file"));
            breadCrumb.put("FilePath", queryParameters.get("file"));
            long apiCallStartTime = System.currentTimeMillis();
            //Call to cv parser for convert cv to cvText
            cvToTextResponse = RestClient.getInstance().consumeRestApi(null, environment.getProperty("parserBaseUrl")+environment.getProperty("pythonCvParserUrl"), HttpMethod.GET, null, Optional.of(queryParameters), Optional.of(MasterDataBean.getInstance().getRestReadTimeoutForCvParser()), Optional.of(headerInformation)).getResponseBody();
            responseTime = System.currentTimeMillis() - apiCallStartTime;
            log.info("Finished rest call- Time taken to convert cv to text : {}ms. For cvParsingDetailsId : {}", responseTime, cvParsingDetailsFromDb.getId());
            ObjectMapper objectMapper = new ObjectMapper();
            cvToTextMap = objectMapper.readValue(cvToTextResponse, HashMap.class);
            List<String> CvSkills = objectMapper.readValue(cvToTextMap.get("cvSkills").replace("'", "\""), List.class);
            String cvText = cvToTextMap.get("cvText");
            String [] candidateSkills = new String[0];
            if(null != CvSkills && CvSkills.size()>0)
                candidateSkills = CvSkills.toArray(new String[CvSkills.size()]);

            if (null != cvText && cvText.trim().length()>IConstant.CV_TEXT_API_RESPONSE_MIN_LENGTH && !cvText.isEmpty()) {
                cvParsingDetailsFromDb.setParsingResponseText(cvText);
                if(null == cvParsingDetailsFromDb.getProcessingTime())
                    cvParsingDetailsFromDb.setProcessingTime(responseTime);
            }else{
                breadCrumb.put("CvText", cvText);
                SentryUtil.logWithStaticAPI(null, "Cv convert python response not good", breadCrumb);
            }

            if (CvSkills.size()>0){
                cvParsingDetailsFromDb.setCandidateSkills(candidateSkills);
                List<CandidateSkillDetails> candidateSkillDetails = new ArrayList<>();
                //Update candidate skills
                if(null != cvParsingDetailsFromDb.getJobCandidateMappingId().getCandidate() && null == cvParsingDetailsFromDb.getJobCandidateMappingId().getCandidate().getCandidateSkillDetails()){
                    Candidate candidate = cvParsingDetailsFromDb.getJobCandidateMappingId().getCandidate();
                    new HashSet<>(CvSkills).forEach(candidateSkill ->{
                        CandidateSkillDetails candidateSkillDetail = new CandidateSkillDetails();
                        candidateSkillDetail.setSkill(candidateSkill);
                        candidateSkillDetail.setCandidateId(candidate.getId());
                        candidateSkillDetails.add(candidateSkillDetail);
                    });
                    candidate.setCandidateSkillDetails(candidateSkillDetails);
                }
            }

            //If existing user have mail with @notavailable.io and mobile is null then call edit candidate to update email and mobile
            if(cvParsingDetailsFromDb.getJobCandidateMappingId().getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL) || null == cvParsingDetailsFromDb.getJobCandidateMappingId().getMobile()){
                String validMobile = null;
                boolean isEditCandidate = false;
                JobCandidateMapping jcmFromDb = cvParsingDetailsFromDb.getJobCandidateMappingId();
                log.info("Update edit candidate for candidateId : {}", cvParsingDetailsFromDb.getCandidateId());
                CvParsingApiDetails cvParsingApiDetails = cvParsingApiDetailsRepository.findByColumnToUpdate(PARSING_RESPONSE_PYTHON);
                StringBuffer queryString = new StringBuffer(cvParsingApiDetails.getApiUrl());
                queryString.append("?file=");
                queryString.append(environment.getProperty(IConstant.CV_STORAGE_LOCATION)).append(jcmFromDb.getJob().getId()).append(File.separator).append(cvParsingDetailsFromDb.getCandidateId()).append(jcmFromDb.getCvFileType());

                //Call Python parser to parse cv
                candidateFromPython = pythonCvParser(queryString.toString(), jcmFromDb.getJob().getId());

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
            }
        } catch (Exception e) {
            log.info(Util.getStackTrace(e));
            log.error("Error while convert cv to text cvFilePath : {}, for cvParsingDetailsId  : {}, error message : {}", queryParameters.get("file"), cvParsingDetailsFromDb.getId(), e.getMessage());
            cvParsingDetailsFromDb.setErrorMessage("Connection timeout issue while rest call to cvParser for converting cv to text");
            breadCrumb.put("Error Msg", ExceptionUtils.getStackTrace(e));
            SentryUtil.logWithStaticAPI(null, "Failed to convert cv to text", breadCrumb);
        }
        return cvParsingDetailsFromDb;
    }

    private Candidate pythonCvParser(String queryString, long jobId){
        Map headerInformation = LoggedInUserInfoUtil.getLoggedInUserJobInformation(jobId);
        log.info("Inside pythonCvParser");
        long PythonStartTime = System.currentTimeMillis();
        Candidate candidateFromPython = null;
        try {
            RestClientResponseBean restClientResponseBean = RestClient.getInstance().consumeRestApi(null, queryString, HttpMethod.GET, null,null,null,Optional.of(headerInformation));
            if(HttpStatus.OK.value() == restClientResponseBean.getStatusCode())
                candidateFromPython = new ObjectMapper().readValue(restClientResponseBean.getResponseBody(), Candidate.class);
            log.info("Received response from Python parser in {}ms.",(System.currentTimeMillis() - PythonStartTime));
        }catch (Exception e){
            log.error("Error while parse resume by Python parser : {}",e.getMessage());
        }
        return candidateFromPython;
    }



    @Transactional(propagation = Propagation.REQUIRED)
    private long callCvRatingApi(CvParsingDetails cvParsingDetails, CvRatingRequestBean requestBean) throws Exception {
        CvRating cvRatingFromDb = null;
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
        String cvRatingResponse = RestClient.getInstance().consumeRestApi(objectMapper.writeValueAsString(requestBean), queryString.toString(), HttpMethod.POST, null,null,java.util.Optional.of(IConstant.REST_READ_TIME_OUT_FOR_CV_TEXT),Optional.of(headerInformation)).getResponseBody();
        log.info("Response received from CV Rating Api : {}, JcmId : {}", cvRatingResponse, jcmId);
        long apiCallEndTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        CvParserResponseBean responseBean = objectMapper.readValue(cvRatingResponse, CvParserResponseBean.class);
        Candidate candidate = responseBean.getCandidate();
        CvRatingResponseWrapper cvRatingResponseWrapper = responseBean.getCvRatingResponseWrapper();
        cvRatingFromDb = cvRatingRepository.findByJobCandidateMappingId(jcmId);
        if(null == cvRatingFromDb){
            cvRatingFromDb = cvRatingRepository.save(new CvRating(jcmId, cvRatingResponseWrapper.getCvRatingResponse().getOverallRating()));
            cvRatingSkillKeywordDetailsRepository.saveAll(convertToCvRatingSkillKeywordDetails(cvRatingResponseWrapper.getCvRatingResponse().getKeywords(), cvRatingFromDb.getId()));
        }else{
            log.info("Update cv_rating id : "+cvRatingFromDb.getId()+", For jcm id : "+jcmId);
            log.info("Old cv_rating : "+cvRatingFromDb.getOverallRating()+", New Rating : "+cvRatingResponseWrapper.getCvRatingResponse().getOverallRating());
            cvRatingFromDb.setOverallRating(cvRatingResponseWrapper.getCvRatingResponse().getOverallRating());
            cvRatingRepository.save(cvRatingFromDb);
        }
        log.info("Time taken to update cv rating data " + (System.currentTimeMillis() - startTime) + "ms.");
        updateCandidateInfo(cvParsingDetails, candidate);
        return (apiCallEndTime - apiCallStartTime);
    }

    private List<CvRatingSkillKeywordDetails> convertToCvRatingSkillKeywordDetails(List<Keyword> keywords, Long cvRatingId) {
        List<CvRatingSkillKeywordDetails> targetList = new ArrayList<>(keywords.size());
        keywords.stream().forEach(keyword ->
            targetList.add(new CvRatingSkillKeywordDetails(cvRatingId,String.join(",",keyword.getSupportingKeywords().stream().map(supportingKeyword -> supportingKeyword.getName()).toArray(String[]::new)), keyword.getName(), keyword.getRating(), keyword.getOccurrence()))
        );
        return targetList;
    }

}