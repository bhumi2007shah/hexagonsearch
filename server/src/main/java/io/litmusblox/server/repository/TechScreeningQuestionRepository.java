/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.TechScreeningQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : Sumit
 * Date : 13/04/20
 * Time : 8:21 AM
 * Class Name : TechScreeningQuestionRepository
 * Project Name : server
 */
public interface TechScreeningQuestionRepository extends JpaRepository<TechScreeningQuestion, Long> {
}
