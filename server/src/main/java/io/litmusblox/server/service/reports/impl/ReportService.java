/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.reports.impl;

import io.litmusblox.server.service.reports.IReportService;
import io.litmusblox.server.service.reports.QueryExecutor;
import io.litmusblox.server.service.reports.beans.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : sameer
 * Date : 26/02/21
 * Time : 11:37 AM
 * Class Name : ReportService
 * Project Name : server
 */
@Service
public class ReportService implements IReportService {

    @Autowired
    QueryExecutor executor;

    /**
     * Service That returns String representation of Daily Submit Report
     * @return
     */
    @Override
    public Map<String, Map<Object, Object>> getDailySubmitReport(RequestBean requestBean) {
        List<Dsr> dsrList = executor.getDsrRows(requestBean);
        Map<String, Map<Object, Object>> submitReportByUser = new HashMap<>();
        String response="";
        if(dsrList.size()>0){
             submitReportByUser = dsrList.stream().collect(Collectors.groupingBy(Dsr::getSubmittedBy, Collectors.toMap(dsr -> dsr.getSubmittedOn(), dsr->dsr.getSubmitCount())));
        }

        return submitReportByUser;
    }

    /**
     * Service That returns String representation of Daily Source Report
     * @return
     */
    @Override
    public Map<String, Map<Object, Object>> getDailySourceReport(RequestBean requestBean) {
        List<Csr> csrList = executor.getCsrRows(requestBean);
        Map<String, Map<Object, Object>> sourcingReportByUser = new HashMap<>();
        String response="";
        if(csrList.size()>0){
            sourcingReportByUser = csrList.stream().collect(Collectors.groupingBy(Csr::getSourcedBy, Collectors.toMap(csr -> csr.getSourcedOn(), csr -> csr.getSourceCount() )));
        }
        return sourcingReportByUser;
    }

    /**
     * Interface That returns String representation of Daily Source Report
     *
     * @param requestBean
     * @return
     */
    @Override
    public Map<String, Map<Date, Map<Object, Object>>> getDailyInterviewReport(RequestBean requestBean) {
        List<Isr> isrList = executor.getIsrRows(requestBean);
        Map<String, Map<Date, Map<Object, Object>>> interviewScheduleReportByUserAndDate = new HashMap<>();
        if(isrList.size()>0){
            interviewScheduleReportByUserAndDate = isrList.stream().collect(
                    Collectors.groupingBy(
                            Isr::getScheduledBy,
                            Collectors.groupingBy(
                                    Isr::getCreatedOn,
                                    Collectors.toMap(
                                            isr -> "scheduled", Isr::getTotalScheduled
                                    )
                            )
                    )
            );
        }
        return interviewScheduleReportByUserAndDate;
    }

    /**
     * Interface That returns String representation of Daily Job Intake Report
     *
     * @param requestBean
     * @return
     */
    @Override
    public Map<String, Map<Object, Object>> getDailyJobIntakeReport(RequestBean requestBean) {
        List<Jir> jirList = executor.getJirRows(requestBean);
        Map<String, Map<Object, Object>> jobIntakeReportByCompany = new HashMap<>();
        if(jirList.size()>0){
             jobIntakeReportByCompany = jirList.stream().collect(Collectors.groupingBy(Jir::getCompanyName, Collectors.toMap(jir -> jir.getCreatedOn(), jir -> jir.getTotalCount())));
        }
        return jobIntakeReportByCompany;
    }
}
