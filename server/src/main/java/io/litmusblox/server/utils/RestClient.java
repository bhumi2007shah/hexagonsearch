/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import io.litmusblox.server.service.MasterDataBean;
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
        return consumeRestApi(requestObj, apiUrl, requestType, authToken, null, null, null);
    }

    /**
     Method that connects to the server as mentioned in the url and performs GET/POST operation
     *
     * @param requestObj the payload to send in the request in json format
     * @param apiUrl the url to connect to
     * @param requestType GET / POST / PUT
     * @param authToken authorization information
     * @param queryParameters Map of query parameters if any
     * @param customReadTimeout use this parameter when the time-out applied from the moment you have established a connection
     * @param headerInformation Map of information to be set in header
     * @return response
     * @throws Exception
     */
    public RestClientResponseBean consumeRestApi(String requestObj, String apiUrl, HttpMethod requestType, String authToken, Optional<Map> queryParameters, Optional<Integer> customReadTimeout, Optional<Map> headerInformation) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        if(null != customReadTimeout && customReadTimeout.isPresent())
            requestFactory.setReadTimeout(customReadTimeout.get().intValue());
        else
            requestFactory.setReadTimeout(MasterDataBean.getInstance().getRestReadTimeout());

        requestFactory.setConnectTimeout(MasterDataBean.getInstance().getRestConnectionTimeout());

        restTemplate.setRequestFactory(requestFactory);

        log.info("Rest client Connection timeout value : {}ms, and read time out value : {}ms.",MasterDataBean.getInstance().getRestConnectionTimeout(),
                ((null != customReadTimeout && customReadTimeout.isPresent())?customReadTimeout.get():MasterDataBean.getInstance().getRestReadTimeout()));
        //log.info("Request object sent: " + requestObj);

        HttpEntity<String> entity;
        if (null != requestObj)
            entity = new HttpEntity<String>(requestObj, getHttpHeader(authToken, true, headerInformation));
        else
            entity = new HttpEntity<String>(getHttpHeader(authToken, false, headerInformation));
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
    private HttpHeaders getHttpHeader(String authToken, boolean setContentType, Optional<Map> headerInformation) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",authToken);

        if(null != headerInformation && headerInformation.isPresent()){
            Map headerInformationToSet = headerInformation.get();
            headerInformationToSet.forEach((k, v) -> {
                headers.set(k.toString(), v.toString());
            });
        }

        if(setContentType) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        }
        return headers;
    }
}
