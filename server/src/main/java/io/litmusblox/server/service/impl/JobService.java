/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.exportData.ExportData;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.*;
import io.litmusblox.server.service.impl.ml.RolePredictionBean;
import io.litmusblox.server.utils.RestClient;
import io.litmusblox.server.utils.SentryUtil;
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

import javax.annotation.Resource;
import javax.naming.OperationNotSupportedException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Implementation class for JobService
 *
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 9:47 AM
 * Class Name : JobService
 * Project Name : server
 */
@Service
@Log4j2

public class JobService implements IJobService {

    @Resource
    JobRepository jobRepository;

    @Resource
    CompanyRepository companyRepository;

    @Resource
    UserRepository userRepository;

    @Resource
    JobScreeningQuestionsRepository jobScreeningQuestionsRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    TempSkillsRepository tempSkillsRepository;

    @Resource
    JobKeySkillsRepository jobKeySkillsRepository;

    @Resource
    JobCapabilitiesRepository jobCapabilitiesRepository;

    @Resource
    SkillMasterRepository skillMasterRepository;

    @Resource
    CompanyAddressRepository companyAddressRepository;

    @Resource
    CompanyBuRepository companyBuRepository;

    @Resource
    JobHiringTeamRepository jobHiringTeamRepository;

    @Resource
    JcmCommunicationDetailsRepository jcmCommunicationDetailsRepository;

    @Resource
    WeightageCutoffByCompanyMappingRepository weightageCutoffByCompanyMappingRepository;

    @Resource
    WeightageCutoffMappingRepository weightageCutoffMappingRepository;

    @Resource
    JcmProfileSharingDetailsRepository jcmProfileSharingDetailsRepository;

    @Resource
    JobHistoryRepository jobHistoryRepository;

    @Resource
    JobCapabilityStarRatingMappingRepository jobCapabilityStarRatingMappingRepository;

    @Resource
    CvRatingRepository cvRatingRepository;

    @Resource
    ExportFormatMasterRepository exportFormatMasterRepository;

    @Resource
    ExportFormatDetailRepository exportFormatDetailRepository;

    @Autowired
    ICompanyService companyService;

    @PersistenceContext
    EntityManager em;

    @Autowired
    CustomQueryExecutor customQueryExecutor;

    @Autowired
    Environment environment;

    @Value("${mlApiUrl}")
    private String mlUrl;

    @Value("${scoringEngineBaseUrl}")
    private String scoringEngineBaseUrl;

    @Value("${scoringEngineAddJobUrlSuffix}")
    private String scoringEngineAddJobUrlSuffix;

    @Transactional
    public Job addJob(Job job, String pageName) throws Exception {//add job with respective pageName

        if(null != job.getStatus() && IConstant.JobStatus.ARCHIVED.equals(job.getStatus()))
            throw new ValidationException("Can't edit job because job in Archived state", HttpStatus.UNPROCESSABLE_ENTITY);

        User recruiter = null;
        User hiringManager = null;
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Received request to add job for page " + pageName + " from user: " + loggedInUser.getEmail());
        long startTime = System.currentTimeMillis();

        if (IConstant.AddJobPages.capabilities.name().equalsIgnoreCase(pageName)) {
            //delete all existing records in the job_capability_star_rating_mapping table for the current job
            jobCapabilityStarRatingMappingRepository.deleteByJobId(job.getId());
            jobCapabilityStarRatingMappingRepository.flush();
        }
        Job oldJob = null;

        if (null != job.getId()) {
            //get handle to existing job object
            oldJob = jobRepository.findById(job.getId()).orElse(null);
           // oldJob = tempJobObj.isPresent() ? tempJobObj.get() : null;
        }

        //set recruiter
        if(null != job.getRecruiter() && null != job.getRecruiter().getId()){
            recruiter =  userRepository.findById(job.getRecruiter().getId()).orElse(null);
            if(null != recruiter)
                job.setRecruiter(recruiter);
        }

        //set hiringManager
        if(null != job.getHiringManager() && null != job.getHiringManager().getId()){
            hiringManager =  userRepository.findById(job.getHiringManager().getId()).orElse(null);
            if(null != hiringManager)
                job.setHiringManager(hiringManager);
        }

        switch (IConstant.AddJobPages.valueOf(pageName)) {
            case overview:
                addJobOverview(job, oldJob, loggedInUser);
                break;
            case screeningQuestions:
                addJobScreeningQuestions(job, oldJob, loggedInUser);
                break;
            case keySkills:
                addJobKeySkills(job, oldJob, loggedInUser);
                break;
            case capabilities:
                addJobCapabilities(job, oldJob, loggedInUser);
                break;
            case jobDetail:
                addJobDetail(job, oldJob, loggedInUser);
                break;
            case hiringTeam:
                addJobHiringTeam(job, oldJob, loggedInUser);
                job.setJobHiringTeamList(jobHiringTeamRepository.findByJobId(job.getId()));
                break;
            case expertise:
                addJobExpertise(job, oldJob);
                break;
            default:
                throw new OperationNotSupportedException("Unknown page: " + pageName);
        }

        if(null != oldJob && null != oldJob.getJobHiringTeamList() && !IConstant.AddJobPages.hiringTeam.name().equals(pageName))
            job.setJobHiringTeamList(oldJob.getJobHiringTeamList());

        populateDataForNextPage(job, pageName);

        log.info("Completed processing request to add job in " + (System.currentTimeMillis() - startTime) + "ms");
        return job;
    }

    private void populateDataForNextPage(Job job, String pageName) throws Exception {
        int currentPageIndex = MasterDataBean.getInstance().getJobPageNamesInOrder().indexOf(pageName);
        if (currentPageIndex != -1) {
            if(MasterDataBean.getInstance().getJobPageNamesInOrder().get(currentPageIndex).equals(IConstant.AddJobPages.overview.name())) {
                log.info("Setting jobkeyskills in job object");
                job.setJobKeySkillsList(jobKeySkillsRepository.findByJobId(job.getId()));
                log.info("Setting capabilities in job object");
                job.setJobCapabilityList(jobCapabilitiesRepository.findByJobId(job.getId()));
            }
            switch (IConstant.AddJobPages.valueOf(MasterDataBean.getInstance().getJobPageNamesInOrder().get(currentPageIndex+1))) {
                case keySkills:
                    //populate key skills for the job
                    job.setJobKeySkillsList(jobKeySkillsRepository.findByJobId(job.getId()));
                    break;
                case capabilities:
                    //populate the capabilities for the job
                    job.setJobCapabilityList(jobCapabilitiesRepository.findByJobId(job.getId()));
                    break;
                /*case hiringTeam:
                    job.setJobHiringTeamList(jobHiringTeamRepository.findByJobId(job.getId()));
                    break;*/
                default:
                    break;
            }
        }
    }


