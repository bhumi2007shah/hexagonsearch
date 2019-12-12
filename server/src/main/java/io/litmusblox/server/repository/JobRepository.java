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


    //find all jobs that are not archived
    @Transactional
    List<Job> findByCreatedByAndDateArchivedIsNullOrderByCreatedOnDesc(User createdBy);
    //find all archived jobs
    @Transactional
    List<Job> findByCreatedByAndDateArchivedIsNotNullOrderByCreatedOnDesc(User createdBy);

    //count of archived jobs
    @Transactional
    Long countByCreatedByAndDateArchivedIsNotNull(User createdBy);

    //count of active jobs
    @Transactional
    Long countByCreatedByAndDateArchivedIsNull(User createdBy);

    //find all jobs for which ml data is not available
    @Transactional
    List<Job> findByMlDataAvailable(Boolean mlDataFlag);

    int countByCreatedBy(User createdBy);

    //find all archived jobs by company
    @Transactional
    List<Job> findByCompanyIdAndDateArchivedIsNotNullOrderByCreatedOnDesc(Company company);

    //count of all active jobs by company
    @Transactional
    Long countByCompanyIdAndDateArchivedIsNull(Company company);

    //find all active jobs by company
    @Transactional
    List<Job> findByCompanyIdAndDateArchivedIsNullOrderByCreatedOnDesc(Company company);

    //count of all archived jobs by company
    @Transactional
    Long countByCompanyIdAndDateArchivedIsNotNull(Company company);

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
}
