/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.JcmProfileSharingDetails;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.JobCandidateMapping;

import java.util.List;

/**
 * Date : 11/11/20
 * Time : 11:24 AM
 * Class Name : IHiringManagerWorkspaceService
 * Project Name : server
 */
public interface IHiringManagerWorkspaceService {

    /**
     * Service to fetch jcmList for stage and job id
     * @param stage stage for which details is required
     * @param jobId for which job id we want data
     * @return all required details for the logged in hiring manager and stage
     * @throws Exception
     */
    SingleJobViewResponseBean getHiringManagerWorkspaceDetails(String stage, Long jobId) throws Exception;

    /**
     * to fetch the candidate profile which the hiring manager has selected
     * @param jcmId for user whose profile is to be fetched
     * @return details of the candidates whose profile is fetched.
     * @throws Exception
     */
    JobCandidateMapping fetchCandidateProfile(Long jcmId) throws Exception;

    /**
     * To fetch job details for hiring manager
     * @param jobId id whose job details is required.
     * @return all relevant job details
     * @throws Exception
     */
    Job getJobDetails(Long jobId) throws Exception;

    /**
     * update hiring manager interest for a particular profile
     * @param jcmProfileSharingDetails contains profile sharing id, hiring manager interest, comments and rejection reason if rejected
     * @throws Exception
     */
    void getHiringManagerInterest(JcmProfileSharingDetails jcmProfileSharingDetails) throws Exception;

    /**
     * Api for retrieving a list of jobs who's at least one candidate shared with hiring manager
     * @return response bean with a list of jobs
     * @throws Exception
     */
    JobWorspaceResponseBean findAllJobsForShareProfileToHiringManager() throws Exception;

    void setTechQuestionForJob(Job job) throws Exception;

}
