/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.service.IMasterDataService;
import io.litmusblox.server.service.impl.IndustryMasterDataRequestBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Rest controller for master data. Exposes apis for the following functions:
 * 1. Fetch specific master data values
 * 2. Reload master data in server cache
 * 3. Save master data to corresponding table
 *
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 2:09 PM
 * Class Name : MasterDataController
 * Project Name : server
 */
@CrossOrigin(allowedHeaders = "*")
@RestController
@RequestMapping("/api/masterdata")
@Log4j2
public class MasterDataController {

    @Autowired
    IMasterDataService masterDataService;

    /**
     * Reload all master data
     * @throws Exception
     */
    @GetMapping(value="/reload")
    void reloadMasterData() throws Exception {
        log.info("Received request to reload master data");
        long startTime = System.currentTimeMillis();
        masterDataService.reloadMasterData();
        log.info("Completed request to reload master data in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * Fetch individual master data items
     *
     * @param requestItems
     * @return
     * @throws Exception
     */
    @PostMapping(value="/fetch/items")
    String fetchForItems(@RequestBody List<String> requestItems) throws Exception {
        return Util.stripExtraInfoFromResponseBean(
                masterDataService.fetchForItems(requestItems),null,
            new HashMap<String, List<String>>(){{
                put("ScreeningQuestions", new ArrayList<>(0));
                put("MasterData", new ArrayList<>(0));
                put("RejectionReasonMasterData", Arrays.asList("stageId"));
            }});
    }

    /**
     * Add master data value. Supported for the following types:
     * 1. UserScreeningQuestion
     *
     * @param jsonData data to persist
     * @param masterDataType the type of master data to be added
     */
    @PostMapping(value="/add/{masterDataType}")
    @ResponseStatus(value = HttpStatus.OK)
    void addMasterData(@RequestBody String jsonData, @PathVariable("masterDataType") String masterDataType) throws Exception {
        log.info("Received request to add master data type: " + masterDataType);
        long startTime = System.currentTimeMillis();
        masterDataService.addMasterData(jsonData, masterDataType);
        log.info("Completed request to add master data type in " + (System.currentTimeMillis() - startTime) + "ms");
    }
    @PostMapping(value = "/addIndustryMasterData")
    @ResponseStatus(value = HttpStatus.OK)
    void addIndustryMasterData(@RequestBody List<IndustryMasterDataRequestBean> industryMasterDataRequestBeanList){
        long startTime = System.currentTimeMillis();
        log.info("inside addIndustryMasterData");
        masterDataService.addIndustryMasterData(industryMasterDataRequestBeanList);
        log.info("Industry added in {}ms", System.currentTimeMillis()-startTime);
    }
}
