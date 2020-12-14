/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JobRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JobRoleRepository extends JpaRepository<JobRole, Long> {

    @Transactional
    @Modifying
    @Query(value = "delete from job_role where job =:jobId", nativeQuery = true)
    void deleteByJob(Long jobId);
}
