/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.ISearchEngineService;
import io.litmusblox.server.service.ImportDataResponseBean;
import io.litmusblox.server.service.MasterDataBean;
import io.litmusblox.server.utils.RestClient;
import io.litmusblox.server.utils.LoggedInUserInfoUtil;
import io.litmusblox.server.utils.RestClientResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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
        Map<String, Object> headerInformation = LoggedInUserInfoUtil.getLoggedInUserInformation();
        if(jsonData == null)
            throw new ValidationException("Invalid request", HttpStatus.BAD_REQUEST);
        String responseData = RestClient.getInstance().consumeRestApi(jsonData, searchEngineBaseUrl+"candidate/search", HttpMethod.POST, authToken, null, null, Optional.of(headerInformation)).getResponseBody();
        log.info("Completed execution of Candidate search method in {} ms", System.currentTimeMillis() - startTime);
        return responseData;
    }

    public String getUnverifiedNodes(String authToken) throws Exception{
        log.info("Inside getUnverifiedNodes method");
        Long startTime = System.currentTimeMillis();
        Map<String, Object> headerInformation = LoggedInUserInfoUtil.getLoggedInUserInformation();
        String responseData = RestClient.getInstance().consumeRestApi(null, searchEngineBaseUrl + "data/getUnverifiedNodes", HttpMethod.GET, JwtTokenUtil.getAuthToken(), null, null, Optional.of(headerInformation)).getResponseBody();
        log.info("Completed execution of getVerifiedNodes method in {} ms", System.currentTimeMillis() - startTime);
        return responseData;
    }

    public void verifyNodes(String jsonData, String authToken) throws Exception{
        log.info("Inside verifyNodes call");
        Long startTime = System.currentTimeMillis();
        Map <String, Object> headerInformation = LoggedInUserInfoUtil.getLoggedInUserInformation();
        if(jsonData == null)
            throw new ValidationException("Invalid request", HttpStatus.BAD_REQUEST);
        RestClient.getInstance().consumeRestApi(jsonData, searchEngineBaseUrl+"candidate/search", HttpMethod.POST, authToken, null, null, Optional.of(headerInformation));
        log.info("Completed execution of verifyNodes method in {} ms", System.currentTimeMillis() - startTime);
    }

    public String importData(MultipartFile masterDataFile, Long companyId, String fileType, String authToken) throws Exception{
        log.info("Inside importData method");
        Long startTime = System.currentTimeMillis();
        Map<String, Object> headerInformation = LoggedInUserInfoUtil.getLoggedInUserInformation();
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
        ObjectMapper mapper = new ObjectMapper();
        ImportDataResponseBean importDataResponseBean = mapper.readValue(response.getResponseBody(), ImportDataResponseBean.class);
        MasterDataBean.getInstance().setVerifiedSkills(importDataResponseBean.getSkillSet());
        log.info("Completed execution of importData method in {} ms", System.currentTimeMillis() - startTime);
        return response.getResponseBody();
    }

    @Override
    public Set<String> getVerifiedSkillFromSearchEngine() {
        Long startTime = System.currentTimeMillis();
        Set<String> skillSet = new HashSet<>();
        Map<String, Object> headerInformation = LoggedInUserInfoUtil.getLoggedInUserInformation();
        try {
            String responseBody = RestClient.getInstance().consumeRestApi(null, searchEngineBaseUrl + "data/getSkillNameList", HttpMethod.GET, null, null, null, Optional.of(headerInformation)).getResponseBody();
            ObjectMapper mapper = new ObjectMapper();
            skillSet = mapper.readValue(responseBody, Set.class);
        } catch (Exception e) {
            log.error("Error while getting verified skill data from search engine");
            Util.getStackTrace(e);
        }
        log.info("Completed execution of get verified skills from search engine method in {} ms", System.currentTimeMillis() - startTime);
        return skillSet;
    }

}

