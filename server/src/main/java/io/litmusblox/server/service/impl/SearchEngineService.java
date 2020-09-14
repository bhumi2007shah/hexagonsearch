/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.User;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.ISearchEngineService;
import io.litmusblox.server.utils.RestClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service to handle all search engine realted operations
 *
 * Date : 14/09/20
 * Time : 5:18 PM
 * Class Name : SearchEngineService
 * Project Name : server
 */

@Log4j2
@Service
public class SearchEngineService implements ISearchEngineService {

    @Value("${searchEngineBaseUrl}")
    String searchEngineBaseUrl;

    public String candidateSearch(String jsonData, String authToken) throws Exception{
        log.info("Inside candidateSearch method");
        Map headerInformation = getLoggedInUserInformation();
        if(jsonData == null)
            throw new ValidationException("Invalid request", HttpStatus.BAD_REQUEST);
        String responseData = RestClient.getInstance().consumeRestApi(jsonData, searchEngineBaseUrl+"candidate/search", HttpMethod.POST, authToken, null, null, Optional.of(headerInformation)).getResponseBody();
        log.info("Completed execution of Candidate search method");
        return responseData;
    }

    public String getUnverifiedNodes(String authToken) throws Exception{
        log.info("Inside getUnverifiedNodes method");
        Map headerInformation = getLoggedInUserInformation();
        String responseData = RestClient.getInstance().consumeRestApi(null, searchEngineBaseUrl + "data/getUnverifiedNodes", HttpMethod.GET, JwtTokenUtil.getAuthToken(), null, null, Optional.of(headerInformation)).getResponseBody();
        log.info("Completed execution of getVerifiedNodes method");
        return responseData;
    }

    public void verifyNodes(String jsonData, String authToken) throws Exception{
        log.info("Inside verifyNodes call");
        Map headerInformation = getLoggedInUserInformation();
        if(jsonData == null)
            throw new ValidationException("Invalid request", HttpStatus.BAD_REQUEST);
        RestClient.getInstance().consumeRestApi(jsonData, searchEngineBaseUrl+"candidate/search", HttpMethod.POST, authToken, null, null, Optional.of(headerInformation));
        log.info("Completed execution of verifyNodes method");
    }

    public Map getLoggedInUserInformation(){
        log.info("Inside getLoggedInUserInformation method.");
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map userDetails = new HashMap(3);
        userDetails.put("userId", loggedInUser.getId());
        userDetails.put("userEmail", loggedInUser.getEmail());
        userDetails.put("userCompanyId", loggedInUser.getCompany().getId());
        log.info("Completed adding loggedInUserInformation.");
        return userDetails;
    }

}

