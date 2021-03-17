/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.service.JobAnalytics.AnalyticsDataResponseBean;
import io.litmusblox.server.service.JobAnalytics.InterviewAnalyticsResponseBean;
import io.litmusblox.server.service.JobAnalytics.InterviewDetailBean;
import io.litmusblox.server.service.JobAnalytics.JobAnalyticsResponseBean;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for all analytics related queries
 *
 * @author : Shital Raval
 * Date : 5/2/20
 * Time : 1:01 PM
 * Class Name : IAnalyticsService
 * Project Name : server
 */
public interface IAnalyticsService {
    /**
     * Find analytics
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    List<AnalyticsResponseBean> analyticsByCompany (String startDate, String endDate) throws Exception;

    /**
     *
     * Service for fetching analytics for a job id.
     *
     * @param jobId jobId for which analytics will be generated
     * @param startDate optional parameter
     * @param endDate optional parameter
     * @return JobAnalyticsResponseBean
     * @throws Exception
     */
     JobAnalyticsResponseBean getJobAnalyticsData(Long jobId, Optional<Date> startDate, Optional<Date> endDate) throws Exception;

    /**
     * Service method to get job and candidate related analytics
     *
     * @return AnalyticsDataResponseBean
     */
    AnalyticsDataResponseBean getAnalyticsData();

    /**
     * Service to fetch interview analytics
     *
     * @param selectedMonthDate for which 2 months we want data
     * @return InterviewAnalyticsResponseBean
     */
    InterviewAnalyticsResponseBean getInterviewAnalyticsData(String selectedMonthDate);

    /**
     * Service to fetch interview details
     *
     * @param selectedDate for selected interview date give details
     * @return list of InterviewDetailBean
     */
    List<InterviewDetailBean> getInterviewDetails(String selectedDate);
}
