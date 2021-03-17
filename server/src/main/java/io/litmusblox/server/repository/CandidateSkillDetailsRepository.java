/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CandidateSkillDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 25/07/19
 * Time : 8:13 PM
 * Class Name : CandidateSkillDetailsRepository
 * Project Name : server
 */
public interface CandidateSkillDetailsRepository extends JpaRepository<CandidateSkillDetails, Long> {

    @Transactional
    void deleteByCandidateId(Long candidateId);
    @Transactional
    List<CandidateSkillDetails>  findByCandidateId(Long candidateId);
}
