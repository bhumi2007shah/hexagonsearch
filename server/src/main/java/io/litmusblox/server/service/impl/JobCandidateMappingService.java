/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.*;
import io.litmusblox.server.uploadProcessor.CsvFileProcessorService;
import io.litmusblox.server.uploadProcessor.ExcelFileProcessorService;
import io.litmusblox.server.uploadProcessor.IUploadDataProcessService;
import io.litmusblox.server.uploadProcessor.NaukriExcelFileProcessorService;
import io.litmusblox.server.utils.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Implementation class for methods exposed by IJobCandidateMappingService
 *
 * @author : Shital Raval
 * Date : 16/7/19
 * Time : 4:56 PM
 * Class Name : JobCandidateMappingService
 * Project Name : server
 */
@Service
@Log4j2
public class JobCandidateMappingService implements IJobCandidateMappingService {

    @Resource
    JobRepository jobRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    JcmCommunicationDetailsRepository jcmCommunicationDetailsRepository;

    @Autowired
    IUploadDataProcessService iUploadDataProcessService;

    @Autowired
    Environment environment;

    @Resource
    CandidateScreeningQuestionResponseRepository candidateScreeningQuestionResponseRepository;

    @Resource
    CandidateMobileHistoryRepository candidateMobileHistoryRepository;

    @Resource
    CandidateEmailHistoryRepository candidateEmailHistoryRepository;

    @Resource
    JobScreeningQuestionsRepository jobScreeningQuestionsRepository;

    @Resource
    CandidateDetailsRepository candidateDetailsRepository;

    @Resource
    CandidateCompanyDetailsRepository candidateCompanyDetailsRepository;

    @Autowired
    ICandidateService candidateService;

    @Resource
    JcmProfileSharingDetailsRepository jcmProfileSharingDetailsRepository;

    @Resource
    JcmProfileSharingMasterRepository jcmProfileSharingMasterRepository;

    @Resource
    JcmHistoryRepository jcmHistoryRepository;

    @Resource
    CvParsingDetailsRepository cvParsingDetailsRepository;

    @Resource
    CvRatingRepository cvRatingRepository;

    @Resource
    CvRatingSkillKeywordDetailsRepository cvRatingSkillKeywordDetailsRepository;

    @Resource
    CandidateEducationDetailsRepository candidateEducationDetailsRepository;

    @Transactional(readOnly = true)
    Job getJob(long jobId) {
        return jobRepository.findById(jobId).get();
    }

    @Transactional(readOnly = true)
    User getUser(){return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();}

    @Transactional(readOnly = true)
    int getUploadCount(Date createdOn, User loggedInUser){return jobCandidateMappingRepository.getUploadedCandidateCount(createdOn, loggedInUser);}

    @Value("${scoringEngineBaseUrl}")
    private String scoringEngineBaseUrl;

    @Value("${scoringEngineAddCandidateUrlSuffix}")
    private String scoringEngineAddCandidateUrlSuffix;

