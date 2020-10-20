/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.User;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.ISearchEngineService;
import io.litmusblox.server.utils.RestClient;
import io.litmusblox.server.utils.RestClientResponseBean;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

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
        Long startTime = System.currentTimeMillis();
        Map<String, Object> headerInformation = getLoggedInUserInformation();
        if(jsonData == null)
            throw new ValidationException("Invalid request", HttpStatus.BAD_REQUEST);
        String responseData = RestClient.getInstance().consumeRestApi(jsonData, searchEngineBaseUrl+"candidate/search", HttpMethod.POST, authToken, null, null, Optional.of(headerInformation)).getResponseBody();
        log.info("Completed execution of Candidate search method in {} ms", System.currentTimeMillis() - startTime);
        return responseData;
    }

    public String getUnverifiedNodes(String authToken) throws Exception{
        log.info("Inside getUnverifiedNodes method");
        Long startTime = System.currentTimeMillis();
        Map<String, Object> headerInformation = getLoggedInUserInformation();
        String responseData = RestClient.getInstance().consumeRestApi(null, searchEngineBaseUrl + "data/getUnverifiedNodes", HttpMethod.GET, JwtTokenUtil.getAuthToken(), null, null, Optional.of(headerInformation)).getResponseBody();
        log.info("Completed execution of getVerifiedNodes method in {} ms", System.currentTimeMillis() - startTime);
        return responseData;
    }

    public void verifyNodes(String jsonData, String authToken) throws Exception{
        log.info("Inside verifyNodes call");
        Long startTime = System.currentTimeMillis();
        Map <String, Object> headerInformation = getLoggedInUserInformation();
        if(jsonData == null)
            throw new ValidationException("Invalid request", HttpStatus.BAD_REQUEST);
        RestClient.getInstance().consumeRestApi(jsonData, searchEngineBaseUrl+"candidate/search", HttpMethod.POST, authToken, null, null, Optional.of(headerInformation));
        log.info("Completed execution of verifyNodes method in {} ms", System.currentTimeMillis() - startTime);
    }

    public String importData(MultipartFile masterDataFile, Long companyId, String fileType, String authToken) throws Exception{
        log.info("Inside importData method");
        Long startTime = System.currentTimeMillis();
        Map<String, Object> headerInformation = getLoggedInUserInformation();
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        ByteArrayResource fileContent = null;
        try {
            fileContent = new ByteArrayResource(masterDataFile.getBytes()) {
                @Override
                public String getFilename() {
                    return masterDataFile.getOriginalFilename();
                }

                @Override
                public long contentLength() {
                    return masterDataFile.getSize();
                }
            };
        } catch(Exception e){
            log.info(e.getCause());
        }
        if(null == fileContent || null == fileType)
            throw new ValidationException("Invalid request", HttpStatus.BAD_REQUEST);
        formData.add("masterDataFile", fileContent);
        if(companyId!= null)
            formData.add("companyId", companyId);
        formData.add("fileType", fileType);
        RestClientResponseBean response = RestClient.getInstance().consumeRestApi(searchEngineBaseUrl + "data/importData", authToken, HttpMethod.POST, formData, Optional.of(headerInformation));
        if(response.getStatusCode() != HttpStatus.OK.value()) {
            String errorMessage = response.getResponseBody().substring(response.getResponseBody().indexOf("message") + 10).replace("\"", "") ;
            throw new ValidationException(errorMessage, response.getStatusCode());
        }
        log.info("Completed execution of importData method in {} ms", System.currentTimeMillis() - startTime);
        return response.getResponseBody();
    }

    public Map<String, Object> getLoggedInUserInformation(){
        Long startTime = System.currentTimeMillis();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Inside getLoggedInUserInformation method. Logged in by user {}", loggedInUser.getEmail());
        Map<String, Object> userDetails = new HashMap(3);
        userDetails.put("userId", loggedInUser.getId());
        userDetails.put("userEmail", loggedInUser.getEmail());
        userDetails.put("userCompanyId", loggedInUser.getCompany().getId());
        log.info("Completed adding loggedInUserInformation in {} ms", System.currentTimeMillis() - startTime);
        return userDetails;
    }

}

