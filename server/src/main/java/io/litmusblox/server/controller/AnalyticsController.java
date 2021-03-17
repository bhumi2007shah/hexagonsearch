/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.service.IAnalyticsService;
import io.litmusblox.server.service.JobAnalytics.AnalyticsDataResponseBean;
import io.litmusblox.server.service.JobAnalytics.InterviewAnalyticsResponseBean;
import io.litmusblox.server.service.JobAnalytics.InterviewDetailBean;
import io.litmusblox.server.service.JobAnalytics.JobAnalyticsResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Analytics class that exposes all REST endpoint for analytics related operation
 *
 * @author : sameer
 * Date : 17/03/20
 * Time : 7:58 PM
 * Class Name : AnalyticsController
 * Project Name : server
 */
@CrossOrigin(allowedHeaders = "*")
@RestController
@RequestMapping("/api/analytics/")
@Log4j2
public class AnalyticsController {

    @Autowired
    IAnalyticsService analyticsService;

    /**
     *
     * API for fetching analytics for a job id.
     *
     * @param jobId
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping(path = {"job/{jobId}","job/{jobId}/{startDate}/{endDate}"})
    String getJobAnalytics(@PathVariable("jobId") Long jobId, @PathVariable("startDate")Optional<Date> startDate, @PathVariable("endDate")Optional<Date>endDate) throws Exception {
        log.info("Received request to fetch analytics request for jobId:{}", jobId);
        long startTime = System.currentTimeMillis();

        //call Analytics service function get job analytics which returns object of JobAnalyticsResponseBean
        JobAnalyticsResponseBean jobAnalyticsResponseBean = analyticsService.getJobAnalyticsData(jobId, startDate, endDate);

        log.info("Completed job analytics request in {}ms", System.currentTimeMillis()-startTime);

        //strip job id from each sub classes
        return Util.stripExtraInfoFromResponseBean(jobAnalyticsResponseBean,
                null,
                new HashMap<String, List<String>>() {{
                    put("CandidateSourceAnalyticsBean", Arrays.asList("jobId"));
                    put("KeySkillStrengthAnalyticsBean", Arrays.asList("jobId"));
                    put("ScreeningStatusAnalyticsBean", Arrays.asList("jobId"));
                    put("SubmittedAnalyticsBean", Arrays.asList("jobId"));
                    put("InterviewAnalyticsBean", Arrays.asList("jobId"));
                }}
        );
    }

    /**
     * API to fetch analytics data related to job and candidates
     *
     * @return  AnalyticsDataResponseBean
     */
    @GetMapping(path = {"/fetchJobAnalytics"})
    @PreAuthorize("hasRole('" + IConstant.UserRole.Names.SUPER_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.CLIENT_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.RECRUITER + "')")
    AnalyticsDataResponseBean getAnalyticsData(){
        log.info("Received request to fetch analytics data related to job and candidate");
        long startTime = System.currentTimeMillis();
        AnalyticsDataResponseBean analyticsDataResponseBean = analyticsService.getAnalyticsData();
        log.info("Completed to fetch analytics data related to job and candidate request in {}ms", System.currentTimeMillis()-startTime);
        return analyticsDataResponseBean;
    }

    /**
     * API to fetch interview analytics
     *
     * @param selectedMonthDate for which 2 months we want data
     * @return InterviewAnalyticsResponseBean
     */
    @GetMapping(path = {"/fetchInterviewAnalytics"})
    @PreAuthorize("hasRole('" + IConstant.UserRole.Names.SUPER_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.CLIENT_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.RECRUITER + "')")
    InterviewAnalyticsResponseBean getInterviewAnalyticsData(@RequestParam String selectedMonthDate){
        log.info("Received request to fetch analytics data related candidate interview");
        long startTime = System.currentTimeMillis();
        InterviewAnalyticsResponseBean interviewAnalyticsResponseBean = analyticsService.getInterviewAnalyticsData(selectedMonthDate);
        log.info("Completed to fetch analytics data related to candidate interview request in {}ms", System.currentTimeMillis()-startTime);
        return interviewAnalyticsResponseBean;
    }

    /**
     * API to fetch interview details
     *
     * @param selectedDate for selected interview date give details
     * @return list of InterviewDetailBean
     */
    @GetMapping(path = {"/fetchInterviewDetails/{selectedDate}"})
    @PreAuthorize("hasRole('" + IConstant.UserRole.Names.SUPER_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.CLIENT_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.RECRUITER + "')")
    List<InterviewDetailBean> getInterviewDetails(@PathVariable String selectedDate){
        log.info("Received request to fetch interview details");
        long startTime = System.currentTimeMillis();
        List<InterviewDetailBean> interviewDetailBeanList = analyticsService.getInterviewDetails(selectedDate);
        log.info("Completed to fetch interview details request in {}ms", System.currentTimeMillis()-startTime);
        return interviewDetailBeanList;
    }
}
