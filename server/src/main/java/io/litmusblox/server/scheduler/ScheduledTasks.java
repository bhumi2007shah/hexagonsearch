/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.scheduler;

import io.litmusblox.server.service.IFtpService;
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.impl.FetchEmailService;
import io.litmusblox.server.uploadProcessor.IProcessUploadedCV;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
@Profile({"prod","testServer"})
@Component
@Log4j2
public class ScheduledTasks {

    @Autowired
    IProcessUploadedCV processUploadedCV;

    @Autowired
    FetchEmailService fetchEmailService;

    @Autowired
    IJobCandidateMappingService jobCandidateMappingService;

    @Autowired
    IFtpService ftpService;

    @Scheduled(fixedDelay = 300000, initialDelay = 5000)
    public void parseAndProcessCv() {
        log.info("started parse and process cv. Thread: {}", Thread.currentThread().getId());
        processUploadedCV.processCv();
        log.info("completed parse and process cv. Thread: {}", Thread.currentThread().getId());
    }

    @Scheduled(fixedRate = 2*60*1000, initialDelay = 2000)
    public void processEmailApplications() {
        log.info("started process email applications. Thread: {}", Thread.currentThread().getId());
        fetchEmailService.processEmail();
        log.info("completed process email applications. Thread: {}", Thread.currentThread().getId());
    }

    @Scheduled(fixedRate = 2*60*1000, initialDelay = 2000)
    public void inviteAutoSourcedCandidates() throws Exception {
        log.info("started inviting autosourced candidates. Thread: {}", Thread.currentThread().getId());
        jobCandidateMappingService.inviteAutoSourcedCandidate();
        log.info("Completed inviting autosourced candidates. Thread: {}", Thread.currentThread().getId());
    }

    @Scheduled(fixedDelay = 300000, initialDelay = 5000)
    public void updateCvRating() {
        log.info("started to update cv rating. Thread: {}", Thread.currentThread().getId());
        processUploadedCV.updateCvRating();
        log.info("completed to update cv rating. Thread: {}", Thread.currentThread().getId());
    }

    @Scheduled(cron = "0 0 23,0-6 * * *", zone = "IST")
    public void createCandidateOnSearchEngine(){
        log.info("started create existing candidate on search engine. Thread {}", Thread.currentThread().getId());
        jobCandidateMappingService.createExistingCandidateOnSearchEngine();
        log.info("Completed process for create existing candidate on search engine. Thread {}", Thread.currentThread().getId());
    }

    @Scheduled(fixedDelay = 20000, initialDelay = 2000)
    public void fetchCandidateXmlFiles(){
        log.info("Started Fetching candidate xml files using FTP Service. Thread {}", Thread.currentThread().getId());
        ftpService.fetchCandidateXmlFiles();
        log.info("Completed fetching candidate xml files. Thread {}", Thread.currentThread().getId());
    }

    public void processCandidateXmlFiles(){
        log.info("Started processing candidate xml files. Thread {}", Thread.currentThread().getId());
        jobCandidateMappingService.xmlFileProcessor();
        log.info("Completed processing candidate xml files. Thread {}", Thread.currentThread().getId());
    }
}