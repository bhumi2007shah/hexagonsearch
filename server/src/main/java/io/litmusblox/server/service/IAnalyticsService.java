/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import java.util.Date;
import java.util.List;

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
    List<AnalyticsResponseBean> analyticsByCompany (Date startDate, Date endDate) throws Exception;
}
