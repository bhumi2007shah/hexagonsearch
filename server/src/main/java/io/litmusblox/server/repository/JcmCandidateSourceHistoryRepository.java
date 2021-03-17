/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmCandidateSourceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository interface for JcmCandidateSourceHistory class
 *
 * @author : sameer
 * Date : 16/03/20
 * Time : 5:25 PM
 * Class Name : JcmCandidateSourceHistoryRepository
 * Project Name : server
 */
public interface JcmCandidateSourceHistoryRepository extends JpaRepository<JcmCandidateSourceHistory, Long> {

    @Transactional
    void deleteByJobCandidateMappingId(Long jcmId);
}
