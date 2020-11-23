/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.service.IBackendDataService;
import io.litmusblox.server.service.JobAnalytics.InterviewAnalyticsResponseBean;
import io.litmusblox.server.service.impl.BackendDataService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.PUT}, allowedHeaders = {"Content-Type", "Authorization","X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Access-Control-Allow-Origin"}, exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"})
@RestController
@RequestMapping("/api/backend/")
@Log4j2
public class BackendDataController {

    @Autowired
    IBackendDataService backendDataService;

    @GetMapping(value = "/migrateCvRating")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    void migrateCvRatingData() throws  Exception{
        log.info("Received request to migrate Cv Rating Data");
        long startTime = System.currentTimeMillis();
        backendDataService.migrateCvRatingData();
        log.info("Completed Cv Rating Data migration request in {} ms", System.currentTimeMillis()-startTime);
    }
}
