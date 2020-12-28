/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.service.HiringManagerWorkspaceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Date : 11/11/20
 * Time : 3:26 PM
 * Class Name : HiringManagerWorkspaceDetailsRepository
 * Project Name : server
 */
public interface HiringManagerWorkspaceDetailsRepository extends JpaRepository<HiringManagerWorkspaceDetails, Long> {

    @Transactional
    boolean existsByUserIdAndJobId( Long userId, Long jobId);
}
