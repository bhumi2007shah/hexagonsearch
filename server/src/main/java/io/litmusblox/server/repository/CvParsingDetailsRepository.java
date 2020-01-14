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
    @Query(nativeQuery = true, value = "select * from cv_parsing_details where processing_status='Failure' and cv_file_name like (concat('%_',:jobId,'_%'))")
    List<CvParsingDetails> getRchilliErrorResponseBeanList(Long jobId);

    @Query(value = "select cpd.* from cv_parsing_details cpd\n" +
            "inner join job_candidate_mapping jcm on jcm.id = cpd.job_candidate_mapping_id\n" +
            "where cpd.parsing_response_text is null \n" +
            "and cpd.cv_rating_api_flag = false \n" +
            "and (cpd.processing_status is null or cpd.processing_status = 'Success')\n" +
            "and jcm.candidate_source not in ('File', 'Individual')\n" +
            "and cpd.cv_convert_api_flag = false \n" +
            "and (jcm.cv_file_type is not null and trim(jcm.cv_file_type) != '')", nativeQuery = true)
    List<CvParsingDetails> getDataForConvertCvToCvText();
}
