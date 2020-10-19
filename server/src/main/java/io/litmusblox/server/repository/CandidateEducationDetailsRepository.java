/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;


import io.litmusblox.server.model.CandidateEducationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Sumit
 * Date : 25/07/19
 * Time : 5:52 PM
 * Class Name : CandidateEducationDetailsRepository
 * Project Name : server
 */
public interface CandidateEducationDetailsRepository extends JpaRepository<CandidateEducationDetails, Long> {

    @Transactional
    void deleteByCandidateId(Long candidateId);

    @Transactional
    @Query(value = "select * from candidate_education_details where candidate_id=:candidateId", nativeQuery = true)
    CandidateEducationDetails findByCandidateId(long candidateId);

    @Transactional
    @Query(value="select * from candidate_education_details where candidate_id=:candidateId and degree=:degree", nativeQuery = true)
    CandidateEducationDetails findByCandidateIdandDegree(long candidateId, String degree);
}
