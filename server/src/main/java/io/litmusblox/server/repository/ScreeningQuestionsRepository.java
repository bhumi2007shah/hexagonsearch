/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Country;
import io.litmusblox.server.model.MasterData;
import io.litmusblox.server.model.ScreeningQuestions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository class for Master screening questions
 *
 * @author : Shital Raval
 * Date : 12/7/19
 * Time : 4:05 PM
 * Class Name : ScreeningQuestionsRepository
 * Project Name : server
 */
public interface ScreeningQuestionsRepository extends JpaRepository<ScreeningQuestions, Long> {
    List<ScreeningQuestions> findByCountryIdAndQuestionCategory(Country country, MasterData questionCategory);
}
