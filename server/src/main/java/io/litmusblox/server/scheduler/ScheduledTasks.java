/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.scheduler;

import io.litmusblox.server.service.impl.FetchEmailService;
import io.litmusblox.server.uploadProcessor.IProcessUploadedCV;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for the application
 *
 * @author : Shital Raval
 * Date : 16/7/19
 * Time : 2:45 PM
 * Class Name : ScheduledTasks
 * Project Name : server
 */
@Component
@Log4j2
public class ScheduledTasks {

    @Autowired
    IProcessUploadedCV processUploadedCV;

    @Autowired
    FetchEmailService fetchEmailService;

    @Scheduled(fixedRate = 30000, initialDelay = 5000)
    public void parseAndProcessCv() {
        log.info("started parse and process cv. Thread: {}", Thread.currentThread().getId());
        processUploadedCV.processCv();
        log.info("completed parse and process cv. Thread: {}", Thread.currentThread().getId());
    }

    @Scheduled(fixedRate = 120000, initialDelay = 5000)
    public void rateAndProcessCv() {
        log.info("started rate and process cv. Thread: {}", Thread.currentThread().getId());
        processUploadedCV.rateCv();
        log.info("completed rate and process cv. Thread: {}", Thread.currentThread().getId());
    }

    @Scheduled(fixedRate = 2*60*1000, initialDelay = 2000)
    public void processEmailApplications() {
        log.info("started process email applications. Thread: {}", Thread.currentThread().getId());
        fetchEmailService.processEmail();
        log.info("completed process email applications. Thread: {}", Thread.currentThread().getId());
    }

    @Scheduled(fixedRate = 120000, initialDelay = 5000)
    public void convertCvFileToCvText() {
        log.info("started convert cv file to cv text. Thread: {}", Thread.currentThread().getId());
        processUploadedCV.cvToCvText();
        log.info("completed convert cv file to cv text. Thread: {}", Thread.currentThread().getId());
    }
}