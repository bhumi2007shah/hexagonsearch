/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.ScreeningQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    @Query(value = "select sq.* from screening_question sq inner join master_data md on md.id = sq.question_category where md.type = 'questionCategory' and sq.country_id =:countryId order by cast(md.value_to_use as integer) asc", nativeQuery = true)
    List<ScreeningQuestions> findByCountryIdAndQuestionCategory(Long countryId);
}
