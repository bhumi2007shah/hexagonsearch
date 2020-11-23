/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CvRatingSkillKeywordDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 23/10/19
 * Time : 10:21 AM
 * Class Name : CvRatingSkillKeywordDetailsRepository
 * Project Name : server
 */
@Repository
public interface CvRatingSkillKeywordDetailsRepository extends JpaRepository<CvRatingSkillKeywordDetails, Long> {
    List<CvRatingSkillKeywordDetails> findByCvRatingId(Long id);
}