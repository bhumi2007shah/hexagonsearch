/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JobKeySkills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 8/7/19
 * Time : 12:12 PM
 * Class Name : JobKeySkillsRepository
 * Project Name : server
 */
public interface JobKeySkillsRepository extends JpaRepository<JobKeySkills, Long> {

    @Transactional
    List<JobKeySkills> findByJobIdAndSeProvided(Long jobId, Boolean mlProvided);

    @Transactional
    void deleteByJobId(Long jobId);

    @Transactional
    List<JobKeySkills> findByJobId(Long jobId);

    @Query(value = "Select skill_name from skills_master where id in (select skill_id from job_key_skills where job_id = :jobId and selected = 't')",nativeQuery = true)
    List<String> findSkillNameByJobId (Long jobId);
}