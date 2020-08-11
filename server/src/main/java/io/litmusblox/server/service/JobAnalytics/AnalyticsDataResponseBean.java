/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.JobAnalytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to hold all job related analytics
 *
 * @author : Sumit
 * Date : 29/07/20
 * Time : 10:02 PM
 * Class Name : AnalyticsDataResponseBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDataResponseBean {

    Integer openJobs = 0;
    Map<String, Integer> candidateCountAnalyticsMap = new LinkedHashMap<>();
    Map<String, Integer> jobAgingAnalyticsMap = new LinkedHashMap<>();
    Map<String, Integer> JobCandidatePipelineAnalyticsMap = new LinkedHashMap<>();
}
