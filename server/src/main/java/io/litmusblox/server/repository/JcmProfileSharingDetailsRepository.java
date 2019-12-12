/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmProfileSharingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
