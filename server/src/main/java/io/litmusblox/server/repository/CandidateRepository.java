/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Sumit
 * Date : 17/7/19
 * Time : 8:05 PM
 * Class Name : CandidateRepository
 * Project Name : server
 */
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Transactional
    @Query(nativeQuery = true, value = "select * from candidate where id=(select candidate_id from candidate_online_profile where profile_type=:profileType and url like CONCAT('%', :uniqueId, '%'))")
    Candidate findCandidateByProfileTypeAndUniqueId(String profileType, String uniqueId);

    /*@Transactional
    @Query(nativeQuery = true, value = "select * from candidate where id in (select candidate_id from job_candidate_mapping where job_id=:jobId)")
    List<Candidate> findAllCandidateByJobId(Long jobId);*/
}
