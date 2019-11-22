/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.*;
import io.litmusblox.server.service.CandidateInteractionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
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
    @Transactional
    List<JobCandidateMapping> findByJobAndStage(Job job, MasterData stage) throws Exception;

    //find count of candidates per stage
    @Transactional
    @Query(value = "select stage, count(candidate_id) from job_candidate_mapping where job_id=:jobId group by stage", nativeQuery = true)
    List<Object[]> findCandidateCountByStage(Long jobId) throws Exception;


    //find count of candidates per stage
    @Transactional
    @Query(value = "select job_id, stage, count(candidate_id) from job_candidate_mapping where job_id in :jobIds group by job_id, stage order by job_id", nativeQuery = true)
    List<Object[]> findCandidateCountByStageJobIds(List<Long> jobIds) throws Exception;

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
    @Query(value = "select j.id as jobId, j.job_title as jobTitle, (select value from master_data where id = jcm.stage) as currentStatus, \n" +
            "jcm.created_on as sourcedOn, (select value from master_data where id = jcm.stage) as lastStage, (select CONCAT(first_name, last_name) from users where id=j.hiring_manager) as hiringManager, \n" +
            "(select CONCAT(first_name, last_name) from users where id=j.recruiter) as recruiter\n" +
            "from job_candidate_mapping jcm \n" +
            "inner join job j on j.id = jcm.job_id\n" +
            "where jcm.candidate_id =:candidateId order by jcm.created_on desc", nativeQuery = true)
    List<CandidateInteractionHistory> getCandidateInteractionHistoryByCandidateId(Long candidateId);
}
