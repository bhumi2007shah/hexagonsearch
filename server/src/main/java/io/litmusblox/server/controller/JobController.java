/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */
package io.litmusblox.server.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.service.IJobService;
import io.litmusblox.server.service.SingleJobViewResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller class that exposes all REST endpoints for Job related operations
 *
 * @author : Shital Raval
 * Date : 1/7/19
 * Time : 2:09 PM
 * Class Name : JobController
 * Project Name : server
 */
@CrossOrigin(allowedHeaders = "*")
@RestController
@RequestMapping("/api/job")
@Log4j2
public class JobController {

    @Autowired
    IJobService jobService;

    @PostMapping(value = "/createJob/{pageName}")
    String addJob(@RequestBody String jobStr, @PathVariable ("pageName") String pageName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        Job job = mapper.readValue(jobStr, Job.class);

        return Util.stripExtraInfoFromResponseBean(
            jobService.addJob(job, pageName),
            (new HashMap<String, List<String>>(){{
                put("User",Arrays.asList("displayName","id"));
                put("ScreeningQuestions", Arrays.asList("question","id"));
                put("JobStageStep", Arrays.asList("id", "stageStepId"));
            }}),
            (new HashMap<String, List<String>>(){{
                put("Job",Arrays.asList("createdOn","createdBy", "updatedOn", "updatedBy"));
                put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company"));
                put("UserScreeningQuestion", Arrays.asList("createdOn", "updatedOn","userId"));
                put("JobScreeningQuestions", Arrays.asList("id","jobId","createdBy", "createdOn", "updatedOn","updatedBy"));
                put("JobCapabilities", Arrays.asList("createdBy", "createdOn", "updatedOn","updatedBy"));
                put("MasterData", new ArrayList<>(0));
                put("CompanyAddress", new ArrayList<>(0));
                put("CompanyStageStep", Arrays.asList("companyId", "updatedBy", "updatedOn", "createdBy", "createdOn"));
                put("StageMaster",new ArrayList<>(0));
            }})
        );
    }

