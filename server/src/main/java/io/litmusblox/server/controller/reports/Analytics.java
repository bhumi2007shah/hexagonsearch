/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.service.reports.IReportService;
import io.litmusblox.server.service.reports.beans.RequestBean;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author : sameer
 * Date : 26/02/21
 * Time : 11:11 AM
 * Class Name : Analytics
 * Project Name : server
 */
@RestController
@RequestMapping("/api/reports/")
@CrossOrigin(allowedHeaders = "*")
@Log4j2
public class Analytics {

    @Autowired
    IReportService reportService;

    @PostMapping(value = "dsr")
    String dailySubmitReport(@RequestBody RequestBean requestBean){
        long startTime = System.currentTimeMillis();
        log.info("Received request to get Daily Submit Report");
        String response = "";
        try {
            response = new ObjectMapper().writeValueAsString(reportService.getDailySubmitReport(requestBean));
        }catch (Exception e){
            throw new WebException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Completed request to get Daily Submit Report in {}ms", System.currentTimeMillis()-startTime);
        return response;
    }

    @PostMapping(value = "csr")
    String dailySourceReport(@RequestBody RequestBean requestBean){
        long startTime = System.currentTimeMillis();
        log.info("Received request to get Daily Submit Report");
        String response = "";
        try {
            response = new ObjectMapper().writeValueAsString(reportService.getDailySourceReport(requestBean));
        }catch (Exception e){
            throw new WebException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Completed request to get Daily Submit Report in {}ms", System.currentTimeMillis()-startTime);
        return response;
    }

    @PostMapping(value = "isr")
    String dailyInterviewReport(@RequestBody RequestBean requestBean){
            long startTime = System.currentTimeMillis();
            log.info("Received request to get Daily Submit Report");
            String response = "";
            try {
                response = new ObjectMapper().writeValueAsString(reportService.getDailyInterviewReport(requestBean));
            }catch (Exception e){
                throw new WebException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        log.info("Completed request to get Daily Submit Report in {}ms", System.currentTimeMillis()-startTime);
        return response;
    }

    @PostMapping(value = "jir")
    String dailyJobIntakeReport(@RequestBody RequestBean requestBean){
            long startTime = System.currentTimeMillis();
            log.info("Received request to get Daily Submit Report");
            String response = "";
            try {
                response = new ObjectMapper().writeValueAsString(reportService.getDailyJobIntakeReport(requestBean));
            }catch (Exception e){
                throw new WebException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        log.info("Completed request to get Daily Submit Report in {}ms", System.currentTimeMillis()-startTime);
        return response;
    }
}
