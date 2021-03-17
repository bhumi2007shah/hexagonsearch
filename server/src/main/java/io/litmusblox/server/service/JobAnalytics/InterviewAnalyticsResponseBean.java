/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.JobAnalytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Sumit
 * Date : 31/07/20
 * Time : 10:18 AM
 * Class Name : InterviewAnalyticsResponseBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewAnalyticsResponseBean {
     Integer totalFutureInterviews;
     Integer next7DaysInterviews;
     Map<String, Integer> monthInterviewMap = new LinkedHashMap<>();
     Map<String, List<String>> twoMonthInterviewDatesMap = new LinkedHashMap<>();
}
