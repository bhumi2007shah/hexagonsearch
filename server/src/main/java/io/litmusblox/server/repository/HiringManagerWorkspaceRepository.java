/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.HiringManagerWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Date : 11/11/20
 * Time : 11:22 PM
 * Class Name : HiringManagerWorkspaceRepository
 * Project Name : server
 */
public interface HiringManagerWorkspaceRepository extends JpaRepository<HiringManagerWorkspace, Long> {

    @Transactional
    @Query(value = "select email_sent_on from  jcm_profile_sharing_details where id = (select share_profile_id from hiring_manager_workspace where jcm_id =:jcmId and user_id =:userId)", nativeQuery = true)
    Date getProfileSharedOnByJcmIdAndUserId(Long jcmId, Long userId);

    @Transactional
    HiringManagerWorkspace findByJcmIdAndUserId(Long jcmId, Long userId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update hiring_manager_workspace set share_profile_id =:shareProfileId where id=:id")
    void updateProfileShareId(Long shareProfileId, Long id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update hiring_manager_workspace set share_interview_id =:interviewerId where id=:id")
    void updateInterviewShareId(Long interviewerId, Long id);

}
