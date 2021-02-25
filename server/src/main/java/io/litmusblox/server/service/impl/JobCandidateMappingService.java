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
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.*;
import io.litmusblox.server.uploadProcessor.CsvFileProcessorService;
import io.litmusblox.server.uploadProcessor.ExcelFileProcessorService;
import io.litmusblox.server.uploadProcessor.IUploadDataProcessService;
import io.litmusblox.server.uploadProcessor.NaukriExcelFileProcessorService;
import io.litmusblox.server.utils.SentryUtil;
import io.litmusblox.server.utils.StoreFileUtil;
import io.litmusblox.server.utils.Util;
import io.litmusblox.server.utils.ZipFileProcessUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static java.util.stream.Collectors.groupingBy;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

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
public class JobCandidateMappingService extends AbstractAccessControl implements IJobCandidateMappingService {

    @Resource
    JobRepository jobRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    JcmCommunicationDetailsRepository jcmCommunicationDetailsRepository;

    @Resource
    MasterDataRepository masterDataRepository;

    @Resource
    ScreeningQuestionsRepository screeningQuestionsRepository;

    @Resource
    HiringManagerWorkspaceRepository hiringManagerWorkspaceRepository;

    @Autowired
    IUploadDataProcessService iUploadDataProcessService;

    @Autowired
    Environment environment;

    @Autowired
    IJobService jobService;

    @Autowired
    CustomQueryExecutor customQueryExecutor;

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
    JcmHistoryRepository jcmHistoryRepository;

    @Resource
    CvParsingDetailsRepository cvParsingDetailsRepository;

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

    @Resource
    InterviewDetailsRepository interviewDetailsRepository;

    @Resource
    InterviewerDetailsRepository interviewerDetailsRepository;

    @Resource
    CompanyRepository companyRepository;

    @Resource
    AsyncOperationsErrorRecordsRepository asyncOperationsErrorRecordsRepository;

    @Resource
    JcmCandidateSourceHistoryRepository jcmCandidateSourceHistoryRepository;

    @Resource
    RejectionReasonMasterDataRepository rejectionReasonMasterDataRepository;

    @Resource
    TechScreeningQuestionRepository techScreeningQuestionRepository;

    @Resource
    JcmOfferDetailsRepository jcmOfferDetailsRepository;

    @Transactional(readOnly = true)
    Job getJob(long jobId) {
        return jobRepository.findById(jobId).isPresent()?jobRepository.findById(jobId).get():null;
    }

    /**
     * Service method to add a individually added candidates to a job
     *
     * @param candidates the list of candidates to be added
     * @param jobId      the job for which the candidate is to be added
     * @param createdBy optional paramter, createdBy, will be supplied only when calling processing candidates from mail
     * @return the status of upload operation
     * @throws Exception
     */
    //In this method we don't have any db related operation so remove @Transactional annotation
    public UploadResponseBean uploadIndividualCandidate(List<Candidate> candidates, Long jobId, boolean ignoreMobile, Optional<User> createdBy, boolean isCallFromNoAuth) throws Exception {

        //verify that the job is live before processing candidates
        Job job = jobRepository.getOne(jobId);
        if(null!=createdBy && !isCallFromNoAuth && !createdBy.isPresent()) {
            User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            validateLoggedInUser(loggedInUser, job);
        }
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
                processCandidateData(candidates, uploadResponseBean, loggedInUser, jobId, candidateProcessed, ignoreMobile, job);
            else
                processCandidateData(candidates, uploadResponseBean, loggedInUser, jobId, candidateProcessed, !IConstant.STR_INDIA.equalsIgnoreCase(loggedInUser.getCountryId().getCountryName()), job);
        } catch (Exception ex) {
            log.error("Error while processing candidates uploaded :: " + ex.getMessage());
            uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        return uploadResponseBean;
    }

