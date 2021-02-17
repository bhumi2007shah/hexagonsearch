/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.model.JcmProfileSharingDetails;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.JobCandidateMapping;
import io.litmusblox.server.service.IHiringManagerWorkspaceService;
import io.litmusblox.server.service.SingleJobViewResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Date : 11/11/20
 * Time : 11:16 AM
 * Class Name : HiringManagerWorkspaceController
 * Project Name : server
 */
@CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.OPTIONS}, allowedHeaders = {"Content-Type", "Authorization","X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"}, exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"})
@RestController
@RequestMapping("/api/hmw")
@Log4j2
public class HiringManagerWorkspaceController {

    @Autowired
    IHiringManagerWorkspaceService hiringManagerWorkspaceService;

    /**
     * Service to fetch jcmList for stage and job id
     * @param stage stage for which details is required
     * @param jobId for which job id we want data
     * @return all required details for the logged in hiring manager and stage
     * @throws Exception
     */
    @GetMapping(value = "/getDetails/{stage}")
    String getDetails(@PathVariable("stage") String stage, @RequestParam("jobId") Long jobId) throws Exception{

        SingleJobViewResponseBean responseBean = hiringManagerWorkspaceService.getHiringManagerWorkspaceDetails(stage, jobId);
        return Util.stripExtraInfoFromResponseBean(responseBean,
                (new HashMap<String, List<String>>(){{
                    put("InterviewDetails",Arrays.asList("interviewType","interviewMode","interviewDate","showNoShow", "cancelled", "candidateConfirmation","candidateConfirmationValue"));
                    put("JcmProfileSharingDetails", Arrays.asList("comments", "hiringManagerInterest", "hiringManagerInterestDate"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("JCMAllDetails",Arrays.asList("jobId","cvFileType","cvLocation","updatedOn", "updatedBy", "countryCode","source","stage","stageName"));
                }})
        );
    }

    /**
     * Service to fetch the candidate profile which the hiring manager has selected
     * @param jcmId for user whose profile is to be fetched
     * @return details of the candidates whose profile is fetched.
     * @throws Exception
     */
    @GetMapping(value = "/fetchCandidateProfile/{jcmId}")
    String getCandidateProfile(@PathVariable("jcmId") Long jcmId) throws Exception{
        JobCandidateMapping responseObj = hiringManagerWorkspaceService.fetchCandidateProfile(jcmId);
        String response = Util.stripExtraInfoFromResponseBean(responseObj,
                new HashMap<String, List<String>>() {{
                    put("User", Arrays.asList("displayName"));
                    put("ScreeningQuestions", Arrays.asList("id","question","options"));
                    put("CvRating", Arrays.asList("overallRating"));
                    put("JobStageStep", new ArrayList<>(0));
                    put("Job",new ArrayList<>(0));
                    put("JcmProfileSharingDetails", Arrays.asList("comments", "hiringManagerInterest", "hiringManagerInterestDate"));
                }},
                new HashMap<String, List<String>>() {{
                    put("Candidate",Arrays.asList("id","createdBy","createdOn","updatedBy","updatedOn","uploadErrorMessage", "firstName", "lastName","email","mobile", "candidateSource"));
                    put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company", "questionType"));
                    put("UserScreeningQuestion", Arrays.asList("createdOn","createdBy","updatedOn","userId","questionType"));
                    put("JobCandidateMapping", Arrays.asList("createdOn","createdBy","updatedOn","updatedBy","techResponseData","candidateSource","candidateInterest","candidateInterestDate","candidateFirstName","candidateLastName","chatbotUuid", "stage", "candidateSourceHistories","interestAccessByDevice","chatbotCompletedByDevice","createdOnSearchEngine"));
                    put("JobCapabilities", Arrays.asList("jobCapabilityStarRatingMappingList","jobId"));
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
                    put("JcmHistory", Arrays.asList("id", "jcmId", "stage"));
                }});
        response = response.replaceAll(Pattern.quote("$companyName"),responseObj.getCreatedBy().getCompany().getCompanyName());
        return response;
    }

    /**
     * To fetch job details for hiring manager
     * @param jobId id whose job details is required.
     * @return all relevant job details
     * @throws Exception
     */
    @GetMapping(value = "/getJobDetails/{jobId}")
    String getJobDetail(@PathVariable("jobId") Long jobId) throws Exception{
        Job responseObj = hiringManagerWorkspaceService.getJobDetails(jobId);

        return Util.stripExtraInfoFromResponseBean(
                responseObj,
                (new HashMap<String, List<String>>(){{
                    put("User",Arrays.asList("id","displayName"));
                    put("CompanyAddress", Arrays.asList("id", "address"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",Arrays.asList("companyId","createdBy","hiringManager","minSalary","maxSalary","resubmitHrChatbot","scoringEngineJobAvailable",
                            "visibleToCareerPage","jobShortCode","companyJobId","hrQuestionAvailable","customizedChatbot","currency","autoInvite",
                            "interviewLocation","jobIndustry","recruiter","jobLocation","jobFunction"));
                    put("JobScreeningQuestions",new ArrayList<>(0));
                    put("ScreeningQuestions",new ArrayList<>(0));
                    put("CompanyScreeningQuestion",new ArrayList<>(0));
                    put("UserScreeningQuestion",new ArrayList<>(0));
                    put("JobCapabilities",new ArrayList<>(0));
                    put("JobStageStep", Arrays.asList("updatedBy", "updatedOn", "createdBy", "createdOn", "jobId"));
                    put("CompanyStageStep", Arrays.asList("companyId", "updatedBy", "updatedOn", "createdBy", "createdOn"));
                    put("StageMaster",new ArrayList<>(0));
                    put("MasterData", new ArrayList<>(0));
                }}));
    }

    /**
     * update hiring manager interest for a particular profile
     * @param jcmProfileSharingDetails contains profile sharing id, hiring manager interest, comments and rejection reason if rejected
     * @throws Exception
     */
    @PutMapping(value = "/hiringManagerInterest")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateHiringManagerInterest(@RequestBody JcmProfileSharingDetails jcmProfileSharingDetails) throws Exception {
        hiringManagerWorkspaceService.getHiringManagerInterest(jcmProfileSharingDetails);
    }

    /**
     * Api for retrieving a list of jobs who's at least one candidate shared with hiring manager
     * @return response bean with a list of jobs
     * @throws Exception
     */
    @GetMapping(value = "/listOfJobs/{jobStatus}")
    String listAllJobsForShareProfileToHiringManager(@PathVariable String jobStatus) throws Exception {
        return Util.stripExtraInfoFromResponseBean(
                hiringManagerWorkspaceService.findAllJobsForShareProfileToHiringManager(jobStatus),
                (new HashMap<String, List<String>>(){{
                    put("User",Arrays.asList("id", "displayName"));
                    put("CompanyAddress", Arrays.asList("address", "city"));
                    put("JobRole", Arrays.asList("role"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",Arrays.asList("jobDescription","jobScreeningQuestionsList","jobKeySkillsList","jobCapabilityList","jobHiringTeamList","jobDetail", "expertise", "education", "noticePeriod", "function",
                            "experienceRange", "userEnteredKeySkill", "updatedOn", "updatedBy","companyId","createdBy","hiringManager","minSalary","maxSalary", "resubmitHrChatbot","scoringEngineJobAvailable","visibleToCareerPage",
                            "jobShortCode", "hrQuestionAvailable","customizedChatbot","currency","autoInvite","interviewLocation","jobIndustry", "recruiter", "jobFunction"));
                    put("MasterData", new ArrayList<>(0));
                }})
        );
    }


    @PostMapping(value = "/addTechQuestions")
    void addTechQuestionsForJob(@RequestBody String jobStr) throws Exception{
        long startTime = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        Job job = mapper.readValue(jobStr, Job.class);
        log.info("received request to add tech question for jobId : {}",job.getId());
        hiringManagerWorkspaceService.setTechQuestionForJob(job);
        log.info("Successfully added JobScreening questions from user Id  : {} in : {}ms",job.getDeepQuestionSelectedBy(),( System.currentTimeMillis() - startTime));
    }

}