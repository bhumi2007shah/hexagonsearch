/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JobHiringTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 16/7/19
 * Time : 3:16 PM
 * Class Name : JobHiringTeamRepository
 * Project Name : server
 */
public interface JobHiringTeamRepository extends JpaRepository<JobHiringTeam, Long> {

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    List<JobHiringTeam> findByJobId(Long jobId);

    @Transactional
    void deleteByJobId(Long jobId);
}
