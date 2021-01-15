/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.JobCandidateMappingRepository;
import io.litmusblox.server.repository.JobRepository;
import io.litmusblox.server.service.IAsyncServicesWrapper;
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.MasterDataBean;
import io.litmusblox.server.utils.StoreFileUtil;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * @author : Shital Raval
 * Date : 3/2/20
 * Time : 2:06 PM
 * Class Name : AsyncServicesWrapper
 * Project Name : server
 */
@Log4j2
@Service
public class AsyncServicesWrapper implements IAsyncServicesWrapper {

    @Autowired
    IJobCandidateMappingService jobCandidateMappingService;

    @Autowired
    Environment environment;

    @Resource
    JobRepository jobRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    /**
     * Service method to add candidates from a file in one of the supported formats
     *
     * @param multipartFile the file with candidate information
     * @param jobId         the job for which the candidates have to be added
     * @param fileFormat    the format of file, for e.g. Naukri, LB format
     * @return the status of upload operation
     * @throws Exception
     */
    @Override
    public void uploadCandidatesFromFile(MultipartFile multipartFile, Long jobId, String fileFormat) throws Exception {
        User loggedInUser = getUser();

        //validate the file source is supported by application
        if(!Arrays.asList(IConstant.UPLOAD_FORMATS_SUPPORTED.values()).contains(IConstant.UPLOAD_FORMATS_SUPPORTED.valueOf(fileFormat))) {
            log.error(IErrorMessages.UNSUPPORTED_FILE_SOURCE + fileFormat);
            StringBuffer info = new StringBuffer("Unsupported file source : ").append(multipartFile.getName());
            Map<String, String> breadCrumb = new HashMap<>();
            breadCrumb.put("Job Id", jobId.toString());
            breadCrumb.put("File Name", multipartFile.getName());
            breadCrumb.put("detail", info.toString());
            throw new WebException(IErrorMessages.UNSUPPORTED_FILE_SOURCE + fileFormat, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }

        //verify that the job is live before processing candidates
        Job job = getJob(jobId);
        if(null == job || !IConstant.JobStatus.PUBLISHED.getValue().equals(job.getStatus())) {
            StringBuffer info = new StringBuffer("Selected job is not live ").append("JobId-").append(jobId);
            Map<String, String> breadCrumb = new HashMap<>();
            breadCrumb.put("Job Id", jobId.toString());
            breadCrumb.put("detail", info.toString());
            throw new WebException(IErrorMessages.JOB_NOT_LIVE, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }

        //validate that the file has an extension that is supported by the application
        Util.validateUploadFileType(multipartFile.getOriginalFilename());

        Date createdOn=Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        int candidatesProcessed = getUploadCount(createdOn, loggedInUser);

        if (candidatesProcessed >= MasterDataBean.getInstance().getConfigSettings().getDailyCandidateUploadPerUserLimit()) {
            log.error(IErrorMessages.MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED + " :: user id : " + loggedInUser.getId() + " : not saving file " + multipartFile);
            throw new WebException(IErrorMessages.MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //Save file
        String fileName = StoreFileUtil.storeFile(multipartFile, jobId, environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.Candidates.toString(),null,null);
        log.info("User " + loggedInUser.getDisplayName() + " uploaded " + fileName);

        log.info("Calling async upload service from thread {}", Thread.currentThread().getName());
        jobCandidateMappingService.uploadCandidatesFromFile(fileName, jobId, fileFormat, getUser(), candidatesProcessed, multipartFile.getOriginalFilename());
    }

    @Transactional(readOnly = true)
    Job getJob(long jobId) {
        return jobRepository.findById(jobId).get();
    }

    @Transactional(readOnly = true)
    User getUser() {
        return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    int getUploadCount(Date createdOn, User loggedInUser) {
        return jobCandidateMappingRepository.getUploadedCandidateCount(createdOn, loggedInUser);
    }

    /**
     * Service method to invite candidates to fill chatbot for a job
     *
     * @param jcmList list of jcm ids for chatbot invitation
     * @throws Exception
     */
    @Override
    public void inviteCandidates(List<Long> jcmList) throws Exception {
        if(jcmList == null || jcmList.size() == 0)
            throw new WebException("Select candidates to invite",HttpStatus.UNPROCESSABLE_ENTITY);

        //make sure all candidates are at the same stage
        if(!areCandidatesInSameStage(jcmList))
            throw new WebException("Select candidates that are all in Source stage", HttpStatus.UNPROCESSABLE_ENTITY);

        log.info("Callling async invite candidate service from thread {}", Thread.currentThread().getName());
        jobCandidateMappingService.inviteCandidates(jcmList, getUser());
    }

    private boolean areCandidatesInSameStage(List<Long> jcmList) throws Exception{
        if(jobCandidateMappingRepository.countDistinctStageForJcmList(jcmList) != 1)
            return false;
        return true;
    }
}