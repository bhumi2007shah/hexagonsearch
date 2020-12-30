/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.model.User;
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.LoginResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
  * @author : Sumit Bagul
  * Date : 29/12/20
  * Time : 4:22 PM
  * Class Name : HarvesterController
  * Project Name : server
 * */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/harvester")
@Log4j2
public class HarvesterController {

    @Autowired
    IJobCandidateMappingService jobCandidateMappingService;

    /**
     * Api to get candidate last updated info
     * @param candidateId candidate id for we want data
     * @param companyId candidate related to which company
     * @return last updated JCM details
     * @throws Exception
     */
    @GetMapping(value = "/candidateProfile")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    String getCandidateProfile(@RequestParam Long candidateId, @RequestParam Long companyId) throws Exception {
        return Util.stripExtraInfoFromResponseBean(jobCandidateMappingService.getCandidateProfileForHarvester(candidateId,companyId),
                new HashMap<String, List<String>>() {{
                    put("User", Arrays.asList("displayName"));
                    put("ScreeningQuestions", Arrays.asList("id","question","options"));
                    put("CvRating", Arrays.asList("overallRating"));
                    put("JobStageStep", new ArrayList<>(0));
                    put("JobRole", Arrays.asList("role"));
                    put("Job",new ArrayList<>(0));
                }},
                new HashMap<String, List<String>>() {{
                    put("JobCapabilities", Arrays.asList("jobCapabilityStarRatingMappingList","jobId"));
                    put("Candidate",Arrays.asList("id","createdBy","createdOn","updatedBy","updatedOn","uploadErrorMessage", "firstName", "lastName","email","mobile", "candidateSource"));
                    put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company", "questionType"));
                    put("UserScreeningQuestion", Arrays.asList("createdOn","createdBy","updatedOn","userId","questionType"));
                    put("JobCandidateMapping", Arrays.asList("createdOn","createdBy","updatedOn","updatedBy","techResponseData","candidateSource","candidateInterest","candidateInterestDate","candidateFirstName","candidateLastName","chatbotUuid", "stage", "candidateReferralDetail", "candidateSourceHistories"));
                    put("CandidateDetails", Arrays.asList("id","candidateId"));
                    put("CandidateEducationDetails", Arrays.asList("id","candidateId"));
                    put("CandidateLanguageProficiency", Arrays.asList("id","candidateId"));
                    put("CandidateOnlineProfile", Arrays.asList("id","candidateId"));
                    put("CandidateProjectDetails", Arrays.asList("id","candidateId"));
                    put("CandidateCompanyDetails", Arrays.asList("id","candidateId"));
                    put("CandidateSkillDetails", Arrays.asList("id","candidateId"));
                    put("CandidateWorkAuthorization", Arrays.asList("id","candidateId"));
                    put("JobScreeningQuestions", Arrays.asList("createdBy", "createdOn", "updatedOn","updatedBy"));
                    put("MasterData", new ArrayList<>(0));
                    put("CompanyAddress", new ArrayList<>(0));
                }});
    }

}
