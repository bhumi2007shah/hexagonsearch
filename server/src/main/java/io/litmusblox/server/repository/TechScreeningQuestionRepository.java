/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.TechScreeningQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 13/04/20
 * Time : 8:21 AM
 * Class Name : TechScreeningQuestionRepository
 * Project Name : server
 */
public interface TechScreeningQuestionRepository extends JpaRepository<TechScreeningQuestion, Long> {

    @Transactional
    void deleteByJobId(Long jobId);

    @Transactional(readOnly = true)
    List<TechScreeningQuestion> findByJobId(Long jobId);

    @Transactional(readOnly = true)
    boolean existsByJobIdAndQuestionCategory(Long id, String key);

    @Transactional(readOnly = true)
    void deleteByJobIdAndQuestionCategoryNotIn(Long id, List<String> selectedKeySkills);
}
