/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmProfileSharingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import javax.sound.midi.Receiver;
import java.util.List;
import java.util.Optional;

/**
 * @author : Sumit
 * Date : 02/08/19
 * Time : 2:01 PM
 * Class Name : JcmProfileSharingDetailsRepository
 * Project Name : server
 */
public interface JcmProfileSharingDetailsRepository extends JpaRepository<JcmProfileSharingDetails, Long> {

    List<JcmProfileSharingDetails> findByJobCandidateMappingId(Long jcmId);

    List<JcmProfileSharingDetails> findByJobCandidateMappingIdIn(List<Long> jcmIdList);

    @Transactional
    void deleteByJobCandidateMappingId(Long jobCandidateMappingId);

    @Transactional
    @Query(value = "select count(details.id) from jcm_profile_sharing_details details, hiring_manager_workspace hmw\n"+
            "where hmw.user_id=1 and hmw.share_profile_id=details.id;", nativeQuery = true)
    Integer getProfileSharingCount(Long userId);
}
