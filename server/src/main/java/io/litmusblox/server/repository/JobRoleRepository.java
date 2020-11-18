/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.JobRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JobRoleRepository extends JpaRepository<JobRole, Long> {

    @Transactional
    void deleteByJob(Job job);
}
