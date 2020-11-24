/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;
import io.litmusblox.server.model.CvRating;
import io.litmusblox.server.model.CvRatingSkillKeywordDetails;
import io.litmusblox.server.model.JobCandidateMapping;
import io.litmusblox.server.repository.CvRatingRepository;
import io.litmusblox.server.repository.CvRatingSkillKeywordDetailsRepository;
import io.litmusblox.server.repository.JobCandidateMappingRepository;
import io.litmusblox.server.service.IBackendDataService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author : Arpan R
 * Date : 24/11/2020
 * Time : 08:00 AM
 * Class Name : BackendDataService
 * Project Name : server
 */

@Service
@Log4j2
public class BackendDataService implements IBackendDataService {

    @Resource
    CvRatingRepository cvRatingRepository;

    @Resource
    CvRatingSkillKeywordDetailsRepository cvRatingSkillKeywordDetailsRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    /**
     * Service method to migrate candidates Cv Rating Data
     *
     * @throws Exception
     */
    @Override
    public void migrateCvRatingData() throws Exception{
        Long startTime = System.currentTimeMillis();
        log.info("Received Request to migrate cv rating data");
        List<CvRating> cvRatingsList = cvRatingRepository.findAll();
        cvRatingsList.forEach(cvRating->{
        JobCandidateMapping objFromDb = jobCandidateMappingRepository.findById(cvRating.getJobCandidateMappingId()).orElse(null);
            if(objFromDb != null) {
                List<CvRatingSkillKeywordDetails> cvRatingSkillKeywordDetailsList = cvRatingSkillKeywordDetailsRepository.findByCvRatingId(cvRating.getId());
                Map<String, Map<String, String>> cv_skill_json = new HashMap<>();
                Map<String, String> weakSkills = new HashMap<>();
                Map<String, String> missingSkills = new HashMap<>();
                Map<String, String> strongSkills = new HashMap<>();
                cvRatingSkillKeywordDetailsList.forEach(cvRatingSkillKeywordDetails -> {
                    if (cvRatingSkillKeywordDetails.getRating() == 3)
                        strongSkills.put(cvRatingSkillKeywordDetails.getSkillName(), String.valueOf(cvRatingSkillKeywordDetails.getOccurrence()));
                    else if (cvRatingSkillKeywordDetails.getRating() == 2)
                        weakSkills.put(cvRatingSkillKeywordDetails.getSkillName(), String.valueOf(cvRatingSkillKeywordDetails.getOccurrence()));
                    else
                        missingSkills.put(cvRatingSkillKeywordDetails.getSkillName(), String.valueOf(cvRatingSkillKeywordDetails.getOccurrence()));
                });
                cv_skill_json.put("1", missingSkills);
                cv_skill_json.put("2", weakSkills);
                cv_skill_json.put("3", strongSkills);
                objFromDb.setOverallRating(cvRating.getOverallRating());
                objFromDb.setCvSkillRatingJson(cv_skill_json);
                jobCandidateMappingRepository.save(objFromDb);
            }
        });
        log.info("completed migrating data in {} ms",System.currentTimeMillis()-startTime);
    }

}
