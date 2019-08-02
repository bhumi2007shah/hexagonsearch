/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.service.IJobControllerMappingService;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Controller class for following:
 * 1. Upload single candidate for a job
 * 2. Upload an excel file of candidates for a job
 *
 * @author : Shital Raval
 * Date : 16/7/19
 * Time : 4:39 PM
 * Class Name : JobCandidateMappingController
 * Project Name : server
 */
@CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS}, allowedHeaders = {"Content-Type", "Authorization","X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"}, exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"})
@RestController
@RequestMapping("/api/jcm")
@Log4j2
public class JobCandidateMappingController {

    @Autowired
    IJobControllerMappingService jobControllerMappingService;

    /**
     * Api to add a single candidate to a job
     *
     * @param candidate the candidate to be added
     * @param jobId the job id for which the candidate is to be added
     * @throws Exception
     */
    @PostMapping(value = "/addCandidate/individual")
    @ResponseStatus(value = HttpStatus.OK)
    String addSingleCandidate(@RequestBody List<Candidate> candidate, @RequestParam("jobId") Long jobId) throws Exception{
        log.info("Received request to add a list of individually added candidates. Number of candidates to be added: " + candidate.size());
        long startTime = System.currentTimeMillis();
        UploadResponseBean responseBean = jobControllerMappingService.uploadIndividualCandidate(candidate, jobId);
        log.info("Completed processing list of candidates in " + (System.currentTimeMillis()-startTime) + "ms.");
        return Util.stripExtraInfoFromResponseBean(responseBean, null,
                new HashMap<String, List<String>>() {{
                    put("CandidateFilter", Arrays.asList("candidateDetails","candidateEducationDetails","candidateProjectDetails","candidateCompanyDetails",
                            "candidateOnlineProfiles","candidateWorkAuthorizations","candidateLanguageProficiencies","candidateSkillDetails"));
                }});
    }

    /**
     * Api method to add candidates from a file in one of the supported formats
     *
     * @param multipartFile the file with candidate information
     * @param jobId the job for which the candidates have to be added
     * @param fileFormat the format of file, for e.g. Naukri, LB format
     * @return the status of upload operation
     * @throws Exception
     */
    @PostMapping(value = "/addCandidate/file")
    @ResponseStatus(value = HttpStatus.OK)
    String addCandidatesFromFile(@RequestParam("file") MultipartFile multipartFile, @RequestParam("jobId")Long jobId, @RequestParam("fileFormat")String fileFormat) throws Exception {
        log.info("Received request to add candidates from a file.");
        long startTime = System.currentTimeMillis();
        UploadResponseBean responseBean = jobControllerMappingService.uploadCandidatesFromFile(multipartFile, jobId, fileFormat);
        log.info("Completed processing candidates from file in " + (System.currentTimeMillis()-startTime) + "ms.");
        return Util.stripExtraInfoFromResponseBean(responseBean, null,
                new HashMap<String, List<String>>() {{
                    put("CandidateFilter", Arrays.asList("candidateDetails","candidateEducationDetails","candidateProjectDetails","candidateCompanyDetails",
                            "candidateOnlineProfiles","candidateWorkAuthorizations","candidateLanguageProficiencies","candidateSkillDetails"));
                    put("UserClassFilter", Arrays.asList("createdBy","company"));
                }});
    }

    /**
     * Api method to source and add a candidate from a plugin, for example Naukri plugin
     *
     * @param candidateString the json string of candidate to be added
     * @param jobId the job for which the candidate is to be added
     * @return the status of upload operation
     * @throws Exception
     */
    @PostMapping(value = "/addCandidate/plugin")
    String uploadCandidateFromPlugin(@RequestParam(name = "candidateCv", required = false) MultipartFile candidateCv, @RequestParam("candidate") String candidateString, @RequestParam("jobId") Long jobId) throws Exception {
        log.info("Received request to add a candidate from plugin");
        long startTime = System.currentTimeMillis();
        Candidate candidate=new ObjectMapper().readValue(candidateString, Candidate.class);
        UploadResponseBean responseBean = jobControllerMappingService.uploadCandidateFromPlugin(candidate, jobId, candidateCv);
        log.info("Completed adding candidate from plugin in " + (System.currentTimeMillis()-startTime) + "ms.");
        return Util.stripExtraInfoFromResponseBean(responseBean, null,
                new HashMap<String, List<String>>() {{
                    put("CandidateFilter", Arrays.asList("candidateDetails","candidateEducationDetails","candidateProjectDetails","candidateCompanyDetails",
                            "candidateOnlineProfiles","candidateWorkAuthorizations","candidateLanguageProficiencies","candidateSkillDetails"));
                }});
    }

    /**
     * Api to invite candidates to fill chatbot for a job
     *
     * @param jcmList list of jcm ids for chatbot invitation
     * @throws Exception
     */
    @PostMapping(value = "/inviteCandidates")
    @ResponseStatus(value = HttpStatus.OK)
    void inviteCandidates(@RequestBody List<Long> jcmList) throws Exception {
        log.info("Received request to invite candidates");
        long startTime = System.currentTimeMillis();
        jobControllerMappingService.inviteCandidates(jcmList);
        log.info("Completed inviting candidates in " + (System.currentTimeMillis()-startTime)+"ms.");
    }
}
