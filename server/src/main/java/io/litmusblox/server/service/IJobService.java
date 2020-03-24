/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.AsyncOperationsErrorRecords;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.JobHistory;
import io.litmusblox.server.service.impl.SearchRequestBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface definition for Job Service
 *
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 9:45 AM
 * Class Name : IJobService
 * Project Name : server
 */
public interface IJobService {
    /**
     * Add a new job
     * @return Response bean with jobId, and optionally list of skills and capabilities from ML
     * @throws Exception
     */
    Job addJob(Job job, String pageName) throws Exception;

    /**
     * Find all jobs for logged in user
     *
     * @param archived flag indicating if only archived jobs need to be fetched
     * @param companyId id of the company for which jobs have to be found
     * @param jobStatus
     * @return response bean with list of jobs created by the user, count of active jobs and count of archived jobs
     * @throws Exception
     */
    JobWorspaceResponseBean findAllJobsForUser(boolean archived, Long companyId, String jobStatus) throws Exception;

    /**
     * For the specified job, retrieve
     * 1. list candidates for job for specified stage
     * 2. count of candidates by each stage
     *
     * @param jobId the job id for which data is to be retrieved
     * @param stage the stage for which data is to be retrieved
     *
     * @return response bean with all details
     * @throws Exception
     */
    SingleJobViewResponseBean getJobViewById(Long jobId, String stage) throws Exception;

    /**
     * Service method to publish a job
     *
     * @param jobId id of the job to be published
     */
    void publishJob(Long jobId) throws Exception;

    /**
     * Service method to archive a job
     *
     * @param jobId id of the job to be archived
     */
    void archiveJob(Long jobId);

    /**
     * Service method to unarchive a job
     *
     * @param jobId id of the job to be unarchived
     */
    void unarchiveJob(Long jobId) throws Exception;

    /**
     * Service method to get job details by job id
     *
     * @param jobId id for which details will be retrieved
     */
    Job getJobDetails(Long jobId) throws Exception;

    /**
     * Service method to get job history by job id
     *
     * @param jobId id for which history will be retrieved
     *
     * @return a list of job history objects
     */
    List<JobHistory> getJobHistory(Long jobId) throws Exception;

    /**
     * Service method to return supported export formats for a company.
     *
     * @param jobId the job id for which supported formats to be returned
     * @return map of format id and name
     * @throws Exception
     */
    Map<Long, String> getSupportedExportFormat(Long jobId) throws Exception;

    /**
     * Service method to return export json data for a job
     *
     * @param jobId the job id for which export data to be returned
     * @param formatId the format id to format export data
     * @return formatted json in String format
     * @throws Exception
     */
    String exportData(Long jobId, Long formatId, String stage) throws Exception;

    /**
     * Method to find a job based on the reference id provided
     * @param jobReferenceId
     * @return
     */
    Job findByJobReferenceId(UUID jobReferenceId);

    /**
     * Service method to find list of jobs matching the search criteria
     *
     * @param searchRequest the request bean with company id and map of search paramters
     * @return List of jobs
     */
    List<Job> searchJobs(SearchRequestBean searchRequest);

    /**
     * Service method to create a list of TechRoleCompetency per job.
     * It will be used by companies with LDEB subscription
     * @param jobId
     * @return
     * @throws Exception
     */
    List<TechRoleCompetencyBean> getTechRoleCompetencyByJob(Long jobId) throws Exception;

    /**
     * Service method to find job by jobShortCode
     *
     * @param jobShortCode jobShortCode
     * @return job object
     */
    Job findJobByJobShortCode(String jobShortCode);

    /**
     * Service method to find all async error records for a job and async operation.
     * @param jobId
     * @return List of AsyncOperationsErrorRecords
     */
    List<AsyncOperationsErrorRecords> findAsyncErrors(Long jobId, String asyncOperation);
}
