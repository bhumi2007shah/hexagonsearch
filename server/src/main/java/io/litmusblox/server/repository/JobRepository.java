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


    //find all jobs that are not archived
    @Transactional
    List<Job> findByCreatedByAndStatusAndDateArchivedIsNullOrderByCreatedOnDesc(User createdBy, String jobStatus);
    //find all archived jobs
    @Transactional
    List<Job> findByCreatedByAndDateArchivedIsNotNullOrderByCreatedOnDesc(User createdBy);

    //find all jobs for which ml data is not available
    @Transactional
    List<Job> findByMlDataAvailable(Boolean mlDataFlag);

    int countByCreatedBy(User createdBy);

    //find all archived jobs by company
    @Transactional
    List<Job> findByCompanyIdAndDateArchivedIsNotNullOrderByCreatedOnDesc(Company company);

    //find all active jobs by company
    @Transactional
    List<Job> findByCompanyIdAndStatusAndDateArchivedIsNullOrderByCreatedOnDesc(Company company, String jobStatus);

    //count of all job attached to a BU
    @Transactional
    int countByBuId(CompanyBu companyBu);

    //count of all job attached to a company address
    @Transactional
    int countByJobLocationOrInterviewLocation(CompanyAddress jobLocation, CompanyAddress interviewLocation);

    @Transactional(readOnly = true)
    Job findByJobReferenceId(UUID jobReferenceId);

    //find job count per status by createdBy
    @Transactional
    @Query(value = "SELECT sum((status LIKE 'Live')\\:\\:INT) AS liveJobCount, sum((status LIKE 'Draft')\\:\\:INT) As draftJobCount, sum((status LIKE 'Archived')\\:\\:INT) AS archivedJobCount " +
            "FROM job where created_by =:createdBy", nativeQuery = true)
    List<Object[]> getJobCountPerStatusByCreatedBy(Long createdBy);

    //find job count per status by companyId
    @Transactional
    @Query(value = "SELECT sum((status LIKE 'Live')\\:\\:INT) AS liveJobCount, sum((status LIKE 'Draft')\\:\\:INT) As draftJobCount, sum((status LIKE 'Archived')\\:\\:INT) AS archivedJobCount " +
            "FROM job where company_id =:companyId", nativeQuery = true)
    List<Object[]> getJobCountPerStatusByCompanyId(Long companyId);

    @Transactional(readOnly = true)
    List<Job> findByCompanyIdInAndStatus(List<Company> companyList, String status);






}
