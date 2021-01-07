/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.*;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Date : 11/11/20
 * Time : 12:48 PM
 * Class Name : HiringManagerWorkspaceService
 * Project Name : server
 */
@Log4j2
@Service
public class HiringManagerWorkspaceService extends AbstractAccessControl implements IHiringManagerWorkspaceService {

    @Resource
    HiringManagerWorkspaceDetailsRepository hiringManagerWorkspaceDetailsRepository;

    @Resource
    HiringManagerWorkspaceRepository hiringManagerWorkspaceRepository;

    @Resource
    JcmProfileSharingDetailsRepository jcmProfileSharingDetailsRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    RejectionReasonMasterDataRepository rejectionReasonMasterDataRepository;

    @Resource
    JcmHistoryRepository jcmHistoryRepository;

    @Resource
    InterviewDetailsRepository interviewDetailsRepository;

    @Autowired
    IJobCandidateMappingService jobCandidateMappingService;

    @Autowired
    JobService jobService;

    @Resource
    JobRepository jobRepository;

    @Resource
    JcmAllDetailsRepository jcmAllDetailsRepository;

    @Resource
    CandidateScreeningQuestionResponseRepository candidateScreeningQuestionResponseRepository;

    /**
     * Service to fetch jcmList for stage and job id
     * @param stage stage for which details is required
     * @param jobId for which job id we want data
     * @return all required details for the logged in hiring manager and stage
     * @throws Exception
     */
    public SingleJobViewResponseBean getHiringManagerWorkspaceDetails(String stage, Long jobId) throws Exception{

        log.info("Inside get all details for stage {}", stage);
        Long startTime = System.currentTimeMillis();

        if(IConstant.Stage.Source.getValue().equals(stage) || IConstant.Stage.Screen.getValue().equals(stage))
            throw new ValidationException("Invalid Request", HttpStatus.BAD_REQUEST);

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Logged in user is: {}", loggedInUser.getDisplayName());
        SingleJobViewResponseBean responseBean = new SingleJobViewResponseBean();

        List<JCMAllDetails> allWorkspaceDetails = jcmAllDetailsRepository.findJcmListForHiringManager(loggedInUser.getId(), jobId, MasterDataBean.getInstance().getStageStepMap().get(MasterDataBean.getInstance().getStageStepMasterMap().get(stage)).getId());

        if (IConstant.Stage.Interview.getValue().equalsIgnoreCase(stage)) {
            allWorkspaceDetails.forEach(entry ->{
                entry.getInterviewDetails().add(interviewDetailsRepository.findLatestEntryByJcmId(entry.getId()));
            });
        }
        allWorkspaceDetails.forEach(jcm->{
           //Set share profile id
            jcm.setProfileSharedOn(hiringManagerWorkspaceRepository.getProfileSharedOnByJcmIdAndUserId(jcm.getId(), loggedInUser.getId()));
        });
        responseBean.setJcmAllDetailsList(allWorkspaceDetails);
        //Set candidate count by stage
        List<Object[]> stageCountListView = jobCandidateMappingRepository.findCandidateCountByStageForHiringManager(loggedInUser.getId(), jobId);
        Map<Long, StageStepMaster> stageStepMasterMap = MasterDataBean.getInstance().getStageStepMap();
        stageCountListView.stream().forEach(objArray -> {
            String key = stageStepMasterMap.get(((Integer) objArray[0]).longValue()).getStage();
            if (null == responseBean.getCandidateCountByStage().get(key))
                responseBean.getCandidateCountByStage().put(key, ((BigInteger) objArray[1]).intValue());
            else //stage exists in response bean, add the count of the other step to existing value
                responseBean.getCandidateCountByStage().put(key,responseBean.getCandidateCountByStage().get(key)  + ((BigInteger) objArray[1]).intValue());
        });

        //add count of rejected candidates
        responseBean.getCandidateCountByStage().put(IConstant.Stage.Reject.getValue(),  jobCandidateMappingRepository.findRejectedCandidateCountForHiringManager(loggedInUser.getId(), jobId));

        log.info("Completed fetching all details in {} ms", System.currentTimeMillis() - startTime);
        return responseBean;
    }

