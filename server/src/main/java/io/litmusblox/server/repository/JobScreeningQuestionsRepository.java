/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JobScreeningQuestions;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 5/7/19
 * Time : 5:17 PM
 * Class Name : JobScreeningQuestionsRepository
 * Project Name : server
 */
public interface JobScreeningQuestionsRepository extends JpaRepository<JobScreeningQuestions, Long> {
    @Transactional
    List<JobScreeningQuestions> findByJobId(Long jobId);

    void deleteByMasterScreeningQuestionIdIsNotNullAndJobId(Long jobId);

    void deleteByUserScreeningQuestionIdIsNotNullAndJobId(Long jobId);

    void deleteByTechScreeningQuestionIdIsNotNullAndJobId(Long jobId);

    @Modifying
    @Transactional
    @Query(value = "delete from job_screening_questions where tech_screening_question_id in (select id from tech_screening_question where question_category not in :questionCategory and job_id =:jobId)", nativeQuery = true)
    void deleteJobScreeningQuestions(Long jobId, List<String> questionCategory);

    @Transactional
    @Cacheable(cacheNames = "userQuestions", key = "#jobId")
    List findByUserScreeningQuestionIdIsNotNullAndJobId(Long jobId);
}
