/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

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
     * @return Map with user id, email, company id,
     */
    public Map getLoggedInUserInformation();
}