    /**
     * to fetch the candidate profile which the hiring manager has selected
     * @param jcmId for user whose profile is to be fetched
     * @return details of the candidates whose profile is fetched.
     * @throws Exception
     */
    public JobCandidateMapping fetchCandidateProfile(Long jcmId) throws Exception{

        log.info("Inside fetchCandidateProfile for hiring manager for jcmId : {}", jcmId);
        Long startTime = System.currentTimeMillis();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        HiringManagerWorkspace workspaceEntry = hiringManagerWorkspaceRepository.findByJcmIdAndUserId(jcmId, loggedInUser.getId());

        if(null == workspaceEntry)
            throw new ValidationException("You are not a valid user.", HttpStatus.UNAUTHORIZED);
        JobCandidateMapping responseObj = jobCandidateMappingService.getCandidateProfile(jcmId, true);

        if(null != workspaceEntry.getShareProfileId()&& responseObj.getStage().getStage().equals(IConstant.Stage.ResumeSubmit.getValue())) {
            JcmProfileSharingDetails details = jcmProfileSharingDetailsRepository.getOne(workspaceEntry.getShareProfileId());
            if (null != details.getHiringManagerInterestDate()) {
                responseObj.setCollectInterest(true);
                responseObj.setHiringManagerInterestDate(details.getHiringManagerInterestDate());
            }
            responseObj.setShareProfileId(workspaceEntry.getShareProfileId());
        }
        log.info("Completed fetching Candidate Profile Details in {} ms", System.currentTimeMillis() - startTime);
        return responseObj;
    }