    /**
     * Fetch details of currently logged in user and
     * query the repository to find the list of all jobs
     *
     * @param archived flag indicating if only archived jobs need to be fetched
     * @param companyId id of the company for which jobs have to be found
     * @param jobStatus depend on status fetch job list
     * @return List of jobs created by the logged in user
     */
    @Transactional(readOnly = true)
    public JobWorspaceResponseBean findAllJobsForUser(boolean archived, Long companyId, String jobStatus) throws Exception {

        log.info("Received request to request to find all jobs for user for archived = " + archived);
        long startTime = System.currentTimeMillis();

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobWorspaceResponseBean responseBean = new JobWorspaceResponseBean();
        String msg = loggedInUser.getEmail() + ", " + companyId + ": ";

        List<Company> companyList = new ArrayList<>();
        switch(loggedInUser.getRole()) {
            case IConstant.UserRole.Names.CLIENT_ADMIN:
                log.info(msg + "Request from Client Admin, all jobs for the company will be returned");
                companyList = new ArrayList<>();
                companyList.add(loggedInUser.getCompany());
                jobsForCompany(responseBean, archived, companyList, jobStatus);
                break;
            case IConstant.UserRole.Names.RECRUITMENT_AGENCY:
                if (null == companyId)
                    throw new ValidationException("Missing Company id in request", HttpStatus.UNPROCESSABLE_ENTITY);
                Company company = companyRepository.findById(companyId).orElse(null);
                if(null == company)
                    throw new ValidationException("Recruitment agency not found for id : "+companyId, HttpStatus.UNPROCESSABLE_ENTITY);
                List<Company> companies = companyRepository.findByRecruitmentAgencyId(company.getId());
                jobsForCompany(responseBean, archived, companies, jobStatus);
                break;
            case IConstant.UserRole.Names.SUPER_ADMIN:
                if (null == companyId)
                    throw new ValidationException("Missing Company id in request", HttpStatus.UNPROCESSABLE_ENTITY);
                log.info(msg + "Request from Super Admin for jobs of Company");
                Company companyObjToUse = companyRepository.findById(companyId).orElse(null);
                companyList.add(companyObjToUse);
                if (null == companyObjToUse)
                    throw new ValidationException("Company not found : " + companyId, HttpStatus.UNPROCESSABLE_ENTITY);
                jobsForCompany(responseBean, archived, companyList, jobStatus);
                break;
            default:
                jobsForLoggedInUser(responseBean, archived, loggedInUser, jobStatus);
        }
        log.info(msg + "Completed processing request to find all jobs for user in " + (System.currentTimeMillis() - startTime) + "ms");
        return responseBean;
    }

    private void jobsForLoggedInUser(JobWorspaceResponseBean responseBean, boolean archived, User loggedInUser, String jobStatus) {
        long startTime = System.currentTimeMillis();
        if (archived)
            responseBean.setListOfJobs(jobRepository.findByCreatedByAndDateArchivedIsNotNullOrderByCreatedOnDesc(loggedInUser));
        else
            responseBean.setListOfJobs(jobRepository.findByCreatedByAndStatusAndDateArchivedIsNullOrderByCreatedOnDesc(loggedInUser, jobStatus));

        List<Object[]> object = jobRepository.getJobCountPerStatusByCreatedBy(loggedInUser.getId());
            if(null != object.get(0)[0]){
                responseBean.setLiveJobs(Integer.parseInt(object.get(0)[0].toString()));
                responseBean.setDraftJobs(Integer.parseInt(object.get(0)[1].toString()));
                responseBean.setArchivedJobs(Integer.parseInt(object.get(0)[2].toString()));
            }
        log.info("Got " + responseBean.getListOfJobs().size() + " jobs in " + (System.currentTimeMillis() - startTime) + "ms");
        getCandidateCountByStage(responseBean.getListOfJobs());
    }

    private void jobsForCompany(JobWorspaceResponseBean responseBean, boolean archived, List<Company> companyList, String jobStatus) {
        long startTime = System.currentTimeMillis();
        companyList.stream().forEach(company -> {
            if (archived)
                responseBean.getListOfJobs().addAll(jobRepository.findByCompanyIdAndDateArchivedIsNotNullOrderByCreatedOnDesc(company));
            else
                responseBean.getListOfJobs().addAll(jobRepository.findByCompanyIdAndStatusAndDateArchivedIsNullOrderByCreatedOnDesc(company, jobStatus));

            List<Object[]> object = jobRepository.getJobCountPerStatusByCompanyId(company.getId());
            if(null != object.get(0)[0]){
                responseBean.setLiveJobs(responseBean.getLiveJobs() + Integer.parseInt(object.get(0)[0].toString()));
                responseBean.setDraftJobs(responseBean.getDraftJobs() + Integer.parseInt(object.get(0)[1].toString()));
                responseBean.setArchivedJobs(responseBean.getArchivedJobs() + Integer.parseInt(object.get(0)[2].toString()));
            }
        });
        log.info("Got " + responseBean.getListOfJobs().size() + " jobs in " + (System.currentTimeMillis() - startTime) + "ms");
        getCandidateCountByStage(responseBean.getListOfJobs());
    }

