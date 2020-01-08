/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmProfileSharingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author : Sumit
 * Date : 02/08/19
 * Time : 2:01 PM
 * Class Name : JcmProfileSharingDetailsRepository
 * Project Name : server
 */
public interface JcmProfileSharingDetailsRepository extends JpaRepository<JcmProfileSharingDetails, Long> {

    JcmProfileSharingDetails findById(UUID id);

    List<JcmProfileSharingDetails> findByJobCandidateMappingId(Long jcmId);

    @Transactional
    void deleteByJobCandidateMappingId(Long jobCandidateMappingId);

    @Transactional
    @Query(value = "select count(details.id) from jcm_profile_sharing_details details, jcm_profile_sharing_master master\n" +
            "where master.sender_id =:userId and master.id = details.profile_sharing_master_id;", nativeQuery = true)
    Integer getProfileSharingCount(Long userId);
}
