/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.service.JCMAllDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : sameer
 * Date : 13/10/20
 * Time : 1:30 PM
 * Class Name : JcmAllDetailsRepository
 * Project Name : server
 */
public interface JcmAllDetailsRepository extends JpaRepository<JCMAllDetails, Long> {
    @Transactional
    List<JCMAllDetails> findAllByJobIdAndChatbotStatus(Long jobId, String chatbotStatus);

    @Query(nativeQuery = true, value = "Select * from job_candidate_mapping_all_details where job_id=:jobId and rejected is true")
    @Transactional
    List<JCMAllDetails> findByJobAndRejectedIsTrue(Long jobId);

    @Query(nativeQuery = true, value = "Select * from job_candidate_mapping_all_details where job_id=:jobId and stage=:stage and rejected is false")
    @Transactional
    List<JCMAllDetails> findByJobAndStageInAndRejectedIsFalse(Long jobId,Long stage);

    @Query(nativeQuery = true, value = "select * from job_candidate_mapping_all_details where id in (Select jcm_id from hiring_manager_workspace_details where user_id=:userId and job_id=:jobId and stage=:stage)")
    @Transactional
    List<JCMAllDetails> findJcmListForHiringManager(Long userId, Long jobId,Long stage);
}
