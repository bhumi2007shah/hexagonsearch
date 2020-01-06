/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.CvParsingDetails;
import io.litmusblox.server.model.CvRating;
import io.litmusblox.server.model.CvRatingSkillKeywordDetails;
import io.litmusblox.server.repository.CvParsingDetailsRepository;
import io.litmusblox.server.repository.CvRatingRepository;
import io.litmusblox.server.repository.CvRatingSkillKeywordDetailsRepository;
import io.litmusblox.server.repository.JobKeySkillsRepository;
import io.litmusblox.server.service.impl.MlCvRatingRequestBean;
import io.litmusblox.server.uploadProcessor.IProcessUploadedCV;
import io.litmusblox.server.uploadProcessor.RChilliCvProcessor;
import io.litmusblox.server.utils.RestClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Class that has the methods to process the files and that will be triggered by scheduler
 *
 * @author : Shital Raval
 * Date : 21/8/19
 * Time : 1:09 PM
 * Class Name : ProcessUploadedCv
 * Project Name : server
 */
@Log4j2
@Service
public class ProcessUploadedCv implements IProcessUploadedCV {

    @Autowired
    RChilliCvProcessor rChilliCvProcessor;

    @Autowired
    Environment environment;

    @Value("${mlCvRatingUrl}")
    private String mlCvRatingUrl;

    @Resource
    CvParsingDetailsRepository cvParsingDetailsRepository;

    @Resource
    JobKeySkillsRepository jobKeySkillsRepository;

    @Resource
    CvRatingRepository cvRatingRepository;

    @Resource
    CvRatingSkillKeywordDetailsRepository cvRatingSkillKeywordDetailsRepository;