    /**
     * Api for retrieving a list of jobs created by user
     * @param archived optional flag indicating if a list of archived jobs is requested. By default only open jobs will be returned
     * @param companyId optional id of the company for which jobs have to be found. Will be populated only when superadmin accesses an account
     * @return response bean with a list of jobs, count of open jobs and count of archived jobs
     * @throws Exception
     */
    @GetMapping(value = "/listOfJobs")
    String listAllJobsForUser(@RequestParam("archived") Optional<Boolean> archived, @RequestParam("companyId") Optional<Long> companyId, @RequestParam("jobStatus") Optional<String> jobStatus) throws Exception {

        return Util.stripExtraInfoFromResponseBean(
                jobService.findAllJobsForUser((archived.isPresent() ? archived.get() : false),(companyId.isPresent()?companyId.get():null), (jobStatus.isPresent()?jobStatus.get():null)),
                (new HashMap<String, List<String>>(){{
                    put("User",Arrays.asList("id", "displayName"));
                    put("CompanyAddress", Arrays.asList("address"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",Arrays.asList("jobDescription","jobScreeningQuestionsList","jobKeySkillsList","jobCapabilityList","jobHiringTeamList","jobDetail", "expertise", "education", "noticePeriod", "function", "experienceRange", "userEnteredKeySkill", "updatedOn", "updatedBy"));
                    put("MasterData", new ArrayList<>(0));
                }})
        );
    }

    /**
     * Api to retrieve
     * 1. list candidates for job for specified stage
     * 2. count of candidates by each stage
     *
     * @param jobId The job id
     * @param stage the stage
     *
     * @return response bean with all details as a json string
     * @throws Exception
     */
    @GetMapping(value = "/jobViewByStage/{jobId}/{stage}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    String getJobViewByIdAndStage(@PathVariable ("jobId") Long jobId, @PathVariable ("stage") String stage) throws Exception {
        SingleJobViewResponseBean responseBean = jobService.getJobViewById(jobId, stage);

        return Util.stripExtraInfoFromResponseBean(responseBean,
                (new HashMap<String, List<String>>(){{
                    put("User",Arrays.asList("displayName"));
                    put("CvRating", Arrays.asList("overallRating"));
                    put("CandidateEducationDetails", Arrays.asList("degree"));
                    put("JobStageStep", Arrays.asList("stageName"));
                    put("CompanyAddress", Arrays.asList("address"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",Arrays.asList("jobDescription","jobScreeningQuestionsList","jobKeySkillsList","jobCapabilityList", "updatedOn", "updatedBy"));
                    put("Candidate", Arrays.asList("candidateProjectDetails","candidateOnlineProfiles","candidateWorkAuthorizations","candidateLanguageProficiencies",
                            "candidateSkillDetails","createdOn","createdBy", "firstName", "lastName", "displayName"));
                    put("JobCandidateMapping", Arrays.asList("updatedOn","updatedBy","techResponseData", "interviewDetails", "candidateReferralDetail", "candidateSourceHistories"));
                    put("CandidateDetails", Arrays.asList("id","candidateId"));
                    put("CandidateCompanyDetails", Arrays.asList("candidateId"));
                }})
        );
    }

    /**
     * Api to set the status of a job as published.
     *
     * @param job to be published
     * @throws Exception
     */
    @PostMapping(value = "/publishJob")
    @ResponseStatus(HttpStatus.OK)
    void publishJob(@RequestBody Job job) throws Exception {
        jobService.publishJob(job);
    }

    /**
     * Api to archive a job
     *
     * @param jobId id of the job to be archived
     * @throws Exception
     */
    @PutMapping(value = "/archiveJob/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    void archiveJob(@PathVariable("jobId") Long jobId) throws Exception {
        jobService.archiveJob(jobId);
    }

    /**
     * Api to unarchive a job
     *
     * @param jobId id of the job to be unarchived
     * @throws Exception
     */
    @PutMapping(value = "/unarchiveJob/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    void unarchiveJob(@PathVariable("jobId") Long jobId) throws Exception {
        jobService.unarchiveJob(jobId);
    }


    /**
     * Api for get job details based on job id
     *
     */
    @GetMapping(value = "/getDetails/{jobId}")
    @ResponseBody
    String getJobDetails(@PathVariable("jobId") Long jobId) throws Exception {
        return Util.stripExtraInfoFromResponseBean(
                jobService.getJobDetails(jobId),
                (new HashMap<String, List<String>>(){{
                    put("User",Arrays.asList("id","displayName"));
                    put("CompanyAddress", Arrays.asList("address"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",new ArrayList<>(0));
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
        //return jobService.getJobDetails(jobId);
    }

    @GetMapping(value="/getHistory/{jobId}")
    @ResponseBody
    String getJobHistory(@PathVariable("jobId") Long jobId)throws Exception{
        return Util.stripExtraInfoFromResponseBean(
                jobService.getJobHistory(jobId),
                (new HashMap<String, List<String>>(){{
                    put("User",Arrays.asList("displayName"));
                    put("CompanyAddress", Arrays.asList("address"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",new ArrayList<>(0));
                    put("JobScreeningQuestions",new ArrayList<>(0));
                    put("ScreeningQuestions",new ArrayList<>(0));
                    put("CompanyScreeningQuestion",new ArrayList<>(0));
                    put("UserScreeningQuestion",new ArrayList<>(0));
                    put("MasterData", new ArrayList<>(0));
                }}));
    }

    @GetMapping(value = "/supportedexportformat/{jobId}")
    Map<Long, String> supportedExportFormat(@PathVariable("jobId") Long jobId) throws Exception{
        return jobService.getSupportedExportFormat(jobId);
    }

    @GetMapping(value = {"/exportdata/{jobId}", "/exportdata/{jobId}/{formatId}"})
    String exportData(@PathVariable("jobId") Long jobId, @PathVariable(required = false, value = "formatId") Optional<Long>formatId, @RequestParam("stage")String stage) throws Exception{
        String data=null;
        if(!formatId.isPresent()){
            data = jobService.exportData(jobId, null, stage);
        }
        else{
            data = jobService.exportData(jobId, formatId.get(), stage);
        }
        return data;
    }

    @GetMapping(value = "/exportTechRoleCompetency/{jobId}")
    String exportTechRoleCompetency(@PathVariable("jobId") Long jobId) throws Exception{
        log.info("Received request to fetch Tech role competency list for job {}", jobId);
        long startTime = System.currentTimeMillis();
        String response = Util.stripExtraInfoFromResponseBean(jobService.getTechRoleCompetencyByJob(jobId),
                new HashMap<String, List<String>>() {{
                    put("Candidate",Arrays.asList("displayName","email", "mobile"));
                    put("Integer", Arrays.asList("score"));
                    put("TechResponseJson", Arrays.asList("name", "complexities", "score", "capabilityStarRating"));
                    put("String", Arrays.asList("candidateProfileLink"));
                }},
                new HashMap<String, List<String>>() {{
                }});
        log.info("Completed processing fetch Tech role competency list for job {} in {}", jobId, (System.currentTimeMillis()-startTime) + "ms.");
        return response;
    }

    @GetMapping(value = "/inviteError/{jobId}")
    String getAsyncInviteError(@PathVariable("jobId") Long jobId){
        log.info("Received request to fetch error report for async invite operation for jobId: {}", jobId);
        long startTime = System.currentTimeMillis();
        String response = Util.stripExtraInfoFromResponseBean(jobService.findAsyncErrors(jobId, IConstant.ASYNC_OPERATIONS.InviteCandidates.name()),
                null,
                new HashMap<String, List<String>>(){{
                    put("AsyncOperationsErrorRecords", Arrays.asList("id", "jobId", "jobCandidateMappingId", "asyncOperation", "createdBy"));
                }}
                );
        log.info("Completed processing request to fetch async invite errors for jobId: {} in {}ms", jobId, System.currentTimeMillis()-startTime);
        return response;
    }

    @GetMapping(value = "/uploadError/{jobId}")
    String getAsyncUploadError(@PathVariable("jobId") Long jobId){
        log.info("Received request to fetch error report for async invite operation for jobId: {}", jobId);
        long startTime = System.currentTimeMillis();
        String response = Util.stripExtraInfoFromResponseBean(jobService.findAsyncErrors(jobId, IConstant.ASYNC_OPERATIONS.FileUpload.name()),
                null,
                new HashMap<String, List<String>>(){{
                    put("AsyncOperationsErrorRecords", Arrays.asList("id", "jobId", "jobCandidateMappingId", "asyncOperation", "createdBy"));
                }}
                );
        log.info("Completed processing request to fetch async invite errors for jobId: {} in {}ms", jobId, System.currentTimeMillis()-startTime);
        return response;
    }

    /**
     * API to update visibility flag for career pages
     * @param jobId jobId For chich we update flag
     */
    @PutMapping(value = "/updateJobVisibilityFlag/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    void updateJobVisibilityFlagForCareerPage(@PathVariable("jobId") Long jobId, @RequestParam("visibilityFlag") boolean visibilityFlag) throws Exception {
        log.info("Received request to update job visibility flag for careerPage for JobId : "+jobId);
        long startTime = System.currentTimeMillis();
        jobService.updateJobVisibilityFlagOnCareerPage(jobId, visibilityFlag);
        log.info("Completed processing request to update job visibility flag for careerPage for jobId: {} in {}ms", jobId, System.currentTimeMillis()-startTime);
    }

}