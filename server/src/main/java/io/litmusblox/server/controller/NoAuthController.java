/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.JobCandidateMapping;
import io.litmusblox.server.model.JobScreeningQuestions;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.TechChatbotRequestBean;
import io.litmusblox.server.service.impl.LbUserDetailsService;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Controller for REST apis that do not require authentication. For e.g.
 * 1. Activate user
 * 2. Reset password
 * 3. Fetch screening questions for the uuid
 * 4. Capture candidate interest
 * 5. Capture candidate response to screening question chatbot
 *
 * @author : Shital Raval
 * Date : 23/7/19
 * Time : 9:54 AM
 * Class Name : NoAuthController
 * Project Name : server
 */
@CrossOrigin(allowedHeaders = "*")
@RestController
@RequestMapping("/api/noAuth")
@Log4j2
public class NoAuthController {

    @Autowired
    IJobCandidateMappingService jobCandidateMappingService;

    @Autowired
    LbUserDetailsService userDetailsService;

    @Autowired
    private HttpServletRequest servletRequest;

    @Value("${scoringEngineIpAddress}")
    private String scoringEngineIpAddress;

    /**
     * Rest api to get all screening questions for the job
     * @param uuid the uuid corresponding to a unique jcm record
     * @return the list of job screening questions
     * @throws Exception
     */
    @GetMapping("/screeningQuestion")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    String screeningQuestionsForUuid(@RequestParam("uuid") UUID uuid) throws Exception {
        log.info("Received request to retrieve screening questions for candidate: " + uuid);
        long startTime = System.currentTimeMillis();
        List<JobScreeningQuestions> response = jobCandidateMappingService.getJobScreeningQuestions(uuid);
        log.info("Completed fetching screening questions in " + (System.currentTimeMillis() - startTime) + "ms.");
        String responseStr = Util.stripExtraInfoFromResponseBean(response,
                (new HashMap<String, List<String>>(){{
                    put("User", Arrays.asList("id"));
                }}),
                new HashMap<String, List<String>>() {{
                    put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company"));
                    put("UserScreeningQuestion", Arrays.asList("createdOn", "updatedOn","userId"));
                    put("JobScreeningQuestions", Arrays.asList("jobId","createdBy", "createdOn", "updatedOn","updatedBy"));
                    put("ScreeningQuestions", new ArrayList<>(0));
                }}
        );
        if(response.size() >0 ) {
            User jobCreatedBy = userDetailsService.findById(response.get(0).getCreatedBy());
            responseStr.replaceAll("`$companyName`",jobCreatedBy.getCompany().getCompanyName());
        }
        return responseStr;
    }

    /**
     * Rest api to get all candidate and job details for the uuid provided
     * @param uuid the uuid corresponding to a unique jcm record
     * @return the list of job screening questions
     * @throws Exception
     */
    @GetMapping("/candidateAndJobDetails")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    String candidateAndJobDetailsForUuid(@RequestParam("uuid") UUID uuid) throws Exception {
        log.info("Received request to retrieve candidate & job information based on uuid : " + uuid);
        long startTime = System.currentTimeMillis();
        JobCandidateMapping mappingObj = jobCandidateMappingService.getJobCandidateMapping(uuid);
        log.info("Completed fetching JobCandidateMapping in " + (System.currentTimeMillis() - startTime) + "ms.");
        return Util.stripExtraInfoFromResponseBean(mappingObj,
                (new HashMap<String, List<String>>(){{
                    put("User", Arrays.asList("displayName"));
                    put("CandidateCompanyDetails", new ArrayList<>(0));
                    put("JobStageStep", new ArrayList<>(0));
                }}),
                new HashMap<String, List<String>>() {{
                    put("Job",Arrays.asList("jobScreeningQuestionsList","jobKeySkillsList","jobCapabilityList", "updatedOn", "updatedBy","companyJobId","noOfPositions","mlDataAvailable","status","createdOn","createdBy","userEnteredKeySkill"));
                    put("Candidate", Arrays.asList("CandidateDetails","candidateDetails","candidateEducationDetails","candidateProjectDetails",
                            "candidateOnlineProfiles","candidateWorkAuthorizations","candidateLanguageProficiencies","candidateSkillDetails","createdOn","createdBy","candidateSource","firstName","lastName","candidateSource","CandidateCompanyDetails"));
                    put("Company", Arrays.asList("companyAddressList", "companyBuList"));
                    put("JobCandidateMapping", Arrays.asList("updatedOn","updatedBy","techResponseData"));

            }}
        );
    }

