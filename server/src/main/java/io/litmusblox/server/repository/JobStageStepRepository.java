/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JobStageStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 19/11/19
 * Time : 1:57 PM
 * Class Name : JobStageStepRepository
 * Project Name : server
 */
@Repository
public interface JobStageStepRepository extends JpaRepository<JobStageStep, Long> {
    @Query(nativeQuery = true, value = "select job_stage_step.* from job_stage_step, company_stage_step, stage_master\n" +
            "where stage_master.id = company_stage_step.stage\n" +
            "and job_stage_step.stage_step_id = company_stage_step.id\n" +
            "and job_id = :jobId\n" +
            "and stage_master.stage_name = :stage")
    @Transactional(readOnly = true)
    List<JobStageStep> findStageIdForJob(Long jobId, String stage);

    @Transactional(readOnly = true)
    List<JobStageStep> findByJobId(Long jobId) throws Exception;

    @Transactional
    void deleteByJobId(Long jobId);
}
