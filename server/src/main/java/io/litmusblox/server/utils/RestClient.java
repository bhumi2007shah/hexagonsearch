/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import io.litmusblox.server.constant.IConstant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Util class to make REST api calls to 3rd party apis
 *
 * @author : Shital Raval
 * Date : 21/8/19
 * Time : 12:58 PM
 * Class Name : RestClient
 * Project Name : server
 */
@Log4j2
public class RestClient {

    //singleton instance
    private static RestClient restObj = null;

    @Value("${restConnectionTimeout}")
    private int connectionTimeout;

    @Value("${restReadTimeout}")
    private int readTimeout;

    //private constructor
    private RestClient() {
    }

    //get instance
    public static RestClient getInstance() {
        if (null == restObj)
            restObj = new RestClient();
        return restObj;
    }

    /**
     * Method that connects to the server as mentioned in the url and performs GET/POST operation
     *
     * @param requestObj the payload to send in the request in json format
     * @param apiUrl the url to connect to
     * @param requestType GET / POST / PUT
     * @param authToken authorization information
     * @return response
     * @throws Exception
     */
    public RestClientResponseBean consumeRestApi(String requestObj, String apiUrl, HttpMethod requestType, String authToken) throws Exception {
        return consumeRestApi(requestObj, apiUrl, requestType, authToken, null, null);
    }

    /**
     Method that connects to the server as mentioned in the url and performs GET/POST operation
     *
     * @param requestObj the payload to send in the request in json format
     * @param apiUrl the url to connect to
     * @param requestType GET / POST / PUT
     * @param authToken authorization information
     * @param queryParameters Map of query parameters if any
     * @param customTimeout use this parameter when the rest client's connection should wait for a longer duration than the default value
     * @return response
     * @throws Exception
     */
    public RestClientResponseBean consumeRestApi(String requestObj, String apiUrl, HttpMethod requestType, String authToken, Optional<Map> queryParameters, Optional<Integer> customTimeout) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        int connectionTimeoutValue = 0;
        if(null != customTimeout && customTimeout.isPresent()){
            requestFactory.setConnectTimeout(customTimeout.get().intValue());
            connectionTimeoutValue = customTimeout.get().intValue();
        }
        else{
            requestFactory.setConnectTimeout(IConstant.REST_CONNECTION_TIME_OUT);
            connectionTimeoutValue = IConstant.REST_CONNECTION_TIME_OUT;
        }

        requestFactory.setReadTimeout(IConstant.REST_READ_TIME_OUT);

        restTemplate.setRequestFactory(requestFactory);

        log.info("Rest client Connection timeout value : {}ms, and read time out value : {}ms.",connectionTimeoutValue, IConstant.REST_READ_TIME_OUT);
        //log.info("Request object sent: " + requestObj);

        HttpEntity<String> entity;
        if (null != requestObj)
            entity = new HttpEntity<String>(requestObj, getHttpHeader(authToken, true));
        else
            entity = new HttpEntity<String>(getHttpHeader(authToken, false));
        try {
            long startTime = System.currentTimeMillis();
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(apiUrl);

            if(null != queryParameters && queryParameters.isPresent()) {
                Map queryParametersToSet = queryParameters.get();
                queryParametersToSet.forEach((k,v) ->{
                    uriBuilder.queryParam(k.toString(),v.toString());
                });

            }

            ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), requestType, entity, String.class);
            log.info("Time taken to retrieve response from REST api: " + (System.currentTimeMillis() - startTime) + "ms.");
            log.info("Rest client response code: {}", response.getStatusCodeValue());
            return new RestClientResponseBean(response.getStatusCodeValue(),response.getBody());
        } catch(HttpStatusCodeException e ) {
            List<String> customHeader = e.getResponseHeaders().get("x-app-err-id");
            String svcErrorMessageID = "";
            if (customHeader != null) {
                svcErrorMessageID = customHeader.get(0);
            }
            log.error("Error response from REST call: " + svcErrorMessageID + " :: " + e.getResponseBodyAsString());
            return new RestClientResponseBean(e.getRawStatusCode(), e.getResponseBodyAsString());
            //throw e;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception while making a REST call: " + e.getMessage());
            throw e;
        }

    }

    /**
     * Method to generate HTTP Header to be used by the REST API
     * @param authToken authorization information
     * @return
     */
    private HttpHeaders getHttpHeader(String authToken, boolean setContentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",authToken);
        if(setContentType) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        }
        return headers;
    }
}
