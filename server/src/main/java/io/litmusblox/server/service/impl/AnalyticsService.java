/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.CompanyRepository;
import io.litmusblox.server.repository.CustomQueryExecutor;
import io.litmusblox.server.repository.JobCandidateMappingRepository;
import io.litmusblox.server.repository.JobRepository;
import io.litmusblox.server.service.AnalyticsResponseBean;
import io.litmusblox.server.service.IAnalyticsService;
import io.litmusblox.server.service.JobAnalytics.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service interface for all analytics related queries
 *
 * @author : Shital Raval
 * Date : 5/2/20
 * Time : 1:47 PM
 * Class Name : AnalyticsService
 * Project Name : server
 */
@Service
@Log4j2
public class AnalyticsService implements IAnalyticsService {

    @Resource
    CompanyRepository companyRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    JobRepository jobRepository;

    @Autowired
    CustomQueryExecutor customQueryExecutor;

    /**
     * Find analytics
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    @Override
    public List<AnalyticsResponseBean> analyticsByCompany(String startDate, String endDate) throws Exception {
        log.info("Received request to find analytics");
        long startTime = System.currentTimeMillis();
        List<Long> companyIds = new ArrayList<>();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (IConstant.UserRole.SUPER_ADMIN.toString().equals(loggedInUser.getRole()))
            companyIds = companyRepository.findAll().stream()
                    .map(Company::getId).collect(Collectors.toList());
        else
            companyIds.add(loggedInUser.getCompany().getId());
        List<AnalyticsResponseBean> responseBeans = customQueryExecutor.analyticsByCompany(startDate, endDate, StringUtils.join(companyIds,","));
        log.info("Completed request to find analytics in {} ms.", System.currentTimeMillis()-startTime);
        return responseBeans;
    }

    /**
     *
     * Service for fetching analytics for a job id.
     *
     * @param jobId jobId for which analytics will be generated
     * @param startDate optional parameter
     * @param endDate optional parameter
     * @return JobAnalyticsResponseBean
     * @throws Exception
     */
    @Override
    public JobAnalyticsResponseBean getJobAnalyticsData(Long jobId, Optional<Date> startDate, Optional<Date> endDate) throws Exception {
        // find a job from db using jobId
        Job job = jobRepository.getOne(jobId);

        // throw exception if job  not found in db
        if(null == job){
            log.error("JOb not found for id: {}", jobId);
            throw new WebException("No job found for id: "+jobId, HttpStatus.BAD_REQUEST);
        }

        JobAnalyticsResponseBean jobAnalyticsResponseBean = new JobAnalyticsResponseBean();

        //set candidate sourced count for a job in jobAnalyticsResponseBean
        jobAnalyticsResponseBean.setCandidateSourced(getCountByJobId(jobId, startDate, endDate));

        //set job created on in jobAnalyticsResponseBean
        jobAnalyticsResponseBean.setJobCreatedOn(job.getCreatedOn());

        // set candidate sources analytics i.e linkedin, naukri, individual etc for a job in jobAnalyticsResponseBean
        jobAnalyticsResponseBean.setCandidateSources(getCandidateSourceAnalytics(jobId, startDate, endDate));

        //set key skill strength analytics i.e: candidate count per score for a job in jobAnalyticsResponseBean
        jobAnalyticsResponseBean.setKeySkillsStrength(getKeySkillsStrengthAnalytics(jobId, startDate, endDate));

        //set Screening status analytics i.e: chatbot status for a job in jobAnalyticsResponseBean
        jobAnalyticsResponseBean.setScreeningStatus(getScreeningStatusAnalytics(jobId, startDate, endDate));

        //set submitted analytics i.e: profile shared status and hiring manager response for a job in jobAnalyticsResponseBean
        jobAnalyticsResponseBean.setSubmitted(getSubmittedAnalytics(jobId, startDate, endDate));

        //set interview analytics i.e: not scheduled, scheduled, rescheduled, show, no show for job in jonAnalyticsResponseBean
        jobAnalyticsResponseBean.setInterview(getInterviewAnalytics(jobId, startDate, endDate));

        return jobAnalyticsResponseBean;
    }

