/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.reports;

import io.litmusblox.server.service.reports.beans.RequestBean;

import java.util.Date;
import java.util.Map;

/**
 * @author : sameer
 * Date : 26/02/21
 * Time : 11:31 AM
 * Class Name : IReportService
 * Project Name : server
 */
public interface IReportService {
    /**
     * Interface That returns String representation of Daily Submit Report
     * @return
     */
    Map<String, Map<Object, Object>> getDailySubmitReport(RequestBean requestBean);

    /**
     * Interface That returns String representation of Daily Source Report
     * @return
     */
    Map<String, Map<Object, Object>> getDailySourceReport(RequestBean requestBean);

    /**
     * Interface That returns String representation of Daily Interview Schedule Report
     * @return
     */
    Map<String, Map<Date, Map<Object, Object>>> getDailyInterviewReport(RequestBean requestBean);

    /**
     * Interface That returns String representation of Daily Job Intake Report
     * @return
     */
    Map<String, Map<Object, Object>> getDailyJobIntakeReport(RequestBean requestBean);
}