    /*@Caching(evict = {@CacheEvict(cacheNames = "TechRoleCompetency", key = "#jobId"), @CacheEvict(cacheNames = "AsyncOperationsErrorRecords", key = "#jobId"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"),
    @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "TechRoleCompetency", key = "#jobId"), @CacheEvict(cacheNames = "candidateCountPerStage"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})*/
    private void processCandidateData(List<Candidate> candidateList, UploadResponseBean uploadResponseBean, User loggedInUser, Long jobId, int candidateProcessed, boolean ignoreMobile, Job job) throws Exception{

        if (null != candidateList && candidateList.size() > 0) {
            iUploadDataProcessService.processData(candidateList, uploadResponseBean, candidateProcessed,jobId, ignoreMobile, Optional.of(loggedInUser));
        }

        for (Candidate candidate:candidateList) {
            try {
                if(null!=candidate.getId()){
                    saveCandidateSupportiveInfo(candidate, loggedInUser);
                    try {
                        //For now we comment create candidate on search engine call because it is not working
                        /*if(candidateService.createCandidateOnSearchEngine(candidate, job, JwtTokenUtil.getAuthToken())==200){
                            JobCandidateMapping jobCandidateMapping = jobCandidateMappingRepository.findByJobAndCandidate(job, candidate);
                            jobCandidateMapping.setCreatedOnSearchEngine(true);
                            jobCandidateMappingRepository.save(jobCandidateMapping);
                        }*/
                    }catch (Exception e){
                        log.error("Error while adding candidate : {} in searchEngine for job : {}:: {}", candidate.getId(), job.getId(), e.getMessage());
                    }
                }
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
        //find candidateId
        Candidate candidateFromDb=candidateService.findByMobileOrEmail(new HashSet<>(Arrays.asList(candidate.getEmail().split(","))), null == candidate.getMobile()?new HashSet<>():new HashSet<>(Arrays.asList(candidate.getMobile().split(","))), (null==candidate.getCountryCode())?loggedInUser.getCountryId().getCountryCode():candidate.getCountryCode(), loggedInUser, Optional.ofNullable(candidate.getAlternateMobile()));

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
            //candidate details
            CandidateDetails candidateDetailsFormDb = null;
            if(null != candidate.getCandidateDetails()) {
                if(null != candidate.getId()) {
                    candidateDetailsFormDb = candidateDetailsRepository.findByCandidateId(candidate);
                    if(null == candidateDetailsFormDb) {
                        //if marital status is more than 10 characters, trim to 10. e.g. got a status as single/unmarried for one of the candidates!
                        if (!Util.isNull(candidate.getCandidateDetails().getMaritalStatus()) && candidate.getCandidateDetails().getMaritalStatus().length() > 10)
                            candidate.getCandidateDetails().setMaritalStatus(candidate.getCandidateDetails().getMaritalStatus().substring(0, 10));
                        candidateService.saveUpdateCandidateDetails(candidate.getCandidateDetails(), candidateFromDb);
                    }
                }
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
     * @param fileName the file with candidate information
     * @param jobId         the job for which the candidates have to be added
     * @param fileFormat    the format of file, for e.g. Naukri, LB format
     * @return the status of upload operation
     * @throws Exception
     */
    // @Transactional(propagation = Propagation.REQUIRED)
    @Async("asyncTaskExecutor")
    public void uploadCandidatesFromFile(String fileName, Long jobId, String fileFormat, User loggedInUser, int candidatesProcessed, String originalFileName) throws Exception {
        log.info("Thread - {} : Started processing uploadCandidatesFromFile in JobCandidateMappingService", Thread.currentThread().getName());
        long startTime = System.currentTimeMillis();
        UploadResponseBean uploadResponseBean = new UploadResponseBean();
        List<Candidate> candidateList = null;
        Job job = jobRepository.getOne(jobId);
        try {
            validateLoggedInUser(loggedInUser, job);
            candidateList = processUploadedFile(fileName, uploadResponseBean, loggedInUser, fileFormat, environment.getProperty(IConstant.REPO_LOCATION), loggedInUser.getCountryCode());

            try {
                processCandidateData(candidateList, uploadResponseBean, loggedInUser, jobId, candidatesProcessed, true, job);
            } catch (Exception ex) {
                log.error("Error while processing file " + fileName + " :: " + ex.getMessage());
                uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
            }
            if (null != uploadResponseBean.getFailedCandidates() && uploadResponseBean.getFailedCandidates().size() > 0) {
                handleErrorRecords(uploadResponseBean.getFailedCandidates(), null, IConstant.ASYNC_OPERATIONS.FileUpload.name(), loggedInUser, jobId, originalFileName);
            }
        } catch (WebException webException) {
            log.error("Error while upload candidate for fileName : {}, jobId : {}, loggedInUser : {}, errorMessage : {}", originalFileName, jobId, loggedInUser.getId(), webException.getErrorMessage());
            asyncOperationsErrorRecordsRepository.save(new AsyncOperationsErrorRecords(jobId, null, null, null, null, webException.getErrorMessage(), IConstant.ASYNC_OPERATIONS.FileUpload.name(), loggedInUser, new Date(), originalFileName));
        } catch (ValidationException validationException) {
            log.error("Error while upload candidate for fileName : {}, jobId : {}, loggedInUser : {}, errorMessage : {}", originalFileName, jobId, loggedInUser.getId(), validationException.getErrorMessage());
            asyncOperationsErrorRecordsRepository.save(new AsyncOperationsErrorRecords(jobId, null, null, null, null, validationException.getErrorMessage(), IConstant.ASYNC_OPERATIONS.FileUpload.name(), loggedInUser, new Date(), originalFileName));
        }
        log.info("Thread - {} : Completed processing uploadCandidatesFromFile in JobCandidateMappingService in {}ms", Thread.currentThread().getName(), System.currentTimeMillis()- startTime);
    }

    private List<Candidate> processUploadedFile(String fileName, UploadResponseBean responseBean, User user, String fileSource, String repoLocation, String countryCode) {
        //code to parse through the records and save data in database
        String fileExtension = Util.getFileExtension(fileName).toLowerCase();
        List<Candidate> candidateList = null;
        switch (fileExtension) {
            case "csv":
                switch (IConstant.UPLOAD_FORMATS_SUPPORTED.valueOf(fileSource)) {
                    case LitmusBlox:
                        candidateList = new CsvFileProcessorService().process(fileName, responseBean, !IConstant.STR_INDIA.equalsIgnoreCase(user.getCountryId().getCountryName()), repoLocation, user, IConstant.UPLOAD_FORMATS_SUPPORTED.LitmusBlox.name());
                        break;
                    case Naukri:
                        candidateList = new CsvFileProcessorService().process(fileName, responseBean, !IConstant.STR_INDIA.equalsIgnoreCase(user.getCountryId().getCountryName()), repoLocation, user, IConstant.UPLOAD_FORMATS_SUPPORTED.Naukri.name());
                        break;
                }
                break;
            case "xls":
            case "xlsx":
                switch (IConstant.UPLOAD_FORMATS_SUPPORTED.valueOf(fileSource)) {
                    case LitmusBlox:
                        candidateList = new ExcelFileProcessorService().process(fileName, responseBean, !IConstant.STR_INDIA.equalsIgnoreCase(user.getCountryId().getCountryName()), repoLocation, user, IConstant.UPLOAD_FORMATS_SUPPORTED.LitmusBlox.name());
                        break;
                    case Naukri:
                        candidateList = new NaukriExcelFileProcessorService().process(fileName, responseBean, !IConstant.STR_INDIA.equalsIgnoreCase(user.getCountryId().getCountryName()), repoLocation, user, IConstant.UPLOAD_FORMATS_SUPPORTED.Naukri.name());
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
    //@Transactional(propagation = Propagation.REQUIRED)
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
                        log.info("For candidate : {} notice period in request is : {} in job : {}", candidate.getEmail(), candidateCompanyDetails.getNoticePeriod(), jobId);
                        candidateCompanyDetails.setNoticePeriod(candidateCompanyDetails.getNoticePeriod()+" "+IConstant.DAYS);
                        candidateCompanyDetails.setNoticePeriodInDb(MasterDataBean.getInstance().getNoticePeriodMapping().get(candidateCompanyDetails.getNoticePeriod()));
                        if (null == candidateCompanyDetails.getNoticePeriodInDb()) {
                            //value in request object is not available in db
                            //SentryUtil.logWithStaticAPI(null,"Unmapped notice period: " + candidateCompanyDetails.getNoticePeriod(), new HashMap<>());
                            candidateCompanyDetails.setNoticePeriodInDb(MasterDataBean.getInstance().getNoticePeriodMapping().get("Others"));
                        }

                    }
                });
            }

            responseBean = uploadIndividualCandidate(Arrays.asList(candidate), jobId, (null == candidate.getMobile() || candidate.getMobile().isEmpty()), createdBy, false);

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
                if(null!=candidateCv){
                    jcm.setCvFileType("."+Util.getFileExtension(candidateCv.getOriginalFilename()));
                    jobCandidateMappingRepository.save(jcm);
                }
                cvParsingDetailsRepository.save(new CvParsingDetails(null!=candidateCv?candidateCv.getOriginalFilename():null, new Date(), null != candidate.getCandidateDetails()?candidate.getCandidateDetails().getTextCv():null, responseBean.getSuccessfulCandidates().get(0).getId(),jcm));
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
    //@Caching(evict = {@CacheEvict(cacheNames = "jcm"), @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "harvesterCandidateProfile"), @CacheEvict(cacheNames = "jcmForInterview")})
    public void captureCandidateInterest(UUID uuid, boolean interest, Long candidateNotInterestedReasonId, String userAgent) throws Exception {
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findByChatbotUuid(uuid);
        String candidateChoice="";
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
            candidateChoice = "interested";
        }
        else{
            if(null == candidateNotInterestedReasonId)
                throw new WebException("Candidate Not Interested Reason is null for uuid " + uuid, HttpStatus.UNPROCESSABLE_ENTITY);
            objFromDb.setChatbotStatus(IConstant.ChatbotStatus.NOT_INTERESTED.getValue());
            MasterData masterData = masterDataRepository.findById(candidateNotInterestedReasonId).get();
            if(!masterData.getType().equals("candidateNotInterestedReason"))
                throw new WebException("Invalid Id for Candidate Not Interested Reason " + candidateNotInterestedReasonId, HttpStatus.UNPROCESSABLE_ENTITY);
            objFromDb.setCandidateNotInterestedReason(masterData.getValue());
            candidateChoice = "not interested and reason is "+objFromDb.getCandidateNotInterestedReason();
        }
        objFromDb.setCandidateInterestDate(new Date());
        objFromDb.setInterestAccessByDevice(userAgent);
        //set stage = Screening where stage = Source
        //commented below code to not set flags to true.
        /*if(!objFromDb.getJob().getHrQuestionAvailable()){
            jcmCommunicationDetailsRepository.updateHrChatbotFlagByJcmId(objFromDb.getId());
        }
        if(!objFromDb.getJob().getHrQuestionAvailable() && !objFromDb.getJob().getScoringEngineJobAvailable()){
            jcmCommunicationDetailsRepository.updateByJcmId(objFromDb.getId());
        }*/
        log.info(objFromDb.getCandidateFirstName()+" "+objFromDb.getCandidateLastName()+" "+candidateChoice+" "+objFromDb.getJob().getId()+" : "+uuid);
        jobCandidateMappingRepository.save(objFromDb);
        StringBuffer historyMsg = new StringBuffer(objFromDb.getCandidateFirstName());
        historyMsg.append(" ").append(objFromDb.getCandidateLastName()).append(" is ").append(candidateChoice).append(objFromDb.getJob().getJobTitle()).append(" - ").append(objFromDb.getJob().getId());
        jcmHistoryRepository.save(new JcmHistory(objFromDb, historyMsg.toString(), new Date(), null, objFromDb.getStage(), true));
    }

    /**
     * Service method to capture candidate response to screening questions from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param screeningQuestionRequestBean Map of questionId and response List of responses received from chatbot and map of quick question response
     * @throws Exception
     */
    //@Caching(evict = {@CacheEvict(cacheNames = "jcm"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public void saveScreeningQuestion(UUID uuid, ScreeningQuestionRequestBean screeningQuestionRequestBean, String userAgent) throws  Exception {
        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findByChatbotUuid(uuid);
        if (null == jcmFromDb){
            log.error("Job candidate mapping not found for uuid : {}", uuid);
            throw new WebException(IErrorMessages.UUID_NOT_FOUND + uuid, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        saveScreeningQuestionResponse(uuid, screeningQuestionRequestBean,jcmFromDb, userAgent);
        Map<String, String> candidateChatbotResponse = jcmFromDb.getCandidateChatbotResponse();
        if (jcmFromDb.getChatbotStatus().equals(IConstant.ChatbotStatus.COMPLETE.getValue())) {
            long updateCandidateResponseStartTime = System.currentTimeMillis();
            log.info("Updating Candidate Details based on Candidate Chatbot Resposne. Chatbot uuid is {}", uuid);
            updateCandidateResponse(jcmFromDb, candidateChatbotResponse);
            jobCandidateMappingRepository.save(jcmFromDb);
            Map<String, Long> stageIdMap = MasterDataBean.getInstance().getStageStepMasterMap();
            jobCandidateMappingRepository.setScreenedByAndOn(
                    Arrays.asList(jcmFromDb.getId()),
                    Util.isNull(jcmFromDb.getScreeningBy())?IConstant.chatCompleteScreeningMessage:jcmFromDb.getScreeningBy(),
                    Util.isNull(jcmFromDb.getScreeningBy())?new Date():jcmFromDb.getScreeningOn(),
                    stageIdMap.get(IConstant.Stage.Source.getValue()),
                    stageIdMap.get(IConstant.Stage.Screen.getValue()),
                    jcmFromDb.getCreatedBy().getId(), new Date()
            );
            log.info("Completed Updating Candidate Details in {} ms.",  System.currentTimeMillis()-updateCandidateResponseStartTime);
        }
    }

    /**
     * Service method to capture candidate response to screening questions from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param screeningQuestionRequestBean Map of questionId and response List of responses received from chatbot
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
   // @Caching(evict = {@CacheEvict(cacheNames = "jcm"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "harvesterCandidateProfile"), @CacheEvict(cacheNames = "jcmCommDetails", key = "#jcmFromDb.id")})
    public void saveScreeningQuestionResponse(UUID uuid, ScreeningQuestionRequestBean screeningQuestionRequestBean, JobCandidateMapping jcmFromDb, String userAgent) throws Exception {
        log.info("Saving chatbot response for uuid : {}, jobId : {} and jcmId : {}", uuid, jcmFromDb.getJob().getId(), jcmFromDb.getId());
        Map<String, String> breadCrumb = new HashMap<>();
        Map<Long, List<String>> response = screeningQuestionRequestBean.getDeepScreeningQuestionResponseMap();
        breadCrumb.put("Chatbot uuid", uuid.toString());
        breadCrumb.put("JcmId",jcmFromDb.getId().toString());
        breadCrumb.put("JobId",jcmFromDb.getJob().getId().toString());
        if(null != response && response.size()>0){
            breadCrumb.put("questionId", (response.keySet().stream().findFirst().orElse(null)).toString());
            breadCrumb.put("Chatbot response", response.toString());
        }

        Map<String, String> quickScreeningResponse = new HashMap<>();
        long startTime = System.currentTimeMillis();

        //Update quick question response
        if(jcmFromDb.getJob().isQuickQuestion() && null != screeningQuestionRequestBean.getQuickScreeningQuestionResponseMap()){
            ObjectMapper objectMapper = new ObjectMapper();
            if(null != jcmFromDb.getCandidateQuickQuestionResponse()){
                quickScreeningResponse = objectMapper.readValue(jcmFromDb.getCandidateQuickQuestionResponse(), HashMap.class);
                quickScreeningResponse.putAll(screeningQuestionRequestBean.getQuickScreeningQuestionResponseMap());
                jcmFromDb.setCandidateQuickQuestionResponse(objectMapper.writeValueAsString(quickScreeningResponse));
            }else
                jcmFromDb.setCandidateQuickQuestionResponse(objectMapper.writeValueAsString(screeningQuestionRequestBean.getQuickScreeningQuestionResponseMap()));

            log.info("Candidate quick question response saved for jcm Id : {}", jcmFromDb.getId());
        }
        //saves HR questions, Custom Questions and Deep Screening Questions
        if(null != response && response.size()>0){
            //Update tech screening question
            response.entrySet().forEach(longListEntry -> {
                String[] valuesToSave = new String[longListEntry.getValue().size()];
                for (int i = 0; i < longListEntry.getValue().size(); i++) {
                    valuesToSave[i] = longListEntry.getValue().get(i);
                    if (i == 0 && valuesToSave[i].length() > IConstant.SCREENING_QUESTION_RESPONSE_MAX_LENGTH) {
                        log.error("Length of user response is greater than {} : {} ", IConstant.SCREENING_QUESTION_RESPONSE_MAX_LENGTH, longListEntry.getValue());
                        valuesToSave[i] = valuesToSave[i].substring(0, IConstant.SCREENING_QUESTION_RESPONSE_MAX_LENGTH);
                    }
                    if (i == 1 && valuesToSave[i].length() > IConstant.SCREENING_QUESTION_COMMENT_MAX_LENGTH) {
                        log.error("Length of user response is greater than {} : {} ", IConstant.SCREENING_QUESTION_COMMENT_MAX_LENGTH, longListEntry.getValue());
                        valuesToSave[i] = valuesToSave[i].substring(0, IConstant.SCREENING_QUESTION_COMMENT_MAX_LENGTH);
                    }
                }

                //Save candidate screening question response
                CandidateScreeningQuestionResponse candidateResponse = candidateScreeningQuestionResponseRepository.findByJobCandidateMappingIdAndJobScreeningQuestionId(jcmFromDb.getId(), longListEntry.getKey());
                if (null == candidateResponse) {
                    candidateResponse = new CandidateScreeningQuestionResponse(
                            jcmFromDb.getId(),
                            longListEntry.getKey(),
                            valuesToSave[0],
                            (valuesToSave.length > 1) ? valuesToSave[1] : null);
                } else {
                    candidateResponse.setResponse(valuesToSave[0]);
                    candidateResponse.setComment((valuesToSave.length > 1) ? valuesToSave[1] : null);
                }
                candidateResponse = candidateScreeningQuestionResponseRepository.save(candidateResponse);
                candidateScreeningQuestionResponseRepository.flush();

                //updating hr_chat_complete_flag
                log.info("Completed adding response to db in {}ms", (System.currentTimeMillis() - startTime));

                //update chatbot response and updated date in jcm
                if (null == jcmFromDb.getCandidateChatbotResponse())
                    jcmFromDb.setCandidateChatbotResponse(new HashMap<>());

                //Set Candidate chatbot response
                jcmFromDb.getCandidateChatbotResponse().put(candidateResponse.getJobScreeningQuestionId().toString(), (candidateResponse.getResponse() + (candidateResponse.getComment() != null ? candidateResponse.getComment() : "")));
                jcmFromDb.setChatbotUpdatedOn(new Date());
            });
        }

        int totalResponses = candidateScreeningQuestionResponseRepository.findByJobCandidateMappingId(jcmFromDb.getId()).size();

        int jobSkillsAttributesListSize = jcmFromDb.getJob().getJobSkillsAttributesList().stream().filter(jobSkillsAttributes -> jobSkillsAttributes.isSelected()).collect(Collectors.toList()).size();

        //Update chatbot status
        if((jcmFromDb.getJob().isQuickQuestion() && jobSkillsAttributesListSize == quickScreeningResponse.size() && totalResponses == jcmFromDb.getJob().getJobScreeningQuestionsList().size())
                || (totalResponses == jcmFromDb.getJob().getJobScreeningQuestionsList().size() && !jcmFromDb.getJob().isQuickQuestion())){
            jcmFromDb.setChatbotStatus(IConstant.ChatbotStatus.COMPLETE.getValue());
            jcmFromDb.setChatbotCompletedByDevice(userAgent);
            if(!jcmFromDb.getJob().isQuickQuestion())
                jcmCommunicationDetailsRepository.updateHrChatbotFlagByJcmId(jcmFromDb.getId());
        }else
            jcmFromDb.setChatbotStatus(IConstant.ChatbotStatus.INCOMPLETE.getValue());

        //Commented below code as we are not setting flag to true as per discussion on 10-01-2020
        //updating chat_complete_flag if corresponding job is not available on scoring engine due to lack of ML data,
        // or candidate already filled all the capabilities in some other job and we already have candidate responses for technical chatbot.
        /*if(!objFromDb.getJob().getScoringEngineJobAvailable() || (objFromDb.getChatbotStatus()!=null && objFromDb.getChatbotStatus().equals("Complete"))){
            jcmCommunicationDetailsRepository.updateByJcmId(objFromDb.getId());
        }*/
        jobCandidateMappingRepository.save(jcmFromDb);

    }

    /**
     * Service method to get all screening questions for the job
     *
     * @param uuid the uuid corresponding to a unique jcm record
     * @return the list of job screening questions
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    //@Cacheable(cacheNames = "screeningQuestions", key = "#uuid")
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
    @Async("asyncTaskExecutor")
    //@Caching(evict = {@CacheEvict(cacheNames = "AsyncOperationsErrorRecords"), @CacheEvict(cacheNames = "jcm"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public void inviteCandidates(List<Long> jcmList, User loggedInUser) throws Exception {
        log.info("Thread - {} : Started invite candidates method", Thread.currentThread().getName());
        InviteCandidateResponseBean inviteCandidateResponseBean = performInvitationAndHistoryUpdation(jcmList, loggedInUser);
        //remove all failed invitations
        jcmList.removeAll(inviteCandidateResponseBean.getFailedJcm().stream().map(JobCandidateMapping::getId).collect(Collectors.toList()));
        //Currently, we are not using the scoring engine service
        if (null != inviteCandidateResponseBean.getFailedJcm() && inviteCandidateResponseBean.getFailedJcm().size() > 0) {
            handleErrorRecords(null, inviteCandidateResponseBean.getFailedJcm(), IConstant.ASYNC_OPERATIONS.InviteCandidates.name(), loggedInUser, inviteCandidateResponseBean.getJobId(), null);
        }
        log.info("Thread - {} : Completed invite candidates method", Thread.currentThread().getName());

    }

    /**
     * Service method to call inviteAutoSourcedOrLDEBCandidates with jcm which are autosourced and currently in sourcing stage
     * @throws Exception
     */
    public void inviteAutoSourcedCandidate()throws Exception{
        List<JobCandidateMapping> jobCandidateMappings = jobCandidateMappingRepository.getNewAutoSourcedJcmList();
        if(jobCandidateMappings.size()>0) {
            log.info("Found {} autosourced candidates to be auto invited", jobCandidateMappings.size());
            Map<User, List<Long>> userJcmMap = jobCandidateMappings.stream()
                    .collect(
                            Collectors.groupingBy(JobCandidateMapping::getCreatedBy, Collectors.mapping(JobCandidateMapping::getId, Collectors.toList()))
                    );
            userJcmMap.entrySet().forEach(userListEntry -> {
                try {
                    inviteCandidates(userListEntry.getValue(), userListEntry.getKey());
                } catch (Exception e) {
                    log.error("Error while inviting candidates for user: {}, {}", userListEntry.getKey(), e.getMessage());
                }
            });
        }
        else{
            log.info("Found 0 candidates to be auto invited");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    //@CacheEvict(cacheNames = "jcmCommDetails")
    private InviteCandidateResponseBean performInvitationAndHistoryUpdation(List<Long> jcmList, User loggedInUser) throws Exception {
        if (jcmList == null || jcmList.size() == 0)
            throw new WebException("Select candidates to invite", HttpStatus.UNPROCESSABLE_ENTITY);

        //make sure all candidates are at the same stage
        if (!areCandidatesInSameStage(jcmList))
            throw new WebException("Select candidates that are all in Source stage", HttpStatus.UNPROCESSABLE_ENTITY);

        if (loggedInUser == null) {
            loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        //list to store candidates for which email contains "@notavailable.io" or invalid email or mobile is null
        List<JobCandidateMapping> failedJcm = new ArrayList<>();

        //List to store jcm ids for which email does not start with "@notavailable.io" and valid email or mobile is not null
        List<Long> jcmListWithoutError = new ArrayList<>();

        //Invite candidate respose bean hoolds status, success count, failure count, failed candidates whose email or mobile is not valid.
        InviteCandidateResponseBean inviteCandidateResponseBean = null;

        //fetch list of jcm from db using ids
        List<JobCandidateMapping> jobCandidateMappingList = jobCandidateMappingRepository.findAllById(jcmList);

        Job jobObjToUse = null;
        //iterate over jcm list

        try {
            for (JobCandidateMapping jobCandidateMapping : jobCandidateMappingList) {

                if (null == jobObjToUse)
                    jobObjToUse =
                            jobCandidateMapping.getJob();

                //This method call from invite candidate which is a scheduled task so we remove validation
                //validateLoggedInUser(loggedInUser, jobCandidateMapping.getJob());

                //https://github.com/hexagonsearch/litmusblox-backend/issues/527
                //Check if mobile or email valid then invite candidate if both are invalid then skip to invite candidate
                if (((!Util.isValidateEmail(jobCandidateMapping.getEmail(), null) || jobCandidateMapping.getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL)) && Util.isNull(jobCandidateMapping.getMobile())) || null != jobCandidateMapping.getChatbotStatus()) {
                    if (null != jobCandidateMapping.getChatbotStatus())
                        jobCandidateMapping.setInviteErrorMessage("Candidate is already invited. Cannot be invited again.");
                    else
                        jobCandidateMapping.setInviteErrorMessage("Invalid mobile and Email address: " + jobCandidateMapping.getEmail());

                    failedJcm.add(jobCandidateMapping);
                    continue;
                }

                jcmListWithoutError.add(jobCandidateMapping.getId());
            }
        } catch(ValidationException validationException){
            log.error("Error while invite candidates loggedInUser : {}, errorMessage : {}", loggedInUser.getId(), validationException.getErrorMessage());
            asyncOperationsErrorRecordsRepository.save(new AsyncOperationsErrorRecords(null, null, null, null, null, validationException.getErrorMessage(), IConstant.ASYNC_OPERATIONS.InviteCandidates.name(), loggedInUser, new Date(), null));
        }

        if(null != jobObjToUse) {
            if (jcmListWithoutError.size() == 0) {
                inviteCandidateResponseBean = new InviteCandidateResponseBean(IConstant.UPLOAD_STATUS.Failure.toString(), 0, jcmList.size(), jobObjToUse.getId(), failedJcm);
            } else {
                if (jcmListWithoutError.size() < jcmList.size()) {
                    inviteCandidateResponseBean = new InviteCandidateResponseBean(IConstant.UPLOAD_STATUS.Partial_Success.toString(), jcmListWithoutError.size(), jcmList.size() - jcmListWithoutError.size(), jobObjToUse.getId(), failedJcm);
                } else {
                    inviteCandidateResponseBean = new InviteCandidateResponseBean(IConstant.UPLOAD_STATUS.Success.toString(), jcmListWithoutError.size(), 0, jobObjToUse.getId(), failedJcm);
                }
                jcmCommunicationDetailsRepository.inviteCandidates(jcmListWithoutError);
                jobCandidateMappingRepository.updateJcmSetStatus(IConstant.ChatbotStatus.INVITED.getValue(), jcmListWithoutError);
            }
        }

        if(jcmListWithoutError.size()>0) {
            //set stage = Screening where stage = Source
            updateJcmHistory(jcmListWithoutError, loggedInUser);
        }
        return inviteCandidateResponseBean;
    }

    //@CacheEvict(cacheNames = "jcmHistory")
    void updateJcmHistory(List<Long> jcmList, User loggedInUser) {
        List<JcmHistory> jcmHistoryList = new ArrayList<>();

        for (Long jcmId : jcmList) {
            JobCandidateMapping jcmObject = jobCandidateMappingRepository.getOne(jcmId);
            StringBuffer historyMessage = new StringBuffer(jcmObject.getCandidateFirstName());
            historyMessage.append(" ").append(jcmObject.getCandidateLastName()).append(" invited for - ").append(jcmObject.getJob().getJobTitle()).append(" - ").append(jcmObject.getJob().getId());
            jcmHistoryList.add(new JcmHistory(jcmObject, historyMessage.toString(), new Date(), loggedInUser, jcmObject.getStage(), false));
        }

        if (jcmHistoryList.size() > 0) {
            jcmHistoryRepository.saveAll(jcmHistoryList);
        }
    }

    /**
     * Service method to process sharing of candidate profiles with Hiring managers
     *
     * @param requestBean The request bean with information about the profile to be shared, the recepient name and recepient email address
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    //@Caching(evict = {@CacheEvict(cacheNames = "jcmHistory"), @CacheEvict(cacheNames = "jcm"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public void shareCandidateProfiles(ShareCandidateProfileRequestBean requestBean) {

        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        JobCandidateMapping jcm = jobCandidateMappingRepository.findById(requestBean.getJcmId().get(0)).orElse(null);
        if(jcm!=null)
            validateLoggedInUser(loggedInUser, jcm.getJob());

        List<String> receiverEmails = new ArrayList<>();

        for (Long user:requestBean.getReceiverUserId()) {

            User receiverUser = userRepository.getOne(user);
            validateloggedInUser(receiverUser, jcm.getJob().getCompanyId().getId());
            Set<JcmProfileSharingDetails> detailsSet = new HashSet<>(requestBean.getJcmId().size());
            requestBean.getJcmId().forEach(jcmId ->{
                JcmProfileSharingDetails jcmProfileSharingDetailsFromDb = jcmProfileSharingDetailsRepository.getProfileSharedByJcmIdAndUserId(jcmId, receiverUser.getId());
                if(null == jcmProfileSharingDetailsFromDb){
                    JcmProfileSharingDetails jcmProfileSharingDetails = jcmProfileSharingDetailsRepository.save(new JcmProfileSharingDetails(jcmId, loggedInUser.getId(), receiverUser.getId(), receiverUser.getDisplayName()));
                    HiringManagerWorkspace hiringManagerWorkspace = hiringManagerWorkspaceRepository.findByJcmIdAndUserId(jcmId, receiverUser.getId());
                    if(null == hiringManagerWorkspace)
                        hiringManagerWorkspaceRepository.save(new HiringManagerWorkspace(jcmId, receiverUser.getId(), jcmProfileSharingDetails.getId(), null));
                    else if (hiringManagerWorkspace.getShareProfileId() == null)
                        hiringManagerWorkspaceRepository.updateProfileShareId(jcmProfileSharingDetails.getId(), hiringManagerWorkspace.getId());
                    else
                        log.info("Profile already shared for Jcm id {} with hiring manager {}", jcmId, receiverUser.getEmail());
                }
            });
            receiverEmails.add(receiverUser.getEmail());
        }

        List<JcmHistory> jcmHistoryList = new ArrayList<>();
        requestBean.getJcmId().forEach(jcmId-> {
            JobCandidateMapping jcmObject = jobCandidateMappingRepository.getOne(jcmId);
            jcmHistoryList.add(new JcmHistory(jcmObject, "Profiles shared with : "+String.join(", ", receiverEmails)+".", new Date(), loggedInUser, jcmObject.getStage(), false));
        });
        jcmHistoryRepository.saveAll(jcmHistoryList);
        //move to Submit stage
        JobCandidateMapping jcmObject = jobCandidateMappingRepository.getOne(requestBean.getJcmId().get(0));
        if(IConstant.Stage.Source.getValue().equals(jcmObject.getStage().getStage()) || IConstant.Stage.Screen.getValue().equals(jcmObject.getStage().getStage())) {
            jobCandidateMappingRepository.setSubmittedByAndOn(
                    requestBean.getJcmId(),
                    loggedInUser.getDisplayName(),
                    new Date(),
                    jcmObject.getStage().getId(),
                    MasterDataBean.getInstance().getStageStepMasterMap().get(IConstant.Stage.ResumeSubmit.getValue()),
                    loggedInUser.getId(), new Date()
            );
        }
    }

    /**
     * Service method to fetch details of a single candidate for a job
     *
     * @param jobCandidateMappingId
     * @return jobCandidateMapping object with required details
     * @throws Exception
     */
    @Transactional
    //@Cacheable(cacheNames = "jcm", key = "#jobCandidateMappingId")
    public JobCandidateMapping getCandidateProfile(Long jobCandidateMappingId, boolean isCallForHiringManager) throws Exception {
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findById(jobCandidateMappingId).orElse(null);
        if(null == objFromDb)
            throw new ValidationException("No job candidate mapping found for id: " + jobCandidateMappingId, HttpStatus.UNPROCESSABLE_ENTITY);

        if(!isCallForHiringManager) {
            User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            validateLoggedInUser(loggedInUser, objFromDb.getJob());
        }else {
            objFromDb.setCandidateHistoryForHiringManager(jcmHistoryRepository.findByJcmIdAndCallLogOutComeIgnoreCase(objFromDb, "For Hiring Manager"));
        }

        objFromDb = setJcmForCandidateProfile(objFromDb);

        return objFromDb;
    }

    private JobCandidateMapping setJcmForCandidateProfile(JobCandidateMapping objFromDb){
        List<JobScreeningQuestions> screeningQuestions = jobScreeningQuestionsRepository.findByJobId(objFromDb.getJob().getId());
        Map<Long, JobScreeningQuestions> screeningQuestionsMap = new LinkedHashMap<>(screeningQuestions.size());
        screeningQuestions.forEach(screeningQuestion-> {
            screeningQuestionsMap.put(screeningQuestion.getId(), screeningQuestion);
        });

        List<CandidateScreeningQuestionResponse> responses = candidateScreeningQuestionResponseRepository.findByJobCandidateMappingId(objFromDb.getId());

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

        List<CandidateInteractionHistory> candidateInteractionHistoryList = jobCandidateMappingRepository.getCandidateInteractionHistoryByCandidateId(objFromDb.getCandidate().getId(), objFromDb.getJob().getCompanyId().getId());
        if(!candidateInteractionHistoryList.isEmpty()){
            objFromDb.getCandidate().setCandidateInteractionHistoryList(candidateInteractionHistoryList);
        }
        return objFromDb;
    }

    /**
     * Method to retrieve the job candidate mapping record based on the uuid
     * @param uuid the uuid against which the record is to be retrieved
     * @return the job candidate mapping
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    //@Cacheable(cacheNames = "jcm", key = "#uuid")
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

        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Job job = jobRepository.getOne(jobId);
        validateLoggedInUser(loggedInUser, job);

        for (MultipartFile fileToProcess :multipartFiles) {
            StringBuffer location = new StringBuffer(environment.getProperty(IConstant.TEMP_REPO_LOCATION));
            location.append(IConstant.DRAG_AND_DROP).append(File.separator);
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

                try {
                    filePath = StoreFileUtil.storeFile(fileToProcess, jobId, location.toString(), fileType,null, loggedInUser);
                    successCount++;
                } catch (Exception e) {
                    log.error(fileToProcess.getOriginalFilename()+" not save to temp location : "+e.getMessage());
                    failureCount++;
                    responseBean.getCvUploadMessage().put(fileToProcess.getOriginalFilename(), IErrorMessages.FAILED_TO_SAVE_FILE + extension);
                }

                if(IConstant.FILE_TYPE.zip.toString().equals(fileType) || IConstant.FILE_TYPE.rar.toString().equals(fileType)){
                    successCount--;
                    countArray=ZipFileProcessUtil.extractZipFile(filePath, location.toString(), loggedInUser, jobId, responseBean, failureCount, successCount);
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
    /*@Caching(evict = {@CacheEvict(cacheNames = "jcm"), @CacheEvict(cacheNames = "TechRoleCompetency"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"),
            @CacheEvict(cacheNames = "TechRoleCompetency", key = "#jobId"), @CacheEvict(cacheNames = "harvesterCandidateProfile"),  @CacheEvict(cacheNames = "jcmCommDetails")})
*/    public void updateTechResponseStatus(TechChatbotRequestBean requestBean) throws Exception {
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
   // @Caching(evict = {@CacheEvict(cacheNames = "jcm", key = "#jobCandidateMapping.id"),@CacheEvict(cacheNames = "jcmHistory", key = "#jobCandidateMapping.id"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "TechRoleCompetency", key = "#jobCandidateMapping.job.id"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public void editCandidate(JobCandidateMapping jobCandidateMapping) {
        User loggedInUser = (null != SecurityContextHolder.getContext().getAuthentication())?(User)SecurityContextHolder.getContext().getAuthentication().getPrincipal():jobCandidateMapping.getCreatedBy();
        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findById(jobCandidateMapping.getId()).orElse(null);

        if(null != jcmFromDb)
            validateLoggedInUser(loggedInUser, jcmFromDb.getJob());

        //set country code in jcm if it is null in request then take existing jcm country code from db
        if(null == jobCandidateMapping.getCandidate().getCountryCode())
            jobCandidateMapping.setCountryCode(jcmFromDb.getCountryCode());
        else
            jobCandidateMapping.setCountryCode(jobCandidateMapping.getCandidate().getCountryCode());

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

            //Update candidate expected ctc
            if(null != jobCandidateMapping.getExpectedCtc())
                jcmFromDb.setExpectedCtc(jobCandidateMapping.getExpectedCtc());

            //Update candidate percentage hike
            if(null != jobCandidateMapping.getPercentageHike())
                jcmFromDb.setPercentageHike(jobCandidateMapping.getPercentageHike());
            String comments = jobCandidateMapping.getComments();
            if(Util.isNotNull(comments)){
                comments = comments.trim();
                String commentsFromDb = jcmFromDb.getComments();
                if(Util.isNotNull(comments) && (commentsFromDb == null || !commentsFromDb.equals(comments)))
                    jcmHistoryRepository.save(new JcmHistory(jcmFromDb,comments,null,false,new Date(),jcmFromDb.getStage(),loggedInUser));
            }

            //Update recruiter comments for candidate
            jcmFromDb.setComments(comments);

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
                    if (!isCompanyPresent.get() && CompanyDetails.getCompanyName().equalsIgnoreCase(companyDetailsByRequest.getCompanyName())) {
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
                log.info("Edit candidate info successfully for candidate : {}", jobCandidateMapping.getCandidate().getId());

                //calling search engine addUpdate api to update candidate data.
                candidateService.createCandidateOnSearchEngine(jcmFromDb.getCandidate(), jcmFromDb, JwtTokenUtil.getAuthToken());
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
        if(Util.isNotNull(jcm.getAlternateMobile()))
            jcm.setAlternateMobile(validateMobile(jcm.getAlternateMobile(), jcm.getCountryCode()));

        if(Util.isNotNull(jcm.getAlternateEmail()))
            jcm.setAlternateEmail(Util.validateEmail(jcm.getAlternateEmail(), null));

        CandidateEmailHistory candidateEmailHistory = null;
        CandidateMobileHistory candidateMobileHistory = null;
        if(Util.isNotNull(jcm.getAlternateEmail())) {
            jcmFromDb.setAlternateEmail(jcm.getAlternateEmail());
            candidateEmailHistory = getEmailHistory(jcm.getAlternateEmail());
        }

        if(Util.isNotNull(jcm.getAlternateMobile())) {
            jcmFromDb.setAlternateMobile(jcm.getAlternateMobile());
            candidateMobileHistory = getMobileHistory(jcm.getAlternateMobile(), jcm.getCountryCode());
        }

        if(null != candidateEmailHistory && null != candidateMobileHistory){
            if(!candidateEmailHistory.getCandidate().getId().equals(candidateMobileHistory.getCandidate().getId()))
                throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY + " - " + jcm.getAlternateMobile() + " "+jcm.getAlternateEmail()+", While add/update alternate email and mobile", HttpStatus.BAD_REQUEST);
        }else if(null == candidateEmailHistory && null == candidateMobileHistory){
            if(Util.isNotNull(jcm.getAlternateMobile()))
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(jcmFromDb.getCandidate(), jcm.getAlternateMobile(), jcm.getCountryCode(), new Date(), loggedInUser));
            if(Util.isNotNull(jcm.getAlternateEmail()))
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(jcmFromDb.getCandidate(), jcm.getAlternateEmail(), new Date(), loggedInUser));
        }else{
            if(null != candidateEmailHistory && null == candidateMobileHistory && Util.isNotNull(jcm.getAlternateMobile())){
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(jcmFromDb.getCandidate(), jcm.getAlternateMobile(), jcm.getCountryCode(), new Date(), loggedInUser));
            }else if(null != candidateMobileHistory && null == candidateEmailHistory && Util.isNotNull(jcm.getAlternateEmail())){
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(jcmFromDb.getCandidate(), jcm.getAlternateEmail(), new Date(), loggedInUser));
            }
        }
        return jcmFromDb;
    }


