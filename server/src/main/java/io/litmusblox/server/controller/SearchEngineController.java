/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.ISearchEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    Environment environment;

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

    /**
     *
     * @param masterDataFile file to passed to be passed to search engine
     * @param companyId company to which the file belongs
     * @param fileType the type of file
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/data/importData")
    String importData(@RequestParam MultipartFile masterDataFile, Long companyId, String fileType) throws Exception{
        return searchEngineService.importData(masterDataFile, companyId, fileType, JwtTokenUtil.getAuthToken());
    }
}

