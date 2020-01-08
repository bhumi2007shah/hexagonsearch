/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Repository class for Job table related CRUD operations
 *
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 9:41 AM
 * Class Name : JobRepository
 * Project Name : server
 */

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    //find all jobs for which ml data is not available
    @Transactional
    List<Job> findByMlDataAvailable(Boolean mlDataFlag);

    int countByCreatedBy(User createdBy);

    //count of all job attached to a BU
    @Transactional
    int countByBuId(CompanyBu companyBu);

    //count of all job attached to a company address
    @Transactional
    int countByJobLocationOrInterviewLocation(CompanyAddress jobLocation, CompanyAddress interviewLocation);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select job_stage_step.id, stage_master.stage_name\n" +
            "from job_stage_step, company_stage_step, stage_master\n" +
            "where job_stage_step.stage_step_id = company_stage_step.id\n" +
            "and company_stage_step.stage = stage_master.id\n" +
            "and job_stage_step.job_id = :jobId")
    List<Object[]> findStagesForJob(Long jobId) throws Exception;

    @Transactional(readOnly = true)
    Job findByJobReferenceId(UUID jobReferenceId);

    //find job list by createdBy
    @Transactional
    List<Job> findByCreatedByOrderByCreatedOnDesc(User createdBy);

    //find job list by company list
    @Transactional
    List<Job> findByCompanyIdOrderByCreatedOnDesc(List<Company> companyList);
}