    /**
     * Service method to add a individually added candidates to a job
     *
     * @param candidates the list of candidates to be added
     * @param jobId      the job for which the candidate is to be added
     * @return the status of upload operation
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UploadResponseBean uploadIndividualCandidate(List<Candidate> candidates, Long jobId) throws Exception {

        //verify that the job is live before processing candidates
        Job job = jobRepository.getOne(jobId);
        if(null == job || !job.getStatus().equals(IConstant.JobStatus.PUBLISHED.getValue())) {
            StringBuffer info = new StringBuffer("Selected job is not live ").append("JobId :").append(jobId);
            Map<String, String> breadCrumb = new HashMap<>();
            breadCrumb.put("detail", info.toString());
            throw new WebException(IErrorMessages.JOB_NOT_LIVE, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }

        UploadResponseBean uploadResponseBean = new UploadResponseBean();
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Date createdOn=Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        int candidateProcessed=jobCandidateMappingRepository.getUploadedCandidateCount(createdOn,loggedInUser);

        if (candidateProcessed >= MasterDataBean.getInstance().getConfigSettings().getDailyCandidateUploadPerUserLimit()) {
            log.error(IErrorMessages.MAX_CANDIDATE_PER_FILE_EXCEEDED + " :: user id : " + loggedInUser.getId() + " : not processing records");
            throw new WebException(IErrorMessages.MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        try {
            processCandidateData(candidates, uploadResponseBean, loggedInUser, jobId, candidateProcessed);
        } catch (Exception ex) {
            log.error("Error while processing candidates uploaded :: " + ex.getMessage());
            uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        return uploadResponseBean;
    }

    private void processCandidateData(List<Candidate> candidateList, UploadResponseBean uploadResponseBean, User loggedInUser, Long jobId, int candidateProcessed) throws Exception{

        if (null != candidateList && candidateList.size() > 0) {
            iUploadDataProcessService.processData(candidateList, uploadResponseBean, candidateProcessed,jobId, !loggedInUser.getCountryId().getCountryName().equalsIgnoreCase(IConstant.STR_INDIA));
        }

        for (Candidate candidate:candidateList) {
            if(null!=candidate.getId())
                saveCandidateSupportiveInfo(candidate, loggedInUser);
        }
    }

    /**
     * Method for save candidates supportive information like Company, project, language, skills etc
     *
     * @param candidate for which candidate add this info
     * @param loggedInUser user which is login currently
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCandidateSupportiveInfo(Candidate candidate, User loggedInUser) throws Exception {
        log.info("Inside saveCandidateSupportiveInfo Method");

        //find candidateId
        Candidate candidateFromDb=candidateService.findByMobileOrEmail(candidate.getEmail(), candidate.getMobile(), (null==candidate.getCountryCode())?loggedInUser.getCountryId().getCountryCode():candidate.getCountryCode(), loggedInUser, Optional.ofNullable(candidate.getAlternateMobile()));

        Long candidateId = null;
        if (null != candidateFromDb)
            candidateId = candidateFromDb.getId();
        if (null != candidateId) {
            candidateFromDb.setMobile(candidate.getMobile());
            candidateFromDb.setEmail(candidate.getEmail().toLowerCase());
            try {
                //if telephone field has value, save to mobile history table
                if (!Util.isNull(candidate.getTelephone()) && candidate.getTelephone().length() > 6) {
                    //check if an entry exists in the mobile history record for this number
                    String telephone = candidate.getTelephone().replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_MOBILE, "");

                    if (!candidateFromDb.getMobile().trim().equals(telephone.trim())) {

                        if (telephone.length() > 15)
                            telephone = telephone.substring(0, 15);

                        if (null == candidateMobileHistoryRepository.findByMobileAndCountryCode(telephone, candidate.getCountryCode()));
                        candidateMobileHistoryRepository.save(new CandidateMobileHistory(candidateFromDb, telephone, (null == candidateFromDb.getCountryCode()) ? loggedInUser.getCountryId().getCountryCode() : candidateFromDb.getCountryCode(), new Date(), loggedInUser));
                    }
                }
            }catch (Exception ex){
                log.error("Error while add telephone number :: " +candidate.getTelephone()+" "+ ex.getMessage());
            }


            //process other information
            if(null != candidate.getCandidateDetails()) {
                //candidate details
                //if marital status is more than 10 characters, trim to 10. e.g. got a status as single/unmarried for one of the candidates!
                if (!Util.isNull(candidate.getCandidateDetails().getMaritalStatus()) && candidate.getCandidateDetails().getMaritalStatus().length() > 10)
                    candidate.getCandidateDetails().setMaritalStatus(candidate.getCandidateDetails().getMaritalStatus().substring(0, 10));
                candidateService.saveUpdateCandidateDetails(candidate.getCandidateDetails(), candidateFromDb);
            }

            //candidate education details
            if(null != candidate.getCandidateEducationDetails() && candidate.getCandidateEducationDetails().size() > 0) {
                candidate.getCandidateEducationDetails().forEach(educationDetails-> {
                    if(educationDetails.getInstituteName().length() > IConstant.MAX_INSTITUTE_LENGTH) {
                        log.info("Institute name too long: " + educationDetails.getInstituteName());
                        educationDetails.setInstituteName(educationDetails.getInstituteName().substring(0,IConstant.MAX_INSTITUTE_LENGTH));
                    }
                });
                candidateService.saveUpdateCandidateEducationDetails(candidate.getCandidateEducationDetails(), candidateFromDb);
            }

            //candidate company details
            if(null != candidate.getCandidateCompanyDetails() && candidate.getCandidateCompanyDetails().size() > 0)
                candidateService.saveUpdateCandidateCompanyDetails(candidate.getCandidateCompanyDetails(), candidateFromDb);

            //candidate project details
            if(null != candidate.getCandidateProjectDetails() && candidate.getCandidateProjectDetails().size() > 0)
                candidateService.saveUpdateCandidateProjectDetails(candidate.getCandidateProjectDetails(), candidateFromDb);

            //candidate online profile
            if(null != candidate.getCandidateOnlineProfiles() && candidate.getCandidateOnlineProfiles().size() > 0)
                candidateService.saveUpdateCandidateOnlineProfile(candidate.getCandidateOnlineProfiles(), candidateFromDb);

            //candidate language proficiency
            if(null != candidate.getCandidateLanguageProficiencies() && candidate.getCandidateLanguageProficiencies().size() > 0)
                candidateService.saveUpdateCandidateLanguageProficiency(candidate.getCandidateLanguageProficiencies(), candidateId);

            //candidate work authorization
            if(null != candidate.getCandidateWorkAuthorizations() && candidate.getCandidateWorkAuthorizations().size() > 0)
                candidateService.saveUpdateCandidateWorkAuthorization(candidate.getCandidateWorkAuthorizations(), candidateId);

            //candidate skill details
            if(null != candidate.getCandidateSkillDetails() && candidate.getCandidateSkillDetails().size() > 0)
                candidateService.saveUpdateCandidateSkillDetails(candidate.getCandidateSkillDetails(), candidateFromDb);
        }
    }

    /**
     * Service method to add candidates from a file in one of the supported formats
     *
     * @param multipartFile the file with candidate information
     * @param jobId         the job for which the candidates have to be added
     * @param fileFormat    the format of file, for e.g. Naukri, LB format
     * @return the status of upload operation
     * @throws Exception
     */
   // @Transactional(propagation = Propagation.REQUIRED)
    public UploadResponseBean uploadCandidatesFromFile(MultipartFile multipartFile, Long jobId, String fileFormat) throws Exception {

        //validate the file source is supported by application
        if(!Arrays.asList(IConstant.UPLOAD_FORMATS_SUPPORTED.values()).contains(IConstant.UPLOAD_FORMATS_SUPPORTED.valueOf(fileFormat))) {
            log.error(IErrorMessages.UNSUPPORTED_FILE_SOURCE + fileFormat);
            StringBuffer info = new StringBuffer("Unsupported file source : ").append(multipartFile.getName());
            Map<String, String> breadCrumb = new HashMap<>();
            breadCrumb.put("Job Id", jobId.toString());
            breadCrumb.put("File Name", multipartFile.getName());
            breadCrumb.put("detail", info.toString());
            throw new WebException(IErrorMessages.UNSUPPORTED_FILE_SOURCE + fileFormat, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }

        //verify that the job is live before processing candidates
        Job job = getJob(jobId);
        if(null == job || !job.getStatus().equals(IConstant.JobStatus.PUBLISHED.getValue())) {
            StringBuffer info = new StringBuffer("Selected job is not live ").append("JobId-").append(jobId);
            Map<String, String> breadCrumb = new HashMap<>();
            breadCrumb.put("Job Id", jobId.toString());
            breadCrumb.put("detail", info.toString());
            throw new WebException(IErrorMessages.JOB_NOT_LIVE, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }

        //validate that the file has an extension that is supported by the application
        Util.validateUploadFileType(multipartFile.getOriginalFilename());

        User loggedInUser = getUser();
        Date createdOn=Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        UploadResponseBean uploadResponseBean = new UploadResponseBean();

        int candidatesProcessed = getUploadCount(createdOn, loggedInUser);

        if (candidatesProcessed >= MasterDataBean.getInstance().getConfigSettings().getDailyCandidateUploadPerUserLimit()) {
            log.error(IErrorMessages.MAX_CANDIDATE_PER_FILE_EXCEEDED + " :: user id : " + loggedInUser.getId() + " : not saving file " + multipartFile);
            throw new WebException(IErrorMessages.MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //Save file
        String fileName = StoreFileUtil.storeFile(multipartFile, loggedInUser.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.Candidates.toString(),null,null);
        log.info("User " + loggedInUser.getDisplayName() + " uploaded " + fileName);
        List<Candidate> candidateList = processUploadedFile(fileName, uploadResponseBean, loggedInUser, fileFormat, environment.getProperty(IConstant.REPO_LOCATION));

        try {
            processCandidateData(candidateList, uploadResponseBean, loggedInUser, jobId, candidatesProcessed);

        } catch (Exception ex) {
            log.error("Error while processing file " + fileName + " :: " + ex.getMessage());
            uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }

        return uploadResponseBean;
    }

    private List<Candidate> processUploadedFile(String fileName, UploadResponseBean responseBean, User user, String fileSource, String repoLocation) {
        //code to parse through the records and save data in database
        String fileExtension = Util.getFileExtension(fileName).toLowerCase();
        List<Candidate> candidateList = null;
        switch (fileExtension) {
            case "csv":
                candidateList = new CsvFileProcessorService().process(fileName, responseBean, !user.getCountryId().getCountryName().equalsIgnoreCase(IConstant.STR_INDIA), repoLocation);
                break;
            case "xls":
            case "xlsx":
                switch (IConstant.UPLOAD_FORMATS_SUPPORTED.valueOf(fileSource)) {
                    case LitmusBlox:
                        candidateList = new ExcelFileProcessorService().process(fileName, responseBean, !user.getCountryId().getCountryName().equalsIgnoreCase(IConstant.STR_INDIA), repoLocation);
                        break;
                    case Naukri:
                        log.info("Reached the naukri parser");
                        candidateList = new NaukriExcelFileProcessorService().process(fileName, responseBean, !user.getCountryId().getCountryName().equalsIgnoreCase(IConstant.STR_INDIA), repoLocation);

                        break;
                }
                break;
            default:
                log.error(IErrorMessages.UNSUPPORTED_FILE_TYPE  + " - "+ fileExtension);
                StringBuffer info = new StringBuffer("Unsupported file source : ").append(fileName);
                Map<String, String> breadCrumb = new HashMap<>();
                breadCrumb.put("File Name", fileName);
                breadCrumb.put("detail", info.toString());
                throw new WebException(IErrorMessages.UNSUPPORTED_FILE_TYPE + " - " + fileExtension, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }
        return candidateList;
    }

    /**
     * Service method to source and add a candidate from a plugin, for example Naukri plugin
     *
     * @param candidate the candidate to be added
     * @param jobId     the job for which the candidate is to be added
     * @return the status of upload operation
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UploadResponseBean uploadCandidateFromPlugin(Candidate candidate, Long jobId, MultipartFile candidateCv) throws Exception {
        UploadResponseBean responseBean = null;
        if (null != candidate) {

            if(null == candidate.getCandidateName() || candidate.getCandidateName().isEmpty()){
               candidate.setCandidateName(IConstant.NOT_AVAILABLE);
               candidate.setFirstName(IConstant.NOT_AVAILABLE);
            }else{
                //populate the first name and last name of the candidate
                Util.handleCandidateName(candidate, candidate.getCandidateName());
            }
            //set source as plugin
            candidate.setCandidateSource(IConstant.CandidateSource.Plugin.getValue());
            if (candidate.getCandidateCompanyDetails() != null && candidate.getCandidateCompanyDetails().size() >0) {
                candidate.getCandidateCompanyDetails().stream().forEach(candidateCompanyDetails -> {
                    if(!Util.isNull(candidateCompanyDetails.getNoticePeriod()) && candidateCompanyDetails.getNoticePeriod().length() > 0) {
                        candidateCompanyDetails.setNoticePeriod(candidateCompanyDetails.getNoticePeriod()+" "+IConstant.DAYS);
                        candidateCompanyDetails.setNoticePeriodInDb(MasterDataBean.getInstance().getNoticePeriodMapping().get(candidateCompanyDetails.getNoticePeriod()));
                        if (null == candidateCompanyDetails.getNoticePeriodInDb()) {
                            //value in request object is not available in db
                            SentryUtil.logWithStaticAPI(null,"Unmapped notice period: " + candidateCompanyDetails.getNoticePeriod(), new HashMap<>());
                            candidateCompanyDetails.setNoticePeriodInDb(MasterDataBean.getInstance().getNoticePeriodMapping().get("Others"));
                        }

                    }
                });
            }
            responseBean = uploadIndividualCandidate(Arrays.asList(candidate), jobId);

            //Store candidate cv to repository location
            try{
                if(null!=candidateCv) {
                    if (responseBean.getSuccessfulCandidates().size()>0)
                        StoreFileUtil.storeFile(candidateCv, jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(),responseBean.getSuccessfulCandidates().get(0),null);
                    else
                        StoreFileUtil.storeFile(candidateCv, jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(),responseBean.getFailedCandidates().get(0), null);

                    responseBean.setCvStatus(true);
                }
            }catch(Exception e){
                log.error("Resume upload failed :"+e.getMessage());
                responseBean.setCvErrorMsg(e.getMessage());
            }

            //#189: save the text format of CV if available
            if(responseBean.getSuccessfulCandidates().size() > 0) {
                JobCandidateMapping jcm = jobCandidateMappingRepository.findByJobAndCandidate(getJob(jobId), responseBean.getSuccessfulCandidates().get(0));
                cvParsingDetailsRepository.save(new CvParsingDetails(new Date(), candidate.getCandidateDetails().getTextCv(), responseBean.getSuccessfulCandidates().get(0).getId(),jcm));
            }
        }
        else {//null candidate object
            log.error(IErrorMessages.INVALID_REQUEST_FROM_PLUGIN);
            StringBuffer info = new StringBuffer("Invalid request object from plugin, missing Candidate info");
            sendSentryMail(info.toString(), null,jobId);
            throw new WebException(IErrorMessages.INVALID_REQUEST_FROM_PLUGIN, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return responseBean;
    }

    /**
     * Service method to capture candidate consent from chatbot
     *
     * @param uuid     the uuid corresponding to a unique jcm record
     * @param interest boolean to capture candidate consent
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void captureCandidateInterest(UUID uuid, boolean interest) throws Exception {
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findByChatbotUuid(uuid);
        if (null == objFromDb)
            throw new WebException(IErrorMessages.UUID_NOT_FOUND + uuid, HttpStatus.UNPROCESSABLE_ENTITY);
        objFromDb.setCandidateInterest(interest);
        objFromDb.setCandidateInterestDate(new Date());
        jobCandidateMappingRepository.save(objFromDb);
        jcmHistoryRepository.save(new JcmHistory(objFromDb, "Candidate is"+ (interest?" interested.":" not interested."), new Date(), null));
    }

    /**
     * Service method to capture candidate response to screening questions from chatbot
     *
     * @param uuid              the uuid corresponding to a unique jcm record
     * @param candidateResponse the response provided by a candidate against each screening question
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveScreeningQuestionResponses(UUID uuid, Map<Long, List<String>> candidateResponse) throws Exception {
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findByChatbotUuid(uuid);
        if (null == objFromDb)
            throw new WebException(IErrorMessages.UUID_NOT_FOUND + uuid, HttpStatus.UNPROCESSABLE_ENTITY);

        //delete existing response for chatbot for the jcm
        candidateScreeningQuestionResponseRepository.deleteByJobCandidateMappingId(objFromDb.getId());

        candidateResponse.forEach((key,value) -> {
            String[] valuesToSave = new String[value.size()];
            for(int i=0;i<value.size();i++) {
                valuesToSave[i] = value.get(i);
                if(valuesToSave[i].length() > 100) {
                    log.error("Length of user response is greater than 100 " + value);
                    valuesToSave[i] = valuesToSave[i].substring(0,100);
                }
            }
            candidateScreeningQuestionResponseRepository.save(new CandidateScreeningQuestionResponse(objFromDb.getId(),key, valuesToSave[0], (valuesToSave.length > 1)?valuesToSave[1]:null));
        });

        //updating hr_chat_complete_flag
        jcmCommunicationDetailsRepository.updateHrChatbotFlagByJcmId(objFromDb.getId());

        //updating chat_complete_flag if corresponding job is not available on scoring engine due to lack of ML data,
        // or candidate already filled all the capabilities in some other job and we already have candidate responses for technical chatbot.
        if(!objFromDb.getJob().getScoringEngineJobAvailable() || (objFromDb.getChatbotStatus()!=null && objFromDb.getChatbotStatus().equals("Complete"))){
            jcmCommunicationDetailsRepository.updateByJcmId(objFromDb.getId());
        }
    }

    /**
     * Service method to get all screening questions for the job
     *
     * @param uuid the uuid corresponding to a unique jcm record
     * @return the list of job screening questions
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<JobScreeningQuestions> getJobScreeningQuestions(UUID uuid) throws Exception {
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findByChatbotUuid(uuid);
        if (null == objFromDb)
            throw new WebException(IErrorMessages.UUID_NOT_FOUND + uuid, HttpStatus.UNPROCESSABLE_ENTITY);

        return jobScreeningQuestionsRepository.findByJobId(objFromDb.getJob().getId());
    }

    /**
     * Service method to invite candidates to fill chatbot for a job
     *
     * @param jcmList list of jcm ids for chatbot invitation
     * @throws Exception
     */
    public void inviteCandidates(List<Long> jcmList) throws Exception {
        performInvitationAndHistoryUpdation(jcmList);
        callScoringEngineToAddCandidates(jcmList);
    }

    @Transactional(readOnly = true)
    private void callScoringEngineToAddCandidates(List<Long> jcmList) {
        //make an api call to scoring engine for each of the jcm
        jcmList.stream().forEach(jcmId->{
            log.info("Calling scoring engine - add candidate api for : " + jcmId);
            JobCandidateMapping jcm = jobCandidateMappingRepository.getOne(jcmId);
            if (null == jcm) {
                log.error(IErrorMessages.JCM_NOT_FOUND + jcmId);
            }
            else {
                if(jcm.getJob().getScoringEngineJobAvailable()) {
                    try {
                        Map queryParams = new HashMap(3);
                        queryParams.put("lbJobId", jcm.getJob().getId());
                        queryParams.put("candidateId", jcm.getCandidate().getId());
                        queryParams.put("candidateUuid", jcm.getChatbotUuid());
                        log.info("Calling Scoring Engine api to add candidate to job");
                        String scoringEngineResponse = RestClient.getInstance().consumeRestApi(null, scoringEngineBaseUrl + scoringEngineAddCandidateUrlSuffix, HttpMethod.PUT, null, Optional.of(queryParams), null);
                        log.info(scoringEngineResponse);

                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            TechChatbotRequestBean techChatbotRequestBean = objectMapper.readValue(scoringEngineResponse, TechChatbotRequestBean.class);
                            jcm.setChatbotUpdatedOn(techChatbotRequestBean.getChatbotUpdatedOn());
                            if (techChatbotRequestBean.getTechResponseJson() != null && !techChatbotRequestBean.getTechResponseJson().isEmpty()) {
                                jcm.getTechResponseData().setTechResponse(techChatbotRequestBean.getTechResponseJson());
                            }
                            if (techChatbotRequestBean.getScore() > 0) {
                                jcm.setScore(techChatbotRequestBean.getScore());
                            }
                            if (techChatbotRequestBean.getChatbotUpdatedOn() != null) {
                                jcm.setChatbotUpdatedOn(techChatbotRequestBean.getChatbotUpdatedOn());
                            }

                            //Candidate has already completed the tech chatbot
                            if (IConstant.CHATBOT_STATUS.Complete.name().equalsIgnoreCase(techChatbotRequestBean.getChatbotStatus())) {
                                log.info("Found complete status from scoring engine: " + jcm.getEmail() + " ~ " + jcm.getId());
                                //Set chatCompleteFlag = true
                                JcmCommunicationDetails jcmCommunicationDetails = jcmCommunicationDetailsRepository.findByJcmId(jcm.getId());
                                jcmCommunicationDetails.setChatCompleteFlag(true);
                                jcmCommunicationDetailsRepository.save(jcmCommunicationDetails);

                                //If hr chat flag is also complete, set chatstatus = complete
                                if (jcmCommunicationDetails.isHrChatCompleteFlag()) {
                                    log.info("Found complete status for hr chat: " + jcm.getEmail() + " ~ " + jcm.getId());
                                    jcm.setChatbotStatus(techChatbotRequestBean.getChatbotStatus());
                                }
                            }
                            jobCandidateMappingRepository.save(jcm);
                        } catch (Exception e) {
                            log.error("Error in response received from scoring engine " + e.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("Error while adding candidate on Scoring Engine: " + e.getMessage());
                    }
                }
                else {
                    log.info("Job has not been added to Scoring engine. Cannot call create candidate api. " + jcm.getJob().getId());
                }
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void performInvitationAndHistoryUpdation(List<Long> jcmList) throws Exception {
        if(jcmList == null || jcmList.size() == 0)
            throw new WebException("Select candidates to invite",HttpStatus.UNPROCESSABLE_ENTITY);

        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        jcmCommunicationDetailsRepository.inviteCandidates(jcmList);

        log.info("Completed updating chat_invite_flag for the list of jcm");

        List<JcmHistory> jcmHistoryList = new ArrayList<>();

        for(Long jcmId: jcmList){
            jcmHistoryList.add(new JcmHistory(new JobCandidateMapping(jcmId), "Candidate invited", new Date(), loggedInUser));
        }

        if(jcmHistoryList.size()>0){
            jcmHistoryRepository.saveAll(jcmHistoryList);
        }

        log.info("Added jmcHistory data");
    }


    /**
     * Service method to process sharing of candidate profiles with Hiring managers
     *
     * @param requestBean The request bean with information about the profile to be shared, the recepient name and recepient email address
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void shareCandidateProfiles(ShareCandidateProfileRequestBean requestBean) {

        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<String> recieverEmails = new ArrayList<>();

        for (String[] array:requestBean.getReceiverInfo()) {

            String receiverNameToUse = array[0], receiverEmailToUse =  array[1];

            if (!Util.validateName(receiverNameToUse.trim())) {
                String cleanName = receiverNameToUse.replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_NAME, "");
                log.error("Special characters found, cleaning First name \"" + receiverNameToUse + "\" to " + cleanName);
                if (!Util.validateName(cleanName))
                    throw new ValidationException(IErrorMessages.NAME_FIELD_SPECIAL_CHARACTERS + " - " + receiverNameToUse, HttpStatus.BAD_REQUEST);
                receiverNameToUse =cleanName;
            }
            if (receiverNameToUse.trim().length()==0 || receiverNameToUse.length()>45)
                throw new WebException(IErrorMessages.INVALID_RECEIVER_NAME, HttpStatus.BAD_REQUEST);

            //validate recevier email
            if (!Util.validateEmail(receiverEmailToUse)) {
                String cleanEmail = receiverEmailToUse.replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_EMAIL,"");
                log.error("Special characters found, cleaning Email \"" + receiverEmailToUse + "\" to " + cleanEmail);
                if (!Util.validateEmail(cleanEmail)) {
                    throw new ValidationException(IErrorMessages.INVALID_EMAIL + " - " + receiverEmailToUse, HttpStatus.BAD_REQUEST);
                }
                receiverEmailToUse=cleanEmail;
            }
            if(receiverEmailToUse.length()>50)
                throw new ValidationException(IErrorMessages.EMAIL_TOO_LONG, HttpStatus.BAD_REQUEST);

            JcmProfileSharingMaster masterObj = jcmProfileSharingMasterRepository.save(new JcmProfileSharingMaster(loggedInUser.getId(), receiverNameToUse, receiverEmailToUse));
            Set<JcmProfileSharingDetails> detailsSet = new HashSet<>(requestBean.getJcmId().size());
            requestBean.getJcmId().forEach(jcmId ->{
                detailsSet.add(new JcmProfileSharingDetails(masterObj,jcmId));
            });
            jcmProfileSharingDetailsRepository.saveAll(detailsSet);
            recieverEmails.add(array[1]);
        }

        jcmHistoryRepository.save(new JcmHistory(new JobCandidateMapping(requestBean.getJcmId().get(0)), "Profiles shared with : "+String.join(", ", recieverEmails)+".", new Date(), loggedInUser));
    }

    /**
     * Service method to capture hiring manager interest
     *
     * @param sharingId     the uuid corresponding to which the interest needs to be captured
     * @param interestValue interested true / false response
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateHiringManagerInterest(UUID sharingId, Boolean interestValue) {
        //TODO: For the uuid,
        //1. fetch record from JCM_PROFILE_SHARING_DETAILS table
        //2. update the record by setting the HIRING_MANAGER_INTEREST = interest value and HIRING_MANAGER_INTEREST_DATE as current date
        JcmProfileSharingDetails jcmProfileSharingDetails = jcmProfileSharingDetailsRepository.findById(sharingId);
        jcmProfileSharingDetails.setHiringManagerInterestDate(new Date());
        jcmProfileSharingDetails.setHiringManagerInterest(interestValue);
        jcmProfileSharingDetailsRepository.save(jcmProfileSharingDetails);
    }

    /**
     * Service method to fetch details of a single candidate for a job
     *
     * @param jobCandidateMappingId
     * @return jobCandidateMapping object with required details
     * @throws Exception
     */
    @Transactional
    public JobCandidateMapping getCandidateProfile(Long jobCandidateMappingId, Date hiringManagerInterestDate) throws Exception {
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findById(jobCandidateMappingId).orElse(null);
        if(null == objFromDb)
            throw new ValidationException("No job candidate mapping found for id: " + jobCandidateMappingId, HttpStatus.UNPROCESSABLE_ENTITY);

        List<JobScreeningQuestions> screeningQuestions = jobScreeningQuestionsRepository.findByJobId(objFromDb.getJob().getId());
        Map<Long, JobScreeningQuestions> screeningQuestionsMap = new HashMap<>(screeningQuestions.size());
        screeningQuestions.forEach(screeningQuestion-> {
            screeningQuestionsMap.put(screeningQuestion.getId(), screeningQuestion);
        });

        List<CandidateScreeningQuestionResponse> responses = candidateScreeningQuestionResponseRepository.findByJobCandidateMappingId(jobCandidateMappingId);

        responses.forEach(candidateResponse -> {
            screeningQuestionsMap.get(candidateResponse.getJobScreeningQuestionId()).getCandidateResponse().add(candidateResponse.getResponse());
            if (null != candidateResponse.getComment())
                screeningQuestionsMap.get(candidateResponse.getJobScreeningQuestionId()).getCandidateResponse().add(candidateResponse.getComment());
        });

        Candidate returnObj = objFromDb.getCandidate();
        returnObj.setTechResponseData(objFromDb.getTechResponseData().getTechResponse());

        //set the cv location
        if(null != returnObj.getCandidateDetails() && null != returnObj.getCandidateDetails().getCvFileType()) {
            StringBuffer cvLocation = new StringBuffer("");
            cvLocation.append(IConstant.CANDIDATE_CV).append(File.separator).append(objFromDb.getJob().getId()).append(File.separator).append(objFromDb.getCandidate().getId()).append(returnObj.getCandidateDetails().getCvFileType());
            returnObj.getCandidateDetails().setCvLocation(cvLocation.toString());
        }
        returnObj.setScreeningQuestionResponses(new ArrayList<>(screeningQuestionsMap.values()));

        returnObj.setEmail(objFromDb.getEmail());
        returnObj.setMobile(objFromDb.getMobile());
        objFromDb.setCvRating(cvRatingRepository.findByJobCandidateMappingId(objFromDb.getId()));
        if(null != objFromDb.getCvRating()) {
            List<CvRatingSkillKeywordDetails> cvRatingSkillKeywordDetails = cvRatingSkillKeywordDetailsRepository.findByCvRatingId(objFromDb.getCvRating().getId());
            Map<Integer, List<CvRatingSkillKeywordDetails>> tempMap = cvRatingSkillKeywordDetails.stream().collect(Collectors.groupingBy(CvRatingSkillKeywordDetails::getRating));
            Map<Integer, Map<String, Integer>> cvSkillsByRating = new HashMap<>(tempMap.size());
            tempMap.forEach((key, value) -> {
                Map<String, Integer> skills = new HashMap<>(value.size());
                value.stream().forEach(skillKeywordDetail -> {
                    skills.put(skillKeywordDetail.getSkillName(), skillKeywordDetail.getOccurrence());
                });
                cvSkillsByRating.put(key, skills);
            });
            objFromDb.setCandidateSkillsByRating(cvSkillsByRating);
        }

        if(null != hiringManagerInterestDate)
            objFromDb.setHiringManagerInterestDate(hiringManagerInterestDate);

        List<CandidateInteractionHistory> candidateInteractionHistoryList = jobCandidateMappingRepository.getCandidateInteractionHistoryByCandidateId(objFromDb.getCandidate().getId());
        if(!candidateInteractionHistoryList.isEmpty()){
            objFromDb.getCandidate().setCandidateInteractionHistoryList(candidateInteractionHistoryList);
        }
        return objFromDb;
    }

    /**
     * Service method to fetch details of a single candidate for a job
     *
     * @param profileSharingUuid uuid corresponding to the profile shared with hiring manager
     * @return candidate object with required details
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public JobCandidateMapping getCandidateProfile(UUID profileSharingUuid) throws Exception {
        JcmProfileSharingDetails details = jcmProfileSharingDetailsRepository.findById(profileSharingUuid);
        if(null == details)
            throw new WebException("Profile not found", HttpStatus.UNPROCESSABLE_ENTITY);

        return getCandidateProfile(details.getJobCandidateMappingId(), details.getHiringManagerInterestDate());
    }

    /**
     * Method to retrieve the job candidate mapping record based on the uuid
     * @param uuid the uuid against which the record is to be retrieved
     * @return the job candidate mapping
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public JobCandidateMapping getJobCandidateMapping(UUID uuid) throws Exception {
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findByChatbotUuid(uuid);
        if (null == objFromDb)
            throw new WebException(IErrorMessages.UUID_NOT_FOUND + uuid, HttpStatus.UNPROCESSABLE_ENTITY);

        objFromDb.setJcmCommunicationDetails(jcmCommunicationDetailsRepository.findByJcmId(objFromDb.getId()));
        objFromDb.getJob().setCompanyName(objFromDb.getJob().getCompanyId().getCompanyName());
        objFromDb.getJob().setCompanyDescription(objFromDb.getJob().getCompanyId().getCompanyDescription());
        return objFromDb;
    }

    private void sendSentryMail(String info,String fileName, Long jobId){
        log.info(info);
        Map<String, String> breadCrumb = new HashMap<>();
        if(null!=jobId)
            breadCrumb.put("JobId",jobId.toString());

        if(null!=fileName)
            breadCrumb.put("FileName",fileName);

        SentryUtil.logWithStaticAPI(null, info, breadCrumb);
    }

    /**
     * Service method to upload candidates by means of drag and drop cv
     *
     * @param multipartFiles files to be processed to upload candidates
     * @param jobId          the job for which the candidate is to be added
     * @return response bean with details about success / failure of each candidate file
     * @throws Exception
     */
    @Transactional
    public CvUploadResponseBean processDragAndDropCv(MultipartFile[] multipartFiles, Long jobId) {
        CvUploadResponseBean responseBean = new CvUploadResponseBean();

        String filePath = null;
        String fileType=null;
        int filesProcessed = 0;
        Integer successCount = 0, failureCount =0;
        Integer[] countArray = new Integer[0];

        for (MultipartFile fileToProcess :multipartFiles) {
            String extension = Util.getFileExtension(fileToProcess.getOriginalFilename()).toLowerCase();
            if (filesProcessed == MasterDataBean.getInstance().getConfigSettings().getMaxCvFiles()) {
                responseBean.getCvUploadMessage().put(fileToProcess.getOriginalFilename(), IErrorMessages.MAX_FILES_PER_UPLOAD);
            }
            //check if the extension is supported by RChilli
            else if(!Arrays.asList(IConstant.cvUploadSupportedExtensions).contains(extension)) {
                failureCount++;
                responseBean.getCvUploadMessage().put(fileToProcess.getOriginalFilename(), IErrorMessages.UNSUPPORTED_FILE_TYPE + extension);
            }
            else {

                if(extension.equals(IConstant.FILE_TYPE.zip.toString()))
                    fileType=IConstant.FILE_TYPE.zip.toString();
                else if(extension.equals(IConstant.FILE_TYPE.rar.toString()))
                    fileType=IConstant.FILE_TYPE.rar.toString();
                else
                    fileType=IConstant.FILE_TYPE.other.toString();

                User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                try {
                    filePath = StoreFileUtil.storeFile(fileToProcess, jobId, environment.getProperty(IConstant.TEMP_REPO_LOCATION), fileType,null, loggedInUser);
                    successCount++;
                } catch (Exception e) {
                    log.error(fileToProcess.getOriginalFilename()+" not save to temp location : "+e.getMessage());
                    failureCount++;
                    responseBean.getCvUploadMessage().put(fileToProcess.getOriginalFilename(), IErrorMessages.FAILED_TO_SAVE_FILE + extension);
                }

                if(fileType.equals(IConstant.FILE_TYPE.zip.toString()) || fileType.equals(IConstant.FILE_TYPE.rar.toString())){
                    successCount--;
                    countArray=ZipFileProcessUtil.extractZipFile(filePath, environment.getProperty(IConstant.TEMP_REPO_LOCATION), loggedInUser.getId(),jobId, responseBean, failureCount,successCount);
                    failureCount=countArray[0];
                    successCount=countArray[1];
                }
            }
        }
        //depending on whether all files succeeded or failed, set status as Success / Failure / Partial Success
        if(successCount == 0) { //Failure count
            responseBean.setUploadRequestStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }else if(failureCount == 0)    //Failure count
            responseBean.setUploadRequestStatus(IConstant.UPLOAD_STATUS.Success.name());
        else
            responseBean.setUploadRequestStatus(IConstant.UPLOAD_STATUS.Partial_Success.name());

        return responseBean;
    }

    /**
     * Service to update tech response status received from scoring engine.
     *
     * @param requestBean bean with update information from scoring engine
     * @throws Exception
     */
    @Transactional
    public void updateTechResponseStatus(TechChatbotRequestBean requestBean) throws Exception {
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findByChatbotUuid(requestBean.getChatbotUuid());
        log.info("Got response for " + requestBean.getChatbotUuid() + " with status as " + requestBean.getChatbotStatus() + " score: " + requestBean.getScore());
        if(null == objFromDb)
            throw new WebException(IErrorMessages.UUID_NOT_FOUND+requestBean.getChatbotUuid(),HttpStatus.UNPROCESSABLE_ENTITY);

        objFromDb.setChatbotStatus(requestBean.getChatbotStatus());
        objFromDb.setScore(requestBean.getScore());
        objFromDb.setChatbotUpdatedOn(requestBean.getChatbotUpdatedOn());
        if(null != requestBean.getTechResponseJson()) {
            log.info("Found tech response json for "  + requestBean.getChatbotUuid() + " with status as " + requestBean.getChatbotStatus() + " score: " + requestBean.getScore());
            objFromDb.getTechResponseData().setTechResponse(requestBean.getTechResponseJson());
        }
        jobCandidateMappingRepository.save(objFromDb);
        if(requestBean.getChatbotStatus().equals(IConstant.CHATBOT_STATUS.Complete.name())) {
            log.info("Updated chatbot status for "  + requestBean.getChatbotUuid() + " with status as " + requestBean.getChatbotStatus() + " score: " + requestBean.getScore());
            jcmCommunicationDetailsRepository.updateByJcmId(objFromDb.getId());
        }
    }

    /**
     * Service to edit candidate info like:mobile,email,TotalExperience
     *
     * @param jobCandidateMapping updated data from JobCandidateMapping model
     */
    @Transactional
    @Override
    public void editCandidate(JobCandidateMapping jobCandidateMapping) {
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findById(jobCandidateMapping.getId()).orElse(null);

        //Update or create mobile no
        if(null!=jobCandidateMapping.getMobile() && !jobCandidateMapping.getMobile().isEmpty()){
            validateMobile(jobCandidateMapping.getMobile(), jobCandidateMapping.getCandidate().getCountryCode());
            CandidateMobileHistory candidateMobileHistory = candidateMobileHistoryRepository.findByMobileAndCountryCode(jobCandidateMapping.getMobile(), jobCandidateMapping.getCandidate().getCountryCode());
            if(null == candidateMobileHistory) {
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(jcmFromDb.getCandidate(), jobCandidateMapping.getMobile(), jobCandidateMapping.getCandidate().getCountryCode(), new Date(), loggedInUser));
                jcmFromDb.setMobile(jobCandidateMapping.getMobile());
            }else {
                if(!jcmFromDb.getCandidate().getId().equals(candidateMobileHistory.getCandidate().getId()))
                    throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY_FOR_MOBILE + jobCandidateMapping.getMobile() + " " + jobCandidateMapping.getEmail(), HttpStatus.BAD_REQUEST);
                else
                    jcmFromDb.setMobile(candidateMobileHistory.getMobile());
            }
        }
        //Update or create email id
        if(null!=jobCandidateMapping.getEmail() && !jobCandidateMapping.getEmail().isEmpty()) {
            validateEmail(jobCandidateMapping.getEmail());
            CandidateEmailHistory candidateEmailHistory = candidateEmailHistoryRepository.findByEmail(jobCandidateMapping.getEmail());
            if (null == candidateEmailHistory) {
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(jcmFromDb.getCandidate(), jobCandidateMapping.getEmail(), new Date(), loggedInUser));
                jcmFromDb.setEmail(jobCandidateMapping.getEmail());
            } else {
                if (!jcmFromDb.getCandidate().getId().equals(candidateEmailHistory.getCandidate().getId()))
                    throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY_FOR_EMAIL + jobCandidateMapping.getMobile() + " " + jobCandidateMapping.getEmail(), HttpStatus.BAD_REQUEST);
                else
                    jcmFromDb.setEmail(candidateEmailHistory.getEmail());
            }
        }

        //Update candidate firstName
        if(Util.isNotNull(jobCandidateMapping.getCandidateFirstName())){
            jcmFromDb.setCandidateFirstName(Util.validateCandidateName(jobCandidateMapping.getCandidateFirstName()));
        }

        //Update candidate lastName
        if(Util.isNotNull(jobCandidateMapping.getCandidateLastName())){
            jcmFromDb.setCandidateLastName(Util.validateCandidateName(jobCandidateMapping.getCandidateLastName()));
        }

        jobCandidateMappingRepository.save(jcmFromDb);

        //Update Candidate total experience
        CandidateDetails candidateDetails = null;
        if(null!=jcmFromDb.getCandidate().getCandidateDetails()){
            candidateDetails = candidateDetailsRepository.findById(jcmFromDb.getCandidate().getCandidateDetails().getId()).orElse(null);
        }

        if(null != candidateDetails){
            candidateDetails.setTotalExperience(jobCandidateMapping.getCandidate().getCandidateDetails().getTotalExperience());
            candidateDetailsRepository.save(candidateDetails);
        }else if(null != jobCandidateMapping.getCandidate().getCandidateDetails().getTotalExperience()){
            candidateDetailsRepository.save(new CandidateDetails(jcmFromDb.getCandidate(), jobCandidateMapping.getCandidate().getCandidateDetails().getTotalExperience()));
        }

        //Update candidate education details
        if(Util.isNotNull(jobCandidateMapping.getCandidate().getCandidateEducationDetails().get(0).getDegree())){
            AtomicBoolean isDegreePresent = new AtomicBoolean(false);
            jcmFromDb.getCandidate().getCandidateEducationDetails().stream().forEach(candidateEducationDetails -> {
                if(candidateEducationDetails.getDegree().equalsIgnoreCase(jobCandidateMapping.getCandidate().getCandidateEducationDetails().get(0).getDegree())){
                    isDegreePresent.set(true);
                }
            });
            if(!isDegreePresent.get() ||  null == jcmFromDb.getCandidate().getCandidateEducationDetails()){
                if(jobCandidateMapping.getCandidate().getCandidateEducationDetails().get(0).getDegree().length()>IConstant.MAX_FIELD_LENGTHS.DEGREE.getValue())
                    jobCandidateMapping.getCandidate().getCandidateEducationDetails().get(0).setDegree(Util.truncateField(jcmFromDb.getCandidate(),IConstant.MAX_FIELD_LENGTHS.DEGREE.name(), IConstant.MAX_FIELD_LENGTHS.DEGREE.getValue(),jobCandidateMapping.getCandidate().getCandidateEducationDetails().get(0).getDegree()));

                CandidateEducationDetails candidateEducationDetails = new CandidateEducationDetails(jcmFromDb.getCandidate().getId(), jobCandidateMapping.getCandidate().getCandidateEducationDetails().get(0).getDegree(), String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                candidateEducationDetailsRepository.save(candidateEducationDetails);
            }
        }

        //Update candidate company detail
        CandidateCompanyDetails companyDetails = null;
        CandidateCompanyDetails companyDetailsByRequest = jobCandidateMapping.getCandidate().getCandidateCompanyDetails().get(0);
        if(Util.isNotNull(companyDetailsByRequest.getCompanyName())){
            AtomicBoolean isCompanyPresent = new AtomicBoolean(false);
            jcmFromDb.getCandidate().getCandidateCompanyDetails().stream().forEach(CompanyDetails -> {
                if(CompanyDetails.getCompanyName().equalsIgnoreCase(companyDetailsByRequest.getCompanyName())){
                    isCompanyPresent.set(true);
                }
            });
            if(!isCompanyPresent.get() || null == jcmFromDb.getCandidate().getCandidateCompanyDetails()){
                Date endDate = null;
                Date startDate = null;
                try {
                    //in getCurrentOrBefore1YearDate method  pass boolean value
                    //get Before 1 year date then pass true if get current date then pass false value
                    endDate = Util.getCurrentOrBefore1YearDate(false);
                    startDate = Util.getCurrentOrBefore1YearDate(true);
                } catch (ParseException e) {
                    log.error("Error while set start date and end date in candidate company detail : "+e.getMessage());
                }
                companyDetails = new CandidateCompanyDetails(jcmFromDb.getCandidate().getId(), companyDetailsByRequest.getCompanyName(), startDate, endDate);
                companyDetails = addCompanyDetailsInfo(companyDetails, companyDetailsByRequest);
                if(null != jcmFromDb.getCandidate().getCandidateCompanyDetails()){
                    List<CandidateCompanyDetails> oldCompanyList = jcmFromDb.getCandidate().getCandidateCompanyDetails();
                    List<CandidateCompanyDetails> newCompanyList = new ArrayList<>(oldCompanyList.size()+1);
                    newCompanyList.add(companyDetails);
                    newCompanyList.addAll(oldCompanyList);
                    candidateCompanyDetailsRepository.deleteAll(oldCompanyList);
                    candidateCompanyDetailsRepository.flush();
                    candidateCompanyDetailsRepository.saveAll(newCompanyList);
                }
            }else {
                if(null != companyDetailsByRequest.getId()){
                    companyDetails = candidateCompanyDetailsRepository.findById(companyDetailsByRequest.getId()).orElse(null);
                    companyDetails = addCompanyDetailsInfo(companyDetails, companyDetailsByRequest);
                    candidateCompanyDetailsRepository.save(companyDetails);
                }
            }
            log.info("Edit candidate info successfully");
        }

    }

    private CandidateCompanyDetails addCompanyDetailsInfo(CandidateCompanyDetails companyDetails, CandidateCompanyDetails companyDetailsByRequest) {
        if(null != companyDetails){
            if (Util.isNotNull(companyDetailsByRequest.getNoticePeriod()))
                companyDetails.setNoticePeriodInDb(MasterDataBean.getInstance().getNoticePeriodMapping().get(companyDetailsByRequest.getNoticePeriod()));

            if (Util.isNotNull(companyDetailsByRequest.getSalary()))
                companyDetails.setSalary(companyDetailsByRequest.getSalary());

            if (Util.isNotNull(companyDetailsByRequest.getDesignation()))
                companyDetails.setDesignation(companyDetailsByRequest.getDesignation());
        }
        return companyDetails;
    }


    private String validateMobile(String mobile, String countryCode){
        if(Util.isNotNull(mobile)) {
            if (!Util.validateMobile(mobile, countryCode) && !countryCode.equals(IConstant.CountryCode.INDIA_CODE.getValue())) {
                String cleanMobile = mobile.replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_MOBILE, "");
                log.error("Special characters found, cleaning mobile number \"" + mobile + "\" to " + cleanMobile);
                if (!Util.validateMobile(cleanMobile, countryCode))
                    throw new ValidationException(IErrorMessages.MOBILE_INVALID_DATA + " - " + mobile, HttpStatus.BAD_REQUEST);
               return cleanMobile;
            }
        }
        return mobile;
    }

    private String validateEmail(String receiverEmailToUse){
        if (!Util.validateEmail(receiverEmailToUse)) {
            String cleanEmail = receiverEmailToUse.replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_EMAIL,"");
            log.error("Special characters found, cleaning Email \"" + receiverEmailToUse + "\" to " + cleanEmail);
            if (!Util.validateEmail(cleanEmail)) {
                throw new ValidationException(IErrorMessages.INVALID_EMAIL + " - " + receiverEmailToUse, HttpStatus.BAD_REQUEST);
            }
            receiverEmailToUse=cleanEmail;
        }
        if(receiverEmailToUse.length()>50)
            throw new ValidationException(IErrorMessages.EMAIL_TOO_LONG, HttpStatus.BAD_REQUEST);

        return receiverEmailToUse;
    }
}