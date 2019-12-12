/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CvParsingDetails;
import io.litmusblox.server.model.JobCandidateMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 30/08/19
 * Time : 1:34 PM
 * Class Name : CvParsingDetailsRepository
 * Project Name : server
 */
@Repository
public interface CvParsingDetailsRepository extends JpaRepository<CvParsingDetails, Long> {

    List<CvParsingDetails> findByRchilliJsonProcessed(boolean rchilliJsonProcessed);

    @Transactional
    @Query(nativeQuery = true, value = "select * from cv_parsing_details where cv_rating_api_flag is false and (processing_status is null or processing_status = 'Success') and parsing_response_text is not null and length(parsing_response_text)>0 order by id desc limit 10")
    List<CvParsingDetails> findCvRatingRecordsToProcess();

    @Transactional
    void deleteByJobCandidateMappingId(JobCandidateMapping jobCandidateMapping);

    @Transactional
    @Query(nativeQuery = true, value = "select * from cv_parsing_details where processing_status='Failure' and cv_file_name like (concat('%',:jobId,'%'))")
    List<CvParsingDetails> getRchilliErrorResonseBeanList(Long jobId);
}
