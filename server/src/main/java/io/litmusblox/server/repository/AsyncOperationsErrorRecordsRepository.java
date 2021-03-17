/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.AsyncOperationsErrorRecords;
import io.litmusblox.server.model.JobCandidateMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : shital
 * Date : 25/2/20
 * Time : 1:03 PM
 * Class Name : AsyncOperationsErrorRecordsRepository
 * Project Name : server
 */
public interface AsyncOperationsErrorRecordsRepository extends JpaRepository<AsyncOperationsErrorRecords, Long> {
    List<AsyncOperationsErrorRecords> findAllByJobIdAndAsyncOperation(Long jobId, String asyncOperation);

    @Transactional
    void deleteByJobCandidateMappingId(JobCandidateMapping jobCandidateMapping);
}