    private String validateMobile(String mobile, String countryCode){
        if(Util.isNotNull(mobile)) {
            mobile = Util.indianMobileConvertor(mobile, countryCode);
            if (!Util.validateMobile(mobile, countryCode, null) && !IConstant.CountryCode.INDIA_CODE.getValue().equals(countryCode)) {
                String cleanMobile = mobile.replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_MOBILE, "");
                log.error("Special characters found, cleaning mobile number \"" + mobile + "\" to " + cleanMobile);
                if (!Util.validateMobile(cleanMobile, countryCode, null))
                    throw new ValidationException(IErrorMessages.MOBILE_INVALID_DATA + " - " + mobile, HttpStatus.BAD_REQUEST);
                return cleanMobile;
            }
        }
        return mobile;
    }

    private boolean removeNotAvailableEmail(JobCandidateMapping jcm){
        boolean emailUpdated = false;
        List<CandidateEmailHistory>candidateEmailHistoryNotAvailableCheck = candidateEmailHistoryRepository.findByCandidateIdOrderByIdDesc(jcm.getCandidate().getId());
        if(candidateEmailHistoryNotAvailableCheck.size()>0){

            //Get not available email history list
            List<CandidateEmailHistory> notAvailableEmailHistory = candidateEmailHistoryNotAvailableCheck.stream()
                    .filter(candidateEmailHistoryNotAvailable -> candidateEmailHistoryNotAvailable.getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL))
                    .collect(Collectors.toList());

            if(notAvailableEmailHistory.size()>0){
                notAvailableEmailHistory.get(0).setEmail(jcm.getEmail());
                CandidateEmailHistory candidateEmailHistoryUpdated = candidateEmailHistoryRepository.save(notAvailableEmailHistory.get(0));
                if(candidateEmailHistoryUpdated.getEmail().equals(jcm.getEmail()))
                    emailUpdated = true;
                notAvailableEmailHistory.remove(0);
                if(notAvailableEmailHistory.size()>0){
                    candidateEmailHistoryRepository.deleteAll(notAvailableEmailHistory);
                }
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
    //@Caching(evict = {@CacheEvict(cacheNames = "jcm", key = "#jobCandidateMapping.id"),@CacheEvict(cacheNames = "jcmHistory", key = "#jobCandidateMapping.id"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public boolean updateOrCreateEmailMobile(JobCandidateMapping jobCandidateMapping, JobCandidateMapping jcmFromDb, User loggedInUser){
        log.info("Inside updateOrCreateEmailMobile for Jcm : {}",jcmFromDb.getId());

        boolean jcmFromDbDeleted = false;
        //check if new email contains @notavailable.io
        if(jobCandidateMapping.getEmail().contains(IConstant.NOT_AVAILABLE_EMAIL)){
            // call getCandidateIdFromMobileHistory to fetch candidate id for new mobile from mobile history from db if exists.
            Long candidateIdFromMobileHistory = getCandidateIdFromMobileHistory(jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode());

            if (candidateIdFromMobileHistory != null && !jobCandidateMapping.getMobile().equals(jcmFromDb.getMobile())) {
                //fetch candidate mobile history for new mobile as it is already existing if control reaches here.
                CandidateMobileHistory candidateMobileHistory = getMobileHistory(jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode());

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
            Long candidateIdFromMobileHistory = getCandidateIdFromMobileHistory(jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode());

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

                            //Update candidate firstName
                            if (Util.isNotNull(jobCandidateMapping.getCandidateFirstName())) {
                                jcmForExistingCandidate.setCandidateFirstName(Util.validateCandidateName(jobCandidateMapping.getCandidateFirstName()));
                            }
                            //Update candidate lastName
                            if (Util.isNotNull(jobCandidateMapping.getCandidateLastName())) {
                                jcmForExistingCandidate.setCandidateLastName(Util.validateCandidateName(jobCandidateMapping.getCandidateLastName()));
                            }
                            //Update candidate email
                            if (Util.isNotNull(jobCandidateMapping.getEmail())) {
                                jcmForExistingCandidate.setEmail(Util.validateEmail(jobCandidateMapping.getEmail(), Optional.ofNullable(existingCandidate)));
                            }

                            //Update candidate email
                            if (Util.isNotNull(jobCandidateMapping.getMobile()) && Util.validateMobile(jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode(), Optional.ofNullable(existingCandidate))) {
                                jcmForExistingCandidate.setMobile(jobCandidateMapping.getMobile());
                            }

                            //call function to delete requested jcm record and change updated by to current user for exiting jcm
                            deleteAndUpdateJcmRecord(jcmFromDb, jcmForExistingCandidate, loggedInUser);
                            jcmFromDbDeleted = true;
                        }
                        else{
                            List<CandidateMobileHistory> existingCandidateMobileList = candidateMobileHistoryRepository.findByCandidateIdOrderByIdDesc(existingCandidate.getId());
                            if(existingCandidateMobileList.size()>0){
                                jcmFromDb.setMobile(existingCandidateMobileList.get(0).getMobile());
                            }
                            //Update candidate firstName
                            if (Util.isNotNull(jobCandidateMapping.getCandidateFirstName())) {
                                jcmFromDb.setCandidateFirstName(Util.validateCandidateName(jobCandidateMapping.getCandidateFirstName()));
                            }
                            //Update candidate lastName
                            if (Util.isNotNull(jobCandidateMapping.getCandidateLastName())) {
                                jcmFromDb.setCandidateLastName(Util.validateCandidateName(jobCandidateMapping.getCandidateLastName()));
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
                    log.error("Email and mobile belongs to different candidate, candidateIdFromEmailHistory : {} and candidateIdFromMobileHistory : {}", candidateIdFromEmailHistory, candidateIdFromMobileHistory);
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
                    CandidateMobileHistory candidateMobileHistory = getMobileHistory(jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode());

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
                        jcmFromDb.setCountryCode(jobCandidateMapping.getCountryCode());
                        //update jcm with existing candidate and delete candidate with email "@notavailable"
                        deleteAndUpdateCandidate(existingCandidate, jcmFromDb);
                    }
                }
            }
            else {
                createUpdateEmailMobileNew(jobCandidateMapping, jcmFromDb, loggedInUser);
            }
        }
        log.info("Candidate edit process completed for candidate : {}", jcmFromDb.getCandidate().getId());
        return jcmFromDbDeleted;
    }

    /**
     * Service method to determine if candidate has already sent a confirmation for the said interview earlier
     *
     * @return List of companies
     * @throws Exception
     */
    @Transactional
    //@Cacheable(cacheNames = "jcmForInterview", key = "#interviewReferenceId")
    public JobCandidateMapping getCandidateConfirmationStatus(UUID interviewReferenceId) throws Exception {
        InterviewDetails interviewDetailsFromDb = interviewDetailsRepository.findByInterviewReferenceId(interviewReferenceId);
        if(null == interviewDetailsFromDb)
            throw new ValidationException("Interview details not found for rf id : "+interviewReferenceId, HttpStatus.BAD_REQUEST);

        return jobCandidateMappingRepository.findById(interviewDetailsFromDb.getJobCandidateMappingId()).orElse(null);
    }

    /**
     * function to remove all occurrences of records for a candidate in different tables.
     * @param candidate for which all records will be removed
     */
    private void deleteCandidate(Candidate candidate){
        log.info("Inside deleteCandidate for candidateId : {}",candidate.getId());
        candidateCompanyDetailsRepository.deleteByCandidateId(candidate.getId());
        candidateDetailsRepository.deleteByCandidateId(candidate);
        candidateEducationDetailsRepository.deleteByCandidateId(candidate.getId());
        candidateOnlineProfilesRepository.deleteByCandidateId(candidate.getId());
        candidateSkillDetailsRepository.deleteByCandidateId(candidate.getId());
        candidateProjectDetailsRepository.deleteByCandidateId(candidate.getId());
        candidateLanguageProficiencyRepository.deleteByCandidateId(candidate.getId());
        candidateWorkAuthorizationRepository.deleteByCandidateId(candidate.getId());
        candidateEmailHistoryRepository.deleteByCandidateId(candidate.getId());
        candidateMobileHistoryRepository.deleteByCandidateId(candidate.getId());
        candidateRepository.delete(candidate);
    }

    private Long getCandidateIdFromEmailHistory(String email){
        log.info("Inside getCandidateIdFromEmailHistory for email : {}",email);
        //Fetch candidateId From Email History
        Long candidateIdFromEmailHistory = candidateEmailHistoryRepository.findCandidateIdByEmail(email);
        return candidateIdFromEmailHistory;
    }

    private Long getCandidateIdFromMobileHistory(String mobile, String countryCode){
        log.info("Inside getCandidateIdFromMobileHistory for mobile : {}, countryCode : {}",mobile, countryCode);
        //Fetch candidateId From Mobile History
        Long candidateIdFromMobileHistory = null;
        if(null != mobile && null != countryCode)
            candidateIdFromMobileHistory = candidateMobileHistoryRepository.findCandidateIdByMobileAndCountryCode(mobile, countryCode);

        return candidateIdFromMobileHistory;
    }

    private CandidateEmailHistory getEmailHistory(String email){
        log.info("Inside getEmailHistory for email : {}",email);
        return candidateEmailHistoryRepository.findByEmail(email);
    }

    private CandidateMobileHistory getMobileHistory(String mobile, String countryCode){
        log.info("Inside getMobileHistory for mobile : {}",mobile);
        return candidateMobileHistoryRepository.findByMobileAndCountryCode(mobile, countryCode);
    }

    private void deleteAndUpdateJcmRecord(JobCandidateMapping jcmFromDb, JobCandidateMapping jcmForExistingCandidate, User loggedInUser){
        log.info("Inside deleteAndUpdateJcmRecord, JcmId : {}",jcmFromDb.getId());
        jcmCommunicationDetailsRepository.deleteByJcmId(jcmFromDb.getId());
        asyncOperationsErrorRecordsRepository.deleteByJobCandidateMappingId(jcmFromDb);
        cvParsingDetailsRepository.deleteByJobCandidateMappingId(jcmFromDb);
        candidateScreeningQuestionResponseRepository.deleteByJobCandidateMappingId(jcmFromDb.getId());
        jcmHistoryRepository.deleteByJcmId(jcmFromDb);
        jcmCandidateSourceHistoryRepository.deleteByJobCandidateMappingId(jcmFromDb.getId());
        jcmProfileSharingDetailsRepository.deleteByJobCandidateMappingId(jcmFromDb.getId());
        candidateTechResponseDataRepository.deleteByJobCandidateMappingId(jcmFromDb);
        jobCandidateMappingRepository.delete(jcmFromDb);
        jcmForExistingCandidate.setUpdatedBy(loggedInUser);
        jobCandidateMappingRepository.flush();
        jobCandidateMappingRepository.save(jcmForExistingCandidate);
    }

    private void deleteAndUpdateCandidate(Candidate existingCandidate, JobCandidateMapping jcmFromDb){
        log.info("Inside deleteAndUpdateCandidate, candidateId : {}, JcmId : {}",existingCandidate.getId(),jcmFromDb.getId());
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
        jobCandidateMapping.setMobile(validateMobile(jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode()));
        if(!Util.isNull(jobCandidateMapping.getMobile())) {
            CandidateMobileHistory candidateMobileHistory = candidateMobileHistoryRepository.findByMobileAndCountryCode(jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode());
            if (null == candidateMobileHistory) {
                log.info("Create new mobile history for mobile : {}, for candidateId : {}", jobCandidateMapping.getMobile(), jcmFromDb.getCandidate().getId());
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(jcmFromDb.getCandidate(), jobCandidateMapping.getMobile(), jobCandidateMapping.getCountryCode(), new Date(), loggedInUser));
                jcmFromDb.setMobile(jobCandidateMapping.getMobile());
                jcmFromDb.setCountryCode(jobCandidateMapping.getCountryCode());
            } else {
                if (!jcmFromDb.getCandidate().getId().equals(candidateMobileHistory.getCandidate().getId()))
                    throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY_FOR_MOBILE + jobCandidateMapping.getMobile() + " " + jobCandidateMapping.getEmail(), HttpStatus.BAD_REQUEST);
                else{
                    jcmFromDb.setMobile(candidateMobileHistory.getMobile());
                    jcmFromDb.setCountryCode(jobCandidateMapping.getCountryCode());
                }
            }
            jobCandidateMappingRepository.save(jcmFromDb);
        }
    }

    private void createUpdateEmail(JobCandidateMapping jobCandidateMapping, JobCandidateMapping jcmFromDb, User loggedInUser){
        jobCandidateMapping.setEmail(Util.validateEmail(jobCandidateMapping.getEmail(), null));
        CandidateEmailHistory candidateEmailHistory = candidateEmailHistoryRepository.findByEmail(jobCandidateMapping.getEmail());
        jobCandidateMapping.getCandidate().setId(jcmFromDb.getCandidate().getId());
        if (null == candidateEmailHistory) {
            if (!removeNotAvailableEmail(jobCandidateMapping)){
                log.info("Create new email history for email : {}, for candidateId : {}", jobCandidateMapping.getEmail(), jcmFromDb.getCandidate().getId());
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(jcmFromDb.getCandidate(), jobCandidateMapping.getEmail(), new Date(), loggedInUser));
            }
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
     * @param candidateRejectionValue which is id of rejection master data
     * @throws Exception
     */
    @Transactional
    //@Caching(evict = {@CacheEvict(cacheNames = "jcm"),@CacheEvict(cacheNames = "jcmHistory"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"),  @CacheEvict(cacheNames = "jcmCommDetails")})
    public void setStageForCandidates(List<Long> jcmList, String stage, Long candidateRejectionValue) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("Setting {} jcms to {} stage", jcmList, stage);

        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //check that all jcm are in the same jobId
        List<Long> jobId = jobCandidateMappingRepository.findDistinctJobIdByJcmID(jcmList);

        if(jobId.size() > 1)
            throw new ValidationException("Access to data not allowed", HttpStatus.UNAUTHORIZED);
        if(jobId.size() == 1) {
            Job job = jobRepository.getOne(jobId.get(0));
            validateLoggedInUser(loggedInUser, job);
        }
        //check that all the jcm are currently in the same stage
        if(!areCandidatesInSameStage(jcmList))
            throw new WebException("Select candidates that are all in Source stage", HttpStatus.UNPROCESSABLE_ENTITY);

        List<JcmHistory> jcmHistoryList = new ArrayList<>(jcmList.size());

        //check if new stage is rejected stage so update candidate rejection reason and rejected flag
        RejectionReasonMasterData reasonMasterData = null;
        if (IConstant.Stage.Reject.getValue().equals(stage)) {
            reasonMasterData = MasterDataBean.getInstance().getCandidateRejections().get(candidateRejectionValue);
            jobCandidateMappingRepository.setRejectedByAndOn(
                    jcmList,
                    loggedInUser.getDisplayName(),
                    new Date(),
                    (null != reasonMasterData)?reasonMasterData.getValue():null,
                    loggedInUser.getId(),
                    new Date()
            );
        }
        else {

            JobCandidateMapping jcmObject = jobCandidateMappingRepository.getOne(jcmList.get(0));
            Map<String, Long> jobStageIds = MasterDataBean.getInstance().getStageStepMasterMap();

            switch(stage){
                case "Sourcing":
                case "Interview":
                    jobCandidateMappingRepository.updateStageStepId(jcmList, jcmObject.getStage().getId(), jobStageIds.get(stage), loggedInUser.getId(), new Date());
                    break;
                case "Screening":
                    jobCandidateMappingRepository.setScreenedByAndOn(jcmList, loggedInUser.getDisplayName(), new Date(), jcmObject.getStage().getId(), jobStageIds.get(stage), loggedInUser.getId(), new Date());
                    break;
                case "Submitted":
                    jobCandidateMappingRepository.setSubmittedByAndOn(jcmList, loggedInUser.getDisplayName(), new Date(), jcmObject.getStage().getId(), jobStageIds.get(stage), loggedInUser.getId(), new Date());
                    break;
                case "Make Offer":
                    jobCandidateMappingRepository.setMakeOfferByAndOn(jcmList, loggedInUser.getDisplayName(), new Date(), jcmObject.getStage().getId(), jobStageIds.get(stage), loggedInUser.getId(), new Date());
                    break;
                case "Offer":
                    jobCandidateMappingRepository.setOfferByAndOn(jcmList, loggedInUser.getDisplayName(), new Date(), jcmObject.getStage().getId(), jobStageIds.get(stage), loggedInUser.getId(), new Date());
                    break;
                case "Hired":
                    jobCandidateMappingRepository.setHiredByAndOn(jcmList, loggedInUser.getDisplayName(), new Date(), jcmObject.getStage().getId(), jobStageIds.get(stage), loggedInUser.getId(), new Date());
                    break;
                default:
                    log.info("Stage not found {}", stage);
                    break;
            }
        }
        RejectionReasonMasterData finalReasonMasterData = reasonMasterData;
        jcmList.stream().forEach(jcm -> {
            JobCandidateMapping mappingObj = jobCandidateMappingRepository.getOne(jcm);
            jcmHistoryList.add(new JcmHistory(mappingObj, IConstant.Stage.Reject.getValue().equals(stage)?"Candidate Rejected from " + mappingObj.getStage().getStage() + " stage "+((null != finalReasonMasterData)? "for reason "+finalReasonMasterData.getLabel():""):"Candidate moved to " + stage, new Date(), loggedInUser, mappingObj.getStage(), false));

        });
        jcmHistoryRepository.saveAll(jcmHistoryList);
        jcmCommunicationDetailsRepository.setScreeningRejectionTimestampNull(jcmList);

        log.info("Completed moving candidates to {} stage in {} ms", stage, (System.currentTimeMillis() - startTime));
    }

    private boolean areCandidatesInSameStage(List<Long> jcmList) throws Exception{
        if(jobCandidateMappingRepository.countDistinctStageForJcmList(jcmList) != 1)
            return false;
        return true;
    }

    /**
     * Service method to get candidate history related to jcm
     *
     * @param jcmId
     * @return JcmHistory list
     */
    @Transactional
    //@Cacheable(cacheNames = "jcmHistory", key = "#jcmId")
    public List<JcmHistory> retrieveCandidateHistory(Long jcmId) {
        JobCandidateMapping jobCandidateMapping = jobCandidateMappingRepository.findById(jcmId).orElse(null);
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(null != jobCandidateMapping)
            validateLoggedInUser(loggedInUser, jobCandidateMapping.getJob());
        return jcmHistoryRepository.getJcmHistoryList(jobCandidateMapping.getJob().getCompanyId().getId(), jobCandidateMapping.getCandidate().getId());
    }

    /**
     *
     * @param comment comment add by  recruiter
     * @param jcmId for which jcm we need to create jcm history
     * @param callOutCome callOutCome if callOutCome is present then set in jcm history
     */
    @Transactional
    //@Caching(evict = {@CacheEvict(cacheNames = "jcm", key = "#jcmId"), @CacheEvict(cacheNames = "jcmHistory", key = "#jcmId")})
    public void addComment(String comment, Long jcmId, String callOutCome) {
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobCandidateMapping jobCandidateMapping = jobCandidateMappingRepository.findById(jcmId).orElse(null);
        if(null == jobCandidateMapping)
            throw new ValidationException("Job candidate mapping not found for jcmId : "+jcmId, HttpStatus.BAD_REQUEST);
        else
            validateLoggedInUser(loggedInUser, jobCandidateMapping.getJob());
        if(Util.isNotNull(comment)) comment = comment.trim();

        if( Util.isNotNull(callOutCome)) {
                List<MasterData> callOutcomeFromDb = (masterDataRepository.findByTypeAndValue("callOutCome",callOutCome));
                if(callOutcomeFromDb.size() == 0)
                    throw new ValidationException(callOutCome+" is not a valid callOutCome", HttpStatus.BAD_REQUEST);

                String valueToUse = callOutcomeFromDb.get(0).getValueToUSe();
                if(Util.isNull(comment) &&  "1".equals(valueToUse) ){
                    throw new ValidationException("Comment is mandatory for "+callOutCome, HttpStatus.BAD_REQUEST);
                }
            }


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
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findById(jcmId).orElse(null);
        if(null == jcmFromDb)
            throw new ValidationException("Job candidate mapping not found for jcmId : "+jcmId, HttpStatus.BAD_REQUEST);
        else
            validateLoggedInUser(loggedInUser, jcmFromDb.getJob());

        //Call private overloaded method upload resume which takes candidateCv and jobCandidateMapping as parameter
        uploadResume(candidateCv, jcmFromDb);
    }

    /**
     * Service method to upload resume against chatbot uuid
     *
     * @param chatbotUuid
     * @param candidateCv
     */
    @Transactional
    public ResponseEntity uploadResume(MultipartFile candidateCv, UUID chatbotUuid) throws Exception {
        String filePath = null;
        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findByChatbotUuid(chatbotUuid);
        if(null == jcmFromDb)
            throw new ValidationException("Job candidate mapping not found for Chatbot UUID : "+chatbotUuid, HttpStatus.BAD_REQUEST);

        //Call private overloaded method upload resume which takes candidateCv and jobCandidateMapping as parameter
        filePath = uploadResume(candidateCv, jcmFromDb);

        if(null == filePath){
            return ResponseEntity.badRequest().build();
        }
        else{
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

    /**
     * private function to upload file for a jcm
     * @param candidateCv
     * @param jcm
     */
    //@CacheEvict(cacheNames = "jcm", key = "#jcm.id")
    private String uploadResume(MultipartFile candidateCv, JobCandidateMapping jcm){
        String extension = Util.getFileExtension(candidateCv.getOriginalFilename()).toLowerCase();
        String filePath = null;
        if(extension.equals(IConstant.FILE_TYPE.rar) || extension.equals(IConstant.FILE_TYPE.zip) || !Arrays.asList(IConstant.cvUploadSupportedExtensions).contains(extension))
            throw new ValidationException(IErrorMessages.UNSUPPORTED_FILE_TYPE+" "+extension+", For JcmId : "+jcm.getId(), HttpStatus.BAD_REQUEST);

        try {
            filePath = StoreFileUtil.storeFile(candidateCv, jcm.getJob().getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(),jcm.getCandidate(),null);
            jcm.setCvFileType("."+extension);
            jobCandidateMappingRepository.save(jcm);
            cvParsingDetailsRepository.save(new CvParsingDetails(candidateCv.getOriginalFilename(), new Date(), null, jcm.getCandidate().getId(),jcm));
        }catch (Exception ex){
            log.error("{}, File name : {}, For jcmId : {}, Cause : {}", IErrorMessages.FAILED_TO_SAVE_FILE, candidateCv.getOriginalFilename(), jcm.getId(), ex.getMessage());
            throw new ValidationException(IErrorMessages.FAILED_TO_SAVE_FILE+" "+candidateCv.getOriginalFilename()+ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return filePath;
    }

    /**
     *Service to add candidate via career page, job portal, employee referral
     *
     * @param candidateSource from where we source the candidate
     * @param candidate candidate all info
     * @param jobShortCode In which job upload candidate
     * @param candidateCv candidate cv
     * @param employeeReferrer if candidate upload by employee referral then this model come
     * @return UploadResponseBean
     * @throws Exception
     */
    @Transactional
    public UploadResponseBean uploadCandidateByNoAuthCall(String candidateSource, Candidate candidate, String jobShortCode, MultipartFile candidateCv, EmployeeReferrer employeeReferrer, String otp) throws Exception {
        UploadResponseBean responseBean = null;
        CandidateDetails candidateDetails = null;
        boolean isOtpVerify = false;
        EmployeeReferrer referrerFromDb;


        if (null != otp && otp.length() == 4 && otp.matches("[0-9]+")) {
            if (null != employeeReferrer) {
                log.info("Verifying Otp: {} against email: {}", otp, employeeReferrer.getEmail());
                isOtpVerify = otpService.verifyOtp(employeeReferrer.getEmail(), Integer.parseInt(otp));
            }
            else {
                log.info("Verifying Otp: {} against mobile: {}", otp, candidate.getMobile());
                isOtpVerify = otpService.verifyOtp(candidate.getMobile(), Integer.parseInt(otp));
            }
        }
        else {
            throw new ValidationException("Invalid OTP : " + otp, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(isOtpVerify){
            log.info("OTP verification succeeded. Processing candidate against job for candidate : {}", candidate.getEmail());
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
            Job job = jobService.findJobByJobShortCode(jobShortCode);
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
            responseBean = uploadIndividualCandidate(Arrays.asList(candidate), job.getId(), false, Optional.ofNullable(userRepository.findByEmail(IConstant.SYSTEM_USER_EMAIL)), true);

            //Store candidate cv to repository location
            try{
                Long candidateId = null;
                if(null!=candidateCv) {
                    if (responseBean.getSuccessfulCandidates().size()>0) {
                        StoreFileUtil.storeFile(candidateCv, job.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(), responseBean.getSuccessfulCandidates().get(0), null);
                        candidateId = responseBean.getSuccessfulCandidates().get(0).getId();
                    }else {
                        StoreFileUtil.storeFile(candidateCv, job.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(), responseBean.getFailedCandidates().get(0), null);
                        candidateId = responseBean.getFailedCandidates().get(0).getId();
                    }

                    responseBean.setCvStatus(true);

                    //Create cvParsingDetail entry to get cv rating for this resume
                    if(null != candidateId)
                        cvParsingDetailsRepository.save(new CvParsingDetails(candidateCv.getOriginalFilename(), new Date(), null, candidateId,jobCandidateMappingRepository.findByJobIdAndCandidateId(job.getId(), candidateId)));

                }
            }catch(Exception e){
                log.error("Resume upload failed :"+e.getMessage());
                responseBean.setCvErrorMsg(e.getMessage());
            }
        }else{
            log.info("OTP verification failed for candidate : {}", candidate.getEmail());
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
    //@Cacheable(cacheNames = "candidateCountPerStage", key = "#jobId.toString().concat('-').concat(#stage)")
    public Map<String, Integer> getCandidateCountPerStatus(Long jobId, String stage) throws Exception {

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Job job = jobRepository.getOne(jobId);
        validateLoggedInUser(loggedInUser, job);

        Map<String, Integer> countMap = new HashMap<>();
        List<Object[]> candidateCountList = jobCandidateMappingRepository.getCandidateCountPerStage(jobId, stage);

        if(null != candidateCountList.get(0)[0]) {
            countMap.put(IConstant.ChatbotStatus.INVITED.getValue(), ((BigInteger)candidateCountList.get(0)[0]).intValue());
            countMap.put(IConstant.ChatbotStatus.NOT_INTERESTED.getValue(), ((BigInteger)candidateCountList.get(0)[1]).intValue());
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
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findByChatbotUuid(uuid);
        if (null == objFromDb)
            throw new WebException(IErrorMessages.UUID_NOT_FOUND + uuid, HttpStatus.UNPROCESSABLE_ENTITY);

        ChatbotResponseBean chatbotResponseBean = new ChatbotResponseBean();

        objFromDb.setJcmCommunicationDetails(jcmCommunicationDetailsRepository.findByJcmId(objFromDb.getId()));

        if(objFromDb.getJob().isCustomizedChatbot()){
            CustomizedChatbotPageContent customizedChatbotPageContent = customizedChatbotPageContentRepository.findByCompanyId(objFromDb.getJob().getCompanyId());
            //check customize chatbot flag true then send customized page data
            List<TechScreeningQuestion> techScreeningQuestions = techScreeningQuestionRepository.findByJobId(objFromDb.getJob().getId());
            Map<String, List<TechScreeningQuestion>> techScreeningQuestionsByCategory = techScreeningQuestions.stream().collect(groupingBy(TechScreeningQuestion::getQuestionCategory));
            objFromDb.setTechScreeningQuestions(techScreeningQuestionsByCategory);
            objFromDb.setUserScreeningQuestions(jobScreeningQuestionsRepository.findByUserScreeningQuestionIdIsNotNullAndJobId(objFromDb.getJob().getId()));
            if(null != customizedChatbotPageContent && !customizedChatbotPageContent.getPageInfo().isEmpty())
                chatbotResponseBean.getChatbotContent().putAll(customizedChatbotPageContent.getPageInfo());
        }
        List<JobSkillsAttributes> jobSkillsAttributeList = new ArrayList<>();
        if(null != objFromDb.getJob().getJobSkillsAttributesList()){
            objFromDb.getJob().getJobSkillsAttributesList().forEach(jobSkillsAttributes -> {
                if(null != jobSkillsAttributes.getAttribute() || (null != jobSkillsAttributes.getSkillId() && jobSkillsAttributes.isSelected()))
                    jobSkillsAttributeList.add(jobSkillsAttributes);
            });
        }
        objFromDb.getJob().setJobSkillsAttributesList(jobSkillsAttributeList);
        chatbotResponseBean.setJobCandidateMapping(objFromDb);

        return chatbotResponseBean;
    }

    /**
     * Service method to schedule interview for jcm list
     *
     * @param interviewDetailsFromReq interview details
     * @return List of schedule interview for list of jcm
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    //@Caching(evict ={@CacheEvict(cacheNames = "interviews"), @CacheEvict(cacheNames = "jcmHistory", key = "#interviewDetailsFromReq.jobCandidateMappingId"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public List<InterviewDetails> scheduleInterview(InterviewDetails interviewDetailsFromReq) {
        if (IConstant.InterviewMode.IN_PERSION.getValue().equals(interviewDetailsFromReq.getInterviewMode()) && null == interviewDetailsFromReq.getInterviewLocation())
            throw new ValidationException("Interview location must not be null", HttpStatus.BAD_REQUEST);

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (interviewDetailsFromReq.getInterviewDate().before(new Date())) {
            log.error("Interview date : {}  should be future date, Current date : {}", interviewDetailsFromReq.getInterviewDate(), new Date());
            throw new ValidationException("Interview date : " + Util.getDateWithTimezone(TimeZone.getTimeZone("IST"), interviewDetailsFromReq.getInterviewDate()) + " should be future date, Current date : " + Util.getDateWithTimezone(TimeZone.getTimeZone("IST"), new Date()), HttpStatus.BAD_REQUEST);
        }
        AtomicReference<Long> interviewDetailsFromDb = new AtomicReference<>();
        interviewDetailsFromReq.getJobCandidateMappingList().forEach(jobCandidateMapping -> {

//            Long companyId = companyRepository.findCompanyIdByJcmId(jobCandidateMapping.getId());
            JobCandidateMapping jcm = jobCandidateMappingRepository.findById(jobCandidateMapping.getId()).orElse(null);
            if(null != jcm)
                validateLoggedInUser(loggedInUser, jcm.getJob());
            if (null != interviewDetailsFromReq.getComments() && interviewDetailsFromReq.getComments().length() > IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.getValue())
                interviewDetailsFromReq.setComments(Util.truncateField(jobCandidateMapping.getCandidate(), IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.name(), IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.getValue(), interviewDetailsFromReq.getComments()));

            interviewDetailsFromDb.set(interviewDetailsRepository.save(new InterviewDetails(jobCandidateMapping.getId(), interviewDetailsFromReq.getInterviewType(), interviewDetailsFromReq.getInterviewMode(), interviewDetailsFromReq.getInterviewLocation(),
                    interviewDetailsFromReq.getInterviewDate(), interviewDetailsFromReq.getInterviewInstruction(), interviewDetailsFromReq.isSendJobDescription(), interviewDetailsFromReq.getComments(), UUID.randomUUID(), new Date(), loggedInUser)).getId());
            interviewDetailsFromReq.getInterviewerDetails().forEach(interviewerDetailsFromReq -> {
                InterviewerDetails interviewerDetails = interviewerDetailsRepository.save(new InterviewerDetails(interviewDetailsFromDb.get(), new Date(), loggedInUser, interviewerDetailsFromReq.getInterviewer()));
                HiringManagerWorkspace hiringManagerWorkspace = hiringManagerWorkspaceRepository.findByJcmIdAndUserId(jobCandidateMapping.getId(), interviewerDetailsFromReq.getInterviewer().getId());
                if(null == hiringManagerWorkspace)
                    hiringManagerWorkspaceRepository.save(new HiringManagerWorkspace(jobCandidateMapping.getId(), interviewerDetailsFromReq.getInterviewer().getId(), null, interviewerDetails.getId()));
                else
                    hiringManagerWorkspaceRepository.updateInterviewShareId(interviewerDetails.getId(), hiringManagerWorkspace.getId());
            });

            interviewDetailsFromReq.getInterviewIdList().add(interviewDetailsFromDb.get());
            jcmHistoryRepository.save(new JcmHistory(jobCandidateMapping, "Interview scheduled on " + Util.getDateWithTimezone(TimeZone.getTimeZone("IST"), interviewDetailsFromReq.getInterviewDate()) + ((null != interviewDetailsFromReq.getInterviewLocation()) ? (" ,address :" + interviewDetailsFromReq.getInterviewLocation().getAddress()) : " "), new Date(), loggedInUser, MasterDataBean.getInstance().getStageStepMap().get(MasterDataBean.getInstance().getStageStepMasterMap().get(IConstant.Stage.Interview.getValue())), false));
        });
        return interviewDetailsRepository.findByIdIn(interviewDetailsFromReq.getInterviewIdList());
    }

    /**
     * Service method to cancel interview
     *
     * @param cancellationDetails interview cancellation details
     * @return Boolean value interview cancel or not
     */
    @Transactional
    //@Caching(evict ={@CacheEvict(cacheNames = "interviews"), @CacheEvict(cacheNames = "jcmHistory", key = "#cancellationDetails.jobCandidateMappingId"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public void cancelInterview(InterviewDetails cancellationDetails) {
        long startTime = System.currentTimeMillis();
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InterviewDetails interviewDetailsFromDb = interviewDetailsRepository.findById(cancellationDetails.getId()).orElse(null);
        if(null == interviewDetailsFromDb)
            throw new ValidationException("Interview details not found for id : "+cancellationDetails.getId(), HttpStatus.BAD_REQUEST);

        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findById(interviewDetailsFromDb.getJobCandidateMappingId()).orElse(null);

        if(null == jcmFromDb)
            throw new ValidationException("Job Candidate Mapping not found for id : "+interviewDetailsFromDb.getJobCandidateMappingId(), HttpStatus.BAD_REQUEST);

        validateLoggedInUser(loggedInUser, jcmFromDb.getJob());

        if(null == cancellationDetails.getCancellationReason())
            throw new ValidationException("For Interview cancel, cancellation reason should not be null : "+cancellationDetails.getId(), HttpStatus.BAD_REQUEST);

        interviewDetailsFromDb.setCancelled(true);
        interviewDetailsFromDb.setUpdatedBy(loggedInUser);
        interviewDetailsFromDb.setUpdatedOn(new Date());
        if(null != cancellationDetails.getCancellationComments() && cancellationDetails.getCancellationComments().length()>IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.getValue())
            interviewDetailsFromDb.setCancellationComments(Util.truncateField(jcmFromDb.getCandidate(), IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.name(), IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.getValue(), cancellationDetails.getCancellationComments()));
        else
            interviewDetailsFromDb.setCancellationComments(cancellationDetails.getCancellationComments());

        interviewDetailsFromDb.setCancellationReason(cancellationDetails.getCancellationReason());
        interviewDetailsRepository.save(interviewDetailsFromDb);
        jcmHistoryRepository.save(new JcmHistory(jcmFromDb, "Interview cancelled on :"+ Util.getDateWithTimezone(TimeZone.getTimeZone("IST"),new Date())+((null != cancellationDetails.getCancellationComments()) ?(" ~ "+cancellationDetails.getCancellationComments()):""), new Date(), loggedInUser, MasterDataBean.getInstance().getStageStepMap().get(MasterDataBean.getInstance().getStageStepMasterMap().get(IConstant.Stage.Interview.getValue())), false));
        log.info("Interview cancelled in " + (System.currentTimeMillis()-startTime) + "ms.");
    }

    /**
     * Service method to mark show noShow for interview
     *
     * @param showNoShowDetails interview showNoShowDetails
     * @return Boolean value is interview mark showNoShow
     */
    @Transactional
    //@Caching(evict ={@CacheEvict(cacheNames = "interviews"), @CacheEvict(cacheNames = "jcmHistory", key = "#showNoShowDetails.jobCandidateMappingId"),@CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public void markShowNoShow(InterviewDetails showNoShowDetails) {
        long startTime = System.currentTimeMillis();
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InterviewDetails interviewDetailsFromDb = interviewDetailsRepository.findById(showNoShowDetails.getId()).orElse(null);
        if(null == interviewDetailsFromDb)
            throw new ValidationException("Interview details not found for id : "+showNoShowDetails.getId(), HttpStatus.BAD_REQUEST);

        if(interviewDetailsFromDb.getInterviewDate().after(new Date())){
            log.error("Interview date : {} should be older or equal to current date : {}", interviewDetailsFromDb.getInterviewDate(),new Date());
            throw new ValidationException("Interview date : "+Util.getDateWithTimezone(TimeZone.getTimeZone("IST"), interviewDetailsFromDb.getInterviewDate())+ " should be older or equal to current date : "+Util.getDateWithTimezone(TimeZone.getTimeZone("IST"), new Date()), HttpStatus.BAD_REQUEST);
        }

        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findById(interviewDetailsFromDb.getJobCandidateMappingId()).orElse(null);
        if(null == jcmFromDb)
            throw new ValidationException("Job Candidate Mapping not found for id : "+interviewDetailsFromDb.getJobCandidateMappingId(), HttpStatus.BAD_REQUEST);

        validateLoggedInUser(loggedInUser, jcmFromDb.getJob());

        if(!showNoShowDetails.isShowNoShow() && null == showNoShowDetails.getNoShowReason())
            throw new ValidationException("Interview NoShowReason should not be null : "+showNoShowDetails.getId(), HttpStatus.BAD_REQUEST);

        if(null != showNoShowDetails.getShowNoShowComments() && showNoShowDetails.getShowNoShowComments().length()>IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.getValue())
            interviewDetailsFromDb.setShowNoShowComments(Util.truncateField(jcmFromDb.getCandidate(), IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.name(), IConstant.MAX_FIELD_LENGTHS.INTERVIEW_COMMENTS.getValue(), showNoShowDetails.getShowNoShowComments()));
        else
            interviewDetailsFromDb.setShowNoShowComments(showNoShowDetails.getShowNoShowComments());

        interviewDetailsFromDb.setShowNoShow(showNoShowDetails.isShowNoShow());
        interviewDetailsFromDb.setUpdatedBy(loggedInUser);
        interviewDetailsFromDb.setUpdatedOn(new Date());
        interviewDetailsFromDb.setNoShowReason(showNoShowDetails.getNoShowReason());
        interviewDetailsRepository.save(interviewDetailsFromDb);
        if(!showNoShowDetails.isShowNoShow())
            jcmHistoryRepository.save(new JcmHistory(jcmFromDb, "Interview no show("+MasterDataBean.getInstance().getNoShowReasons().get(showNoShowDetails.getNoShowReason().getId())+") : "+ Util.getDateWithTimezone(TimeZone.getTimeZone("IST"),new Date())+((null != showNoShowDetails.getShowNoShowComments())?(" ~ "+showNoShowDetails.getShowNoShowComments()):""), new Date(), loggedInUser, MasterDataBean.getInstance().getStageStepMap().get(MasterDataBean.getInstance().getStageStepMasterMap().get(IConstant.Stage.Interview.getValue())), false));
        else
            jcmHistoryRepository.save(new JcmHistory(jcmFromDb, "Interview show on : "+ Util.getDateWithTimezone(TimeZone.getTimeZone("IST"),new Date())+((null != showNoShowDetails.getShowNoShowComments())?(" ~ "+showNoShowDetails.getShowNoShowComments()):""), new Date(), loggedInUser, MasterDataBean.getInstance().getStageStepMap().get(MasterDataBean.getInstance().getStageStepMasterMap().get(IConstant.Stage.Interview.getValue())), false));

        log.info("Interview marked Show NoShow in " + (System.currentTimeMillis()-startTime) + "ms.");
    }

    /**
     * Service method to set candidate confirmation for interview
     *
     *@param confirmationDetails interviewDetails model for confirmation
     */
    @Transactional
    //@Caching(evict ={@CacheEvict(cacheNames = "interviews"), @CacheEvict(cacheNames = "jcmHistory", key = "#confirmationDetails.jobCandidateMappingId"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public void candidateConfirmationForInterview(InterviewDetails confirmationDetails) {
        InterviewDetails interviewDetailsFromDb = interviewDetailsRepository.findByInterviewReferenceId(confirmationDetails.getInterviewReferenceId());
        if(null == interviewDetailsFromDb)
            throw new ValidationException("Interview details not found for refId : "+confirmationDetails.getInterviewReferenceId(), HttpStatus.BAD_REQUEST);

        interviewDetailsFromDb.setCandidateConfirmationValue(MasterDataBean.getInstance().getInterviewConfirmation().get(confirmationDetails.getConfirmationText()));

        if(confirmationDetails.getConfirmationText().contains("Yes"))
            interviewDetailsFromDb.setCandidateConfirmation(true);

        interviewDetailsFromDb.setCandidateConfirmationTime(new Date());
        interviewDetailsRepository.save(interviewDetailsFromDb);
        jcmHistoryRepository.save(new JcmHistory(jobCandidateMappingRepository.findById(interviewDetailsFromDb.getJobCandidateMappingId()).orElse(null), "Candidate response for interview on "+ Util.getDateWithTimezone(TimeZone.getTimeZone("IST"),interviewDetailsFromDb.getInterviewDate())+" : "+confirmationDetails.getConfirmationText()+" "+Util.getDateWithTimezone(TimeZone.getTimeZone("IST"), new Date()), new Date(), null, MasterDataBean.getInstance().getStageStepMap().get(MasterDataBean.getInstance().getStageStepMasterMap().get(IConstant.Stage.Interview.getValue())), true));
    }

    /**
     * Service method to get address data(area, city, state) for live job's from job location
     *
     * @param companyShortName first find company then find jobList by companyId
     * @return address string set(eg. "Baner, Pune, Maharashtra")
     */
    @Transactional(readOnly = true)
    //@Cacheable(cacheNames = "companyAddress", key = "#companyShortName")
    public Set<String> getLiveJobAddressStringSetByCompanyId(String companyShortName) {
        log.info("Inside getLiveJobAddressStringSetByCompanyId for company short name : {}",companyShortName);
        List<Company> companyList = new ArrayList<>();
        Set<String> companyAddressSet = new HashSet<>();
        Company company = companyRepository.findByShortNameIgnoreCase(companyShortName);
        if(null == company)
            throw new WebException("Company not found for company short name : "+companyShortName, HttpStatus.UNPROCESSABLE_ENTITY);

        List<Company> companyListFromDB = companyRepository.findByRecruitmentAgencyId(company.getId());
        if(companyListFromDB.size()>0)
            companyList.addAll(companyListFromDB);
        else
            companyList.add(company);

        List<Job> jobList = jobRepository.findByCompanyIdInAndStatus(companyList, IConstant.JobStatus.PUBLISHED.getValue());

        log.info("Found {} job{} for {} compan{}", jobList.size(),(jobList.size()>1)?"'s":"", companyList.size(), (companyList.size()>1)?"ies":"y");

        jobList.forEach(job -> {
            if(null != job.getJobLocation()){
                StringBuffer addressString = new StringBuffer();
                if(null!=job.getJobLocation().getArea())
                    addressString.append(job.getJobLocation().getArea()).append(", ");
                if(null!=job.getJobLocation().getCity())
                    addressString.append(job.getJobLocation().getCity()).append(", ");
                if(null!=job.getJobLocation().getState())
                    addressString.append(job.getJobLocation().getState());
                if(Util.isNotNull(addressString.toString()))
                    companyAddressSet.add(addressString.toString());
            }
        });
        return companyAddressSet;
    }

    @Transactional
    private void handleErrorRecords(List<Candidate> failedCandidates, List<JobCandidateMapping> failedJcm, String asyncOperation, User loggedInUser, Long jobId, String fileName) {
        List<AsyncOperationsErrorRecords> recordsToSave = null;

        //call constructor for failed candidate upload from file
        if(null != failedCandidates && failedCandidates.size() > 0) {
            recordsToSave = new ArrayList<>(failedCandidates.size());
            for(Candidate candidate: failedCandidates) {
                log.error("Error while upload candidate for candidateEmail : {} fileName : {}, jobId : {}, loggedInUser : {}, errorMessage : {}",candidate.getEmail(), fileName, jobId, loggedInUser.getId(), candidate.getUploadErrorMessage());
                recordsToSave.add(new AsyncOperationsErrorRecords(jobId, candidate.getFirstName(), candidate.getLastName(), candidate.getEmail(), candidate.getMobile(), candidate.getUploadErrorMessage(), asyncOperation, loggedInUser, new Date(), fileName));
            };
        }
        //call constructor for failed jcms for invite candidates flow
        else if (null != failedJcm && failedJcm.size() > 0) {
            recordsToSave = new ArrayList<>(failedJcm.size());
            for(JobCandidateMapping jcm : failedJcm) {
                log.error("Error while invite candidate for jcmId : {} fileName : {}, jobId : {}, loggedInUser : {}, errorMessage : {}", jcm.getId(),fileName, jobId, loggedInUser.getId(), jcm.getInviteErrorMessage());
                recordsToSave.add(new AsyncOperationsErrorRecords(jobId, jcm, jcm.getInviteErrorMessage(), asyncOperation, loggedInUser, new Date()));
            }
        }

        //save records to db
        if(null != recordsToSave && recordsToSave.size() > 0) {
            asyncOperationsErrorRecordsRepository.saveAll(recordsToSave);
        }
    }

    /**
     *Service method to get All future Interviews for a particular company
     *
     * @param companyId id of company whose future interview List is to be fetched
     * @return List of future interviews details for the particular company
     */
    //@Cacheable(cacheNames = "interviews", key = "#companyId")
    public List<InterviewsResponseBean> getInterviewsForCompany(Long companyId){
        log.info("Inside getInterviewsForCompany method for company Id: {}", companyId);
        long startTime = System.currentTimeMillis();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long validCompanyId = validateCompanyId(loggedInUser, companyId);
        List<InterviewsResponseBean> response =  customQueryExecutor.getInterviewDetailsByCompany(validCompanyId, loggedInUser);
        log.info("Completed execution of getInterviewForCompany for companyId {} in {} ms", companyId, System.currentTimeMillis() - startTime);
        return response;
    }

    @Transactional(propagation = Propagation.REQUIRED)
   // @Caching(evict = {@CacheEvict(cacheNames = "jcm", key = "#jobCandidateMapping.id"), @CacheEvict(cacheNames = "singleJobView"), @CacheEvict(cacheNames = "singleJobViewByStatus"), @CacheEvict(cacheNames = "exportData"), @CacheEvict(cacheNames = "harvesterCandidateProfile")})
    public void updateCandidateResponse(JobCandidateMapping jobCandidateMapping, Map<String, String> candidateResponse) throws Exception {
        CandidateDetails candidateDetails = new CandidateDetails();
        CandidateCompanyDetails companyDetails = new CandidateCompanyDetails();
        candidateResponse.forEach((key, value) -> {
            JobScreeningQuestions jobScreeningQuestions = jobScreeningQuestionsRepository.findById(Long.parseLong(key)).get();
            try {
                if (null != jobScreeningQuestions.getMasterScreeningQuestionId()) {
                    ScreeningQuestions screeningQuestions = screeningQuestionsRepository.findById(jobScreeningQuestions.getMasterScreeningQuestionId().getId()).get();
                    MasterData masterData = screeningQuestions.getQuestionCategory();
                    String response = value;
                    log.info("Candidate Response: {} for Question: {}", response, screeningQuestions.getQuestion());
                    if (masterData.getValue().equals("Current Company"))
                        companyDetails.setCompanyName(response.trim());
                    else if (masterData.getValue().equals("Job Title"))
                        companyDetails.setDesignation(response.trim());
                    else if (masterData.getValue().equals("Notice Period")) {
                        MasterData md = MasterDataBean.getInstance().getNoticePeriodMapping().get(response);
                        if(response.contains("immediately"))
                            md = MasterDataBean.getInstance().getNoticePeriodMapping().get("0 Days");
                        if(null == md)
                            md = MasterDataBean.getInstance().getNoticePeriodMapping().get("Others");
                        companyDetails.setNoticePeriodInDb(md);
                    }
                    else if (masterData.getValue().equals("Current Salary"))
                        companyDetails.setSalary(response);
                    else if (masterData.getValue().equals("Total Experience")) {
                        //As all the other experience options are in years only two options are in months so in-order to store the candidate experience in months 0.5 is used
                        if(response.contains("months"))
                            response="0.5";
                            //For any other options finding for the first digit or digits and saving it two db (the lowest range will be saved to db eg: 1-3 years 1 will be saved
                        else {
                            Pattern pattern = Pattern.compile("\\d+");
                            Matcher match = pattern.matcher(response);
                            if (match.find())
                                response = match.group(0);
                            else
                                response = "";
                        }
                        if(Util.isNotNull(response))
                            candidateDetails.setTotalExperience(Double.parseDouble(response));
                    }
                    else if (masterData.getValue().equals("Location"))
                        candidateDetails.setLocation(response);
                    else if (masterData.getValue().equals("Expected Salary"))
                        jobCandidateMapping.setExpectedCtc(Double.parseDouble(response));
                    else if (masterData.getValue().equals("Education")) {
                        //Find candidate education detail by degree as well. This code will not handle multiple degrees
                        CandidateEducationDetails candidateEducationDetails = candidateEducationDetailsRepository.findByCandidateIdAndDegree(jobCandidateMapping.getCandidate().getId(), response);
                        if (candidateEducationDetails == null) {
                            CandidateEducationDetails educationDetails = new CandidateEducationDetails();
                            educationDetails.setCandidateId(jobCandidateMapping.getCandidate().getId());
                            educationDetails.setDegree(response);
                            candidateEducationDetailsRepository.save(educationDetails);
                        } else {
                            candidateEducationDetails.setDegree(response);
                            candidateEducationDetailsRepository.save(candidateEducationDetails);
                        }
                    }
                }
            } catch (Exception e) {
                log.info("Error while Updating Candidate Response :: {}", e.getMessage());
            }
        });
        if(null != companyDetails && null != companyDetails.getCompanyName()){
            CandidateCompanyDetails candidateCompanyDetails = candidateCompanyDetailsRepository.findByCandidateIdAndCompanyName(jobCandidateMapping.getCandidate().getId(), companyDetails.getCompanyName());
            if (null == candidateCompanyDetails) {
                if(Util.isNotNull(companyDetails.getCompanyName())) {
                    companyDetails.setCandidateId(jobCandidateMapping.getCandidate().getId());
                    candidateCompanyDetails = companyDetails;
                }
            } else {
                candidateCompanyDetails.setCompanyName(companyDetails.getCompanyName());
                candidateCompanyDetails.setDesignation(companyDetails.getDesignation());
                candidateCompanyDetails.setNoticePeriodInDb(companyDetails.getNoticePeriodInDb());
                if (Util.isNotNull(companyDetails.getSalary()))
                    candidateCompanyDetails.setSalary(companyDetails.getSalary());
            }
            if(null != jobCandidateMapping.getCandidate().getCandidateCompanyDetails() && null != candidateCompanyDetails && jobCandidateMapping.getCandidate().getCandidateCompanyDetails().size()!=0){
                reStructureCompanyList(jobCandidateMapping, candidateCompanyDetails);
            }else
                candidateCompanyDetailsRepository.save(candidateCompanyDetails);
        }
        CandidateDetails details = candidateDetailsRepository.findByCandidateId(jobCandidateMapping.getCandidate());
        if (null == details){
            candidateDetails.setCandidateId(jobCandidateMapping.getCandidate());
            candidateDetailsRepository.save(candidateDetails);
        }
        else{
            details.setLocation(candidateDetails.getLocation());
            if(null != candidateDetails.getTotalExperience())
                details.setTotalExperience(candidateDetails.getTotalExperience());
            candidateDetailsRepository.save(details);
        }
        jobCandidateMappingRepository.save(jobCandidateMapping);
    }

    public void createExistingCandidateOnSearchEngine(){
        long apiCallStartTime = System.currentTimeMillis();
        List<JobCandidateMapping> jobCandidateMappingList = jobCandidateMappingRepository.findJcmNotInSearchEngine();
        jobCandidateMappingList.forEach(jobCandidateMapping -> {
            log.info("Candidate : {} creating on search engine", jobCandidateMapping.getCandidate().getId());
            candidateService.createCandidateOnSearchEngine(jobCandidateMapping.getCandidate(), jobCandidateMapping,null);
        });
        log.info("Time taken to creating existing candidate on search engine in : {}ms.", apiCallStartTime-System.currentTimeMillis());
    }

    /**
     * Service method to get candidate last updated info
     * @param candidateId candidate id for we want data
     * @param companyId candidate related to which company
     * @return JobCandidateMapping - last updated JCM details
     */
    @Transactional
   // @Cacheable(cacheNames = "harvesterCandidateProfile", key = "#candidateId.concat('-').concat(#companyId)")
    public JobCandidateMapping getCandidateProfileForHarvester(Long candidateId, Long companyId) {
        long startTime = System.currentTimeMillis();
        log.info("Get candidate profile based on last updated jcm for candidateId : {}, companyId : {}", candidateId, companyId);

        //LoggedIn user
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Validate loggedInUser and company id
        validateloggedInUser(loggedInUser, companyId);

        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.getLastUpdatedJCMForCandidate(candidateId, companyId);
        if(null != jcmFromDb)
            jcmFromDb = setJcmForCandidateProfile(jcmFromDb);
        else
            log.error("Jcm record not found for candidateId : {}, companyId : {}",candidateId, companyId);
        log.info("Time taken to fetch candidate profile based on last updated jcm in : {}ms.", startTime-System.currentTimeMillis());
        return jcmFromDb;
    }

    /**
     * Service method to add candidate by Harvester using candidate id and job id
     *
     * @param candidateIdList candidate id to upload candidates
     * @param jobId the job for which the candidate is to be added
     * @return the status of upload operation
     * @throws Exception
     */
    @Transactional
    //@CacheEvict(cacheNames = "harvesterCandidateProfile")
    public List<UploadResponseBean> uploadIndividualCandidateByHarvester(List<Long> candidateIdList, Long jobId) throws Exception {
        log.info("Upload candidate for job : {} and candidateIdList : {}", jobId, candidateIdList);
        long startTime = System.currentTimeMillis();

        //LoggedIn user
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //validate job
        Job jobFromDb = jobRepository.findById(jobId).orElse(null);
        if(null == jobFromDb)
            throw new WebException("Job not found for jobId : "+jobId, HttpStatus.UNPROCESSABLE_ENTITY);

        //Validate loggedIn user and job
        validateLoggedInUser(loggedInUser, jobFromDb);

        List<UploadResponseBean> responseBeanList = new ArrayList<>();
        candidateIdList.forEach(candidateId->{
            //validate candidate
            Candidate candidate = candidateRepository.findById(candidateId).orElse(null);
            if(null == candidate)
                throw new WebException("Candidate not found for candidateId : "+candidateId, HttpStatus.UNPROCESSABLE_ENTITY);
            candidate.setCandidateSource(IConstant.CandidateSource.LBHarvester.name());

            JobCandidateMapping lastUpdatedJcm = jobCandidateMappingRepository.getLastUpdatedJCMForCandidate(candidateId, loggedInUser.getCompany().getId());

            //Validate
            if(null == lastUpdatedJcm){
                log.info("Candidate : {} not found for company : {}",candidateId, loggedInUser.getCompany().getId());
                throw new WebException("Candidate : "+candidateId+" not found for company : "+loggedInUser.getCompany().getId(), HttpStatus.UNPROCESSABLE_ENTITY);
            }

            candidate.setEmail(lastUpdatedJcm.getEmail());
            candidate.setMobile(lastUpdatedJcm.getMobile());
            candidate.setCandidateName(lastUpdatedJcm.getCandidateFirstName()+" "+lastUpdatedJcm.getCandidateLastName());
            StringBuffer cvLocation = new StringBuffer();
            MultipartFile candidateCv = null;
            if(null != lastUpdatedJcm && null != lastUpdatedJcm.getCvFileType()){
                cvLocation.append(environment.getProperty(IConstant.REPO_LOCATION)).append(IConstant.CANDIDATE_CV).append(File.separator).append(lastUpdatedJcm.getJob().getId()).append(File.separator).append(lastUpdatedJcm.getCandidate().getId()).append(lastUpdatedJcm.getCvFileType());
                File file = new File(cvLocation.toString());
                FileInputStream input = null;
                try {
                    input = new FileInputStream(file);
                    candidateCv = new MockMultipartFile("file",file.getName(), "text/plain", IOUtils.toByteArray(input));
                } catch (Exception e) {
                    log.error(Util.getStackTrace(e));
                }
            }
            //upload candidate
            UploadResponseBean responseBean = null;
            try {
                responseBean = uploadCandidateFromPlugin(candidate, jobId, candidateCv,  Optional.of(loggedInUser));
            } catch (Exception e) {
                log.error(Util.getStackTrace(e));
                if(null == responseBean)
                    responseBean = new UploadResponseBean();
                responseBean.setErrorMessage(e.getMessage());
            }
            responseBeanList.add(responseBean);
        });
        log.info("Time taken to upload candidates by harvester in : {}ms.", startTime-System.currentTimeMillis());
        return responseBeanList;
    }

    @Override
    public void saveOfferDetails(JcmOfferDetails jcmOfferDetails) {
        log.info("inside save offerDetails");
        String error;
        Long jcmId = jcmOfferDetails.getJcmId().getId();
        JobCandidateMapping jcmFromDb = jobCandidateMappingRepository.findById(jcmId).orElse(null);

        if(null == jcmFromDb){
            error = "jcmId : "+ jcmId+" does not exist";
            log.error(error);
            throw new WebException(error,HttpStatus.BAD_REQUEST);
        }


        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        validateLoggedInUser(loggedInUser, jcmFromDb.getJob());


        JcmOfferDetails jcmOfferFromDb = jcmOfferDetailsRepository.findByJcmId(jcmFromDb);
        if(null != jcmOfferFromDb)
            jcmOfferDetails.setId(jcmOfferFromDb.getId());

        try {
            jcmOfferDetailsRepository.save(jcmOfferDetails);
        }catch (Exception e){
            log.error("Offer compensation not valid for jcmId : {}",jcmOfferDetails.getJcmId());
            throw new WebException("Offer compensation not valid for jcmId : "+jcmOfferDetails.getJcmId(), HttpStatus.BAD_REQUEST);
        }
        jcmHistoryRepository.save(new JcmHistory(jcmFromDb,"Offer details added",new Date(),loggedInUser,jcmFromDb.getStage(),false));
        log.info("offer details saved successfully!");
    }
}
