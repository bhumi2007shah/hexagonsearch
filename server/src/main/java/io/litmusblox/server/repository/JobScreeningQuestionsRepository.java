/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JobScreeningQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
