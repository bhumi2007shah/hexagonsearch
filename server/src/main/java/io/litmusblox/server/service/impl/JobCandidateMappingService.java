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
import java.math.BigInteger;
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

    @Resource
    CandidateOnlineProfilesRepository candidateOnlineProfilesRepository;

    @Resource
    CandidateSkillDetailsRepository candidateSkillDetailsRepository;

    @Resource
    CandidateProjectDetailsRepository candidateProjectDetailsRepository;

    @Resource
    CandidateLanguageProficiencyRepository candidateLanguageProficiencyRepository;

    @Resource
    CandidateWorkAuthorizationRepository candidateWorkAuthorizationRepository;

    @Resource
    CandidateTechResponseDataRepository candidateTechResponseDataRepository;

    @Resource
    CandidateRepository candidateRepository;

    @Resource
    StageStepMasterRepository stageStepMasterRepository;

    @Resource
    EmployeeReferrerRepository employeeReferrerRepository;

    @Autowired
    IProcessOtpService otpService;

    @Resource
    UserRepository userRepository;

    @Resource
    CustomizedChatbotPageContentRepository customizedChatbotPageContentRepository;

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
     * @param createdBy optional paramter, createdBy, will be supplied only when calling processing candidates from mail
     * @return the status of upload operation
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UploadResponseBean uploadIndividualCandidate(List<Candidate> candidates, Long jobId, boolean ignoreMobile, Optional<User> createdBy) throws Exception {

        //verify that the job is live before processing candidates
        Job job = jobRepository.getOne(jobId);
        if(null == job || !IConstant.JobStatus.PUBLISHED.getValue().equals(job.getStatus())) {
            StringBuffer info = new StringBuffer("Selected job is not live ").append("JobId :").append(jobId);
            Map<String, String> breadCrumb = new HashMap<>();
            breadCrumb.put("detail", info.toString());
            throw new WebException(IErrorMessages.JOB_NOT_LIVE, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }

        UploadResponseBean uploadResponseBean = new UploadResponseBean();
        User loggedInUser = (null != createdBy && createdBy.isPresent())?createdBy.get():(User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Date createdOn=Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        int candidateProcessed=jobCandidateMappingRepository.getUploadedCandidateCount(createdOn,loggedInUser);

        if (candidateProcessed >= MasterDataBean.getInstance().getConfigSettings().getDailyCandidateUploadPerUserLimit()) {
            log.error(IErrorMessages.MAX_CANDIDATE_PER_FILE_EXCEEDED + " :: user id : " + loggedInUser.getId() + " : not processing records");
            throw new WebException(IErrorMessages.MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        try {
            if(ignoreMobile)
                processCandidateData(candidates, uploadResponseBean, loggedInUser, jobId, candidateProcessed, ignoreMobile);
            else
                processCandidateData(candidates, uploadResponseBean, loggedInUser, jobId, candidateProcessed, !IConstant.STR_INDIA.equalsIgnoreCase(loggedInUser.getCountryId().getCountryName()));
        } catch (Exception ex) {
            log.error("Error while processing candidates uploaded :: " + ex.getMessage());
            uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        return uploadResponseBean;
    }

    private void processCandidateData(List<Candidate> candidateList, UploadResponseBean uploadResponseBean, User loggedInUser, Long jobId, int candidateProcessed, boolean ignoreMobile) throws Exception{

        if (null != candidateList && candidateList.size() > 0) {
            iUploadDataProcessService.processData(candidateList, uploadResponseBean, candidateProcessed,jobId, ignoreMobile, Optional.of(loggedInUser));
        }

        for (Candidate candidate:candidateList) {
            try {
                if(null!=candidate.getId())
                    saveCandidateSupportiveInfo(candidate, loggedInUser);
            }catch (Exception ex){
                log.error("Error while processing candidates supportive info :: " + ex.getMessage());
            }

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
                    if(null != educationDetails.getInstituteName() && educationDetails.getInstituteName().length() > IConstant.MAX_INSTITUTE_LENGTH) {
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
        if(null == job || !IConstant.JobStatus.PUBLISHED.getValue().equals(job.getStatus())) {
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
        List<Candidate> candidateList = processUploadedFile(fileName, uploadResponseBean, loggedInUser, fileFormat, environment.getProperty(IConstant.REPO_LOCATION), job.getCompanyId().getCountryId().getCountryCode());

        try {
            processCandidateData(candidateList, uploadResponseBean, loggedInUser, jobId, candidatesProcessed, false);

        } catch (Exception ex) {
            log.error("Error while processing file " + fileName + " :: " + ex.getMessage());
            uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }

        return uploadResponseBean;
    }

    private List<Candidate> processUploadedFile(String fileName, UploadResponseBean responseBean, User user, String fileSource, String repoLocation, String countryCode) {
        //code to parse through the records and save data in database
        String fileExtension = Util.getFileExtension(fileName).toLowerCase();
        List<Candidate> candidateList = null;
        switch (fileExtension) {
            case "csv":
                candidateList = new CsvFileProcessorService().process(fileName, responseBean, !IConstant.STR_INDIA.equalsIgnoreCase(user.getCountryId().getCountryName()), repoLocation, countryCode);
                break;
            case "xls":
            case "xlsx":
                switch (IConstant.UPLOAD_FORMATS_SUPPORTED.valueOf(fileSource)) {
                    case LitmusBlox:
                        candidateList = new ExcelFileProcessorService().process(fileName, responseBean, !IConstant.STR_INDIA.equalsIgnoreCase(user.getCountryId().getCountryName()), repoLocation, countryCode);
                        break;
                    case Naukri:
                        log.info("Reached the naukri parser");
                        candidateList = new NaukriExcelFileProcessorService().process(fileName, responseBean, !IConstant.STR_INDIA.equalsIgnoreCase(user.getCountryId().getCountryName()), repoLocation, countryCode);

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
     * @param createdBy optional paramter, createdBy, will be supplied only when calling processing candidates from mail
     * @return the status of upload operation
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UploadResponseBean uploadCandidateFromPlugin(Candidate candidate, Long jobId, MultipartFile candidateCv, Optional<User> createdBy) throws Exception {
        UploadResponseBean responseBean = null;
        if (null != candidate) {

            if(null == candidate.getCandidateName() || candidate.getCandidateName().isEmpty()){
                candidate.setCandidateName(IConstant.NOT_AVAILABLE);
                candidate.setFirstName(IConstant.NOT_AVAILABLE);
            }else{
                //populate the first name and last name of the candidate
                Util.handleCandidateName(candidate, candidate.getCandidateName());
            }

            // If email is null set email to notavailable<timeInMillis>@notavailable.io
            if(null == candidate.getEmail() || candidate.getEmail().isEmpty()){
                candidate.setEmail("notavailable"+System.currentTimeMillis()+"@notavailable.io");
            }

            //check source of candidate and set source as coorect one from IConstant
            if (candidate.getCandidateSource().contains(IConstant.CandidateSource.NaukriMassMail.getValue()))
                candidate.setCandidateSource(IConstant.CandidateSource.NaukriMassMail.getValue());
            else if (candidate.getCandidateSource().contains(IConstant.CandidateSource.NaukriJobPosting.getValue()))
                candidate.setCandidateSource(IConstant.CandidateSource.NaukriJobPosting.getValue());
            else if(candidate.getCandidateSource().contains(IConstant.CandidateSource.Naukri.getValue())){
                candidate.setCandidateSource(IConstant.CandidateSource.Naukri.getValue());
            }
            else if(candidate.getCandidateSource().contains(IConstant.CandidateSource.LinkedIn.getValue())){
                candidate.setCandidateSource(IConstant.CandidateSource.LinkedIn.getValue());
            }
            else if(candidate.getCandidateSource().contains(IConstant.CandidateSource.IIMJobs.getValue())){
                candidate.setCandidateSource(IConstant.CandidateSource.IIMJobs.getValue());
            }

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

            responseBean = uploadIndividualCandidate(Arrays.asList(candidate), jobId, (null == candidate.getMobile() || candidate.getMobile().isEmpty()), createdBy);

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

        if(interest) {
            //setting chatbot status as complete if both hr and technical chatbot are missing.
            if (!objFromDb.getJob().getHrQuestionAvailable() && !objFromDb.getJob().getScoringEngineJobAvailable()) {
                objFromDb.setChatbotStatus(IConstant.ChatbotStatus.COMPLETE.getValue());
            } else {
                objFromDb.setChatbotStatus(IConstant.ChatbotStatus.INCOMPLETE.getValue());
            }
        }
        else{
            objFromDb.setChatbotStatus(IConstant.ChatbotStatus.NOT_INSTERESTED.getValue());
        }
        objFromDb.setCandidateInterestDate(new Date());
        //commented below code to not set flags to true.
        /*if(!objFromDb.getJob().getHrQuestionAvailable()){
            jcmCommunicationDetailsRepository.updateHrChatbotFlagByJcmId(objFromDb.getId());
        }
        if(!objFromDb.getJob().getHrQuestionAvailable() && !objFromDb.getJob().getScoringEngineJobAvailable()){
            jcmCommunicationDetailsRepository.updateByJcmId(objFromDb.getId());
        }*/
        jobCandidateMappingRepository.save(objFromDb);
        jcmHistoryRepository.save(new JcmHistory(objFromDb, "Candidate is"+ (interest?" interested.":" not interested."), new Date(), null, objFromDb.getStage()));
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
        JcmCommunicationDetails jcmCommunicationDetailsFromDb = jcmCommunicationDetailsRepository.findByJcmId(objFromDb.getId());
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

        //update chatbot updated date
        objFromDb.setChatbotUpdatedOn(new Date());

        //set chatbot status to complete if scoring engine does not have job or tech chatbot is complete.
        if(!objFromDb.getJob().getScoringEngineJobAvailable() || jcmCommunicationDetailsFromDb.isTechChatCompleteFlag()){
            objFromDb.setChatbotStatus(IConstant.ChatbotStatus.COMPLETE.getValue());

        }

        //Commented below code as we are not setting flag to true as per discussion on 10-01-2020
        //updating chat_complete_flag if corresponding job is not available on scoring engine due to lack of ML data,
        // or candidate already filled all the capabilities in some other job and we already have candidate responses for technical chatbot.
        /*if(!objFromDb.getJob().getScoringEngineJobAvailable() || (objFromDb.getChatbotStatus()!=null && objFromDb.getChatbotStatus().equals("Complete"))){
            jcmCommunicationDetailsRepository.updateByJcmId(objFromDb.getId());
        }*/
        jobCandidateMappingRepository.save(objFromDb);
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
    public InviteCandidateResponseBean inviteCandidates(List<Long> jcmList) throws Exception {
        InviteCandidateResponseBean inviteCandidateResponseBean = performInvitationAndHistoryUpdation(jcmList);
        callScoringEngineToAddCandidates(jcmList);
        return inviteCandidateResponseBean;
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
                        String scoringEngineResponse = RestClient.getInstance().consumeRestApi(null, scoringEngineBaseUrl + scoringEngineAddCandidateUrlSuffix, HttpMethod.PUT, null, Optional.of(queryParams), null).getResponseBody();
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
                            if (IConstant.ChatbotStatus.COMPLETE.getValue().equalsIgnoreCase(techChatbotRequestBean.getChatbotStatus())) {
                                log.info("Found complete status from scoring engine: " + jcm.getEmail() + " ~ " + jcm.getId());
                                //Set chatCompleteFlag = true
                                JcmCommunicationDetails jcmCommunicationDetails = jcmCommunicationDetailsRepository.findByJcmId(jcm.getId());
                                jcmCommunicationDetails.setTechChatCompleteFlag(true);
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
    private InviteCandidateResponseBean performInvitationAndHistoryUpdation(List<Long> jcmList) throws Exception {
        if(jcmList == null || jcmList.size() == 0)
            throw new WebException("Select candidates to invite",HttpStatus.UNPROCESSABLE_ENTITY);

        //make sure all candidates are at the same stage
        if(!areCandidatesInSameStage(jcmList))
            throw new WebException("Select candidates that are all in Source stage", HttpStatus.UNPROCESSABLE_ENTITY);

        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //list to store candidates for which email contains "@notavailable.io" or mobile is null
        List<Candidate>failedCandidates = new ArrayList<>();

        //List to store jcm ids for which email does not start with "@notavailable.io" or mobile is not null
        List<Long> jcmListWithoutError = new ArrayList<>();

        //Invite candidate respose bean hoolds status, success count, failure count, failed candidates whose email or mobile is not valid.
        InviteCandidateResponseBean inviteCandidateResponseBean = null;

        //fetch list of jcm from db using ids
        List<JobCandidateMapping>jobCandidateMappingList = jobCandidateMappingRepository.findAllById(jcmList);

        Job jobObjToUse = null;
        //iterate over jcm list
        for(JobCandidateMapping jobCandidateMapping: jobCandidateMappingList){

            //check if email does not contain "@notavailable.io" or mobile is not null
            if(jobCandidateMapping.getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL)){
                failedCandidates.add(jobCandidateMapping.getCandidate());
                continue;
            }
            else if(Util.isNull(jobCandidateMapping.getMobile())){
                failedCandidates.add(jobCandidateMapping.getCandidate());
                continue;
            }
            if(null == jobObjToUse)
                jobObjToUse = jobCandidateMapping.getJob();

            jcmListWithoutError.add(jobCandidateMapping.getId());
        }

        if(jcmListWithoutError.size()==0){
            inviteCandidateResponseBean =  new InviteCandidateResponseBean(IConstant.UPLOAD_STATUS.Failure.toString(), 0, jcmList.size(), failedCandidates);
        }
        else{
            if(jcmListWithoutError.size()<jcmList.size()) {
                inviteCandidateResponseBean = new InviteCandidateResponseBean(IConstant.UPLOAD_STATUS.Partial_Success.toString(),jcmListWithoutError.size(), jcmList.size()-jcmListWithoutError.size(), failedCandidates);
            }
            else{
                inviteCandidateResponseBean = new InviteCandidateResponseBean(IConstant.UPLOAD_STATUS.Success.toString(), jcmListWithoutError.size(), 0, failedCandidates);
            }
            jcmCommunicationDetailsRepository.inviteCandidates(jcmListWithoutError);
            jobCandidateMappingRepository.updateJcmSetStatus(IConstant.ChatbotStatus.INVITED.getValue(), jcmListWithoutError);
        }

        if(null == jobObjToUse && jcmListWithoutError.size() > 0) {
            log.error("Job stage steps not found. Cannot move candidate from Source to Screen");
        } else if(jcmListWithoutError.size()>0) {
            //set stage = Screening where stage = Source
            Map<String, Long> stageIdMap = MasterDataBean.getInstance().getStageStepMasterMap();
            jobCandidateMappingRepository.updateStageStepId(jcmListWithoutError, stageIdMap.get(IConstant.Stage.Source.getValue()), stageIdMap.get(IConstant.Stage.Screen.getValue()), loggedInUser.getId(), new Date());
            updateJcmHistory(jcmListWithoutError, loggedInUser);
        }
        return inviteCandidateResponseBean;
    }

    void updateJcmHistory(List<Long> jcmList, User loggedInUser) {
        log.info("Completed updating chat_invite_flag for the list of jcm");

        List<JcmHistory> jcmHistoryList = new ArrayList<>();

        for (Long jcmId : jcmList) {
            JobCandidateMapping tempObj = jobCandidateMappingRepository.getOne(jcmId);
            jcmHistoryList.add(new JcmHistory(tempObj, "Candidate invited", new Date(), loggedInUser, tempObj.getStage()));
        }

        if (jcmHistoryList.size() > 0) {
            jcmHistoryRepository.saveAll(jcmHistoryList);
        }

        log.info("Added jmcHistory data");
    }

    private boolean areCandidatesInSameStage(List<Long> jcmList) throws Exception{
        if(jobCandidateMappingRepository.countDistinctStageForJcmList(jcmList) != 1)
            return false;
        return true;
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
        
        JobCandidateMapping tempObj = jobCandidateMappingRepository.getOne(requestBean.getJcmId().get(0));
        jcmHistoryRepository.save(new JcmHistory(tempObj, "Profiles shared with : "+String.join(", ", recieverEmails)+".", new Date(), loggedInUser, tempObj.getStage()));

        //move to Submit stage
        if(IConstant.Stage.Source.getValue().equals(tempObj.getStage().getStage()) || IConstant.Stage.Screen.getValue().equals(tempObj.getStage().getStage())){
            jobCandidateMappingRepository.updateStageStepId(requestBean.getJcmId(), tempObj.getStage().getId(), MasterDataBean.getInstance().getStageStepMasterMap().get(IConstant.Stage.ResumeSubmit.getValue()), loggedInUser.getId(), new Date());
        }

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
        StringBuffer cvLocation = new StringBuffer("");
        if(null != objFromDb && null != objFromDb.getCvFileType()) {
            cvLocation.append(IConstant.CANDIDATE_CV).append(File.separator).append(objFromDb.getJob().getId()).append(File.separator).append(objFromDb.getCandidate().getId()).append(objFromDb.getCvFileType());
            objFromDb.setCvLocation(cvLocation.toString());
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

        List<CandidateInteractionHistory> candidateInteractionHistoryList = jobCandidateMappingRepository.getCandidateInteractionHistoryByCandidateId(objFromDb.getCandidate().getId(), objFromDb.getJob().getCompanyId().getId());
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

                if(IConstant.FILE_TYPE.zip.toString().equals(extension))
                    fileType=IConstant.FILE_TYPE.zip.toString();
                else if(IConstant.FILE_TYPE.rar.toString().equals(extension))
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

                if(IConstant.FILE_TYPE.zip.toString().equals(fileType) || IConstant.FILE_TYPE.rar.toString().equals(fileType)){
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
        if(IConstant.ChatbotStatus.COMPLETE.getValue().equals(requestBean.getChatbotStatus())) {
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

        //update or create email id and mobile
        Boolean jcmFromDbDeleted = updateOrCreateEmailMobile(jobCandidateMapping, jcmFromDb, loggedInUser);

        if(!jcmFromDbDeleted) {
            //Update candidate firstName
            if (Util.isNotNull(jobCandidateMapping.getCandidateFirstName())) {
                jcmFromDb.setCandidateFirstName(Util.validateCandidateName(jobCandidateMapping.getCandidateFirstName()));
            }
            //Update candidate lastName
            if (Util.isNotNull(jobCandidateMapping.getCandidateLastName())) {
                jcmFromDb.setCandidateLastName(Util.validateCandidateName(jobCandidateMapping.getCandidateLastName()));
            }
            //Update candidate reason of change
            if(null != jobCandidateMapping.getReasonForChange()){
                if (jobCandidateMapping.getReasonForChange().length() > IConstant.MAX_FIELD_LENGTHS.REASON_FOR_CHANGE.getValue())
                    jobCandidateMapping.setReasonForChange(Util.truncateField(jcmFromDb.getCandidate(), IConstant.MAX_FIELD_LENGTHS.REASON_FOR_CHANGE.name(), IConstant.MAX_FIELD_LENGTHS.REASON_FOR_CHANGE.getValue(), jobCandidateMapping.getReasonForChange()));

                jcmFromDb.setReasonForChange(jobCandidateMapping.getReasonForChange());
            }


            //Update candidate servingNoticePeriod
            jcmFromDb.setServingNoticePeriod(jobCandidateMapping.isServingNoticePeriod());
            //Update candidate negotiableNoticePeriod
            jcmFromDb.setNegotiableNoticePeriod(jobCandidateMapping.isNegotiableNoticePeriod());
            //Update candidate otherOffers
            jcmFromDb.setOtherOffers(jobCandidateMapping.isOtherOffers());
            //Update candidate updateResume
            jcmFromDb.setUpdateResume(jobCandidateMapping.isUpdateResume());

            //Update candidate Communication Skill Rating
            if (null != jobCandidateMapping.getCommunicationRating()) {
                jcmFromDb.setCommunicationRating(jobCandidateMapping.getCommunicationRating());
            }

            jcmFromDb = updateOrCreateAlternateMobileEmail(jobCandidateMapping, jcmFromDb, loggedInUser);

            jobCandidateMappingRepository.save(jcmFromDb);

            //Update Candidate Details
            CandidateDetails candidateDetails = null;
            CandidateDetails candidateDetailsByRequest = jobCandidateMapping.getCandidate().getCandidateDetails();
            if(null != candidateDetailsByRequest){
                if (null != jcmFromDb.getCandidate().getCandidateDetails()) {
                    candidateDetails = candidateDetailsRepository.findById(jcmFromDb.getCandidate().getCandidateDetails().getId()).orElse(null);
                }

                if (null != candidateDetails) {
                    candidateDetails.setTotalExperience(candidateDetailsByRequest.getTotalExperience());
                    candidateDetails.setRelevantExperience(candidateDetailsByRequest.getRelevantExperience());
                    candidateDetails.setDateOfBirth(candidateDetailsByRequest.getDateOfBirth());
                    candidateDetails.setLocation(candidateDetailsByRequest.getLocation());
                    candidateDetailsRepository.save(candidateDetails);
                } else {
                    candidateDetailsRepository.save(new CandidateDetails(candidateDetailsByRequest.getDateOfBirth(), candidateDetailsByRequest.getLocation(), candidateDetailsByRequest.getTotalExperience(), candidateDetailsByRequest.getRelevantExperience(), jcmFromDb.getCandidate()));
                }
            }

            //Update education details
            JobCandidateMapping finalJcmFromDb = jcmFromDb;
            if(jobCandidateMapping.getCandidate().getCandidateEducationDetails().size()>0){
                candidateEducationDetailsRepository.deleteByCandidateId(finalJcmFromDb.getCandidate().getId());
                candidateEducationDetailsRepository.flush();
            }

            jobCandidateMapping.getCandidate().getCandidateEducationDetails().forEach(candidateEducationFromRequest ->{
                if(null != candidateEducationFromRequest.getDegree()){
                    if (candidateEducationFromRequest.getDegree().length() > IConstant.MAX_FIELD_LENGTHS.DEGREE.getValue())
                        candidateEducationFromRequest.setDegree(Util.truncateField(finalJcmFromDb.getCandidate(), IConstant.MAX_FIELD_LENGTHS.DEGREE.name(), IConstant.MAX_FIELD_LENGTHS.DEGREE.getValue(), candidateEducationFromRequest.getDegree()));

                    if (null != candidateEducationFromRequest.getSpecialization() && candidateEducationFromRequest.getSpecialization().length() > IConstant.MAX_FIELD_LENGTHS.SPECIALIZATION.getValue())
                        candidateEducationFromRequest.setSpecialization(Util.truncateField(finalJcmFromDb.getCandidate(), IConstant.MAX_FIELD_LENGTHS.SPECIALIZATION.name(), IConstant.MAX_FIELD_LENGTHS.SPECIALIZATION.getValue(), candidateEducationFromRequest.getSpecialization()));

                    if (null != candidateEducationFromRequest.getInstituteName() && candidateEducationFromRequest.getInstituteName().length() > IConstant.MAX_FIELD_LENGTHS.INSTITUTE_NAME.getValue())
                        candidateEducationFromRequest.setInstituteName(Util.truncateField(finalJcmFromDb.getCandidate(), IConstant.MAX_FIELD_LENGTHS.INSTITUTE_NAME.name(), IConstant.MAX_FIELD_LENGTHS.INSTITUTE_NAME.getValue(), candidateEducationFromRequest.getInstituteName()));

                    CandidateEducationDetails candidateEducationDetails = new CandidateEducationDetails(finalJcmFromDb.getCandidate().getId(), candidateEducationFromRequest.getDegree(), Util.isNotNull(candidateEducationFromRequest.getYearOfPassing())?candidateEducationFromRequest.getYearOfPassing():String.valueOf(Calendar.getInstance().get(Calendar.YEAR)), candidateEducationFromRequest.getInstituteName(), candidateEducationFromRequest.getSpecialization());
                    candidateEducationDetailsRepository.save(candidateEducationDetails);
                }
            });

            //Update KeySkills
            List<String> candidateSkillsFromDb = new ArrayList<>();
            jcmFromDb.getCandidate().getCandidateSkillDetails().forEach(candidateSkill->{candidateSkillsFromDb.add(candidateSkill.getSkill().toLowerCase());});
            Long candidateId = jcmFromDb.getCandidate().getId();
            jobCandidateMapping.getCandidateKeySkills().forEach(Skill->{
                if(!candidateSkillsFromDb.contains(Skill.toLowerCase()))
                    candidateSkillDetailsRepository.save(new CandidateSkillDetails(candidateId, Skill));
            });


            //Update candidate company detail
            CandidateCompanyDetails companyDetails = null;
            CandidateCompanyDetails companyDetailsByRequest = jobCandidateMapping.getCandidate().getCandidateCompanyDetails().get(0);
            if (Util.isNotNull(companyDetailsByRequest.getCompanyName())) {
                AtomicBoolean isCompanyPresent = new AtomicBoolean(false);
                jcmFromDb.getCandidate().getCandidateCompanyDetails().stream().forEach(CompanyDetails -> {
                    if (CompanyDetails.getCompanyName().equalsIgnoreCase(companyDetailsByRequest.getCompanyName())) {
                        companyDetailsByRequest.setId(CompanyDetails.getId());
                        isCompanyPresent.set(true);
                    }
                });
                if (!isCompanyPresent.get() || null == jcmFromDb.getCandidate().getCandidateCompanyDetails()) {
                    Date endDate = null;
                    Date startDate = null;
                    try {
                        //in getCurrentOrBefore1YearDate method  pass boolean value
                        //get Before 1 year date then pass true if get current date then pass false value
                        endDate = Util.getCurrentOrBefore1YearDate(false);
                        startDate = Util.getCurrentOrBefore1YearDate(true);
                    } catch (ParseException e) {
                        log.error("Error while set start date and end date in candidate company detail : " + e.getMessage());
                    }
                    companyDetails = new CandidateCompanyDetails(jcmFromDb.getCandidate().getId(), companyDetailsByRequest.getCompanyName(), startDate, endDate);
                    companyDetails = addCompanyDetailsInfo(companyDetails, companyDetailsByRequest);
                    if (null != jcmFromDb.getCandidate().getCandidateCompanyDetails()) {
                        reStructureCompanyList(jcmFromDb, companyDetails);
                    }
                } else {
                    if (null != companyDetailsByRequest.getId()) {
                        companyDetails = candidateCompanyDetailsRepository.findById(companyDetailsByRequest.getId()).orElse(null);
                        companyDetails = addCompanyDetailsInfo(companyDetails, companyDetailsByRequest);
                        reStructureCompanyList(jcmFromDb, companyDetails);
                    }
                }
                log.info("Edit candidate info successfully");
            }
        }
    }

    private void reStructureCompanyList(JobCandidateMapping jcmFromDb, CandidateCompanyDetails companyDetails){
        List<CandidateCompanyDetails> oldCompanyList = jcmFromDb.getCandidate().getCandidateCompanyDetails();
        List<CandidateCompanyDetails> removeCompanyList = new ArrayList<>();
        CandidateCompanyDetails finalCompanyDetails = companyDetails;
        oldCompanyList.forEach(oldCompanyDetails->{
            if(oldCompanyDetails.getId().equals(finalCompanyDetails.getId())){
                removeCompanyList.add(oldCompanyDetails);
            }
        });
        oldCompanyList.removeAll(removeCompanyList);
        List<CandidateCompanyDetails> newCompanyList = new ArrayList<>(oldCompanyList.size() + 1);
        newCompanyList.add(companyDetails);
        newCompanyList.addAll(oldCompanyList);
        candidateCompanyDetailsRepository.deleteAll(oldCompanyList);
        candidateCompanyDetailsRepository.flush();
        candidateCompanyDetailsRepository.saveAll(newCompanyList);
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

    //Method for update alternate mobile and email in jcm
    private JobCandidateMapping updateOrCreateAlternateMobileEmail(JobCandidateMapping jcm, JobCandidateMapping jcmFromDb, User loggedInUser){
        log.info("inside updateOrCreateAlternateMobileEmail");
        if(Util.isNotNull(jcm.getAlternateMobile()))
            jcm.setAlternateMobile(validateMobile(jcm.getAlternateMobile(), jcmFromDb.getCountryCode()));

        if(Util.isNotNull(jcm.getAlternateEmail()))
            jcm.setAlternateEmail(validateEmail(jcm.getAlternateEmail()));

        CandidateEmailHistory candidateEmailHistory = null;
        CandidateMobileHistory candidateMobileHistory = null;
        if(Util.isNotNull(jcm.getAlternateEmail())) {
            jcmFromDb.setAlternateEmail(jcm.getAlternateEmail());
            candidateEmailHistory = getEmailHistory(jcm.getAlternateEmail());
        }

        if(Util.isNotNull(jcm.getAlternateMobile())) {
            jcmFromDb.setAlternateMobile(jcm.getAlternateMobile());
            candidateMobileHistory = getMobileHistory(jcm.getAlternateMobile(), jcmFromDb.getCountryCode());
        }

        if(null != candidateEmailHistory && null != candidateMobileHistory){
            if(!candidateEmailHistory.getCandidate().getId().equals(candidateMobileHistory.getCandidate().getId()))
                throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY + " - " + jcm.getAlternateMobile() + " "+jcm.getAlternateEmail()+", While add/update alternate email and mobile", HttpStatus.BAD_REQUEST);
        }else if(null == candidateEmailHistory && null == candidateMobileHistory){
            if(Util.isNotNull(jcm.getAlternateMobile()))
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(jcmFromDb.getCandidate(), jcm.getAlternateMobile(), jcmFromDb.getCountryCode(), new Date(), loggedInUser));
            if(Util.isNotNull(jcm.getAlternateEmail()))
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(jcmFromDb.getCandidate(), jcm.getAlternateEmail(), new Date(), loggedInUser));
        }else{
            if(null != candidateEmailHistory && null == candidateMobileHistory && Util.isNotNull(jcm.getAlternateMobile())){
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(jcmFromDb.getCandidate(), jcm.getAlternateMobile(), jcmFromDb.getCountryCode(), new Date(), loggedInUser));
            }else if(null != candidateMobileHistory && null == candidateEmailHistory && Util.isNotNull(jcm.getAlternateEmail())){
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(jcmFromDb.getCandidate(), jcm.getAlternateEmail(), new Date(), loggedInUser));
            }
        }
        return jcmFromDb;
    }


    private String validateMobile(String mobile, String countryCode){
        if(Util.isNotNull(mobile)) {
            mobile = Util.indianMobileConvertor(mobile, countryCode);
            if (!Util.validateMobile(mobile, countryCode) && !IConstant.CountryCode.INDIA_CODE.getValue().equals(countryCode)) {
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

    private boolean removeNotAvailableEmail(JobCandidateMapping jcm){
        boolean emailUpdated = false;
        List<CandidateEmailHistory>candidateEmailHistoryNotAvailableCheck = candidateEmailHistoryRepository.findByCandidateIdOrderByIdDesc(jcm.getCandidate().getId());
        if(candidateEmailHistoryNotAvailableCheck.size()>0){
            List<CandidateEmailHistory> notAvailableEmailHistory = candidateEmailHistoryNotAvailableCheck.stream().filter(
                    candidateEmailHistoryNotAvailable ->{
                        return candidateEmailHistoryNotAvailable.getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL);
                    })
                    .collect(Collectors.toList());
            notAvailableEmailHistory.get(0).setEmail(jcm.getEmail());
            CandidateEmailHistory candidateEmailHistoryUpdated = candidateEmailHistoryRepository.save(notAvailableEmailHistory.get(0));
            if(candidateEmailHistoryUpdated.getEmail().equals(jcm.getEmail()))
                emailUpdated = true;
            notAvailableEmailHistory.remove(0);
            if(notAvailableEmailHistory.size()>0){
                candidateEmailHistoryRepository.deleteAll(notAvailableEmailHistory);
            }
        }
        return emailUpdated;
    }

    /**
     * function to update candidate data nad jcm record with new email or mobile requested in candidate edit function.
     * @param jobCandidateMapping - updated jcm record.
     * @param jcmFromDb - jcm record from db with candidate id
     * @param loggedInUser - user updating the jcm record.
     * @return boolean whether jcmFromDbDeleted, in case id candidate with new email or mobile already existing and jcm from db has null mobile and email has @notavailable.
     * Flowchart for this method - https://github.com/hexagonsearch/litmusblox-backend/issues/253
     */
    private boolean updateOrCreateEmailMobile(JobCandidateMapping jobCandidateMapping, JobCandidateMapping jcmFromDb, User loggedInUser){

        boolean jcmFromDbDeleted = false;
        //check if new email contains @notavailable.io
        if(jobCandidateMapping.getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL)){
            // call getCandidateIdFromMobileHistory to fetch candidate id for new mobile from mobile history from db if exists.
            Long candidateIdFromMobileHistory = getCandidateIdFromMobileHistory(jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode());

            if (candidateIdFromMobileHistory != null && !jobCandidateMapping.getMobile().equals(jcmFromDb.getMobile())) {
                //fetch candidate mobile history for new mobile as it is already existing if control reaches here.
                CandidateMobileHistory candidateMobileHistory = getMobileHistory(jobCandidateMapping.getMobile(), jcmFromDb.getCountryCode());

                //extracting existing candidate from db for new email.
                Candidate existingCandidate = candidateMobileHistory.getCandidate();

                //check if existingCandidate belongs to same job
                JobCandidateMapping jcmForExistingCandidate = jobCandidateMappingRepository.findByJobAndCandidate(jcmFromDb.getJob(), existingCandidate);

                if(jcmForExistingCandidate!=null) {
                    //call function to delete requested jcm record and change updated by to current user for exiting jcm
                    deleteAndUpdateJcmRecord(jcmFromDb, jcmForExistingCandidate, loggedInUser);
                    jcmFromDbDeleted = true;
                }
                else{
                    //update jcmFromDb with existing candidate's email.
                    List<CandidateEmailHistory> existingCandidateEmailList = candidateEmailHistoryRepository.findByCandidateIdOrderByIdDesc(existingCandidate.getId());
                    if(existingCandidateEmailList.size()>0){
                        jcmFromDb.setEmail(existingCandidateEmailList.get(0).getEmail());
                    }
                    jcmFromDb.setMobile(jobCandidateMapping.getMobile());
                    //update jcm with existing candidate and delete candidate with email "@notavailable"
                    deleteAndUpdateCandidate(existingCandidate, jcmFromDb);
                }
            }
            else {
                createUpdateEmailMobileNew(jobCandidateMapping, jcmFromDb, loggedInUser);
            }
        }
        else {
            // call getCandidateIdFromMobileHistory to fetch candidate id for new mobile from mobile history from db if exists.
            Long candidateIdFromMobileHistory = getCandidateIdFromMobileHistory(jobCandidateMapping.getMobile(), jcmFromDb.getCountryCode());

            //call getCandidateIdFromEmailHistory to fetch candidate id for new email from email history from db if exists.
            Long candidateIdFromEmailHistory = getCandidateIdFromEmailHistory(jobCandidateMapping.getEmail());

            //check candidateIdFromEmailHistory and candidateIdFromMobileHistory is not null
            if (candidateIdFromEmailHistory != null && candidateIdFromMobileHistory != null) {
                //check if both id's belong to same candidate or not, if not throw web exception
                if (candidateIdFromEmailHistory.equals(candidateIdFromMobileHistory)) {
                    //check if email has "@notavailable.io"
                    if (jcmFromDb.getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL)) {
                        //fetch candidate email history for new email as it is already existing if control reaches here.
                        CandidateEmailHistory candidateEmailHistory = getEmailHistory(jobCandidateMapping.getEmail());

                        //extracting existing candidate from db for new email.
                        Candidate existingCandidate = candidateEmailHistory.getCandidate();

                        //check if existingCandidate belongs to same job
                        JobCandidateMapping jcmForExistingCandidate = jobCandidateMappingRepository.findByJobAndCandidate(jcmFromDb.getJob(), existingCandidate);

                        if(jcmForExistingCandidate!=null) {
                            //call function to delete requested jcm record and change updated by to current user for exiting jcm
                            deleteAndUpdateJcmRecord(jcmFromDb, jcmForExistingCandidate, loggedInUser);
                            jcmFromDbDeleted = true;
                        }
                        else{
                            List<CandidateMobileHistory> existingCandidateMobileList = candidateMobileHistoryRepository.findByCandidateIdOrderByIdDesc(existingCandidate.getId());
                            if(existingCandidateMobileList.size()>0){
                                jcmFromDb.setMobile(existingCandidateMobileList.get(0).getMobile());
                            }
                            jcmFromDb.setEmail(jobCandidateMapping.getEmail());
                            //update jcm with existing candidate and delete candidate with email "@notavailable"
                            deleteAndUpdateCandidate(existingCandidate, jcmFromDb);
                        }
                    } else {
                        createUpdateEmailMobileNew(jobCandidateMapping, jcmFromDb, loggedInUser);
                    }
                }
                else {
                    throw new WebException("Email and mobile belongs to different candidate", HttpStatus.BAD_REQUEST);
                }
            } else if (candidateIdFromEmailHistory != null) {
                if (jcmFromDb.getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL)) {

                    //fetch candidate email history for new email as it is already existing if control reaches here.
                    CandidateEmailHistory candidateEmailHistory = getEmailHistory(jobCandidateMapping.getEmail());

                    //extracting existing candidate from db for new email.
                    Candidate existingCandidate = candidateEmailHistory.getCandidate();

                    //check if existingCandidate belongs to same job
                    JobCandidateMapping jcmForExistingCandidate = jobCandidateMappingRepository.findByJobAndCandidate(jcmFromDb.getJob(), existingCandidate);

                    if(jcmForExistingCandidate!=null) {
                        //call function to delete requested jcm record and change updated by to current user for exiting jcm
                        deleteAndUpdateJcmRecord(jcmFromDb, jcmForExistingCandidate, loggedInUser);
                        jcmFromDbDeleted = true;
                    }
                    else{
                        List<CandidateMobileHistory> existingCandidateMobileList = candidateMobileHistoryRepository.findByCandidateIdOrderByIdDesc(existingCandidate.getId());
                        if(existingCandidateMobileList.size()>0){
                            jcmFromDb.setMobile(existingCandidateMobileList.get(0).getMobile());
                        }
                        jcmFromDb.setEmail(jobCandidateMapping.getEmail());
                        //update jcm with existing candidate and delete candidate with email "@notavailable"
                        deleteAndUpdateCandidate(existingCandidate, jcmFromDb);
                    }
                }
                else{
                    createUpdateEmailMobileNew(jobCandidateMapping, jcmFromDb, loggedInUser);
                }
            }
            else if(candidateIdFromMobileHistory!=null){
                if(jobCandidateMapping.getMobile().equals(jcmFromDb.getMobile())) {
                    createUpdateEmail(jobCandidateMapping, jcmFromDb, loggedInUser);
                }
                else {
                    //fetch candidate mobile history for new mobile as it is already existing if control reaches here.
                    CandidateMobileHistory candidateMobileHistory = getMobileHistory(jobCandidateMapping.getMobile(), jcmFromDb.getCountryCode());

                    //extracting existing candidate from db for new mobile.
                    Candidate existingCandidate = candidateMobileHistory.getCandidate();

                    //check if existingCandidate belongs to same job
                    JobCandidateMapping jcmForExistingCandidate = jobCandidateMappingRepository.findByJobAndCandidate(jcmFromDb.getJob(), existingCandidate);

                    if (jcmForExistingCandidate != null) {
                        //call function to delete requested jcm record and change updated by to current user for exiting jcm
                        deleteAndUpdateJcmRecord(jcmFromDb, jcmForExistingCandidate, loggedInUser);
                        jcmFromDbDeleted = true;
                    } else {
                        //update jcmFromDb with existing candidate's email.
                        List<CandidateEmailHistory> existingCandidateEmailList = candidateEmailHistoryRepository.findByCandidateIdOrderByIdDesc(existingCandidate.getId());
                        if (existingCandidateEmailList.size() > 0) {
                            jcmFromDb.setEmail(existingCandidateEmailList.get(0).getEmail());
                        }
                        jcmFromDb.setMobile(jobCandidateMapping.getMobile());
                        //update jcm with existing candidate and delete candidate with email "@notavailable"
                        deleteAndUpdateCandidate(existingCandidate, jcmFromDb);
                    }
                }
            }
            else {
               createUpdateEmailMobileNew(jobCandidateMapping, jcmFromDb, loggedInUser);
            }
        }

        return jcmFromDbDeleted;
    }

    /**
     * function to remove all occurrences of records for a candidate in different tables.
     * @param candidate for which all records will be removed
     */
    private void deleteCandidate(Candidate candidate){
        candidateCompanyDetailsRepository.deleteByCandidateId(candidate.getId());
        candidateDetailsRepository.deleteByCandidateId(candidate);
        candidateEducationDetailsRepository.deleteByCandidateId(candidate.getId());
        candidateOnlineProfilesRepository.deleteByCandidateId(candidate.getId());
        candidateSkillDetailsRepository.deleteByCandidateId(candidate.getId());
        candidateProjectDetailsRepository.deleteByCandidateId(candidate.getId());
        candidateLanguageProficiencyRepository.deleteByCandidateId(candidate.getId());
        candidateWorkAuthorizationRepository.deleteByCandidateId(candidate.getId());
        candidateEmailHistoryRepository.deleteByCandidateId(candidate);
        candidateMobileHistoryRepository.deleteByCandidateId(candidate);
        candidateRepository.delete(candidate);
    }

    private Long getCandidateIdFromEmailHistory(String email){
        //Fetch candidateId From Email History
        Long candidateIdFromEmailHistory = candidateEmailHistoryRepository.findCandidateIdByEmail(email);
        return candidateIdFromEmailHistory;
    }

    private Long getCandidateIdFromMobileHistory(String mobile, String countryCode){
        //Fetch candidateId From Mobile History
        Long candidateIdFromMobileHistory = candidateMobileHistoryRepository.findCandidateIdByMobileAndCountryCode(mobile, countryCode);
        return candidateIdFromMobileHistory;
    }

    private CandidateEmailHistory getEmailHistory(String email){
        return candidateEmailHistoryRepository.findByEmail(email);
    }

    private CandidateMobileHistory getMobileHistory(String mobile, String countryCode){
        return candidateMobileHistoryRepository.findByMobileAndCountryCode(mobile, countryCode);
    }

    private void deleteAndUpdateJcmRecord(JobCandidateMapping jcmFromDb, JobCandidateMapping jcmForExistingCandidate, User loggedInUser){
        jcmCommunicationDetailsRepository.deleteByJcmId(jcmFromDb.getId());
        cvParsingDetailsRepository.deleteByJobCandidateMappingId(jcmFromDb);
        cvRatingRepository.deleteByJobCandidateMappingId(jcmFromDb.getId());
        candidateScreeningQuestionResponseRepository.deleteByJobCandidateMappingId(jcmFromDb.getId());
        jcmHistoryRepository.deleteByJcmId(jcmFromDb);
        jcmProfileSharingDetailsRepository.deleteByJobCandidateMappingId(jcmFromDb.getId());
        candidateTechResponseDataRepository.deleteByJobCandidateMappingId(jcmFromDb);
        jobCandidateMappingRepository.delete(jcmFromDb);
        jcmForExistingCandidate.setUpdatedBy(loggedInUser);
        jobCandidateMappingRepository.save(jcmForExistingCandidate);
    }

    private void deleteAndUpdateCandidate(Candidate existingCandidate, JobCandidateMapping jcmFromDb){
        //extracting candidate with email "@notavailable"
        Candidate oldCandidate = jcmFromDb.getCandidate();
        jcmFromDb.setCandidate(existingCandidate);
        jobCandidateMappingRepository.save(jcmFromDb);
        jobCandidateMappingRepository.flush();

        //delete all related entries from different tables for candidate id with email "@notavailable.io.
        deleteCandidate(oldCandidate);
    }

    private void createUpdateEmailMobileNew(JobCandidateMapping jobCandidateMapping, JobCandidateMapping jcmFromDb, User loggedInUser){
        //Update or create email id
        if (null != jobCandidateMapping.getEmail() && !jobCandidateMapping.getEmail().isEmpty()) {
            createUpdateEmail(jobCandidateMapping, jcmFromDb, loggedInUser);
        }

        //call function to Update or create mobile no
        if (null != jobCandidateMapping.getMobile() && !jobCandidateMapping.getMobile().isEmpty()) {
            createUpdateMobile(jobCandidateMapping, jcmFromDb, loggedInUser);
        }
    }



    private void createUpdateMobile(JobCandidateMapping jobCandidateMapping, JobCandidateMapping jcmFromDb, User loggedInUser){
        jobCandidateMapping.setMobile(validateMobile(jobCandidateMapping.getMobile(), jobCandidateMapping.getCandidate().getCountryCode()));
        if(!Util.isNull(jobCandidateMapping.getMobile())) {
            CandidateMobileHistory candidateMobileHistory = candidateMobileHistoryRepository.findByMobileAndCountryCode(jobCandidateMapping.getMobile(), jobCandidateMapping.getCandidate().getCountryCode());
            if (null == candidateMobileHistory) {
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(jcmFromDb.getCandidate(), jobCandidateMapping.getMobile(), jobCandidateMapping.getCandidate().getCountryCode(), new Date(), loggedInUser));
                jcmFromDb.setMobile(jobCandidateMapping.getMobile());
            } else {
                if (!jcmFromDb.getCandidate().getId().equals(candidateMobileHistory.getCandidate().getId()))
                    throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY_FOR_MOBILE + jobCandidateMapping.getMobile() + " " + jobCandidateMapping.getEmail(), HttpStatus.BAD_REQUEST);
                else
                    jcmFromDb.setMobile(candidateMobileHistory.getMobile());
            }
            jobCandidateMappingRepository.save(jcmFromDb);
        }
    }

    private void createUpdateEmail(JobCandidateMapping jobCandidateMapping, JobCandidateMapping jcmFromDb, User loggedInUser){
        jobCandidateMapping.setEmail(validateEmail(jobCandidateMapping.getEmail()));
        CandidateEmailHistory candidateEmailHistory = candidateEmailHistoryRepository.findByEmail(jobCandidateMapping.getEmail());
        if (null == candidateEmailHistory) {
            if (!removeNotAvailableEmail(jobCandidateMapping))
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(jcmFromDb.getCandidate(), jobCandidateMapping.getEmail(), new Date(), loggedInUser));
            jcmFromDb.setEmail(jobCandidateMapping.getEmail());
        } else {
            if (!jcmFromDb.getCandidate().getId().equals(candidateEmailHistory.getCandidate().getId()))
                throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY_FOR_EMAIL + jobCandidateMapping.getMobile() + " " + jobCandidateMapping.getEmail(), HttpStatus.BAD_REQUEST);
            else
                jcmFromDb.setEmail(candidateEmailHistory.getEmail());
        }

        jobCandidateMappingRepository.save(jcmFromDb);
    }

    /**
     * Service to set a specific stage like Interview, Offer etc
     *
     * @param jcmList The list of candidates for the job that need to be moved to the specified stage
     * @param stage   the new stage
     * @throws Exception
     */
    @Transactional
    public void setStageForCandidates(List<Long> jcmList, String stage) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("Setting {} jcms to {} stage", jcmList, stage);
        //check that all the jcm are currently in the same stage
        if(!areCandidatesInSameStage(jcmList))
            throw new WebException("Select candidates that are all in Source stage", HttpStatus.UNPROCESSABLE_ENTITY);

        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<JcmHistory> jcmHistoryList = new ArrayList<>(jcmList.size());

        //check if new stage is rejected stage
        if (IConstant.Stage.Reject.getValue().equals(stage)) {
            jobCandidateMappingRepository.updateForRejectStage(jcmList, loggedInUser.getId(), new Date());
        }
        else {

            JobCandidateMapping jobCandidateMappingObj = jobCandidateMappingRepository.getOne(jcmList.get(0));
            Map<String, Long> jobStageIds = MasterDataBean.getInstance().getStageStepMasterMap();
            jobCandidateMappingRepository.updateStageStepId(jcmList, jobCandidateMappingObj.getStage().getId(), jobStageIds.get(stage), loggedInUser.getId(), new Date());
        }
        jcmList.stream().forEach(jcm -> {
            JobCandidateMapping mappingObj = jobCandidateMappingRepository.getOne(jcm);
            jcmHistoryList.add(new JcmHistory(mappingObj, IConstant.Stage.Reject.getValue().equals(stage)?"Candidate Rejected from " + mappingObj.getStage().getStage() + " stage":"Candidate moved to " + stage, new Date(), loggedInUser, mappingObj.getStage()));

        });
        jcmHistoryRepository.saveAll(jcmHistoryList);

        log.info("Completed moving candidates to {} stage in {} ms", stage, (System.currentTimeMillis() - startTime));
    }

    /**
     * Service to return error list for drag and drop CV's for a job
     *
     * @param jobId job id for which files with error wil be returned
     * @return List of RChilliErrorResponseBean which have file name, processed date, status, jcmId, candidate name if available
     * @throws Exception
     */
    public List<ResponseBean> getRchilliError(Long jobId) throws Exception{
        List<ResponseBean> rChilliErrorResponseBeanList = new ArrayList<>();

        //fetch records with error from cv parsing details table using jobId
        List<CvParsingDetails> cvParsingDetailsList = cvParsingDetailsRepository.getRchilliErrorResponseBeanList(jobId);

        //for each cv parsing record create rchilliResponseBean and push to rChilliResponseBeanList
        cvParsingDetailsList.forEach(cvParsingDetails -> {
            ResponseBean rChilliErrorResponseBean = new ResponseBean();
            //Replace job id and user id from file name to return clean file name as uploaded by user.
            rChilliErrorResponseBean.setFileName(cvParsingDetails.getCvFileName().replaceAll("\\d+_\\d+_",""));
            rChilliErrorResponseBean.setProcessedOn(cvParsingDetails.getProcessedOn());
            rChilliErrorResponseBean.setStatus(cvParsingDetails.getProcessingStatus());
            rChilliErrorResponseBeanList.add(rChilliErrorResponseBean);
        });
        return rChilliErrorResponseBeanList;
    }

    /**
     * Service method to get candidate history related to jcm
     *
     * @param jcmId
     * @return JcmHistory list
     */
    @Transactional
    public List<JcmHistory> retrieveCandidateHistory(Long jcmId) {
        JobCandidateMapping jobCandidateMapping = jobCandidateMappingRepository.findById(jcmId).orElse(null);
        return jcmHistoryRepository.getJcmHistoryList(jobCandidateMapping.getJob().getCompanyId().getId(), jobCandidateMapping.getCandidate().getId());
    }

    /**
     *
     * @param comment comment add by  recruiter
     * @param jcmId for which jcm we need to create jcm history
     * @param callOutCome callOutCome if callOutCome is present then set in jcm history
     */
    @Transactional
    public void addComment(String comment, Long jcmId, String callOutCome) {
        log.info("inside addComment");
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobCandidateMapping jobCandidateMapping = jobCandidateMappingRepository.findById(jcmId).orElse(null);
        if(null == jobCandidateMapping)
            throw new ValidationException("Job candidate mapping not found for jcmId : "+jcmId, HttpStatus.BAD_REQUEST);

        jcmHistoryRepository.save(new JcmHistory(jobCandidateMapping, comment, callOutCome, false, new Date(), jobCandidateMapping.getStage(), loggedInUser));
    }

    /**
     * Service to upload resume against jcm
     *
     * @param jcmId
     * @param candidateCv
     */
    @Transactional
    public void uploadResume(MultipartFile candidateCv, Long jcmId) throws Exception {
        log.info("inside uploadResume");
        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findById(jcmId).orElse(null);
        if(null == jcmFromDb)
            throw new ValidationException("Job candidate mapping not found for jcmId : "+jcmId, HttpStatus.BAD_REQUEST);

        String extension = Util.getFileExtension(candidateCv.getOriginalFilename()).toLowerCase();
        if(extension.equals(IConstant.FILE_TYPE.rar) || extension.equals(IConstant.FILE_TYPE.zip) || !Arrays.asList(IConstant.cvUploadSupportedExtensions).contains(extension))
            throw new ValidationException(IErrorMessages.UNSUPPORTED_FILE_TYPE+" "+extension+", For JcmId : "+jcmId, HttpStatus.BAD_REQUEST);

        try {
            StoreFileUtil.storeFile(candidateCv, jcmFromDb.getJob().getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(),jcmFromDb.getCandidate(),null);
            jcmFromDb.setCvFileType("."+extension);
            jobCandidateMappingRepository.save(jcmFromDb);
        }catch (Exception ex){
           log.error("{}, File name : {}, For jcmId : ", IErrorMessages.FAILED_TO_SAVE_FILE, candidateCv.getOriginalFilename(), jcmId, ex.getMessage());
            throw new ValidationException(IErrorMessages.FAILED_TO_SAVE_FILE+" "+candidateCv.getOriginalFilename()+ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     *Service to add candidate via career page, job portal, employee referral
     *
     * @param candidateSource from where we source the candidate
     * @param candidate candidate all info
     * @param jobReferenceId In which job upload candidate
     * @param candidateCv candidate cv
     * @param employeeReferrer if candidate upload by employee referral then this model come
     * @return UploadResponseBean
     * @throws Exception
     */
    @Transactional
    public UploadResponseBean uploadCandidateByNoAuthCall(String candidateSource, Candidate candidate, UUID jobReferenceId, MultipartFile candidateCv, EmployeeReferrer employeeReferrer, String otp) throws Exception {
        log.info("Inside uploadCandidateByNoAuthCall");
        UploadResponseBean responseBean = null;
        CandidateDetails candidateDetails = null;
        Boolean isOtpVerify;
        EmployeeReferrer referrerFromDb;

        if(null != employeeReferrer)
            isOtpVerify = otpService.verifyOtp(employeeReferrer.getMobile(), otp);
        else
            isOtpVerify = otpService.verifyOtp(candidate.getMobile(), otp);

        if(isOtpVerify){
            if(null == candidate.getCandidateName() || candidate.getCandidateName().isEmpty()){
                candidate.setCandidateName(IConstant.NOT_AVAILABLE);
                candidate.setFirstName(IConstant.NOT_AVAILABLE);
            }else{
                //populate the first name and last name of the candidate
                Util.handleCandidateName(candidate, candidate.getCandidateName());
            }

            if(!Arrays.asList(IConstant.CandidateSource.values()).contains(IConstant.CandidateSource.valueOf(candidateSource)))
                throw new ValidationException("Not a valid candidate source : "+candidateSource, HttpStatus.BAD_REQUEST);

            if(null != candidateCv){
                String cvFileType = Util.getFileExtension(candidateCv.getOriginalFilename());
                if(null == candidate.getCandidateDetails()){
                    candidateDetails = new CandidateDetails();
                    candidate.setCandidateDetails(candidateDetails);
                }
                candidate.getCandidateDetails().setCvFileType("."+cvFileType);
            }
            Job job = jobRepository.findByJobReferenceId(jobReferenceId);
            candidate.setCandidateSource(candidateSource);

            if(null != employeeReferrer){
                referrerFromDb = employeeReferrerRepository.findByEmail(employeeReferrer.getEmail());
                if(null == referrerFromDb){
                    referrerFromDb = employeeReferrerRepository.save(new EmployeeReferrer(employeeReferrer.getFirstName(), employeeReferrer.getLastName(), employeeReferrer.getEmail(), employeeReferrer.getEmployeeId(), employeeReferrer.getMobile(), employeeReferrer.getLocation(), employeeReferrer.getCreatedOn()));
                }
                referrerFromDb.setReferrerRelation(employeeReferrer.getReferrerRelation());
                referrerFromDb.setReferrerContactDuration(employeeReferrer.getReferrerContactDuration());
                candidate.setEmployeeReferrer(referrerFromDb);
            }
            //Upload candidate
            responseBean = uploadIndividualCandidate(Arrays.asList(candidate), job.getId(), false, Optional.ofNullable(userRepository.findByEmail(IConstant.SYSTEM_USER_EMAIL)));

            //Store candidate cv to repository location
            try{
                if(null!=candidateCv) {
                    if (responseBean.getSuccessfulCandidates().size()>0)
                        StoreFileUtil.storeFile(candidateCv, job.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(),responseBean.getSuccessfulCandidates().get(0),null);
                    else
                        StoreFileUtil.storeFile(candidateCv, job.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(),responseBean.getFailedCandidates().get(0), null);

                    responseBean.setCvStatus(true);
                }
            }catch(Exception e){
                log.error("Resume upload failed :"+e.getMessage());
                responseBean.setCvErrorMsg(e.getMessage());
            }
        }else{
            responseBean = new UploadResponseBean();
            responseBean.setFailureCount(1);
            responseBean.setSuccessCount(0);
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
            responseBean.setErrorMessage(IErrorMessages.OTP_VERIFICATION_FAILED);
        }
        return responseBean;
    }

    /**
     * Service method to fetch a list of count of candidate per chatbot status per job
     *
     * @param jobId the job id for which data has to be fetched
     * @param stage the stage, defaulted out to Screening
     * @return the count of candidate per chatbot status
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getCandidateCountPerStatus(Long jobId, String stage) throws Exception {

        Map<String, Integer> countMap = new HashMap<>();
        List<Object[]> candidateCountList = jobCandidateMappingRepository.getCandidateCountPerStage(jobId, stage);

        if(null != candidateCountList.get(0)[0]) {
            countMap.put(IConstant.ChatbotStatus.INVITED.getValue(), ((BigInteger)candidateCountList.get(0)[0]).intValue());
            countMap.put(IConstant.ChatbotStatus.NOT_INSTERESTED.getValue(), ((BigInteger)candidateCountList.get(0)[1]).intValue());
            countMap.put(IConstant.ChatbotStatus.COMPLETE.getValue(), ((BigInteger)candidateCountList.get(0)[2]).intValue());
            countMap.put(IConstant.ChatbotStatus.INCOMPLETE.getValue(), ((BigInteger)candidateCountList.get(0)[3]).intValue());
        }

        return countMap;
    }

    /**
     *Service method to fetch data related to job like job detail, screening questions and corresponding candidate
     *Merge two api getScreeningQuestions and getCandidateAndJobDetails in current api
     *
     * @param uuid the uuid corresponding to a unique jcm record
     * @throws Exception
     * return ChatbotResponseBean String
     */
    @Transactional
    public ChatbotResponseBean getChatbotDetailsByUuid(UUID uuid) throws Exception {
        log.info("Inside getChatbotDetailsByUuid");
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findByChatbotUuid(uuid);
        if (null == objFromDb)
            throw new WebException(IErrorMessages.UUID_NOT_FOUND + uuid, HttpStatus.UNPROCESSABLE_ENTITY);

        ChatbotResponseBean chatbotResponseBean = new ChatbotResponseBean();
        chatbotResponseBean.setJobCandidateMapping(objFromDb);

        objFromDb.setJcmCommunicationDetails(jcmCommunicationDetailsRepository.findByJcmId(objFromDb.getId()));

        if(objFromDb.getJob().isCustomizedChatbot()){
            CustomizedChatbotPageContent customizedChatbotPageContent = customizedChatbotPageContentRepository.findByCompanyId(objFromDb.getJob().getCompanyId());
            //check customize chatbot flag true then send customized page data
            if(null != customizedChatbotPageContent && !customizedChatbotPageContent.getPageInfo().isEmpty())
                chatbotResponseBean.getChatbotContent().putAll(customizedChatbotPageContent.getPageInfo());

        }
        return chatbotResponseBean;
    }
}