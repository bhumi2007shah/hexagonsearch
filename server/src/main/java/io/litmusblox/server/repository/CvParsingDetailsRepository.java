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


    @Transactional
    @Query(nativeQuery = true, value = "select * from cv_parsing_details where cv_rating_api_flag is false and (processing_status is null or processing_status = 'Success') and  parsing_response_text is not null and length(trim(parsing_response_text))>=:responseTxtLimit and candidate_skills is not null and CV_RATING_API_CALL_RETRY_COUNT < 4 order by id desc limit 10")
    List<CvParsingDetails> findCvRatingRecordsToProcess(int responseTxtLimit);

    @Transactional
    void deleteByJobCandidateMappingId(JobCandidateMapping jobCandidateMapping);

    @Query(value = "select cpd.* from cv_parsing_details cpd " +
            "inner join job_candidate_mapping jcm on jcm.id = cpd.job_candidate_mapping_id " +
            "inner join (select max(job_id) as jobId from job_skills_attributes where selected = 't' group by job_id) as jsa on jsa.jobId = jcm.job_id " +
            "where jcm.overall_rating is null and (jcm.cv_file_type is not null and trim(jcm.cv_file_type) != '') and cpd.cv_rating_api_flag = false " +
            "order by cpd.id desc limit 10;\n", nativeQuery = true)
    List<CvParsingDetails> getDataForUpdateCvRating();
}
