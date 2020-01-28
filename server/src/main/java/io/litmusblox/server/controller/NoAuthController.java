/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.service.*;
import io.litmusblox.server.service.impl.LbUserDetailsService;
import io.litmusblox.server.service.impl.SearchRequestBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Pattern;

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

    @Autowired
    IMasterDataService masterDataService;

    @Autowired
    IJobService jobService;

    @Autowired
    IProcessOtpService processOtpService;

    @Autowired
    ICompanyService companyService;

    @Value("${scoringEngineIpAddress}")
    private String scoringEngineIpAddress;

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
                    put("MasterData", new ArrayList<>(0));
                    put("CompanyAddress", new ArrayList<>(0));
                }});
       // log.info("before call to replace:\n {}",response);
        response = response.replaceAll(Pattern.quote("$companyName"),responseObj.getCreatedBy().getCompany().getCompanyName());
       // log.info("after call to replace:\n {}",response);

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

    /**
     * REST Api to get masterData for only specific fields, which is used in noAuth call
     *
     * @param requestItems (fileType, referrerRelation)
     * @return MasterData
     * @throws Exception
     */
    @PostMapping(value="/fetch/items")
    @ResponseStatus(value = HttpStatus.OK)
    String fetchItems(@RequestBody List<String> requestItems) throws Exception {
        return Util.stripExtraInfoFromResponseBean(
                masterDataService.fetchForItemsForNoAuth(requestItems),null,
                new HashMap<String, List<String>>() {{
                    put("MasterData", new ArrayList<>(0));
                }});
    }

    /**
     * API to return job details based on job reference id
     *
     * @param jobReferenceId job reference id to search for
     * @return String representation of the job details information
     * @throws Exception
     */
    @GetMapping(value = "/jobDetailsByReferenceId/{jobReferenceId}")
    String jobDetailsByReferenceId(@PathVariable("jobReferenceId") UUID jobReferenceId) throws Exception {
        return Util.stripExtraInfoFromResponseBean(jobService.findByJobReferenceId(jobReferenceId),
                new HashMap<String, List<String>>() {{
                    put("Job",Arrays.asList("id","jobTitle","jobDescription", "jobLocation" , "function"));
                    put("MasterData", Arrays.asList("value"));
                }}, null);
    }

    /**
     * API to search jobs for a company based on search criteria
     *
     * @param searchRequest the request bean with search criteria
     * @return the list of jobs matching the search criteria
     */
    @PostMapping(value="/searchJobs")
    String searchJobs(@RequestBody String searchRequest) throws Exception {
        log.info("Received request to search jobs");
        long startTime = System.currentTimeMillis();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Job> jobsFound = jobService.searchJobs(objectMapper.readValue(searchRequest, SearchRequestBean.class));
        log.info("Complete processing search operation in {} ms.", (System.currentTimeMillis() - startTime));
        return Util.stripExtraInfoFromResponseBean(jobsFound,
                new HashMap<String, List<String>>() {{
                    put("Job", Arrays.asList("jobTitle", "jobDescription", "jobLocation", "function", "jobReferenceId","jobType"));
                    put("CompanyAddress", Arrays.asList("address"));
                    put("MasterData", Arrays.asList("value"));
                }}, null);

    }

    /**
     * Rest API to upload candidate via career page, job portal, employee referral
     *
     * @param candidateSource From where we source the candidate
     * @param candidateCv candidate cv
     * @param candidateString  Candidate all info string
     * @param jobReferenceId In which job upload candidate
     * @param employeeReferrerString employee info string
     * @return UploadResponseBean
     * @throws Exception
     */
    @PostMapping(value = "/addCandidate/{candidateSource}")
    @ResponseStatus(value = HttpStatus.OK)
    String uploadCandidate(@PathVariable("candidateSource") String candidateSource, @RequestParam(name = "candidateCv", required = false) MultipartFile candidateCv, @RequestParam("candidate") String candidateString, @RequestParam("jobReferenceId") UUID jobReferenceId, @RequestParam(name = "employeeReferrer", required = false) String employeeReferrerString, @RequestParam("otp") String otp) throws Exception{
        EmployeeReferrer employeeReferrer = null;
        ObjectMapper objectMapper=new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        Candidate candidate=objectMapper.readValue(candidateString, Candidate.class);
        if(null != employeeReferrerString)
            employeeReferrer =objectMapper.readValue(employeeReferrerString, EmployeeReferrer.class);

        long startTime = System.currentTimeMillis();
        UploadResponseBean responseBean = jobCandidateMappingService.uploadCandidateByNoAuthCall(candidateSource, candidate, jobReferenceId, candidateCv, employeeReferrer, otp);
        log.info("Candidate upload in " + (System.currentTimeMillis() - startTime) + "ms.");
        return Util.stripExtraInfoFromResponseBean(responseBean, null,
                new HashMap<String, List<String>>() {{
                    put("Candidate", Arrays.asList("candidateDetails","candidateEducationDetails","candidateProjectDetails","candidateCompanyDetails",
                            "candidateOnlineProfiles","candidateWorkAuthorizations","candidateLanguageProficiencies","candidateSkillDetails"));
                    put("UploadResponseBean", Arrays.asList("fileName","processedOn", "candidateName"));
                }});
    }

    /**
     * REST Api to handle send Otp request from search job page
     * @param mobile mobile number to send otp to
     * @param email email address to send otp to
     * @throws Exception
     */
    @GetMapping(value = "/sendOtp")
    @ResponseStatus(value = HttpStatus.OK)
    void sendOtp(@RequestParam String mobile, @RequestParam String email) throws Exception {
        processOtpService.sendOtp(mobile, email);
    }


    /**
     *REST Api to fetch data related to job like job detail, screening questions and corresponding candidate
     *Merge two api getScreeningQuestions and getCandidateAndJobDetails in current api
     * @param uuid the uuid corresponding to a unique jcm record
     * @throws Exception
     * return ChatbotResponseBean String
     */
    @GetMapping(value = "/chatbotDetails")
    @ResponseStatus(value = HttpStatus.OK)
    String getChatbotDetail(@RequestParam("uuid") UUID uuid) throws Exception {
        log.info("Received request to retrieve chatbot details for chatbotUuId: " + uuid);
        long startTime = System.currentTimeMillis();
        ChatbotResponseBean response = jobCandidateMappingService.getChatbotDetailsByUuid(uuid);
        log.info("Completed fetching chatbot detail in " + (System.currentTimeMillis() - startTime) + "ms.");
        String responseStr = Util.stripExtraInfoFromResponseBean(response,
                (new HashMap<String, List<String>>(){{
                    put("User", Arrays.asList("id", "displayName"));
                    put("CandidateCompanyDetails", new ArrayList<>(0));
                    put("JobStageStep", new ArrayList<>(0));
                    put("Candidate",new ArrayList<>(0));
                }}),
                new HashMap<String, List<String>>() {{
                    put("Job",Arrays.asList("jobKeySkillsList","jobCapabilityList", "updatedOn", "updatedBy","companyJobId","noOfPositions","mlDataAvailable","status","createdOn","createdBy","userEnteredKeySkill"));
                    put("Company", Arrays.asList("companyAddressList", "companyBuList"));
                    put("JobCandidateMapping", Arrays.asList("updatedOn","updatedBy","techResponseData"));
                    put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company"));
                    put("UserScreeningQuestion", Arrays.asList("createdOn", "updatedOn","userId"));
                    put("JobScreeningQuestions", Arrays.asList("jobId","createdBy", "createdOn", "updatedOn","updatedBy"));
                    put("MasterData", new ArrayList<>(0));
                    put("ScreeningQuestions", new ArrayList<>(0));

                }}
        );
        if(null != response) {
            User jobCreatedBy = userDetailsService.findById(response.getJobCandidateMapping().getJob().getCompanyId().getId());
            responseStr.replaceAll("`$companyName`",jobCreatedBy.getCompany().getCompanyName());
        }
        return responseStr;
    }

    /**
     * Rest API to fetch company address by company id
     *
     * @param companyId company id for which we find addresses
     * @return List of CompanyAddresses
     */
    @GetMapping(value = "/getCompanyAddress/{companyId}")
    @ResponseStatus(value = HttpStatus.OK)
    List<CompanyAddress> getCompanyAddress(@PathVariable("companyId") Long companyId) throws Exception {
        return companyService.getCompanyAddress(companyId);
    }

}
