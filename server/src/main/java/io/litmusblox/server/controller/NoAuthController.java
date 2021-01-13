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
import org.springframework.http.ResponseEntity;
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
    void captureCandidateInterest(@RequestParam("uuid") UUID uuid, @RequestParam("interest") boolean interest, @RequestParam("candidateNotInterestReasonId") Optional<Long> candidateNotInterestedReasonId ) throws Exception {
        log.info("Received candidate interest capture request: " + uuid);
        long startTime = System.currentTimeMillis();
        jobCandidateMappingService.captureCandidateInterest(uuid, interest, (candidateNotInterestedReasonId.isPresent())?candidateNotInterestedReasonId.get():null, servletRequest.getHeader("User-Agent"));
        log.info("Completed capturing candidate request in {}ms",(System.currentTimeMillis()-startTime));
    }

    /**
     * Rest api to capture candidate response to screening questions from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param screeningQuestionRequestBean Candidates response for question id
     * @throws Exception
     */
    @PostMapping("/screeningQuestionResponse")
    @ResponseStatus(HttpStatus.OK)
    void screeningQuestionResponse(@RequestParam("uuid") UUID uuid, @RequestBody ScreeningQuestionRequestBean screeningQuestionRequestBean) throws Exception{
        log.info("Received screening question responses from candidate: " + uuid);
        long startTime = System.currentTimeMillis();
        jobCandidateMappingService.saveScreeningQuestion(uuid, screeningQuestionRequestBean, servletRequest.getHeader("User-Agent"));
        log.info("Completed saving candidate response to screening questions in {}ms",(System.currentTimeMillis()-startTime));
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
        log.info("Completed processing request to update tech chatbot status in {}ms",(System.currentTimeMillis()-startTime));
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
     * API to return job details based on job short code
     *
     * @param jobShortCode job short code to search for
     * @return String representation of the job details information
     * @throws Exception
     */
    @GetMapping(value = "/jobDetailsByJobShortCode/{jobShortCode}")
    String jobDetailsByReferenceId(@PathVariable("jobShortCode") String jobShortCode) throws Exception {
        return Util.stripExtraInfoFromResponseBean(jobService.findJobByJobShortCode(jobShortCode),
                new HashMap<String, List<String>>() {{
                    put("Job",Arrays.asList("id","jobTitle","jobDescription", "jobLocation" , "function","jobShortCode"));
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
                    put("Job", Arrays.asList("id","jobTitle", "jobDescription", "jobLocation", "function", "jobReferenceId","jobType", "jobShortCode"));
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
     * @param jobShortCode In which job upload candidate
     * @param employeeReferrerString employee info string
     * @return UploadResponseBean
     * @throws Exception
     */
    @PostMapping(value = "/addCandidate/{candidateSource}")
    @ResponseStatus(value = HttpStatus.OK)
    String uploadCandidate(@PathVariable("candidateSource") String candidateSource, @RequestParam(name = "candidateCv", required = false) MultipartFile candidateCv, @RequestParam("candidate") String candidateString, @RequestParam("jobShortCode") String jobShortCode, @RequestParam(name = "employeeReferrer", required = false) String employeeReferrerString, @RequestParam("otp") String otp) throws Exception{
        EmployeeReferrer employeeReferrer = null;
        ObjectMapper objectMapper=new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        Candidate candidate=objectMapper.readValue(candidateString, Candidate.class);
        if(null != employeeReferrerString)
            employeeReferrer =objectMapper.readValue(employeeReferrerString, EmployeeReferrer.class);

        long startTime = System.currentTimeMillis();
        UploadResponseBean responseBean = jobCandidateMappingService.uploadCandidateByNoAuthCall(candidateSource, candidate, jobShortCode, candidateCv, employeeReferrer, otp);
        log.info("Candidate upload in {}ms",(System.currentTimeMillis()-startTime));
        return Util.stripExtraInfoFromResponseBean(responseBean, null,
                new HashMap<String, List<String>>() {{
                    put("Candidate", Arrays.asList("candidateDetails","candidateEducationDetails","candidateProjectDetails","candidateCompanyDetails",
                            "candidateOnlineProfiles","candidateWorkAuthorizations","candidateLanguageProficiencies","candidateSkillDetails"));
                    put("UploadResponseBean", Arrays.asList("fileName","processedOn", "candidateName"));
                }});
    }

    /**
     * REST Api to handle send Otp request from search job page
     * @param isEmployeeReferral true if the send otp request was from employee referral flow
     * @param mobileNumber mobile number to send otp to
     * @param countryCode country code
     * @param email email address of the employee
     * @param recepientName name of the message receiver
     * @param companyShortName shortname of the company
     * @throws Exception
     */
    @GetMapping(value = "/sendOtp")
    @ResponseStatus(value = HttpStatus.OK)
    String sendOtp(@RequestParam(name = "isEmployeeReferral") boolean isEmployeeReferral, @RequestParam(name = "mobileNumber", required = false) String mobileNumber, @RequestParam(name = "countryCode") Optional<String> countryCode, @RequestParam(name = "email", required = false) String email, @RequestParam(name = "recepientName") Optional<String> recepientName, @RequestParam(name = "companyShortName") Optional<String> companyShortName, @RequestParam(name ="uuid") Optional<UUID> uuid) throws Exception {
        return processOtpService.sendOtp(isEmployeeReferral, mobileNumber,countryCode.isPresent()?countryCode.get():null, email, recepientName.isPresent()?recepientName.get():null, companyShortName.isPresent()?companyShortName.get():null,uuid.isPresent()?uuid.get():null );
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
        log.info("Received request to retrieve chatbot details for chatbotUuId: {}", uuid);
        long startTime = System.currentTimeMillis();
        ChatbotResponseBean response = jobCandidateMappingService.getChatbotDetailsByUuid(uuid);
        log.info("Completed fetching chatbot detail in {}ms.", (System.currentTimeMillis() - startTime));
        String responseStr = Util.stripExtraInfoFromResponseBean(response,
                (new HashMap<String, List<String>>(){{
                    put("User", Arrays.asList("id", "displayName"));
                    put("CandidateCompanyDetails", new ArrayList<>(0));
                    put("JobStageStep", new ArrayList<>(0));
                    put("Candidate",new ArrayList<>(0));
                    put("JobRole", Arrays.asList("role"));
                }}),
                new HashMap<String, List<String>>() {{
                    put("Job",Arrays.asList("jobKeySkillsList","jobCapabilityList", "updatedOn", "updatedBy","companyJobId","noOfPositions","mlDataAvailable","status","createdOn","createdBy","userEnteredKeySkill"));
                    put("Company", Arrays.asList("companyAddressList", "companyBuList"));
                    put("JobCandidateMapping", Arrays.asList("updatedOn","updatedBy","techResponseData", "candidateReferralDetail", "candidateSourceHistories"));
                    put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company"));
                    put("UserScreeningQuestion", Arrays.asList("createdOn", "updatedOn","userId"));
                    put("JcmCommunicationDetails", Arrays.asList("id","jcmId"));
                    put("JobScreeningQuestions", Arrays.asList("jobId","createdBy", "createdOn", "updatedOn","updatedBy"));
                    put("MasterData", new ArrayList<>(0));
                    put("ScreeningQuestions", new ArrayList<>(0));
                }}
        );
        if(null != response) {
            User jobCreatedBy = userDetailsService.findById(response.getJobCandidateMapping().getJob().getCreatedBy().getId());
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

    /**
     * REST Api to set candidate confirmation for interview
     *
     * @param confirmationDetails interviewDetails model for confirmation
     */
    @PutMapping(value = "/candidateConfirmation")
    @ResponseStatus(value = HttpStatus.OK)
    void candidateConfirmationForInterview(@RequestBody InterviewDetails confirmationDetails) throws Exception {
        long startTime = System.currentTimeMillis();
        jobCandidateMappingService.candidateConfirmationForInterview(confirmationDetails);
        log.info("Candidate Interview confirmation done in {}ms",(System.currentTimeMillis()-startTime));
    }

    /**
     * REST Api to determine if candidate has already sent a confirmation for the said interview earlier
     *
     * @return List of companies
     * @throws Exception
     */
    @GetMapping(value = "/getCandidateConfirmationStatus/{interviewReferenceId}")
    @ResponseStatus(value = HttpStatus.OK)
    String getCandidateConfirmationStatus(@PathVariable("interviewReferenceId") UUID interviewReferenceId) throws Exception {
        long startTime = System.currentTimeMillis();
        JobCandidateMapping jobCandidateMapping = jobCandidateMappingService.getCandidateConfirmationStatus(interviewReferenceId);
        log.info("Get candidate confirmation status in {}ms",(System.currentTimeMillis()-startTime));
        String responseStr = Util.stripExtraInfoFromResponseBean(jobCandidateMapping,
                (new HashMap<String, List<String>>(){{
                    put("User", Arrays.asList("displayName"));
                    put("JobCandidateMapping", Arrays.asList("displayName", "currentInterviewDetail","job"));
                    put("Job", Arrays.asList("companyId"));
                }}),
                null
        );
        return responseStr;
    }

    /**
     * REST API to get address data(area, city, state) for live job's from job location
     *
     * @param companyShortName first find company then find jobList by companyId
     * @return address string set(eg. "Baner, Pune, Maharashtra")
     */
    @GetMapping(value = "/getLiveJobAddressStringSet/{companyShortName}")
    @ResponseStatus(value = HttpStatus.OK)
    Set<String> getLiveJobAddressStringSet(@PathVariable("companyShortName") String companyShortName) throws Exception {
        log.info("Inside getLiveJobAddressStringSet");
        long startTime = System.currentTimeMillis();
        Set<String> addressStringSet = jobCandidateMappingService.getLiveJobAddressStringSetByCompanyId(companyShortName);
        log.info("Get live job address string set in {}ms",(System.currentTimeMillis()-startTime));
        return addressStringSet;
    }

    @PostMapping(value = "/uploadResume")
    ResponseEntity uploadResume(@RequestParam("candidateCv") MultipartFile multipartFile, @RequestParam("chatbotUuid") UUID chatbotUuid) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("inside uploadResume");
        ResponseEntity responseEntity = jobCandidateMappingService.uploadResume(multipartFile, chatbotUuid);
        log.info("Resume upload successFully in {}ms", System.currentTimeMillis()-startTime);
        return responseEntity;
    }
}


