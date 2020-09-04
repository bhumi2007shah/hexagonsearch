/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.ICandidateService;
import io.litmusblox.server.service.MasterDataBean;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.uploadProcessor.IUploadDataProcessService;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author : Sumit
 * Date : 17/7/19
 * Time : 2:49 PM
 * Class Name : UploadDataProcessService
 * Project Name : server
 */
@Service
@Log4j2
public class UploadDataProcessService implements IUploadDataProcessService {

    @Resource
    JobRepository jobRepository;

    @Resource
    CandidateRepository candidateRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    JcmCommunicationDetailsRepository jcmCommunicationDetailsRepository;

    @Resource
    JcmHistoryRepository jcmHistoryRepository;

    @Resource
    StageStepMasterRepository stageStepMasterRepository;

    @Resource
    CandidateReferralDetailRepository candidateReferralDetailRepository;

    @Autowired
    ICandidateService candidateService;

    @Autowired
    JcmCandidateSourceHistoryRepository jcmCandidateSourceHistoryRepository;

    //@Transactional(propagation = Propagation.REQUIRED)
    public void processData(List<Candidate> candidateList, UploadResponseBean uploadResponseBean, int candidateProcessed, Long jobId, boolean ignoreMobile, Optional<User> createdBy){
        log.info("inside processData");

        int recordsProcessed = 0;
        int successCount = 0;
        int failureCount = uploadResponseBean.getFailureCount();

        User loggedInUser = createdBy.isPresent()?createdBy.get():(User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Job job=jobRepository.getOne(jobId);

        for (Candidate candidate:candidateList) {

            long startTime = System.currentTimeMillis();

            if(recordsProcessed >= MasterDataBean.getInstance().getConfigSettings().getCandidatesPerFileLimit()) {
                log.error(IErrorMessages.MAX_CANDIDATE_PER_FILE_EXCEEDED + " : user id : " +  loggedInUser.getId());
                candidate.setUploadErrorMessage(IErrorMessages.MAX_CANDIDATE_PER_FILE_EXCEEDED + ". Max number of " +
                        "candidates per file is "+MasterDataBean.getInstance().getConfigSettings().getCandidatesPerFileLimit()+". All candidates from this candidate onwards have not been processed");
                uploadResponseBean.getFailedCandidates().add(candidate);
                failureCount++;
                break;
            }
            //check for daily limit per user
            if ((recordsProcessed + candidateProcessed) >= MasterDataBean.getInstance().getConfigSettings().getDailyCandidateUploadPerUserLimit()) {
                log.error(IErrorMessages.MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED  + " : user id : " +  loggedInUser.getId());
                candidate.setUploadErrorMessage(IErrorMessages.MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED);
                uploadResponseBean.getFailedCandidates().add(candidate);
                failureCount++;
                break;
            }


            try {
                recordsProcessed++;
                validateDataAndSaveJcmAndJcmCommModel(uploadResponseBean,candidate, loggedInUser, ignoreMobile, job);
                successCount++;
            }catch(ValidationException ve) {
                log.error("Error while processing candidate : " + candidate.getEmail() + " : " + ve.getErrorMessage(), HttpStatus.BAD_REQUEST);
                candidate.setUploadErrorMessage(ve.getErrorMessage());
                uploadResponseBean.getFailedCandidates().add(candidate);
                failureCount++;
            } catch(Exception e) {
                //e.printStackTrace();
                log.error("Error while processing candidate : " + candidate.getEmail() + " : " + e.getMessage(), HttpStatus.BAD_REQUEST);
                if(null == candidate.getUploadErrorMessage())
                    candidate.setUploadErrorMessage(IErrorMessages.INTERNAL_SERVER_ERROR);
                uploadResponseBean.getFailedCandidates().add(candidate);
                failureCount++;
            }

            log.info("Time taken to upload candidate is {}ms ",System.currentTimeMillis()-startTime);
        }

        uploadResponseBean.setFailureCount(failureCount);
        uploadResponseBean.setSuccessCount(successCount);

        if(uploadResponseBean.getFailureCount() == 0)
            uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Success.name());
        else if(uploadResponseBean.getSuccessCount() == 0)
            uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        else
            uploadResponseBean.setStatus(IConstant.UPLOAD_STATUS.Partial_Success.name());

        uploadResponseBean.setCandidatesProcessedCount(candidateProcessed + successCount);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Candidate validateDataAndSaveJcmAndJcmCommModel(UploadResponseBean uploadResponseBean, Candidate candidate, User loggedInUser, Boolean ignoreMobile, Job job) throws Exception {

        log.info("Inside validateDataAndSaveJcmAndJcmCommModel method");
        if (Util.isNotNull(candidate.getFirstName())) {
            //validate candidate used in multiple places so create util method
             candidate.setFirstName(Util.validateCandidateName(candidate.getFirstName()));
        }else
            candidate.setUploadErrorMessage(IErrorMessages.NAME_NULL_OR_BLANK);

        if (Util.isNotNull(candidate.getLastName())) {
            candidate.setLastName(Util.validateCandidateName(candidate.getLastName()));
        }

        if(Util.isNull(candidate.getEmail()))
            candidate.setEmail("notavailable" + new Date().getTime() + IConstant.NOT_AVAILABLE_EMAIL);

        String [] email = candidate.getEmail().split(",");
        Set<String> emailSet = new HashSet<>();
        String invalidEmailId = null;

        for(int i=0; i< email.length; i++) {
            if (!Util.isValidateEmail(email[i], Optional.of(candidate))) {
                email[i] = email[i].replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_EMAIL, "");
                log.error("Special characters found, cleaning Email \"" + email[i] + "\" to " + email[i]);
                if (!Util.isValidateEmail(email[i], Optional.of(candidate))) {
                    if(invalidEmailId == null)
                        invalidEmailId = email[i];
                    continue;
                }
            }
            emailSet.add(email[i]);
        }

        if(emailSet.size() == 0) {
            candidate.setEmail(invalidEmailId);
            throw new ValidationException(IErrorMessages.INVALID_EMAIL + " - " + candidate.getEmail(), HttpStatus.BAD_REQUEST);
        }

        candidate.setEmail(emailSet.stream().findFirst().get());
        StringBuffer msg = new  StringBuffer(candidate.getFirstName()).append(" ").append(candidate.getLastName()).append(" (").append(candidate.getEmail());

        Set<String> mobileSet = new HashSet<>();
        String [] mobile;
        String inValidMobileNumber = null;
        if(Util.isNotNull(candidate.getMobile())) {
            mobile = candidate.getMobile().split(",");
            for (int i=0; i< mobile.length; i++) {
                mobile[i] = (Util.indianMobileConvertor(mobile[i], (null != candidate.getCountryCode()) ? candidate.getCountryCode() : job.getCompanyId().getCountryId().getCountryCode()));
                if (!Util.validateMobile(mobile[i], (null != candidate.getCountryCode()) ? candidate.getCountryCode() : job.getCompanyId().getCountryId().getCountryCode(), Optional.of(candidate))) {
                    mobile[i] = mobile[i].replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_MOBILE, "");
                    log.error("Special characters found, cleaning mobile number \"" + candidate.getMobile() + "\" to " + mobile[i]);
                    if (!Util.validateMobile(mobile[i], (null != candidate.getCountryCode()) ? candidate.getCountryCode() : job.getCompanyId().getCountryId().getCountryCode(), Optional.of(candidate))){
                        if(null == inValidMobileNumber)
                            inValidMobileNumber = mobile[i];
                        continue;
                    }
                }
                mobileSet.add(mobile[i]);
            }

            if(mobileSet.size()==0) {
                candidate.setMobile(inValidMobileNumber);
                throw new ValidationException(IErrorMessages.MOBILE_INVALID_DATA + " - " + inValidMobileNumber, HttpStatus.BAD_REQUEST);
            }
            candidate.setMobile(mobileSet.stream().findFirst().get());
            msg.append(",").append(candidate.getMobile()).append(") ");

        }else {
            //mobile number of candidate is null
            //check if ignore mobile flag is set
            if(ignoreMobile) {
                if(null != candidate.getMobile() && candidate.getMobile().length()==0)
                    candidate.setMobile(null);

                log.info("Ignoring check for mobile number required for " + candidate.getEmail());
            }
            else {
                //ignore mobile flag is false, throw an exception
                throw new ValidationException(IErrorMessages.MOBILE_NULL_OR_BLANK + " - " + candidate.getMobile(), HttpStatus.BAD_REQUEST);
            }

        }
        log.info(msg);

