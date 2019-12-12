/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmCommunicationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository class for JCM Communication Details object
 *
 * @author : Shital Raval
 * Date : 31/7/19
 * Time : 5:21 PM
 * Class Name : JcmCommunicationDetails
 * Project Name : server
 */
@Repository
public interface JcmCommunicationDetailsRepository extends JpaRepository<JcmCommunicationDetails,Long> {
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true,value = "Update Jcm_Communication_Details set chat_Invite_Flag=true where jcm_Id in :jcmIdList")
    void inviteCandidates(List<Long> jcmIdList);

    @Transactional
    JcmCommunicationDetails findByJcmId(Long jcmId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = "Update Jcm_Communication_Details set chat_complete_flag = true where jcm_id =:jcmId")
    void updateByJcmId(Long jcmId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = "update jcm_Communication_Details set hr_chat_complete_flag = true where jcm_id =:jcmId")
    void updateHrChatbotFlagByJcmId(Long jcmId);

    @Transactional
    void deleteByJcmId(Long jobCandidateMappingId);
}