    /**
     * function to find sourced candidate count
     * @param jobId for which candidate count to be evaluated
     * @param startDate is date which should be less than or equal to jcm creation date
     * @param endDate is date which should be greater than or equal to jcm creation date
     * @return candidate count
     */
    private Long getCountByJobId(Long jobId, Optional<Date> startDate, Optional<Date> endDate){
        if(startDate.isPresent() && endDate.isPresent()){
            return jobCandidateMappingRepository.countByJobIdAndDate(jobId, startDate.get(), endDate.get());
        }
        else{
            return jobCandidateMappingRepository.countByJobId(jobId);
        }
    }

    /**
     * function to call sourcesAnalyticsByJob()
     * @param jobId for which analytics will be generated
     * @param startDate is date which should be less than or equal to jcm creation date
     * @param endDate is date which should be greater than or equal to jcm creation date
     * @return CandidateSourcesAnalyticsBean that contains count fo candidate for each source like naukri, linkedin, file, individual etc.
     */
    private CandidateSourceAnalyticsBean getCandidateSourceAnalytics(Long jobId, Optional<Date> startDate, Optional<Date> endDate){
        if(startDate.isPresent() && endDate.isPresent()){
            return customQueryExecutor.sourcesAnalyticsByJob(jobId, startDate.get(), endDate.get());
        }
        else{
            return customQueryExecutor.sourcesAnalyticsByJob(jobId, null, null);
        }
    }

    /**
     * function to call skillStrengthAnalyticsByJob()
     * @param jobId for which skill strength analytics will be generated
     * @param startDate is date which should be less than or equal to jcm creation date
     * @param endDate is date which should be greater than or equal to jcm creation date
     * @return KeySkillStrengthAnalyticsBean that contains candidate count per score for a job
     */
    private KeySkillStrengthAnalyticsBean getKeySkillsStrengthAnalytics(Long jobId, Optional<Date> startDate, Optional<Date> endDate){
        if(startDate.isPresent() && endDate.isPresent()){
            return customQueryExecutor.skillStrengthAnalyticsByJob(jobId, startDate.get(), endDate.get());
        }
        else{
            return customQueryExecutor.skillStrengthAnalyticsByJob(jobId, null, null);
        }
    }

    /**
     * function to call screeningStatusAnalytics()
     * @param jobId for which screening analytics will be generated
     * @param startDate is date which should be less than or equal to jcm creation date
     * @param endDate is date which should be greater than or equal to jcm creation date
     * @return ScreeningStatusAnalyticsBean that contains count of candidate per chatbot status i.e: invited, incomplete etc. for a job
     */
    private ScreeningStatusAnalyticsBean getScreeningStatusAnalytics(Long jobId, Optional<Date> startDate, Optional<Date> endDate){
        if(startDate.isPresent() && endDate.isPresent()){
            return customQueryExecutor.screeningStatusAnalyticsByJob(jobId, startDate.get(), endDate.get());
        }
        else{
            return customQueryExecutor.screeningStatusAnalyticsByJob(jobId, null, null);
        }
    }

    /**
     * function to call submittedAnalyticsByJob()
     * @param jobId for which submimtted analytics will be generated
     * @param startDate is date which should be less than or equal to jcm creation date
     * @param endDate is date which should be greater than or equal to jcm creation date
     * @return SubmittedAnalyticsBean that contains hiring manager status for a shared profile for that job
     */
    private SubmittedAnalyticsBean getSubmittedAnalytics(Long jobId, Optional<Date> startDate, Optional<Date> endDate){
        if(startDate.isPresent() && endDate.isPresent()){
            return customQueryExecutor.submittedAnalyticsByJob(jobId, startDate.get(), endDate.get());
        }
        else{
            return customQueryExecutor.submittedAnalyticsByJob(jobId, null, null);
        }
    }

    /**
     * function to call interviewAnalyticsByJob()
     * @param jobId for which interview analytics will be generated
     * @param startDate is date which should be less than or equal to jcm creation date
     * @param endDate is date which should be greater than or equal to jcm creation date
     * @return InterviewAnalyticsResponseBean that contains count of show, no show, scheduled, not scheduled, rescheduled for a job
     */
    private InterviewAnalyticsBean getInterviewAnalytics(Long jobId, Optional<Date> startDate, Optional<Date> endDate){
        if(startDate.isPresent() && endDate.isPresent()){
            return customQueryExecutor.interviewAnalyticsByJob(jobId, startDate.get(), endDate.get());
        }
        else{
            return customQueryExecutor.interviewAnalyticsByJob(jobId, null, null);
        }
    }
}