        //create a candidate if no history found for email and mobile
        Candidate existingCandidate = candidateService.findByMobileOrEmail(emailSet,mobileSet,(Util.isNull(candidate.getCountryCode())?job.getCompanyId().getCountryId().getCountryCode():candidate.getCountryCode()), loggedInUser, Optional.ofNullable(candidate.getAlternateMobile()));
        if(null == existingCandidate && candidate.getCandidateSource().equalsIgnoreCase(IConstant.CandidateSource.LinkedIn.getValue())){
            existingCandidate = candidateService.findByProfileTypeAndUniqueId(candidate.getCandidateOnlineProfiles());
        }
        Candidate candidateObjToUse = existingCandidate;
        if(null == existingCandidate) {
            candidate.setCreatedOn(new Date());
            candidate.setCreatedBy(loggedInUser);
            if(Util.isNull(candidate.getCountryCode()))
                candidate.setCountryCode(job.getCompanyId().getCountryId().getCountryCode());
            candidateObjToUse = candidateService.createCandidate(candidate.getFirstName(), candidate.getLastName(), emailSet, mobileSet, candidate.getCountryCode(), loggedInUser, Optional.ofNullable(candidate.getAlternateMobile()));
            candidate.setId(candidateObjToUse.getId());
        }
        else {
            log.info("Found existing candidate: " + existingCandidate.getId());
            candidate.setId(existingCandidate.getId());
            if(Util.isNotNull(existingCandidate.getEmail()))
                candidate.setEmail(existingCandidate.getEmail());
        }