    /**
     * To fetch job details for hiring manager
     * @param jobId id whose job details is required.
     * @return all relevant job details
     * @throws Exception
     */
    public Job getJobDetails(Long jobId) throws Exception {
        log.info("Inside get job details for hiring manager for job id: {}", jobId);
        Long startTime = System.currentTimeMillis();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!hiringManagerWorkspaceDetailsRepository.existsByUserIdAndJobId(loggedInUser.getId(), jobId))
            throw new ValidationException("You are not a valid user.", HttpStatus.UNAUTHORIZED);
        Job responseObj = jobService.getJobDetails(jobId, true);
        log.info("Completed get Job Details in {} ms", System.currentTimeMillis() - startTime);
        return responseObj;
    }

    /**
     * update hiring manager interest for a particular profile
     * @param jcmProfileSharingDetails contains profile sharing id, hiring manager interest, comments and rejection reason if rejected
     * @throws Exception
     */
    public void getHiringManagerInterest(JcmProfileSharingDetails jcmProfileSharingDetails) throws Exception {
        log.info("Inside capture hiring Manager Interest for profile id: {}", jcmProfileSharingDetails.getId() );
        Long startTime = System.currentTimeMillis();
        JcmProfileSharingDetails existingJcmProfileSharingDetails = jcmProfileSharingDetailsRepository.getOne(jcmProfileSharingDetails.getId());
        if(null == existingJcmProfileSharingDetails)
            throw new WebException("Profile not found", HttpStatus.UNPROCESSABLE_ENTITY);
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HiringManagerWorkspace workspaceEntry = hiringManagerWorkspaceRepository.findByJcmIdAndUserId(existingJcmProfileSharingDetails.getJobCandidateMappingId(), loggedInUser.getId());
       if(null == workspaceEntry || workspaceEntry.getShareProfileId() == null)
            throw new ValidationException("You are not a valid user.", HttpStatus.UNAUTHORIZED);
        existingJcmProfileSharingDetails.setHiringManagerInterestDate(new Date());
        existingJcmProfileSharingDetails.setHiringManagerInterest(jcmProfileSharingDetails.getHiringManagerInterest());
        JobCandidateMapping jcmObj = jobCandidateMappingRepository.getOne(existingJcmProfileSharingDetails.getJobCandidateMappingId());
        StringBuffer jcmHistoryMsg = new StringBuffer("Hiring Manager ").append(loggedInUser.getDisplayName()).append(" is").append(jcmProfileSharingDetails.getHiringManagerInterest()?" interested ":" not interested ").append("in this Profile");
        if(!jcmProfileSharingDetails.getHiringManagerInterest()){
            if(null == jcmProfileSharingDetails.getRejectionReason().getId())
                throw new ValidationException("Invalid Request", HttpStatus.BAD_REQUEST);
            else{
                existingJcmProfileSharingDetails.setRejectionReason(jcmProfileSharingDetails.getRejectionReason());
                RejectionReasonMasterData rejectionReasonMasterData = rejectionReasonMasterDataRepository.getOne(jcmProfileSharingDetails.getRejectionReason().getId());
                jcmHistoryMsg.append(", Rejection Reason: ").append(rejectionReasonMasterData.getValue()).append("- ").append(rejectionReasonMasterData.getLabel());
            }
        }
        if(null != jcmProfileSharingDetails.getComments()) {
            if (jcmProfileSharingDetails.getComments().length() > IConstant.MAX_FIELD_LENGTHS.HIRING_MANAGER_COMMENTS.getValue())
                jcmProfileSharingDetails.setComments(Util.truncateField(jcmObj.getCandidate(), IConstant.MAX_FIELD_LENGTHS.HIRING_MANAGER_COMMENTS.name(), IConstant.MAX_FIELD_LENGTHS.HIRING_MANAGER_COMMENTS.getValue(), jcmProfileSharingDetails.getComments()));

            existingJcmProfileSharingDetails.setComments(jcmProfileSharingDetails.getComments());
            jcmHistoryMsg.append(", Comments: ").append(existingJcmProfileSharingDetails.getComments());
        }
        jcmProfileSharingDetailsRepository.save(existingJcmProfileSharingDetails);
        jcmHistoryRepository.save(new JcmHistory(jcmObj, jcmHistoryMsg.toString(), new Date(), null, jcmObj.getStage(), true));
        log.info(jcmHistoryMsg.toString());
        log.info("Completed getHiringManagerInterest call in {} ms", System.currentTimeMillis() - startTime);
    }

    /**
     * Api for retrieving a list of jobs who's at least one candidate shared with hiring manager
     * @return response bean with a list of jobs
     * @throws Exception
     */
    @Transactional
    public JobWorspaceResponseBean findAllJobsForShareProfileToHiringManager() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Inside job list for hiring Manager for HMId: {}", loggedInUser.getId());
        Long startTime = System.currentTimeMillis();

        List<Job> jobListForHiringManager = jobRepository.getJobListForHiringManager(loggedInUser.getId());

        JobWorspaceResponseBean responseBean = new JobWorspaceResponseBean();
        if(null != jobListForHiringManager && jobListForHiringManager.size()>0){
            responseBean.setListOfJobs(jobListForHiringManager);
            responseBean.setLiveJobs(jobListForHiringManager.size());
        }
        //Set candidate count per stage
        List<Object[]> countByStage = jobCandidateMappingRepository.getCandidateCountByStageForHmw(loggedInUser.getId());
        if(null != countByStage.get(0)[0]){
            responseBean.setSourcingCandidateCount(Integer.parseInt(countByStage.get(0)[0].toString()));
            responseBean.setScreeningCandidateCount(Integer.parseInt(countByStage.get(0)[1].toString()));
            responseBean.setSubmittedCandidateCount(Integer.parseInt(countByStage.get(0)[2].toString()));
            responseBean.setInterviewCandidateCount(Integer.parseInt(countByStage.get(0)[3].toString()));
            responseBean.setMakeOfferCandidateCount(Integer.parseInt(countByStage.get(0)[4].toString()));
            responseBean.setOfferCandidateCount(Integer.parseInt(countByStage.get(0)[5].toString()));
            responseBean.setHiredCandidateCount(Integer.parseInt(countByStage.get(0)[6].toString()));
            responseBean.setRejectedCandidateCount(jobCandidateMappingRepository.getRejectedCandidateCountForHmw(loggedInUser.getId()));
        }

        log.info("Completed getJobListForHiringManager in {} ms", System.currentTimeMillis() - startTime);
        return responseBean;
    }
}
