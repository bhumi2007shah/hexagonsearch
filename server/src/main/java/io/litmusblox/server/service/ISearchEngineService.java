/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Date : 14/09/20
 * Time : 5:16 PM
 * Class Name : ISearchEngineService
 * Project Name : server
 */

public interface ISearchEngineService {

    /**
     * @param authToken jwtToken generated
     * @return response from search engine call
     * @throws Exception
     */
    public String getUnverifiedNodes(String authToken) throws Exception;

    /**
     * @param jsonData  data received from request and to be passed to searchEngine
     * @param authToken jwtToken  generated
     * @return response from search engine call
     * @throws Exception
     */
    public String candidateSearch(String jsonData, String authToken) throws Exception;

    /**
     *
     * @param jsonData data received from request and to be passed to searchEngine
     * @param authToken jwtToken generated
     * @throws Exception
     */
    public void verifyNodes(String jsonData, String authToken) throws Exception;

    /**
     *
     * @param masterDataFile File to be passed to the search engine
     * @param companyId the company to which the file data is to be added
     * @param fileType type of type
     * @param authToken jwtToken generated
     * @return
     * @throws Exception
     */
    public String importData(MultipartFile masterDataFile, Long companyId, String fileType, String authToken) throws Exception;

    /**
     *
     * @return Map with user id, email, company id,
     */
    public Map<String, Object> getLoggedInUserInformation();
}
