/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.CandidateDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Sumit
 * Date : 25/7/19
 * Time : 5:09 PM
 * Class Name : CandidateDetailsRepository
 * Project Name : server
 */
public interface CandidateDetailsRepository extends JpaRepository<CandidateDetails, Long> {

    @Transactional
    void deleteByCandidateId(Candidate candidateId);

    CandidateDetails findByCandidateId(Candidate candidate);

}
