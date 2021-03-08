/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.CandidateEmailHistoryRepository;
import io.litmusblox.server.repository.CandidateMobileHistoryRepository;
import io.litmusblox.server.repository.CompanyRepository;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.ISearchEngineService;
import io.litmusblox.server.service.ImportDataResponseBean;
import io.litmusblox.server.service.JobAnalytics.CandidateSearchBean;
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

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    CandidateEmailHistoryRepository candidateEmailHistoryRepository ;
    @Resource
    CandidateMobileHistoryRepository candidateMobileHistoryRepository ;
    @Resource
    CompanyRepository companyRepository;

    public String candidateSearch(String jsonData, String authToken) throws Exception{
        log.info("Inside candidateSearch method");
        Long startTime = System.currentTimeMillis();
        Map<String, Object> headerInformation = LoggedInUserInfoUtil.getLoggedInUserInformation();
        String responseData = new String();

        if(jsonData == null)
            throw new ValidationException("Invalid request", HttpStatus.BAD_REQUEST);

        ObjectMapper objectMapper = new ObjectMapper();
        CandidateSearchBean candidateSearchBean = objectMapper.readValue(jsonData, CandidateSearchBean.class);



        if (candidateSearchBean.getEmail()!=null)
        {
            String email=candidateSearchBean.getEmail();
            CandidateEmailHistory candidateEmailHistory= candidateEmailHistoryRepository.findByEmail(email);

            if (candidateEmailHistory!=null)
            {
                 responseData=SearchCandidatebyEmailorMobile(candidateEmailHistory.getCandidate());
                log.info("Completed execution of Candidate search method by email in {} ms", System.currentTimeMillis() - startTime);

            }

        }
        else if (candidateSearchBean.getMobile()!=null)
        {
            String mobile=candidateSearchBean.getMobile();
            CandidateMobileHistory candidateMobileHistory=candidateMobileHistoryRepository.findByMobileAndCountryCode(mobile,"+91");
            if (candidateMobileHistory!=null)
            {
                responseData=SearchCandidatebyEmailorMobile(candidateMobileHistory.getCandidate());

                log.info("Completed execution of Candidate search method by mobile in {} ms", System.currentTimeMillis() - startTime);
            }

        }
        else {
            responseData = RestClient.getInstance().consumeRestApi(jsonData, searchEngineBaseUrl + "candidate/search", HttpMethod.POST, authToken, null, null, Optional.of(headerInformation)).getResponseBody();
            log.info("Completed execution of Candidate search method in {} ms", System.currentTimeMillis() - startTime);
        }


            return responseData;

    }


    private String SearchCandidatebyEmailorMobile(Candidate candidate) throws JsonProcessingException {
        List<CandidateSearchBean> candidateSearchBeanList = new ArrayList<>();
        CandidateSearchBean candidateSearchBean=new CandidateSearchBean();
        List<CandidateEmailHistory> candidateEmailHistoryList=candidateEmailHistoryRepository.findByCandidateIdOrderByIdDesc(candidate.getId());
        List<CandidateMobileHistory> candidateMobileHistoryList=candidateMobileHistoryRepository.findByCandidateIdOrderByIdDesc(candidate.getId());
        candidateSearchBean.setMobile(candidateMobileHistoryList.get(0).getMobile());

        candidateSearchBean.setCandidateName(candidate.getFirstName()+" "+candidate.getLastName());
        candidateSearchBean.setCandidateId(candidate.getId());
        candidateSearchBean.setEmail(candidateEmailHistoryList.get(0).getEmail());
        if(null!=candidate.getCandidateDetails())
        {
            String location = candidate.getCandidateDetails().getLocation();
            Set<String> LocationSet = new HashSet<String>();
            LocationSet.add(location);
            candidateSearchBean.setLocations(LocationSet);
            candidateSearchBean.setExperienceFromDb(candidate.getCandidateDetails().getRelevantExperience());
            candidateSearchBean.setMaxExperience(candidate.getCandidateDetails().getTotalExperience());
        }
        Set<String> Skillset = candidate.getCandidateSkillDetails().stream().map(CandidateSkillDetails::getSkill).collect(Collectors.toSet());
        candidateSearchBean.setSkills(Skillset);

        Set<String> Qualifications = candidate.getCandidateEducationDetails().stream().map(CandidateEducationDetails::getDegree).collect(Collectors.toSet());
        candidateSearchBean.setQualifications(Qualifications);
        if (null!= candidate.getCandidateCompanyDetails() && !candidate.getCandidateCompanyDetails().isEmpty())
        {
            if (null != candidate.getCandidateCompanyDetails().get(0).getNoticePeriodInDb())
            {
                String filtered_notice_period = candidate.getCandidateCompanyDetails().get(0).getNoticePeriodInDb().getValue().replaceAll("[A-Za-z]", "").trim();
                if (null != filtered_notice_period)
                    candidateSearchBean.setNoticePeriod(Long.valueOf(filtered_notice_period));
            }

        }
       Company company= companyRepository.findCompanyByCandidateId(candidate.getId());
        if (null!=company)
        {
            candidateSearchBean.setCompanyId(company.getId());
            candidateSearchBean.setCompanyName(company.getCompanyName());
        }
        candidateSearchBeanList.add(candidateSearchBean);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String responseData= objectMapper.writeValueAsString(candidateSearchBeanList);
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