        log.info(msg);

        //find duplicate candidate for job
        JobCandidateMapping jobCandidateMapping = jobCandidateMappingRepository.findByJobAndCandidate(job, candidateObjToUse);

        if(null!=jobCandidateMapping){
            //saving candidate source history even if candidate is duplicate for this job
            jcmCandidateSourceHistoryRepository.save(new JcmCandidateSourceHistory(jobCandidateMapping.getId(), candidate.getCandidateSource(), loggedInUser));

            log.error(IErrorMessages.DUPLICATE_CANDIDATE + " : " + candidateObjToUse.getId() + candidate.getEmail() + " : " + candidate.getMobile());
            candidate.setUploadErrorMessage(IErrorMessages.DUPLICATE_CANDIDATE);
            candidate.setId(candidateObjToUse.getId());

            throw new ValidationException(IErrorMessages.DUPLICATE_CANDIDATE + " - " +"JobId: " + job.getId(), HttpStatus.BAD_REQUEST);
        }else{
            //Create new entry for JobCandidateMapping
            candidateObjToUse.setCountryCode(Util.isNull(candidate.getCountryCode())?job.getCompanyId().getCountryId().getCountryCode():candidate.getCountryCode());
            candidateObjToUse.setEmail(candidate.getEmail());
            candidateObjToUse.setMobile(candidate.getMobile());
            candidateObjToUse.setCandidateSource(candidate.getCandidateSource());

            StageStepMaster stageStepForSource = stageStepMasterRepository.findByStage(IConstant.Stage.Source.getValue());

            JobCandidateMapping savedObj = jobCandidateMappingRepository.save(new JobCandidateMapping(job,candidateObjToUse,stageStepForSource, candidate.getCandidateSource(), IConstant.AUTOSOURCED_TYPE.contains(candidateObjToUse.getCandidateSource()), new Date(),loggedInUser, UUID.randomUUID(), candidate.getFirstName(), candidate.getLastName(), (null != candidate.getCandidateDetails())?candidate.getCandidateDetails().getCvFileType():null));

            if(savedObj.getCandidateSource().equals(IConstant.CandidateSource.EmployeeReferral.getValue())){
                candidateReferralDetailRepository.save(new CandidateReferralDetail(savedObj, candidate.getEmployeeReferrer(), candidate.getEmployeeReferrer().getReferrerRelation(), candidate.getEmployeeReferrer().getReferrerContactDuration()));
            }

            //string to store detail about jcmHistory
            msg.append("sourced for - ").append(job.getJobTitle()).append(" - ").append(job.getId());
            jcmHistoryRepository.save(new JcmHistory(savedObj, msg.toString(), new Date(), loggedInUser, savedObj.getStage(), true  ));
            savedObj.setTechResponseData(new CandidateTechResponseData(savedObj));
            jobCandidateMappingRepository.save(savedObj);
            //create an empty record in jcm Communication details table
            jcmCommunicationDetailsRepository.save(new JcmCommunicationDetails(savedObj.getId()));

            //saving candidate source history
            jcmCandidateSourceHistoryRepository.save(new JcmCandidateSourceHistory(savedObj.getId(), savedObj.getCandidateSource(), loggedInUser));

        }

        if(null!=uploadResponseBean){
            uploadResponseBean.getSuccessfulCandidates().add(candidateObjToUse);
        }
        return candidate;
    }


}
