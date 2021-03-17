/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;
import io.litmusblox.server.model.CandidateTechResponseData;
import io.litmusblox.server.model.JobCandidateMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : sameer
 * Date : 23/11/19
 * Time : 11:26 PM
 * Class Name : CandidateTechResponseData
 * Project Name : server
 */
public interface CandidateTechResponseDataRepository extends JpaRepository<CandidateTechResponseData, Long> {
    @Transactional
    void deleteByJobCandidateMappingId(JobCandidateMapping jobCandidateMapping);
}