    private void getCandidateCountByStage(List<Job> jobs) {
        if(jobs != null & jobs.size() > 0) {
            long startTime = System.currentTimeMillis();
            //Converting list of jobs into map, so each job is available by key
            Map<Long, Job> jobsMap = jobs.stream().collect(Collectors.toMap(Job::getId, Function.identity()));
            log.info("Getting candidate count for " + jobs.size() + " jobs");
            try {
                List<Long> jobIds = new ArrayList<>();
                jobIds.addAll(jobsMap.keySet());
                //get counts by stage for ALL job ids in 1 db call
                List<Object[]> stageCountList = jobCandidateMappingRepository.findCandidateCountByStageJobIds(jobIds, false);
                //Format results in a map<jobId, resultset>
                Map<Long, List<Object[]>> stageCountMapByJobId = stageCountList.stream().collect(groupingBy(obj -> ((Integer) obj[0]).longValue()));
                log.info("Got stageCountByJobIds, row count: " + stageCountMapByJobId.size());
                //Loop through map to assign count by stage for each job
                stageCountMapByJobId.forEach((key, value) -> {
                    Job job = jobsMap.get(key);
                    value.stream().forEach(objArray -> {
                        job.getCandidateCountByStage().put(objArray[1].toString(), ((BigInteger) objArray[2]).intValue());
                    });
                    try {
                        job.getCandidateCountByStage().put(IConstant.Stage.Reject.getValue(), jobCandidateMappingRepository.findRejectedCandidateCount(job.getId()));
                    } catch (Exception e) {
                        log.error("Exception while finding rejected candidate count for job with id {}" + job.getId());
                    }
                });

                log.info("Got candidate count by stage for " + jobs.size() + " jobs in " + (System.currentTimeMillis() - startTime) + "ms");
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * For the specified job, retrieve
     * 1. list candidates for job for specified stage
     * 2. count of candidates by each stage
     *
     * @return response bean with all details
     * @throws Exception
     */
    @Transactional
    public SingleJobViewResponseBean getJobViewById(Long jobId, String stage) throws Exception {
        log.info("Received request to request to find a list of all candidates for job: {} and stage {} ",jobId, stage);
        long startTimeMethod = System.currentTimeMillis(), startTime = System.currentTimeMillis();
        //If the job is not published, do not process the request
        Job job = jobRepository.getOne(jobId);


        if (null == job) {
            StringBuffer info = new StringBuffer("Invalid job id ").append(jobId);
            log.info(info.toString());
            Map<String, String> breadCrumb = new HashMap<>();
            breadCrumb.put("Job Id ",jobId.toString());
            breadCrumb.put("detail", info.toString());
            throw new WebException("Invalid job id " + jobId,  HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }
        else {
            User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!IConstant.UserRole.Names.SUPER_ADMIN.equals(loggedInUser.getRole()) && !IConstant.UserRole.Names.RECRUITMENT_AGENCY.equals(loggedInUser.getRole()) && !job.getCompanyId().getId().equals(loggedInUser.getCompany().getId()))
                throw new WebException(IErrorMessages.JOB_COMPANY_MISMATCH, HttpStatus.UNAUTHORIZED);

            if(IConstant.JobStatus.DRAFT.getValue().equals(job.getStatus())) {
                StringBuffer info = new StringBuffer(IErrorMessages.JOB_NOT_LIVE).append(job.getStatus());
                log.info(info.toString());
                Map<String, String> breadCrumb = new HashMap<>();
                breadCrumb.put("Job Id", job.getId().toString());
                breadCrumb.put("detail", info.toString());
                throw new WebException(IErrorMessages.JOB_NOT_LIVE, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        SingleJobViewResponseBean responseBean = new SingleJobViewResponseBean();

        List<JobCandidateMapping> jcmList;

        if(IConstant.Stage.Reject.getValue().equals(stage))
            jcmList = jobCandidateMappingRepository.findByJobAndRejectedIsTrue(job);
        else
            jcmList= jobCandidateMappingRepository.findByJobAndStageInAndRejectedIsFalse(job, MasterDataBean.getInstance().getStageStepMap().get(MasterDataBean.getInstance().getStageStepMasterMap().get(stage)));

        log.info("****Time to fetch jcmList {} ms", (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();

        jcmList.parallelStream().forEach(jcmFromDb-> {
            jcmFromDb.setJcmCommunicationDetails(jcmCommunicationDetailsRepository.findByJcmId(jcmFromDb.getId()));
            jcmFromDb.setCvRating(cvRatingRepository.findByJobCandidateMappingId(jcmFromDb.getId()));

            if (IConstant.Stage.ResumeSubmit.getValue().equalsIgnoreCase(stage) || IConstant.Stage.Interview.getValue().equalsIgnoreCase(stage)) {
                List<JcmProfileSharingDetails>jcmProfileSharingDetails = jcmProfileSharingDetailsRepository.findByJobCandidateMappingId(jcmFromDb.getId());
                jcmProfileSharingDetails.forEach(detail->{
                    detail.setHiringManagerName(detail.getProfileSharingMaster().getReceiverName());
                    detail.setHiringManagerEmail(detail.getProfileSharingMaster().getReceiverEmail());
                });
                jcmFromDb.setInterestedHiringManagers(
                        jcmProfileSharingDetails
                                .stream()
                                .filter( jcmProfileSharingDetail -> jcmProfileSharingDetail.getHiringManagerInterestDate()!=null && jcmProfileSharingDetail.getHiringManagerInterest())
                                .collect(Collectors.toList())
                );

                jcmFromDb.setNotInterestedHiringManagers(
                        jcmProfileSharingDetails
                                .stream()
                                .filter( jcmProfileSharingDetail -> jcmProfileSharingDetail.getHiringManagerInterestDate()!=null && !jcmProfileSharingDetail.getHiringManagerInterest())
                                .collect(Collectors.toList())
                );

                jcmFromDb.setNotRespondedHiringManagers(
                        jcmProfileSharingDetails
                                .stream()
                                .filter( jcmProfileSharingDetail -> jcmProfileSharingDetail.getHiringManagerInterestDate()==null )
                                .collect(Collectors.toList())
                );
            }
        });
        log.info("****JCM list populated with profile sharing, hiring manager and all details in {} ms", (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();

        Collections.sort(jcmList);

        log.info("****Sorting of jcmlist done in {} ms", (System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();

        responseBean.setCandidateList(jcmList);

        //log.info("****Populated list of candidates in {} ms", (System.currentTimeMillis() - startTime));
        //startTime = System.currentTimeMillis();

        Map<Long, StageStepMaster> stageStepMasterMap = MasterDataBean.getInstance().getStageStepMap();

        List<Object[]> stageCountList = jobCandidateMappingRepository.findCandidateCountByStage(jobId);

        stageCountList.stream().forEach(objArray -> {
            String key = stageStepMasterMap.get(((Integer) objArray[0]).longValue()).getStage();
            if (null == responseBean.getCandidateCountByStage().get(key))
                responseBean.getCandidateCountByStage().put(key, ((BigInteger) objArray[1]).intValue());
            else //stage exists in response bean, add the count of the other step to existing value
                responseBean.getCandidateCountByStage().put(key,responseBean.getCandidateCountByStage().get(key)  + ((BigInteger) objArray[1]).intValue());
        });
        //add count of rejected candidates
        responseBean.getCandidateCountByStage().put(IConstant.Stage.Reject.getValue(),  jobCandidateMappingRepository.findRejectedCandidateCount(jobId));
        log.info("****Populated response bean with various stage specific counts in {} ms", (System.currentTimeMillis() - startTime));
        log.info("Completed processing request to find candidates for job {}  and stage: {} in {} ms.", jobId, stage ,(System.currentTimeMillis() - startTimeMethod));

        return responseBean;
    }

    private void addJobOverview(Job job, Job oldJob, User loggedInUser) { //method for add job for Overview page
        //boolean deleteExistingJobStageStep = (null != job.getId());

        //validate title
        if (job.getJobTitle().length() > IConstant.TITLE_MAX_LENGTH)  //Truncate job title if it is greater than max length
            job.setJobTitle(job.getJobTitle().substring(0, IConstant.TITLE_MAX_LENGTH));
        Company userCompany = null;
        if(null != job.getCompanyId())
            userCompany = companyRepository.getOne(job.getCompanyId().getId());

        if (null == userCompany) {
            throw new ValidationException("Cannot find company for current job", HttpStatus.EXPECTATION_FAILED);
        }

        job.setCompanyId(userCompany);
        String historyMsg = "Created";

        //TODO Currently we do not delete jobStageStep refer ticket #326, we need to revisit this code after getting select stage step pages
        /*if(deleteExistingJobStageStep){
            jobStageStepRepository.deleteByJobId(job.getId());
            jobStageStepRepository.flush();
        }*/

        if (null != oldJob && !IConstant.JobStatus.PUBLISHED.equals(oldJob.getStatus())) {//only update existing job
            if(null != job.getHiringManager())
                oldJob.setHiringManager(job.getHiringManager());
            if(null != job.getRecruiter())
                oldJob.setRecruiter(job.getRecruiter());

            oldJob.setJobTitle(job.getJobTitle());
            oldJob.setJobDescription(job.getJobDescription());
            oldJob.setUpdatedBy(loggedInUser);
            oldJob.setUpdatedOn(new Date());
            oldJob = jobRepository.save(oldJob);
            historyMsg = "Updated";

            //remove all data from job_key_skills and job_capabilities
            jobKeySkillsRepository.deleteByJobId(job.getId());
            jobCapabilitiesRepository.deleteByJobId(job.getId());

            jobKeySkillsRepository.flush();
            jobCapabilitiesRepository.flush();

        } else if(null == oldJob){ //Create new entry for job
            job.setCreatedOn(new Date());
            job.setMlDataAvailable(false);
            job.setStatus(IConstant.JobStatus.DRAFT.getValue());
            job.setCreatedBy(loggedInUser);
            job.setJobReferenceId(UUID.randomUUID());
            //End of code to be removed
            oldJob = jobRepository.save(job);
        }
        //TODO: remove one JobRepository call
        //Add Job details
        addJobDetail(job, oldJob, loggedInUser);

        saveJobHistory(job.getId(), historyMsg + " job overview", loggedInUser);

        if(null != oldJob && !IConstant.JobStatus.PUBLISHED.equals(oldJob.getStatus())){
            //make a call to ML api to obtain skills and capabilities
            if(MasterDataBean.getInstance().getConfigSettings().getMlCall()==1) {
                try {
                    RolePredictionBean rolePredictionBean = new RolePredictionBean();
                    RolePredictionBean.RolePrediction rolePrediction= new RolePredictionBean.RolePrediction();
                    rolePrediction.setJobTitle(job.getJobTitle());
                    rolePrediction.setJobDescription(job.getJobDescription());
                    if(null != job.getSelectedRole() && job.getSelectedRole().size()>0)
                        rolePrediction.getRecruiterRoles().addAll(job.getSelectedRole());
                    rolePredictionBean.setRolePrediction(rolePrediction);
                    callMl(rolePredictionBean, job.getId(), job);
                    if(null == oldJob) {
                        job.setMlDataAvailable(true);
                        jobRepository.save(job);
                    }
                    else {
                        oldJob.setMlDataAvailable(true);
                        jobRepository.save(oldJob);
                    }
                } catch (Exception e) {
                    log.error("Error while fetching data from ML: " + e.getMessage());
                    job.setMlErrorMessage(IErrorMessages.ML_DATA_UNAVAILABLE);
                }
            }
        }
        //populate key skills for the job
        job.setJobKeySkillsList(jobKeySkillsRepository.findByJobId(job.getId()));
    }

    private void callMl(RolePredictionBean requestBean, long jobId, Job job) throws Exception {
        log.info("inside callMl method");
        String mlResponse = null;
        String mlRequest = null;
        String function = MasterDataBean.getInstance().getFunction().get(job.getFunction().getId());
        Map breadCrumb = new HashMap<String, String>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> roles = new ArrayList<>();

            if(null != job.getSelectedRole() && job.getSelectedRole().size()>0){
                requestBean.getRolePrediction().getRecruiterRoles().addAll(job.getSelectedRole());
            }

            //Send function to ml
            if(null != function)
                requestBean.getRolePrediction().setIndustry(function);

            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mlRequest = objectMapper.writeValueAsString(requestBean);
            log.info("Sending request to ml for LB job id : "+jobId);
            mlResponse = RestClient.getInstance().consumeRestApi(mlRequest, mlUrl, HttpMethod.POST,null).getResponseBody();
            log.info("Response received: " + mlResponse);
            log.info("Getting response from ml for LB job id : "+jobId);
            long startTime = System.currentTimeMillis();

            //add data in breadCrumb
            breadCrumb.put("Job Id: ", String.valueOf(jobId));
            breadCrumb.put("Request", mlRequest);
            breadCrumb.put("Response", mlResponse);

            MLResponseBean responseBean = objectMapper.readValue(mlResponse, MLResponseBean.class);

            //if ml status is jdc_jtm_Error
            if(IConstant.MlRolePredictionStatus.JDC_JTM_ERROR.getValue().equalsIgnoreCase(responseBean.getRolePrediction().getStatus()) ||
                    IConstant.MlRolePredictionStatus.JDC_JTN_ERROR.getValue().equalsIgnoreCase(responseBean.getRolePrediction().getStatus()) ||
                    IConstant.MlRolePredictionStatus.JDB_JTM_ERROR.getValue().equalsIgnoreCase(responseBean.getRolePrediction().getStatus())){
                log.info("ml response status is " +responseBean.getRolePrediction().getStatus()+" for job id : "+jobId);
                responseBean.getRolePrediction().getJdRoles().forEach(role -> {
                    roles.add(role.getRoleName());
                });
                responseBean.getRolePrediction().getJtRoles().forEach(role -> {
                    roles.add(role.getRoleName());
                });
                job.setRoles(roles);
                return;
            }else if(IConstant.MlRolePredictionStatus.NO_ERROR.getValue().equalsIgnoreCase(responseBean.getRolePrediction().getStatus())){
                //if ml status is no_Error
                log.info("ml response status is no_Error for job id : "+jobId);
                int numUniqueSkills = handleSkillsFromML(responseBean.getTowerGeneration().getSkills(), jobId);
                if(numUniqueSkills != responseBean.getTowerGeneration().getSkills().size()) {
                    log.error(IErrorMessages.ML_DATA_DUPLICATE_SKILLS + mlResponse);
                    SentryUtil.logWithStaticAPI(null, IErrorMessages.ML_DATA_DUPLICATE_SKILLS + mlResponse, breadCrumb);
                }
                Set<Integer> uniqueCapabilityIds = new HashSet<>();
                handleCapabilitiesFromMl(responseBean.getTowerGeneration().getSuggestedCapabilities(), jobId, true, uniqueCapabilityIds);
                handleCapabilitiesFromMl(responseBean.getTowerGeneration().getAdditionalCapabilities(), jobId, false, uniqueCapabilityIds);
            }else{
                SentryUtil.logWithStaticAPI(null, "ml status is different than expected or suff_error", breadCrumb);
                if(IConstant.MlRolePredictionStatus.SUFF_ERROR.getValue().equalsIgnoreCase(responseBean.getRolePrediction().getStatus())) {
                    //if ml status is suff_Error
                    log.info("ml response status is suff_Error for job id : " + jobId);
                    throw new ValidationException("There was no enough data in JD and JT for this job : " + jobId, HttpStatus.BAD_REQUEST);
                }
            }
            log.info("Time taken to process ml data: " + (System.currentTimeMillis() - startTime) + "ms.");

        }catch(Exception e) {
            log.error("Error While processing ml call : "+e.getMessage());
            SentryUtil.logWithStaticAPI(null, "Error While processing ml call : "+e.getMessage(), breadCrumb);
        }
    }

    /**
     * Method to handle all skills provided by ML
     *
     * @param skillsList List of skills obtained from ML
     * @param jobId the job id for which the skills have to persisted
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private int handleSkillsFromML(List<Skills> skillsList, long jobId) throws Exception {
        log.info("Size of skill list: " + skillsList.size());
        Set<Skills> uniqueSkills = skillsList.stream().collect(Collectors.toSet());
        List<JobKeySkills> jobKeySkillsToSave = new ArrayList<>(uniqueSkills.size());
        uniqueSkills.forEach(skill-> {
            //find a skill from the master table for the skill name provided
            SkillsMaster skillFromDb = skillMasterRepository.findBySkillNameIgnoreCase(skill.getName());
            //if none if found, add a skill
            if (null == skillFromDb) {
                skillFromDb = new SkillsMaster(skill.getName());
                skillMasterRepository.save(skillFromDb);
            }
            //add a record in job_key_skills with this skill id
            jobKeySkillsToSave.add(new JobKeySkills(skillFromDb, true,true, new Date(), (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal(), jobId));
        });
        jobKeySkillsRepository.saveAll(jobKeySkillsToSave);
        return uniqueSkills.size();
    }

    /**
     * Method to handle all capabilities provided by ML
     *
     * @param capabilitiesList
     * @param jobId
     * @param selectedByDefault
     * @param uniqueCapabilityIds
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void handleCapabilitiesFromMl(List<Capabilities> capabilitiesList, long jobId, boolean selectedByDefault, Set<Integer> uniqueCapabilityIds) throws Exception {
        log.info("Size of capabilities list to process: " + capabilitiesList.size());
        List<JobCapabilities> jobCapabilitiesToSave = new ArrayList<>(capabilitiesList.size());
        capabilitiesList.forEach(capability->{
            if (capability.getCapCode() !=0 && !uniqueCapabilityIds.contains(capability.getCapCode())) {
                jobCapabilitiesToSave.add(new JobCapabilities(Long.valueOf(capability.getCapCode()), capability.getCapability(), selectedByDefault, mapWeightage(capability.getCapabilityWeight()), new Date(), (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), jobId));
                uniqueCapabilityIds.add(capability.getCapCode());
            }
        });
        jobCapabilitiesRepository.saveAll(jobCapabilitiesToSave);
    }

    private int mapWeightage(int capabilityWeight) {
        if(capabilityWeight <= 2)
            return 2;
        else if (capabilityWeight <=6)
            return 6;
        return 10;
    }

    private void addJobScreeningQuestions(Job job, Job oldJob, User loggedInUser) throws Exception { //method for add screening questions

        //commented out the check as per ticket #146
        /*
        if (job.getJobScreeningQuestionsList().size() > MasterDataBean.getInstance().getConfigSettings().getMaxScreeningQuestionsLimit()) {
            throw new ValidationException(IErrorMessages.SCREENING_QUESTIONS_VALIDATION_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
        }
        */
        if(null != oldJob && IConstant.JobStatus.PUBLISHED.equals(oldJob.getStatus())){
            return;
        }
        String historyMsg = "Added";

        if (null != oldJob.getJobScreeningQuestionsList() && oldJob.getJobScreeningQuestionsList().size() > 0) {
            historyMsg = "Updated";
            jobScreeningQuestionsRepository.deleteAll(oldJob.getJobScreeningQuestionsList());//delete old job screening question list
            jobScreeningQuestionsRepository.flush();
        }


        job.getJobScreeningQuestionsList().forEach(n -> {
            n.setCreatedBy(loggedInUser.getId());
            n.setCreatedOn(new Date());
            n.setJobId(job.getId());
            n.setUpdatedOn(new Date());
            n.setUpdatedBy(loggedInUser.getId());
        });

        oldJob.setJobScreeningQuestionsList(job.getJobScreeningQuestionsList());
        if(job.getJobScreeningQuestionsList().size()>0) {
            oldJob.setHrQuestionAvailable(true);
        }
        else{
            oldJob.setHrQuestionAvailable(false);
        }

        jobRepository.save(oldJob);
        saveJobHistory(job.getId(), historyMsg + " screening questions", loggedInUser);

        //populate key skills for the job
       // job.setJobKeySkillsList(jobKeySkillsRepository.findByJobId(job.getId()));
    }

    private void addJobKeySkills(Job job, Job oldJob, User loggedInUser) throws Exception { //update and add new key skill
        if(null != oldJob && IConstant.JobStatus.PUBLISHED.equals(oldJob.getStatus()))
            return;

        List<JobKeySkills> mlProvidedKeySkills = jobKeySkillsRepository.findByJobIdAndMlProvided(oldJob.getId(), true);

        //if there were key skills suggested by ML, and the request for add job - key skills has a 0 length array, throw an error, otherwise, proceed
        if (mlProvidedKeySkills.size() > 0 && null != job.getJobKeySkillsList() && job.getJobKeySkillsList().isEmpty()) {
            throw new ValidationException("Job key skills " + IErrorMessages.EMPTY_AND_NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
        }

        //delete all key skills where MlProvided=false
        List<JobKeySkills> userProvidedJobKeySkillslist = jobKeySkillsRepository.findByJobIdAndMlProvided(job.getId(), false);
        if (userProvidedJobKeySkillslist.size() > 0) {
            jobKeySkillsRepository.deleteAll(userProvidedJobKeySkillslist);
        }
        jobKeySkillsRepository.flush();


        //For each keyskill in the request (will have only the mlProvided true ones), update the values for selected
        Map<Long, JobKeySkills> newSkillValues = new HashMap();
        job.getJobKeySkillsList().stream().forEach(jobKeySkill -> newSkillValues.put(jobKeySkill.getSkillId().getId(), jobKeySkill));

        oldJob.getJobKeySkillsList().forEach(oldKeySkill -> {
            if (oldKeySkill.getMlProvided()) {
                JobKeySkills newValue = newSkillValues.get(oldKeySkill.getSkillId().getId());
                if(null != newValue) {
                    oldKeySkill.setSelected(newValue.getSelected());
                    log.info("ML provided skill {} not returned in the api call", oldKeySkill.getSkillId().getSkillName());
                }
                else
                    oldKeySkill.setSelected(false);
                oldKeySkill.setUpdatedOn(new Date());
                oldKeySkill.setUpdatedBy(loggedInUser);
            }
        });

        //get all skillMaster and tempskills master data
        Map<String, Long> skillsMasterMapByName = new HashMap<>();
        Map<Long, SkillsMaster> skillsMasterMap = new HashMap<>();
        List<SkillsMaster> skillsMasterList = skillMasterRepository.findAll();
        skillsMasterList.forEach(skillsMaster -> {
            skillsMasterMapByName.put(skillsMaster.getSkillName(), skillsMaster.getId());
            skillsMasterMap.put(skillsMaster.getId(), skillsMaster);
        });

        Map<String, Long> tempSkillsMapByName = new HashMap<>();
        Map<Long, TempSkills> tempSkillsMap = new HashMap<>();
        tempSkillsRepository.findAll().forEach(tempSkill -> {
            tempSkillsMapByName.put(tempSkill.getSkillName(), tempSkill.getId());
            tempSkillsMap.put(tempSkill.getId(), tempSkill);
        });

        //For each user entered key skill, do the following:
        //(i) check if the skill is already present in the ml key skill list for the job. If yes, skip this skill
        //(ii) if not, check if the skill is present in the skills master table. If yes, use that skill id to insert a record in the db
        //(iii) if not, check if the skill is present in the temp key skills table. If yes, use that id and insert a record in the db with temp key skill id column populated
        //(iv) if not, enter a record in the temp key skills table and use the id of the newly inserted record to insert a record in job key skill table with the temp key skill id column populated

        for (String userSkills : job.getUserEnteredKeySkill()) {

            if (skillsMasterMapByName.keySet().contains(userSkills)) {
                Long skillId = skillsMasterMapByName.get(userSkills);
                //does the skill match one of the those provided by ML?
                if (null != newSkillValues.get(skillId)) {
                    //found a match, skip this skill
                    continue;
                } else {
                    //no match found in mlProvided skill, add a record
                    jobKeySkillsRepository.save(new JobKeySkills(skillsMasterMap.get(skillId), false, true, new Date(), loggedInUser, job.getId()));
                }

            }
            //check if the user entered skill exists in the temp skills table
            else if (tempSkillsMapByName.keySet().contains(userSkills)) {
                Long tempSkillId = tempSkillsMapByName.get(userSkills);
                jobKeySkillsRepository.save(new JobKeySkills(tempSkillsMap.get(tempSkillId), false, true, new Date(), loggedInUser, job.getId()));

            }
            //this is a new skill, add to temp skills and refer to jobkeyskills table
            else {
                TempSkills tempSkills = tempSkillsRepository.save(new TempSkills(userSkills, false));
                jobKeySkillsRepository.save(new JobKeySkills(tempSkills, false, true, new Date(), loggedInUser, job.getId()));
            }
        }
        saveJobHistory(job.getId(), "Added key skills", loggedInUser);
        //populate the capabilities for the job
        job.setJobCapabilityList(jobCapabilitiesRepository.findByJobId(job.getId()));
    }


    private void addJobCapabilities(Job job, Job oldJob, User loggedInUser) { //add job capabilities

        if(null != oldJob && IConstant.JobStatus.PUBLISHED.equals(oldJob.getStatus()))
            return;

        //if there are capabilities that were returned from ML, and the request for add job - capabilities has a 0 length array, throw an error, otherwise, proceed
        if (oldJob.getJobCapabilityList().size() > 0 && null != job.getJobCapabilityList() && job.getJobCapabilityList().isEmpty()) {
            throw new ValidationException("Job Capabilities " + IErrorMessages.EMPTY_AND_NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
        }

        int selectedCapabilityCount = job.getJobCapabilityList().stream().filter(capability->capability.getSelected()).collect(Collectors.toList()).size();

        if(selectedCapabilityCount>MasterDataBean.getInstance().getConfigSettings().getMaxCapabilities())
            throw new ValidationException("Job Capabilities more than max capabilities limit for jobId : "+ job.getId(), HttpStatus.BAD_REQUEST);

        //For each capability in the request, update the values for selected and importance_level
        Map<Long, JobCapabilities> newCapabilityValues = new HashMap();
        job.getJobCapabilityList().stream().forEach(jobCapability -> newCapabilityValues.put(jobCapability.getId(), jobCapability));

        log.info("Capability count: Old Job: " + oldJob.getJobCapabilityList().size() + " new capability list size: " + newCapabilityValues.size());

        oldJob.getJobCapabilityList().forEach(oldCapability -> {
            JobCapabilities newValue = newCapabilityValues.get(oldCapability.getId());
            if(newValue.getSelected()) {
                List<WeightageCutoffByCompanyMapping> wtgCompanyMappings = weightageCutoffByCompanyMappingRepository.findByCompanyIdAndWeightage(oldJob.getCompanyId().getId(), newValue.getWeightage());
                if (null != wtgCompanyMappings && wtgCompanyMappings.size() > 0) {
                    wtgCompanyMappings.stream().forEach(starRatingMapping -> oldCapability.getJobCapabilityStarRatingMappingList().add(new JobCapabilityStarRatingMapping(newValue.getId(), oldJob.getId(), newValue.getWeightage(), starRatingMapping.getCutoff(), starRatingMapping.getPercentage(), starRatingMapping.getStarRating())));

                } else {
                    List<WeightageCutoffMapping> weightageCutoffMappings = weightageCutoffMappingRepository.findByWeightage(newValue.getWeightage());
                    if (null != weightageCutoffMappings && weightageCutoffMappings.size() > 0) {
                        weightageCutoffMappings.stream().forEach(starRatingMapping -> oldCapability.getJobCapabilityStarRatingMappingList().add(new JobCapabilityStarRatingMapping(newValue.getId(), oldJob.getId(), newValue.getWeightage(), starRatingMapping.getCutoff(), starRatingMapping.getPercentage(), starRatingMapping.getStarRating())));
                    }
                }
            }
            oldCapability.setWeightage(newValue.getWeightage());
            oldCapability.setSelected(newValue.getSelected());
            if(newValue.getSelected())
                oldCapability.setWeightage(newValue.getWeightage());
            oldCapability.setUpdatedOn(new Date());
            oldCapability.setUpdatedBy(loggedInUser);
        });


        //29th July: Do not auto-publish the job. The job should be explicitly published by means of an API call
        //oldJob.setStatus(IConstant.JobStatus.PUBLISHED.getValue());
        //oldJob.setDatePublished(new Date());
        jobRepository.save(oldJob);
        saveJobHistory(job.getId(), "Added capabilities", loggedInUser);

        job.getJobCapabilityList().clear();
        job.getJobCapabilityList().addAll(oldJob.getJobCapabilityList());
    }

    private void addJobDetail(Job job, Job oldJob, User loggedInUser) {//add job details

        MasterDataBean masterDataBean = MasterDataBean.getInstance();

        oldJob.setCompanyJobId(job.getCompanyJobId());
        oldJob.setNoOfPositions(job.getNoOfPositions());

        //Update Function
        if (null == masterDataBean.getFunction().get(job.getFunction().getId())) {
            //throw new ValidationException("In Job, function " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
            log.error("In Job, function " + IErrorMessages.NULL_MESSAGE + job.getId());
        }else{
            oldJob.setFunction(job.getFunction());
        }

        //Update Currency
        if (null == job.getCurrency()) {
            // throw new ValidationException("In Job, Currency " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
            log.error("In Job, Currency " + IErrorMessages.NULL_MESSAGE + job.getId());
        }else{
            oldJob.setCurrency(job.getCurrency());
        }

        List<CompanyAddress> companyAddressList = companyAddressRepository.findByCompanyId(loggedInUser.getCompany().getId());
        List<CompanyBu> companyBuList = companyBuRepository.findByCompanyId(loggedInUser.getCompany().getId());

        Map<Long, CompanyBu> companyBuMap = new HashMap<>();
        Map<Long, CompanyAddress> companyAddressMap = new HashMap<>();

        companyBuList.forEach(companyBu -> companyBuMap.put(companyBu.getId(), companyBu));
        companyAddressList.forEach(companyAddress -> companyAddressMap.put(companyAddress.getId(), companyAddress));

        //Update Job and Interview Location
        if(null != job.getJobLocation() && null != job.getInterviewLocation()){
            if (companyAddressList.isEmpty() || null == companyAddressMap.get(job.getJobLocation().getId())
                    || null == companyAddressMap.get(job.getInterviewLocation().getId())) {
                // throw new ValidationException("In Job, company address " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
                log.error("In Job, company address " + IErrorMessages.NULL_MESSAGE + job.getId());
            }else{
                oldJob.setInterviewLocation(companyAddressMap.get(job.getInterviewLocation().getId()));
                oldJob.setJobLocation(companyAddressMap.get(job.getJobLocation().getId()));
            }
        }

        //Update Bu
        if(null != job.getBuId()){
            if (companyBuList.isEmpty() || null == companyBuMap.get(job.getBuId().getId())) {
                // throw new ValidationException("In Job, company bu " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
                log.error("In Job, company bu " + IErrorMessages.NULL_MESSAGE + job.getId());
            }else{
                oldJob.setBuId(companyBuMap.get(job.getBuId().getId()));
            }
        }

        //Update ExperienceRange
        if(null != job.getExperienceRange() && null != masterDataBean.getExperienceRange().get(job.getExperienceRange().getId())){
            oldJob.setExperienceRange(job.getExperienceRange());
        }else{
            // throw new ValidationException("In Job, experience Range " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
            log.error("In Job, ExperienceRange " + IErrorMessages.NULL_MESSAGE + job.getId());
        }

        //Update Salary
        oldJob.setMinSalary(job.getMinSalary());
        oldJob.setMaxSalary(job.getMaxSalary());

        if(!IConstant.JobStatus.PUBLISHED.equals(oldJob.getStatus())){

            //Update Education
            if(null != job.getEducation()){
                if (null == masterDataBean.getEducation().get(job.getEducation().getId())) {
                    //throw new ValidationException("In Job, education " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
                    log.error("In Job, education " + IErrorMessages.NULL_MESSAGE + job.getId());
                }else{
                    oldJob.setEducation(job.getEducation());
                }
            }
            //Update Notice period
            oldJob.setNoticePeriod(job.getNoticePeriod());
        }

        oldJob.setUpdatedOn(new Date());
        jobRepository.save(oldJob);

        //populate all users for the company of current user
        List<User> userList = userRepository.findByCompanyId(loggedInUser.getCompany().getId());
        job.getUsersForCompany().addAll(userList);
    }

    private void addJobHiringTeam(Job job, Job oldJob, User loggedInUser) throws Exception {
        log.info("inside addJobHiringTeam");
        if(null != oldJob){
            jobHiringTeamRepository.deleteByJobId(oldJob.getId());
            jobHiringTeamRepository.flush();
        }
        AtomicLong i = new AtomicLong();
        Map<Long, StageStepMaster> stageStepMasterMap = MasterDataBean.getInstance().getStageStepMap();
        if(null != job.getHiringTeamStepMapping() && job.getHiringTeamStepMapping().size()>0) {
            List<JobHiringTeam> jobHiringTeamList = new ArrayList<>(job.getJobHiringTeamList().size());
            job.getHiringTeamStepMapping().forEach(stageStep -> {
                jobHiringTeamList.add(new JobHiringTeam(oldJob.getId(), stageStepMasterMap.get(stageStep.get(0)), User.builder().id(stageStep.get(1)).build(), i.longValue(), new Date(), loggedInUser));
                i.getAndIncrement();
            });
            jobHiringTeamRepository.saveAll(jobHiringTeamList);
            oldJob.setJobHiringTeamList(jobHiringTeamList);
            jobHiringTeamRepository.flush();
        }
        jobRepository.save(oldJob);
    }

    private void addJobExpertise(Job job, Job oldJob){

        if(null != oldJob && IConstant.JobStatus.PUBLISHED.equals(oldJob.getStatus()))
            return;

        MasterDataBean masterDataBean = MasterDataBean.getInstance();
        if(null == masterDataBean.getExpertise().get(job.getExpertise().getId())){
            throw new ValidationException("In Job, Expertise " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
        }
        oldJob.setExpertise(job.getExpertise());
        jobRepository.save(oldJob);
    }

    /**
     * Service method to publish a job
     *
     * @param jobId id of the job to be published
     */
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void publishJob(Long jobId) throws Exception {
        log.info("Received request to publish job with id: " + jobId);
        Job publishedJob = changeJobStatus(jobId,IConstant.JobStatus.PUBLISHED.getValue());
        log.info("Completed publishing job with id: " + jobId);
        if(null != publishedJob.getCompanyId().getShortName() && !publishedJob.getCompanyId().isSubdomainCreated()) {
            log.info("Subdomain does not exist for company: {}. Creating one.", publishedJob.getCompanyId().getCompanyName());
            companyService.createSubdomain(publishedJob.getCompanyId());
            log.info("Reloading apache for subdomain {}.", publishedJob.getCompanyId().getShortName());
            companyService.reloadApache(Arrays.asList(publishedJob.getCompanyId()));
        }
        if(publishedJob.getJobCapabilityList().size() == 0)
            log.info("No capabilities exist for the job: " + jobId + " Scoring engine api call will NOT happen");
        else if(jobCapabilitiesRepository.findByJobIdAndSelected(jobId, true).size() == 0)
            log.info("No capabilities have been selected for the job: {}. Scoring engine api call will NOT happen", jobId);
        else {
            log.info("Calling Scoring Engine Api to create a job");
            try {
                String scoringEngineResponse = RestClient.getInstance().consumeRestApi(convertJobToRequestPayload(jobId, publishedJob), scoringEngineBaseUrl + scoringEngineAddJobUrlSuffix, HttpMethod.POST, null).getResponseBody();
                publishedJob.setScoringEngineJobAvailable(true);
                jobRepository.save(publishedJob);
            } catch (Exception e) {
                log.error("Error during create job api call on Scoring engine: " + e.getMessage());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String convertJobToRequestPayload(Long jobId, Job publishedJob) throws Exception {
        List<JobCapabilities> jobCapabilities = jobCapabilitiesRepository.findByJobIdAndSelected(jobId,true);
        MasterData expertise = null;
        if(null != publishedJob.getExpertise())
            expertise = MasterDataBean.getInstance().getExpertise().get(publishedJob.getExpertise().getId());

        List<Capability> capabilityList = new ArrayList<>(jobCapabilities.size());
        jobCapabilities.stream().forEach(jobCapability -> {
            capabilityList.add(new Capability(jobCapability.getCapabilityId(), jobCapability.getWeightage(), jobCapability.getJobCapabilityStarRatingMappingList()));
        });
        ScoringEngineJobBean jobRequestBean;
        if(null != expertise)
             jobRequestBean = new ScoringEngineJobBean(jobId, Long.parseLong(expertise.getValueToUSe()), capabilityList);
        else
             jobRequestBean = new ScoringEngineJobBean(jobId, null, capabilityList);

        return (new ObjectMapper()).writeValueAsString(jobRequestBean);
    }

    /**
     * Service method to archive a job
     *
     * @param jobId id of the job to be archived
     */
    @Transactional
    public void archiveJob(Long jobId) {
        log.info("Received request to archive job with id: " + jobId);
        changeJobStatus(jobId,IConstant.JobStatus.ARCHIVED.getValue());
        log.info("Completed archiving job with id: " + jobId);
    }

    /**
     * Service method to unarchive a job
     *
     * @param jobId id of the job to be unarchived
     */
    @Transactional
    public void unarchiveJob(Long jobId) throws Exception {
        log.info("Received request to unarchive job with id: " + jobId);
        changeJobStatus(jobId,null);
        log.info("Completed unarchiving job with id: " + jobId);
    }

    /**
     * common method to Publish, Archive or Unarchive a job
     * @param jobId the job on which the operation is to be performed
     * @param status the status to be set. If the job is being unarchived, the status will be sent as null
     */
    private Job changeJobStatus(Long jobId, String status) {
        Job job = jobRepository.getOne(jobId);
        if (null == job) {
            throw new WebException("Job with id " + jobId + "does not exist", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(null == status) {
            //check that the old status of job is archived
            if (!IConstant.JobStatus.ARCHIVED.getValue().equals(job.getStatus()))
                throw new WebException(IErrorMessages.JOB_NOT_ARCHIVED, HttpStatus.UNPROCESSABLE_ENTITY);
            if(null == job.getDatePublished())
                job.setStatus(IConstant.JobStatus.DRAFT.getValue());
            else
                job.setStatus(IConstant.JobStatus.PUBLISHED.getValue());

            job.setDateArchived(null);
        }
        else  {
            if (status.equals(IConstant.JobStatus.ARCHIVED.getValue()))
                job.setDateArchived(new Date());
            else
                job.setDatePublished(new Date());
            job.setStatus(status);
        }

        if(job.getJobScreeningQuestionsList().size()>0){
            job.setHrQuestionAvailable(true);
        }
        job.setUpdatedOn(new Date());
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        job.setUpdatedBy(loggedInUser);
        saveJobHistory(job.getId(), "Status changed to " +job.getStatus(), loggedInUser);
        return jobRepository.save(job);
    }

    @Transactional
    public Job getJobDetails(Long jobId) throws Exception {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (null == job) {
            throw new WebException("Job with id " + jobId + " does not exist", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return job;
    }

    private void saveJobHistory(Long jobId, String historyMsg, User loggedInUser) {
        jobHistoryRepository.save(new JobHistory(jobId, historyMsg, loggedInUser));
    }

    @Transactional
    public List<JobHistory>getJobHistory(Long jobId)throws Exception{
        Job job = jobRepository.findById(jobId).orElse(null);
        if (null == job) {
            throw new WebException("Job with id " + jobId + "does not exist ", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return jobHistoryRepository.findByJobIdOrderByIdDesc(jobId);
    }

    /**
     * Service method to return supported export formats for a company.
     *
     * @param jobId the job id for which supported formats to be returned
     * @return map of format id and name
     * @throws Exception
     */
    @Transactional
    public Map<Long, String> getSupportedExportFormat(Long jobId){
        log.info("Received Request to fetch supported export formats for jobId: {}", jobId);
        long startTime = System.currentTimeMillis();

        Job job = jobRepository.findById(jobId).orElse(null);

        if(null==job){
            throw new WebException("Job with id " + jobId + "does not exist ", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Map<Long, String> exportFormatMapForCompany = new HashMap<>();

        //add all default export format supported by litmusblox by default to exportFormatMapForCompany
        MasterDataBean.getInstance().getDefaultExportFormats().forEach(exportFormatMaster -> {
            exportFormatMapForCompany.put(exportFormatMaster.getId(), exportFormatMaster.getFormat());
        });

        //add company specific export format to exportFormatMapForCompany.
        exportFormatMasterRepository.findByCompanyIdAndSystemSupportedIsTrue(job.getCompanyId().getId()).forEach(exportFormatMaster -> {
            exportFormatMapForCompany.put(exportFormatMaster.getId(), exportFormatMaster.getFormat());
        });

        log.info("Finished Request to fetch supported export formats for jobId: {} in {}", jobId, (System.currentTimeMillis()-startTime));
        return exportFormatMapForCompany;
    }

    /**
     * Service method to return export json data for a job
     *
     * @param jobId the job id for which export data to be returned
     * @param formatId the format id to format export data
     * @return formatted json in String format
     * @throws Exception
     */
    public String exportData(Long jobId, Long formatId, String stage) throws Exception {
        log.info("Received Request to fetch export data for jobId: {} and formatId: {} ", jobId , formatId!=null?formatId: "default");
        long startTime = System.currentTimeMillis();
        //get default export format master
        ExportFormatMaster exportFormatMaster = exportFormatMasterRepository.getOne(formatId!=null?formatId:1L);
        Job job= jobRepository.getOne(jobId);
        Company company = null;

        if(null != job){
            company = job.getCompanyId();
        }

        //if default format is not available in db then throw exception
        if(null==exportFormatMaster){
            throw new WebException("Default format is missing from database", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //get list of headers and column names frm  db for default format
        List<ExportFormatDetail> defaultExportColumns = exportFormatDetailRepository.findByExportFormatMasterOrderByPositionAsc(exportFormatMaster);

        defaultExportColumns = defaultExportColumns.stream().filter(exportFormatDetail -> {
            return (exportFormatDetail.getStage().isEmpty() || exportFormatDetail.getStage().equalsIgnoreCase(stage));
        }).collect(Collectors.toList());

        if(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getRole().equalsIgnoreCase(IConstant.UserRole.Names.SUPER_ADMIN)){
            defaultExportColumns.add(new ExportFormatDetail(IConstant.CHAT_LINK, IConstant.CHAT_LINK_HEADER));
        }

        Map<String, String> exportHeaderColumnMap = new LinkedHashMap<>();


        if(defaultExportColumns.size()>0) {
            defaultExportColumns.forEach(exportColumn -> {
                exportHeaderColumnMap.put(exportColumn.getColumnName(), exportColumn.getHeader());
            });
        }

        List<String>columnNames = new ArrayList<String>(exportHeaderColumnMap.keySet());

        String columnsToExport = String.join(", ", columnNames);

        //list of objects from db to create export data json
        List<Object[]> exportDataList = ExportData.exportDataList(jobId, stage, columnsToExport, em);

        if(exportDataList.size()==0){
            throw new WebException("No Export data available for jobId: "+jobId, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        List<LinkedHashMap<String, Object>> exportResponseBean = new ArrayList<>();

        Company finalCompany = company;
        exportDataList.forEach(data-> {
                LinkedHashMap<String, Object> candidateData = new LinkedHashMap<>();
            for (int i = 0; i < data.length; ++i) {
                if(columnNames.get(i).equals("chatbotLink")){
                    candidateData.put(exportHeaderColumnMap.get(columnNames.get(i)), data[i]!=null? (environment.getProperty(IConstant.CHAT_LINK)+data[i].toString()):"");
                } else {
                    candidateData.put(exportHeaderColumnMap.get(columnNames.get(i)), data[i] != null ? data[i].toString() : "");
                }
            }
            if (exportResponseBean.stream().filter(object -> {
                return object.get("Email").toString().equalsIgnoreCase(candidateData.get("Email").toString());
            }).collect(Collectors.toList()).size() == 0){
                LinkedHashMap<String, String>questionAnswerMapForCandidate = ExportData.getQuestionAnswerForCandidate(candidateData.get("Email").toString(), jobId, finalCompany, em);
                /*questionAnswerMapForCandidate = questionAnswerMapForCandidate.entrySet().stream().sorted(comparingByKey())
                        .collect(toMap(e->e.getKey(), e->e.getValue(), (e1, e2)-> e2, LinkedHashMap::new));*/
                if(questionAnswerMapForCandidate.size()!=0){
                    questionAnswerMapForCandidate.forEach(candidateData::put);
                }
                if(!exportResponseBean.contains(candidateData)) {
                    exportResponseBean.add(candidateData);
                }
            }
        });

        log.info("Completed processing export data in {}", System.currentTimeMillis() - startTime);
        return new ObjectMapper().writeValueAsString(exportResponseBean);
    }

    /**
     * Method to find a job based on the reference id provided
     *
     * @param jobReferenceId
     * @return
     */
    @Transactional(readOnly = true)
    public Job findByJobReferenceId(UUID jobReferenceId) {
        return jobRepository.findByJobReferenceId(jobReferenceId);
    }


    /**
     * Service method to find list of jobs matching the search criteria
     *
     * @param searchRequest the request bean with company id and map of search paramters
     * @return List of jobs
     */
    static String SELECT_QUERY_PREFIX = "Select jobId from jobDetailsView where companyId = ";
    static String AND = " and ", IN_BEGIN = " in (", BRACKET_CLOSE = ")", LIKE_BEGIN = " LIKE \'%", LIKE_END = "%\'", LOWER_BEGIN = "LOWER(", OR = " or ";
    @Transactional(readOnly = true)
    public List<Job> searchJobs(SearchRequestBean searchRequest) {
        StringBuffer query = new StringBuffer().append(SELECT_QUERY_PREFIX);
        if(null != searchRequest.getCompanyId())
            query.append(searchRequest.getCompanyId());
        else {
            Company objFromDb = companyRepository.findByShortNameIgnoreCase(searchRequest.getCompanyShortName());
            if(null == objFromDb)
                throw new WebException("No company found for short name: " + searchRequest.getCompanyShortName(), HttpStatus.UNPROCESSABLE_ENTITY);
            query.append(objFromDb.getId());
        }
        if(searchRequest.getSearchParam().size()>0)
            query.append(AND);
        final AtomicBoolean firstSearchParam = new AtomicBoolean(true);
        searchRequest.getSearchParam().forEach(searchParam -> {
            if(firstSearchParam.get()) {
                query.append("(");
                firstSearchParam.set(false);
            }
            else
                query.append(OR);

            if(searchParam.isMultiSelect())
                query.append(searchParam.getKey()).append(IN_BEGIN).append(searchParam.getValue()).append(BRACKET_CLOSE);
            else {
                String[] searchValues = searchParam.getValue().toLowerCase().split(",");
                for (int i=0;i<searchValues.length;i++) {
                    if (i>0)
                        query.append(OR);
                    query.append(LOWER_BEGIN).append(searchParam.getKey()).append(BRACKET_CLOSE).append(LIKE_BEGIN).append(searchValues[i].toLowerCase()).append(LIKE_END);
                }
                //query.append(LOWER_BEGIN).append(searchParam.getKey()).append(BRACKET_CLOSE).append(LIKE_BEGIN).append(searchParam.getValue().toLowerCase()).append(LIKE_END);
            }
        });
        if(searchRequest.getSearchParam().size()>0)
            query.append(BRACKET_CLOSE);

        log.info("Query generated:\n {}", query.toString());
        return customQueryExecutor.executeSearchQuery(query.toString());//"Select id from job where id < 20;");
    }

    /**
     * Service method to create a list of TechRoleCompetency per job.
     * It will be used by companies with LDEB subscription
     * @param jobId
     * @return
     * @throws Exception
     */
    @Override
    public List<TechRoleCompetencyBean> getTechRoleCompetencyByJob(Long jobId) throws Exception {
        Job job = jobRepository.getOne(jobId);

        //check if job is null for jobId
        if( null  == job ){
            log.error("job not found for id {}", jobId);
            throw new WebException("No job with id "+jobId+" found.", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // check if company has LDEB subscription or not
        if(!IConstant.CompanySubscription.LDEB.toString().equalsIgnoreCase(job.getCompanyId().getSubscription())){
            log.error("Unauthorized access");
            throw new WebException("You don't have LDEB subscription.", HttpStatus.UNAUTHORIZED);
        }

        List<TechRoleCompetencyBean> techRoleCompetencyBeans = new ArrayList<>();

        //Find all jcm for job id
        List<JobCandidateMapping> jobCandidateMappings = jobCandidateMappingRepository.findAllByJobId(jobId);

        log.info("Found {} records", jobCandidateMappings.size());

        if(jobCandidateMappings.size()>0){
            ObjectMapper mapper = new ObjectMapper();
            //Create TechRoleCompetencyBean object and add it to list of TechRoleCompetencyBean
            jobCandidateMappings.stream().parallel().forEach(jcm->{
                try {
                    TechRoleCompetencyBean techRoleCompetencyBean = new TechRoleCompetencyBean();
                    techRoleCompetencyBean.setCandidate(new Candidate(jcm.getDisplayName(), jcm.getEmail(), jcm.getMobile()));
                    techRoleCompetencyBean.setScore(jcm.getScore());
                    techRoleCompetencyBean.setTechResponseJson(
                            jcm.getTechResponseData().getTechResponse()!=null?
                                    //Map string of array of TechResponseJson to array of TechResponseBean
                                    // and convert it to list, then assign it to techRoleCompetencyBean object
                                    Arrays.asList(mapper.readValue(jcm.getTechResponseData().getTechResponse(), TechResponseJson[].class))
                                    :
                                    null
                    );

                    List<JcmProfileSharingDetails> jcmProfileSharingDetails = jcmProfileSharingDetailsRepository.findByJobCandidateMappingId(jcm.getId());
                    if(jcmProfileSharingDetails.size()>0){
                        techRoleCompetencyBean.setCandidateProfileLink(environment.getProperty("shareProfileLink") + jcmProfileSharingDetails.get(0).getId());
                    }
                    techRoleCompetencyBeans.add(techRoleCompetencyBean);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        else{
            log.info("No candidates found for job id {}", jobId);
        }

        return techRoleCompetencyBeans;
    }
}