    /**
     * Rest api to capture candidate consent from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param interest boolean to capture candidate consent
     * @throws Exception
     */
    @PutMapping("/candidateInterest")
    @ResponseStatus(HttpStatus.OK)
    void captureCandidateInterest(@RequestParam("uuid") UUID uuid, @RequestParam("interest") boolean interest) throws Exception {
        log.info("Received candidate interest capture request: " + uuid);
        long startTime = System.currentTimeMillis();
        jobCandidateMappingService.captureCandidateInterest(uuid, interest);
        log.info("Completed capturing candidate request in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    /**
     * Rest api to capture candidate response to screening questions from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param candidateResponse the response provided by a candidate against each screening question
     * @throws Exception
     */
    @PostMapping("/screeningQuestionResponse")
    @ResponseStatus(HttpStatus.OK)
    void screeningQuestionResponses(@RequestParam("uuid") UUID uuid, @RequestBody Map<Long,List<String>> candidateResponse) throws Exception{
        log.info("Received screening question responses from candidate: " + uuid);
        long startTime = System.currentTimeMillis();
        jobCandidateMappingService.saveScreeningQuestionResponses(uuid, candidateResponse);
        log.info("Completed saving candidate response to screening questions in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    /**
     * REST Api to capture hiring manager interest
     *
     * @param sharingId the uuid corresponding to which the interest needs to be captured
     * @param interestValue interested true / false response
     * @throws Exception
     */
    @PutMapping(value = "/hiringManagerInterest/{sharingId}/{interestValue}")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateHiringManagerInterest(@PathVariable(value = "sharingId") UUID sharingId, @PathVariable(value = "interestValue") Boolean interestValue) {
        log.info("Received Hiring Manager Interest information");
        long startTime = System.currentTimeMillis();

        jobCandidateMappingService.updateHiringManagerInterest(sharingId, interestValue);

        log.info("Completed processing request for Hiring Manager Interest in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    /**
     * REST Api to fetch details of a single candidate for the hiring manager
     *
     * @param profileSharingUuid uuid corresponding to the hiring manager record
     * @return candidate object as json
     * @throws Exception
     */
    @GetMapping("/fetchCandidateProfile/{profileSharingUuid}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    String getCandidateProfile(@PathVariable("profileSharingUuid") UUID profileSharingUuid) throws Exception {
        log.info("Received request to fetch candidate profile");
        long startTime = System.currentTimeMillis();
        JobCandidateMapping responseObj = jobCandidateMappingService.getCandidateProfile(profileSharingUuid);
        String response = Util.stripExtraInfoFromResponseBean(responseObj,
                new HashMap<String, List<String>>() {{
                    put("User", Arrays.asList("displayName"));
                    put("ScreeningQuestions", Arrays.asList("id","question","options"));
                    put("CvRating", Arrays.asList("overallRating"));
                    put("JobStageStep", new ArrayList<>(0));
                }},
                new HashMap<String, List<String>>() {{
                    put("Job",Arrays.asList("createdBy","createdOn","updatedBy","updatedOn","noOfPositions","jobDescription","mlDataAvailable","datePublished","status","scoringEngineJobAvailable","function","education","expertise","jobKeySkillsList","userEnteredKeySkill"));
                    put("Candidate",Arrays.asList("id","createdBy","createdOn","updatedBy","updatedOn","uploadErrorMessage", "firstName", "lastName","email","mobile", "candidateSource"));
                    put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company", "questionType"));
                    put("UserScreeningQuestion", Arrays.asList("createdOn","createdBy","updatedOn","userId","questionType"));
                    put("JobCandidateMapping", Arrays.asList("createdOn","createdBy","updatedOn","updatedBy","techResponseData","candidateSource","candidateInterest","candidateInterestDate","candidateFirstName","candidateLastName","chatbotUuid", "stage"));
                    put("JobCapabilities", Arrays.asList("jobCapabilityStarRatingMappingList","jobId"));
                    put("CandidateDetails", Arrays.asList("id","candidateId"));
                    put("CandidateEducationDetails", Arrays.asList("id","candidateId"));
                    put("CandidateLanguageProficiency", Arrays.asList("id","candidateId"));
                    put("CandidateOnlineProfile", Arrays.asList("id","candidateId"));
                    put("CandidateProjectDetails", Arrays.asList("id","candidateId"));
                    put("CandidateCompanyDetails", Arrays.asList("id","candidateId"));
                    put("CandidateSkillDetails", Arrays.asList("id","candidateId"));
                    put("CandidateWorkAuthorization", Arrays.asList("id","candidateId"));
                    put("JobScreeningQuestions", Arrays.asList("id","jobId","createdBy", "createdOn", "updatedOn","updatedBy"));
                }});
        response.replaceAll("`$companyName`",responseObj.getCreatedBy().getCompany().getCompanyName());

        log.info("Completed processing fetch candidate profile request in " + (System.currentTimeMillis()-startTime) + "ms.");
        return response;
    }

    /**
     * REST Api to listen to updates from scoring engine. The updates will be sent when
     * 1. the candidate fills the first response of the tech chatbot
     * 2. the candidate finishes responding to all questions of the tech chatbot
     *
     * @param requestBean bean with update information from scoring engine
     * @throws Exception
     */
    @PostMapping("/updateTechChatbotStatus")
    @ResponseStatus(value = HttpStatus.OK)
    void updateTechChatbotStatus(@RequestBody TechChatbotRequestBean requestBean) throws Exception {
        if(!servletRequest.getRemoteAddr().equals(scoringEngineIpAddress) && !servletRequest.getRemoteAddr().equals(IConstant.LOCALHOST_LOOPBACK))
            throw new WebException("Unauthorized access!",HttpStatus.UNAUTHORIZED);

        log.info("Received request to update tech chatbot status");
        long startTime = System.currentTimeMillis();
        jobCandidateMappingService.updateTechResponseStatus(requestBean);
        log.info("Completed processing request to update tech chatbot status in " + (System.currentTimeMillis() - startTime) + "ms.");
    }
}
