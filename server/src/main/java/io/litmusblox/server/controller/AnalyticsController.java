/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.service.IAnalyticsService;
import io.litmusblox.server.service.JobAnalytics.JobAnalyticsResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
}
