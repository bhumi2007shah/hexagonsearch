/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.service.HiringManagerWorkspaceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Date : 11/11/20
 * Time : 3:26 PM
 * Class Name : HiringManagerWorkspaceDetailsRepository
 * Project Name : server
 */
public interface HiringManagerWorkspaceDetailsRepository extends JpaRepository<HiringManagerWorkspaceDetails, Long> {

    @Transactional
    @Query(value = "select hmwd.jcm_id from hiring_manager_workspace_details as hmwd where hmwd.user_id =:userId and hmwd.job_id =:jobId limit 1;", nativeQuery = true)
    Integer existsByUserIdAndJobId(Long userId, Long jobId);
}
