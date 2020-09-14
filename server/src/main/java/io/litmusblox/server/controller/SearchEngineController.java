/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.ISearchEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
/**
 * Controller to manage search engine operations
 *
 * Date : 14/09/20
 * Time : 5:13 PM
 * Class Name : SearchEngineController
 * Project Name : server
 */

@CrossOrigin(allowedHeaders = "*")
@RestController
@RequestMapping("/api/searchEngine")

public class SearchEngineController{

    @Autowired
    ISearchEngineService searchEngineService;

    @Value("${searchEngineBaseUrl}")
    String searchEngineBaseUrl;

    /**
     *
     * @param jsonData data to be passed to search engine
     * @return
     * @throws Exception
     */
    @PostMapping(value="data/verifyNodes")
    void verifyNodes(@RequestBody String jsonData) throws  Exception{
        searchEngineService.verifyNodes(jsonData, JwtTokenUtil.getAuthToken());
    }


    /**
     *
     * @param jsonData data to be passed to search engine
     * @return List of Candidates response
     * @throws Exception
     */
    @PostMapping(value="candidate/search")
    String  candidateSearch(@RequestBody String jsonData) throws Exception{
        return searchEngineService.candidateSearch(jsonData, JwtTokenUtil.getAuthToken());
    }

    /**
     *
     * @return response from search engine
     * @throws Exception
     */
    @GetMapping(value = "/data/getUnverifiedNodes")
    String getUnverifiedNodes() throws Exception{
        return searchEngineService.getUnverifiedNodes(JwtTokenUtil.getAuthToken());
    }
}

