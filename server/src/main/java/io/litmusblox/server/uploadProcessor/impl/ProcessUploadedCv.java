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
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.service.impl.MlCvRatingRequestBean;
import io.litmusblox.server.uploadProcessor.IProcessUploadedCV;
import io.litmusblox.server.uploadProcessor.RChilliCvProcessor;
import io.litmusblox.server.utils.RestClient;
import io.litmusblox.server.utils.SentryUtil;
import io.litmusblox.server.utils.StoreFileUtil;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
        AtomicReference<String> pythonResponse = new AtomicReference<>();
        String fileName = filePath.toString().substring(filePath.toString().lastIndexOf(File.separator) + 1);
        String[] s = fileName.split("_");
        cvParsingApiDetailsRepository.findAllByActiveOrderByApiSequenceAsc(true).forEach(cvParsingApiDetails -> {
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
                        pythonResponse.set(rest.consumeRestApi(null, queryString.toString(), HttpMethod.GET, null,null, java.util.Optional.of(IConstant.REST_READ_TIME_OUT_FOR_CV_TEXT)).getResponseBody());
                        log.info("Python parser response : {}",pythonResponse.get());
                        if(null == candidate.get() || null == candidate.get().getCvParsingDetails()){
                            candidate.set(new Candidate());
                            candidate.get().setCvParsingDetails(new CvParsingDetails());
                        }
                        candidate.get().getCvParsingDetails().setParsingResponsePython(pythonResponse.get());
                        candidate.get().getCvParsingDetails().setProcessingTime((System.currentTimeMillis() - PythonStartTime));
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
                        candidate.get().getCvParsingDetails().setParsingResponseMl(rest.consumeRestApi(queryObjectString.toString(), cvParsingApiDetails.getApiUrl(), HttpMethod.POST, null).getResponseBody());
                        log.info("Received response from ML parser in {}ms.", (System.currentTimeMillis() - mlStartTime));
                    } catch (Exception e) {
                        log.error("Error while parse resume by ML parser : {}", e.getMessage());
                    }
                    break;
            }
        });
        addCandidate(candidate.get(), Long.parseLong(s[1]), filePath.toString(), Long.parseLong(s[0]), candidateSource);
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
    private void addCandidate(Candidate candidate, Long jobId, String filePath, Long userId, String candidateSource){
        log.info("Inside addCandidate");
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        if (null != candidate && null != candidate.getCvParsingDetails()) {

            Candidate candidateFromPython = null;
            UploadResponseBean uploadResponseBean = null;
            Long candidateId = null;
            String errorMessage = null;
            try {
                candidateFromPython = new ObjectMapper().readValue(candidate.getCvParsingDetails().getParsingResponsePython(), Candidate.class);
                candidateFromPython.setCandidateSource(candidateSource);
                if (Util.isNull(candidateFromPython.getEmail()) || !Util.isValidateEmail(candidateFromPython.getEmail()))
                    candidateFromPython.setEmail("notavailable" + new Date().getTime() + IConstant.NOT_AVAILABLE_EMAIL);

                if (null == candidateFromPython.getCandidateName())
                    candidateFromPython.setCandidateName(IConstant.NOT_AVAILABLE);

                if (null == candidateFromPython.getFirstName() || !Util.validateName(candidateFromPython.getFirstName()))
                    candidateFromPython.setFirstName(IConstant.NOT_FIRST_NAME);

                if (null == candidateFromPython.getLastName() || !Util.validateName(candidateFromPython.getLastName()))
                    candidateFromPython.setLastName(IConstant.NOT_LAST_NAME);


                if (null != candidateFromPython.getAlternateMobile() && candidateFromPython.getAlternateMobile().length() == 0)
                    candidateFromPython.setAlternateMobile(null);

                 uploadResponseBean = jobCandidateMappingService.uploadIndividualCandidate(Arrays.asList(candidateFromPython), jobId, false, userRepository.findById(userId));
                 if(uploadResponseBean.getCvStatus().equals(IConstant.UPLOAD_STATUS.Success)){
                     candidateId = uploadResponseBean.getSuccessfulCandidates().get(0).getId();
                 }
            } catch (Exception e) {
                log.info(Util.getStackTrace(e));
                errorMessage = ((ValidationException) e).getErrorMessage();
                log.error("Error while upload candidate via python response : " + e.getMessage());
            }
            try {
                candidate.getCvParsingDetails().setCvFileName(fileName);
                candidate.getCvParsingDetails().setProcessedOn(new Date());
                File file = new File(filePath);
                if (null != candidateId) {
                    JobCandidateMapping jobCandidateMapping = jobCandidateMappingRepository.findByJobIdAndCandidateId(jobId, candidateId);
                    jobCandidateMapping.setCvFileType("." + Util.getFileExtension(fileName));
                    jobCandidateMappingRepository.save(jobCandidateMapping);
                    candidate.getCvParsingDetails().setProcessingStatus(IConstant.UPLOAD_STATUS.Success.name());
                    candidate.getCvParsingDetails().setCandidateId(candidateId);
                    candidate.getCvParsingDetails().setJobCandidateMappingId(jobCandidateMapping);

                    //StringBuffer errorFile = new StringBuffer(environment.getProperty(IConstant.REPO_LOCATION));
                    //errorFile.append(File.separator).append(IConstant.ERROR_FILES_REPO_LOCATION).append(File.separator).append(fileName);
                    StoreFileUtil.storeFile(Util.createMultipartFile(file), jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(), uploadResponseBean.getSuccessfulCandidates().get(0), null);
                }else if(null == candidate.getCvParsingDetails().getParsingResponseJson()){
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
                    //call rest api with the text part of cv
                    log.info("Processing CV for job id: " + cvToRate.getJobCandidateMappingId().getJob().getId() + " and candidate id: " + cvToRate.getJobCandidateMappingId().getCandidate().getId());
                    List<String> jdKeySkills = jobKeySkillsRepository.findSkillNameByJobId(cvToRate.getJobCandidateMappingId().getJob().getId());
                    if (jdKeySkills.size() == 0)
                        log.error("Found no key skills for jobId: {}.  Not making api call to rate CV.", cvToRate.getJobCandidateMappingId().getJob().getId());
                    else {
                        try {
                            cvRatingApiProcessingTime = callCvRatingApi(new MlCvRatingRequestBean(jdKeySkills, cvToRate.getParsingResponseText(), cvToRate.getJobCandidateMappingId().getJob().getFunction().getValue()), cvToRate.getJobCandidateMappingId().getId());
                        } catch (Exception e) {
                            log.info("Error while performing CV rating operation " + e.getMessage());
                            processingError = true;
                        }
                    }
                    if (!processingError) {
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

    @Transactional
    private CvParsingDetails processCvForCvToText(CvParsingDetails cvParsingDetailsFromDb){
        log.info("Inside processCvForCvToText");
        String cvText = null;
        Candidate candidateFromPython = null;
        Map<String, String> queryParameters = new HashMap<>();
        Map<String, String> breadCrumb = new HashMap<>();
        breadCrumb.put("cvParsingDetailsId", cvParsingDetailsFromDb.getId().toString());
        breadCrumb.put("Jcm id", cvParsingDetailsFromDb.getJobCandidateMappingId().getId().toString());
        try {
            queryParameters.put("file", environment.getProperty(IConstant.CV_STORAGE_LOCATION) + cvParsingDetailsFromDb.getJobCandidateMappingId().getJob().getId() + "/" + cvParsingDetailsFromDb.getCandidateId() + cvParsingDetailsFromDb.getJobCandidateMappingId().getCvFileType());
            log.info("Cv storage file path : {}", queryParameters.get("file"));
            breadCrumb.put("FilePath", queryParameters.get("file"));
            long apiCallStartTime = System.currentTimeMillis();
            cvText = RestClient.getInstance().consumeRestApi(null, environment.getProperty("pythonCvParserUrl"), HttpMethod.GET, null, Optional.of(queryParameters), Optional.of(IConstant.REST_READ_TIME_OUT_FOR_CV_TEXT)).getResponseBody();
            log.info("Finished rest call- Time taken to convert cv to text : {}ms. For cvParsingDetailsId : {}", (System.currentTimeMillis() - apiCallStartTime), cvParsingDetailsFromDb.getId());
            if (null != cvText && cvText.trim().length()>IConstant.CV_TEXT_API_RESPONSE_MIN_LENGTH && !cvText.isEmpty()) {
                cvParsingDetailsFromDb.setParsingResponseText(cvText);
            }else{
                breadCrumb.put("CvText", cvText);
                SentryUtil.logWithStaticAPI(null, "Cv convert python response not good", breadCrumb);
            }

            if(cvParsingDetailsFromDb.getJobCandidateMappingId().getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL) || null == cvParsingDetailsFromDb.getJobCandidateMappingId().getMobile()){
                String validMobile = null;
                boolean isEditCandidate = false;
                JobCandidateMapping jcmFromDb = cvParsingDetailsFromDb.getJobCandidateMappingId();
                log.info("Update edit candidate for candidateId : {}", cvParsingDetailsFromDb.getCandidateId());
                CvParsingApiDetails cvParsingApiDetails = cvParsingApiDetailsRepository.findByColumnToUpdate(PARSING_RESPONSE_PYTHON);
                StringBuffer queryString = new StringBuffer(cvParsingApiDetails.getApiUrl());
                queryString.append("?file=");
                queryString.append(environment.getProperty(IConstant.CV_STORAGE_LOCATION)).append(jcmFromDb.getJob().getId()).append(File.separator).append(cvParsingDetailsFromDb.getCandidateId()).append(jcmFromDb.getCvFileType());
                candidateFromPython = pythonCvParser(queryString.toString());
                if(Util.isNotNull(candidateFromPython.getEmail()) && Util.isValidateEmail(candidateFromPython.getEmail())){
                    log.info("candidate old email : {}, python response email : {}", jcmFromDb.getEmail(), candidateFromPython.getEmail());
                    cvParsingDetailsFromDb.getJobCandidateMappingId().setEmail(candidateFromPython.getEmail());
                    isEditCandidate = true;
                }
                if(Util.isNull(jcmFromDb.getMobile()) && Util.isNotNull(candidateFromPython.getMobile())){
                    validMobile = Util.indianMobileConvertor(candidateFromPython.getMobile(), cvParsingDetailsFromDb.getJobCandidateMappingId().getCountryCode());
                    if(Util.validateMobile(validMobile, cvParsingDetailsFromDb.getJobCandidateMappingId().getCountryCode())){
                        log.info("candidate old mobile : {}, python response mobile : {}, For JcmId : {}", jcmFromDb.getMobile(), candidateFromPython.getMobile(), jcmFromDb.getId());
                        cvParsingDetailsFromDb.getJobCandidateMappingId().setMobile(validMobile);
                        isEditCandidate = true;
                    }
                }
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

    private Candidate pythonCvParser(String queryString){
        log.info("Inside pythonCvParser");
        long PythonStartTime = System.currentTimeMillis();
        Candidate candidateFromPython = null;
        try {
            String pythonResponse = RestClient.getInstance().consumeRestApi(null, queryString, HttpMethod.GET, null).getResponseBody();
            candidateFromPython = new ObjectMapper().readValue(pythonResponse, Candidate.class);
            log.info("Received response from Python parser in {}ms.",(System.currentTimeMillis() - PythonStartTime));
        }catch (Exception e){
            log.error("Error while parse resume by Python parser : {}",e.getMessage());
        }
        return candidateFromPython;
    }



    @Transactional(propagation = Propagation.REQUIRED)
    private long callCvRatingApi(MlCvRatingRequestBean requestBean, Long jcmId) throws Exception {
        CvRating cvRatingFromDb = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        long apiCallStartTime = System.currentTimeMillis();
        String mlResponse = RestClient.getInstance().consumeRestApi(objectMapper.writeValueAsString(requestBean), mlCvRatingUrl, HttpMethod.POST, null).getResponseBody();
        log.info("Response received from CV Rating Api: " + mlResponse);
        long apiCallEndTime = System.currentTimeMillis();

        long startTime = System.currentTimeMillis();
        CvRatingResponseWrapper responseBean = objectMapper.readValue(mlResponse, CvRatingResponseWrapper.class);

        cvRatingFromDb = cvRatingRepository.findByJobCandidateMappingId(jcmId);
        if(null == cvRatingFromDb){
            cvRatingFromDb = cvRatingRepository.save(new CvRating(jcmId, responseBean.getCvRatingResponse().getOverallRating()));
            cvRatingSkillKeywordDetailsRepository.saveAll(convertToCvRatingSkillKeywordDetails(responseBean.getCvRatingResponse().getKeywords(), cvRatingFromDb.getId()));
        }else{
            log.info("Update cv_rating id : "+cvRatingFromDb.getId()+", For jcm id : "+jcmId);
            log.info("Old cv_rating : "+cvRatingFromDb.getOverallRating()+", New Rating : "+responseBean.getCvRatingResponse().getOverallRating());
            cvRatingFromDb.setOverallRating(responseBean.getCvRatingResponse().getOverallRating());
            cvRatingRepository.save(cvRatingFromDb);
        }
        log.info("Time taken to process ml cv rating data data: " + (System.currentTimeMillis() - startTime) + "ms.");
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