    /**
     * Method that will be called by scheduler
     *
     * @throws Exception
     */
    @Override
    public void processCv() {
        try{
            Stream<Path> filePathStream= Files.walk(Paths.get(environment.getProperty(IConstant.TEMP_REPO_LOCATION)));
            filePathStream.forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        log.info("Temp folder Cv path : "+filePath.getFileName());
                        rChilliCvProcessor.processFile(filePath.toString(), null);
                        log.info("Completed processing " + filePath.toString());
                    }
                });
        } catch (Exception e) {
            log.info("Error while processing temp location files : "+e.getMessage());
        }
    }

    /**
     * Method that will be called by scheduler
     * All eligible records of CV will be run against CV rating api and rated
     */
   // @Transactional
    public void rateCv() {
        List<CvParsingDetails> cvToRateList = cvParsingDetailsRepository.findCvRatingRecordsToProcess();
        log.info("Found " + cvToRateList.size() + " records for CV rating process");

        cvToRateList.stream().forEach(cvToRate -> {
            try {
                boolean processingError = false;
                long cvRatingApiProcessingTime = -1;
                if(null != cvToRate.getJobCandidateMappingId()) {
                    //call rest api with the text part of cv
                    log.info("Processing CV for job id: " + cvToRate.getJobCandidateMappingId().getJob().getId() + " and candidate id: " + cvToRate.getJobCandidateMappingId().getCandidate().getId());
                    List<String> jdKeySkills = jobKeySkillsRepository.findSkillNameByJobId(cvToRate.getJobCandidateMappingId().getJob().getId());
                    if (jdKeySkills.size() == 0)
                        log.error("Found no key skills for " + cvToRate.getJobCandidateMappingId().getJob().getId());
                    else {
                        try {
                            cvRatingApiProcessingTime = callCvRatingApi(new MlCvRatingRequestBean(jdKeySkills, cvToRate.getParsingResponseText(), cvToRate.getJobCandidateMappingId().getJob().getFunction().getValue()), cvToRate.getJobCandidateMappingId().getId());
                        } catch (Exception e) {
                            log.info("Error while performing CV rating operation " + e.getMessage());
                            processingError = true;
                        }
                    }
                    if (!processingError) {
                        cvToRate.setCvRatingApiFlag(true);
                        cvToRate.setCvRatingApiResponseTime(cvRatingApiProcessingTime);
                        cvParsingDetailsRepository.save(cvToRate);
                    }
                }
                else {
                    log.error("JCM Id not set for CV Parsing details record with id: " + cvToRate.getId());
                }
            }catch(Exception ex) {
                log.error("Error processing record to rate cv with jcmId: " + cvToRate.getJobCandidateMappingId().getId() + "\n" + ex.getMessage());
            }
        });
    }

    /**
     * Method to convert cv file to cv text
     * In cv_parsing_detail if parsing_response_text is null then convert cv to text and save
     */
    @Transactional
    public void CvToCvText() {
        log.info("inside CvToCvText");
        List<CvParsingDetails> cvParsingDetailsList = new ArrayList<>();
        List<CvParsingDetails> cvParsingDetails = cvParsingDetailsRepository.getDataForConvertCvToCvText();
        if(null != cvParsingDetails && cvParsingDetails.size()>0){
            cvParsingDetails.forEach(cvParsingDetailsFromDb->{
                String cvText = null;
                Map<String, String> queryParameters = new HashMap<>();
                queryParameters.put("file", environment.getProperty("cvStorageUrl")+cvParsingDetailsFromDb.getJobCandidateMappingId().getJob().getId()+"/"+cvParsingDetailsFromDb.getCandidateId()+cvParsingDetailsFromDb.getJobCandidateMappingId().getCvFileType());
                log.info("CvFile path : {}",queryParameters.get("file"));
                try {
                    long apiCallStartTime = System.currentTimeMillis();
                    cvText = RestClient.getInstance().consumeRestApi(null, environment.getProperty("pythonCvParserUrl"), HttpMethod.GET, null, Optional.of(queryParameters), null);
                    log.info("Time taken to convert cv to text : {}ms. For cvParsingDetailsId : {}",(System.currentTimeMillis() - apiCallStartTime), cvParsingDetailsFromDb.getId());
                    if(null != cvText){
                        cvParsingDetailsFromDb.setParsingResponseText(cvText);
                        cvParsingDetailsList.add(cvParsingDetailsFromDb);
                    }
                } catch (Exception e) {
                    log.error("Error while convert cv to text for cvParsingDetailsId  : {}, error message : {}",cvParsingDetailsFromDb.getId(), e.getMessage());
                }
            });
            if(cvParsingDetailsList.size()>0)
                cvParsingDetailsRepository.saveAll(cvParsingDetailsList);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private long callCvRatingApi(MlCvRatingRequestBean requestBean, Long jcmId) throws Exception {
        CvRating cvRatingFromDb = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        long apiCallStartTime = System.currentTimeMillis();
        String mlResponse = RestClient.getInstance().consumeRestApi(objectMapper.writeValueAsString(requestBean), mlCvRatingUrl, HttpMethod.POST, null);
        log.info("Response received from CV Rating Api: " + mlResponse);
        long apiCallEndTime = System.currentTimeMillis();

        long startTime = System.currentTimeMillis();
        CvRatingResponseWrapper responseBean = objectMapper.readValue(mlResponse, CvRatingResponseWrapper.class);

        cvRatingFromDb = cvRatingRepository.findByJobCandidateMappingId(jcmId);
        if(null == cvRatingFromDb){
            cvRatingFromDb = cvRatingRepository.save(new CvRating(jcmId, responseBean.getCvRatingResponse().getOverallRating()));
            cvRatingSkillKeywordDetailsRepository.saveAll(convertToCvRatingSkillKeywordDetails(responseBean.getCvRatingResponse().getKeywords(), cvRatingFromDb.getId()));
        }else{
            log.info("Update cv_rating id : "+cvRatingFromDb.getId()+", For jcm id : "+jcmId);
            log.info("Old cv_rating : "+cvRatingFromDb.getOverallRating()+", New Rating : "+responseBean.getCvRatingResponse().getOverallRating());
            cvRatingFromDb.setOverallRating(responseBean.getCvRatingResponse().getOverallRating());
            cvRatingRepository.save(cvRatingFromDb);
        }
        log.info("Time taken to process ml cv rating data data: " + (System.currentTimeMillis() - startTime) + "ms.");
        return (apiCallEndTime - apiCallStartTime);
    }

    private List<CvRatingSkillKeywordDetails> convertToCvRatingSkillKeywordDetails(List<Keyword> keywords, Long cvRatingId) {
        List<CvRatingSkillKeywordDetails> targetList = new ArrayList<>(keywords.size());
        keywords.stream().forEach(keyword ->
            targetList.add(new CvRatingSkillKeywordDetails(cvRatingId,String.join(",",keyword.getSupportingKeywords().stream().map(supportingKeyword -> supportingKeyword.getName()).toArray(String[]::new)), keyword.getName(), keyword.getRating(), keyword.getOccurrence()))
        );
        return targetList;
    }

}