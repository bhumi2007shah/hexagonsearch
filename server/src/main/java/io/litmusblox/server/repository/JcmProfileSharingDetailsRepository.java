/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmProfileSharingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    @Query(value = "select * from jcm_profile_sharing_details where id = (select share_profile_id from hiring_manager_workspace where jcm_id =:jcmId and user_id =:userId)", nativeQuery = true)
    JcmProfileSharingDetails getProfileSharedByJcmIdAndUserId(Long jcmId, Long userId);
}
