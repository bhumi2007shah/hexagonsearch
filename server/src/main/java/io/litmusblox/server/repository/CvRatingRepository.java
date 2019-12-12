/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CvRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Shital Raval
 * Date : 22/10/19
 * Time : 2:55 PM
 * Class Name : CvRatingRepository
 * Project Name : server
 */
@Repository
public interface CvRatingRepository extends JpaRepository<CvRating, Long> {
    CvRating findByJobCandidateMappingId(Long jobCandidateMappingId);

    @Transactional
    void deleteByJobCandidateMappingId(Long jobCandidateMappingId);
}