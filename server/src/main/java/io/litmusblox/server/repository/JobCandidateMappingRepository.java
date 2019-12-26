/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.*;
import io.litmusblox.server.service.CandidateInteractionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Repository class for JobCandidateMapping
 *
 * @author : Shital Raval
 * Date : 10/7/19
 * Time : 5:23 PM
 * Class Name : JobCandidateMappingRepository
 * Project Name : server
 */
public interface JobCandidateMappingRepository extends JpaRepository<JobCandidateMapping, Long> {

    //find by job and stage id
    @Transactional (readOnly = true)
    List<JobCandidateMapping> findByJobAndStageInAndRejectedIsFalse(Job job, List<JobStageStep> stage) throws Exception;

    //find all rejected candidates
    List<JobCandidateMapping> findByJobAndRejectedIsTrue(Job job) throws Exception;

    //find count of candidates per stage
    @Transactional(readOnly = true)
    @Query(value = "select stage, count(candidate_id) from job_candidate_mapping where job_id=:jobId and rejected is false group by stage", nativeQuery = true)
    List<Object[]> findCandidateCountByStage(Long jobId) throws Exception;

    @Transactional(readOnly = true)
    @Query(value = "select count(candidate_id) from job_candidate_mapping where job_id=:jobId and rejected is true", nativeQuery = true)
    int findRejectedCandidateCount(Long jobId) throws Exception;

    //find count of candidates per stage
    @Transactional
    @Query(value = "select job_candidate_mapping.job_id, stage_name, count(candidate_id) from job_candidate_mapping, job_stage_step, company_stage_step, stage_master\n" +
            "where job_candidate_mapping.job_id in :jobIds " +
            "and job_candidate_mapping.stage = job_stage_step.id\n" +
            "and job_candidate_mapping.rejected=:rejected\n" +
            "and job_stage_step.stage_step_id = company_stage_step.id\n" +
            "and company_stage_step.stage = stage_master.id\n" +
            "and job_stage_step.job_id=job_candidate_mapping.job_id\n" +
            "group by job_candidate_mapping.job_id, stage_name order by job_candidate_mapping.job_id", nativeQuery = true)
    List<Object[]> findCandidateCountByStageJobIds(List<Long> jobIds, boolean rejected) throws Exception;

    //find by job and Candidate
    @Transactional
    JobCandidateMapping findByJobAndCandidate(Job job, Candidate candidate);

    //find by jobId and CandidateId
    @Transactional
    JobCandidateMapping findByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Query(value = "select COUNT(jcm) from JOB_CANDIDATE_MAPPING jcm where jcm.CREATED_ON >=:createdOn and jcm.CREATED_BY =:user", nativeQuery = true)
    Integer getUploadedCandidateCount(@Param("createdOn") Date createdOn, @Param("user") User user);

    @Transactional
    JobCandidateMapping findByChatbotUuid(UUID uuid) throws Exception;

    @Transactional
    @Query(value = "select j.id as jobId, j.job_title as jobTitle, (select step from company_stage_step where id =(select stage_step_id from job_stage_step where id = jcm.stage)) as currentStatus, \n" +
            "jcm.created_on as sourcedOn, (select value from master_data where id = jcm.stage) as lastStage, (select CONCAT(first_name, last_name) from users where id=j.hiring_manager) as hiringManager, \n" +
            "(select CONCAT(first_name, last_name) from users where id=j.recruiter) as recruiter\n" +
            "from job_candidate_mapping jcm \n" +
            "inner join job j on j.id = jcm.job_id\n" +
            "where jcm.candidate_id =:candidateId order by jcm.created_on desc", nativeQuery = true)
    List<CandidateInteractionHistory> getCandidateInteractionHistoryByCandidateId(Long candidateId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set stage = :newStageId, rejected = false, updated_by = :updatedBy, updated_on = :updatedOn where stage = :oldStageId and id in :jcmList")
    void updateStageStepId(List<Long> jcmList, Long oldStageId, Long newStageId, Long updatedBy, Date updatedOn);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select count(distinct stage) from job_candidate_mapping where id in :jcmList")
    int countDistinctStageForJcmList(List<Long> jcmList) throws Exception;

    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set rejected=true, updated_by=:updatedBy, updated_on = :updatedOn where id in :jcmList")
    void updateForRejectStage(List<Long> jcmList, Long updatedBy, Date updatedOn);
}