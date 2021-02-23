/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author : sameer
 * Date : 01/09/20
 * Time : 2:22 PM
 * Class Name : FileSanitization
 * Project Name : server
 */
@Component
@Log4j2
public class FileSanitization {

    private static String parserBase;
    private static String sanitizeUrl;

    @Value("${parserBaseUrl}")
    public void setParserBase(String parserBase){
        FileSanitization.parserBase = parserBase;
    }

    @Value("${pythonFilSanitizationUrl}")
    public void setSanitizeUrl(String sanitizeUrl){
        FileSanitization.sanitizeUrl = sanitizeUrl;
    }

    public static String sanitizePdf(MultipartFile multipartFile) {

        log.info("Inside file sanitization method for file {}", multipartFile.getOriginalFilename());

        String responseByteArray = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileContent = null;
        try {
            log.info("extracting byte array resource from file {}", multipartFile.getOriginalFilename());
            fileContent = new ByteArrayResource(multipartFile.getBytes()){
                @Override
                public String getFilename() {
                    return multipartFile.getOriginalFilename();
                }

                @Override
                public long contentLength() {
                    return multipartFile.getSize();
                }
            };
        }
        catch (Exception e){
            log.info(e.getCause());
        }
        if(null!=fileContent) {
            log.info("Creating request object and calling python rest api to sanitize byte array of file {}", multipartFile.getOriginalFilename());
            Map headerInformation = LoggedInUserInfoUtil.getLoggedInUserInformation();
            headers.add("userId", headerInformation.get("userId").toString());
            headers.add("userEmail", headerInformation.get("userEmail").toString());
            headers.add("userCompanyId", headerInformation.get("userCompanyId").toString());
            body.add("file", fileContent);

            HttpEntity<MultiValueMap<String, Object>> requestEntity
                    = new HttpEntity<>(body, headers);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            RestTemplate restTemplate = new RestTemplate();
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                        parserBase+sanitizeUrl,
                        requestEntity,
                        String.class
                        );

                if(responseEntity.getStatusCode()==HttpStatus.OK && responseEntity.hasBody()){
                    log.info("Received sanitized file content for file {} from python api", multipartFile.getOriginalFilename());
                    responseByteArray = responseEntity.getBody();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responseByteArray;
    }
}
