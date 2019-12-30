/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */
package io.litmusblox.server.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.service.IJobService;
import io.litmusblox.server.service.SingleJobViewResponseBean;
import io.litmusblox.server.utils.Util;
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
                put("JobStageStep", Arrays.asList("id"));
            }}),
            (new HashMap<String, List<String>>(){{
                put("Job",Arrays.asList("createdOn","createdBy", "updatedOn", "updatedBy"));
                put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company"));
                put("UserScreeningQuestion", Arrays.asList("createdOn", "updatedOn","userId"));
                put("JobScreeningQuestions", Arrays.asList("id","jobId","createdBy", "createdOn", "updatedOn","updatedBy"));
                put("JobCapabilities", Arrays.asList("createdBy", "createdOn", "updatedOn","updatedBy"));
            }})
        );
    }

    /**
     * Api for retrieving a list of jobs created by user
     * @param archived optional flag indicating if a list of archived jobs is requested. By default only open jobs will be returned
     * @param companyName optional name of the company for which jobs have to be found. Will be populated only when superadmin accesses an account
     * @return response bean with a list of jobs, count of open jobs and count of archived jobs
     * @throws Exception
     */
    @GetMapping(value = "/listOfJobs")
    String listAllJobsForUser(@RequestParam("archived") Optional<Boolean> archived, @RequestParam("companyName") Optional<String> companyName) throws Exception {

        return Util.stripExtraInfoFromResponseBean(
                jobService.findAllJobsForUser((archived.isPresent() ? archived.get() : false),(companyName.isPresent()?companyName.get():null)),
                (new HashMap<String, List<String>>(){{
                    put("User",Arrays.asList("id", "displayName"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",Arrays.asList("jobDescription","jobScreeningQuestionsList","jobKeySkillsList","jobCapabilityList","jobHiringTeamList","jobDetail", "expertise", "education", "noticePeriod", "function", "experienceRange", "userEnteredKeySkill", "updatedOn", "updatedBy"));
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
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",Arrays.asList("jobDescription","jobScreeningQuestionsList","jobKeySkillsList","jobCapabilityList", "updatedOn", "updatedBy"));
                    put("Candidate", Arrays.asList("candidateProjectDetails","candidateOnlineProfiles","candidateWorkAuthorizations","candidateLanguageProficiencies",
                            "candidateSkillDetails","createdOn","createdBy", "firstName", "lastName", "displayName"));
                    put("JobCandidateMapping", Arrays.asList("updatedOn","updatedBy","techResponseData"));
                    put("CandidateDetails", Arrays.asList("id","candidateId"));
                    put("CandidateCompanyDetails", Arrays.asList("candidateId"));
                }})
        );
    }

    /**
     * Api to set the status of a job as published.
     *
     * @param jobId id of the job which is to be published
     * @throws Exception
     */
    @PutMapping(value = "/publishJob/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    void publishJob(@PathVariable("jobId") Long jobId) throws Exception {
        jobService.publishJob(jobId);
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
                    put("User",Arrays.asList("displayName"));
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",new ArrayList<>(0));
                    put("JobScreeningQuestions",new ArrayList<>(0));
                    put("ScreeningQuestions",new ArrayList<>(0));
                    put("CompanyScreeningQuestion",new ArrayList<>(0));
                    put("UserScreeningQuestion",new ArrayList<>(0));
                    put("JobCapabilities",new ArrayList<>(0));
                    put("JobStageStep",new ArrayList<>(0));
                    put("CompanyStageStep",new ArrayList<>(0));
                    put("StageMaster",new ArrayList<>(0));
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
                }}),
                (new HashMap<String, List<String>>(){{
                    put("Job",new ArrayList<>(0));
                    put("JobScreeningQuestions",new ArrayList<>(0));
                    put("ScreeningQuestions",new ArrayList<>(0));
                    put("CompanyScreeningQuestion",new ArrayList<>(0));
                    put("UserScreeningQuestion",new ArrayList<>(0));
                }}));
    }

    /**
     * REST API to return the stage steps for a job
     *
     * @param jobId the job id for which stage steps are to be returned
     * @return list of stage steps
     * @throws Exception
     */
    @GetMapping(value = "/getStageStep/{jobId}")
    @ResponseBody
    String getJobStageStep(@PathVariable("jobId") Long jobId) throws Exception {
        return Util.stripExtraInfoFromResponseBean(jobService.getJobStageStep(jobId),
                (new HashMap<String, List<String>>(){{
                    put("User",new ArrayList<>(0));
                    put("Company", new ArrayList<>(0));
                }}),
                new HashMap<String, List<String>>() {{
                    put("CompanyStageStep", Arrays.asList("id","createdOn", "createdBy","updatedOn","updatedBy","companyId"));
                    put("JobStageStep", Arrays.asList("jobId","createdOn","createdBy","updatedOn", "updatedBy"));
                    put("StageMaster", Arrays.asList("id"));
        }});
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
}