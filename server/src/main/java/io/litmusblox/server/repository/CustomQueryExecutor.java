/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.StageStepMaster;
import io.litmusblox.server.service.AnalyticsResponseBean;
import io.litmusblox.server.service.JCMAllDetails;
import io.litmusblox.server.service.JobAnalytics.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : Shital Raval
 * Date : 27/12/19
 * Time : 10:37 AM
 * Class Name : CustomQueryExecutor
 * Project Name : server
 */
@Service
@Log4j2
public class CustomQueryExecutor {
    @PersistenceContext
    EntityManager entityManager;

    @Resource
    JobRepository jobRepository;

    public List<Job> executeSearchQuery(String queryString) {
        List<Integer> jobIds = entityManager.createNativeQuery(queryString).getResultList();
        List<Long> resultSet  = jobIds.stream()
                .mapToLong(Integer::longValue)
                .boxed().collect(Collectors.toList());
        List<Job> jobs = jobRepository.findByIdInOrderByDatePublishedDesc(resultSet);
        return jobs;
    }


    private static final String analyticsMainQuery = "SELECT  company.id, company.company_name, count(distinct job.id) as job_count,\n" +
            "count(job_candidate_mapping.id) as candidates_uploaded_count,\n" +
            "sum((chatbot_status is not null)\\:\\:INT) AS candidates_invited_count,\n" +
            "sum((chatbot_status LIKE 'Complete')\\:\\:INT) AS chatbot_complete_count,\n" +
            "sum((chatbot_status LIKE 'Incomplete')\\:\\:INT) AS chatbot_incomplete_count,\n" +
            "sum((chatbot_status LIKE 'Invited')\\:\\:INT) AS chatbot_not_visited_count,\n" +
            "sum((chatbot_status LIKE 'Not Interested')\\:\\:INT) AS chatbot_not_interested_count\n" +
            "from company, job left join job_candidate_mapping\n" +
            "on job_candidate_mapping.job_id = job.id\n" +
            "where job.company_id = company.id\n" +
            "and job.company_id in (";
    private static final String groupByClause = "group by company.id";
    private static final String selectionStartDate = " and job.date_published >= '";
    private static final String selectionEndDate = " and job.date_published <= '";

    //Job Analytics queries;

    private static final String jobAnalyticsStartDate = " and created_on >= '";
    private static final String jobAnalyticsEndDate =" and created_on <= '";
    private static final String groupByJobId = " group by job_id;";

    //Source Analytics query
    private static final String jobSourcesAnalyticsMainQuery = "select \n" +
            "job_id as job_id,\n" +
            "sum((candidate_source like 'Individual')\\:\\:INT) as individual,\n" +
            "sum((candidate_source like 'File')\\:\\:INT) as file,\n" +
            "sum((candidate_source like 'Naukri')\\:\\:INT) as naukri,\n" +
            "sum((candidate_source like 'LinkedIn')\\:\\:INT) as linkedIn,\n" +
            "sum((candidate_source like 'IIMJobs')\\:\\:INT) as iimjobs,\n" +
            "sum((candidate_source like 'DragDropCv')\\:\\:INT) as drag_drop_cv,\n" +
            "sum((candidate_source like 'NaukriMassMail')\\:\\:INT) as naukri_mass_mail,\n" +
            "sum((candidate_source like 'NaukriJobPosting')\\:\\:INT) as naukri_job_posting,\n" +
            "sum((candidate_source like 'EmployeeReferral')\\:\\:INT) as employee_referral,\n" +
            "sum((candidate_source like 'CareerPage')\\:\\:INT) as career_page,\n" +
            "sum((candidate_source like 'JobPosting')\\:\\:INT) as job_posting\n" +
            "from job_candidate_mapping where job_id = ";

    //Skill Strength analytics query
    private static final String jobSkillStrengthAnalyticsMainQuery = "select \n" +
            "job_id as job_id,\n" +
            "sum((score is null or score = 0)\\:\\:INT) as not_measured,\n" +
            "sum((score=1)\\:\\:INT) as very_weak,\n" +
            "sum((score=2)\\:\\:INT) as weak,\n" +
            "sum((score=3)\\:\\:INT) as good,\n" +
            "sum((score=4)\\:\\:INT) as strong,\n" +
            "sum((score=5)\\:\\:INT) as very_strong\n" +
            "from job_candidate_mapping where job_id = ";

    //screening status analytics query
    private static final String jobScreeningStatusAnalyticsMainQuery = "select \n" +
            "job_id as job_id,\n" +
            "sum((chatbot_status is null)\\:\\:INT) as not_invited,\n" +
            "sum((chatbot_status like 'Invited')\\:\\:INT) as invited,\n" +
            "sum((chatbot_status like 'Incomplete')\\:\\:INT) as incomplete,\n" +
            "sum((chatbot_status like 'Completed')\\:\\:INT) as completed,\n" +
            "sum((chatbot_status like 'Not Interested')\\:\\:INT) as not_interested\n" +
            "from job_candidate_mapping where job_id = ";

    //submitted analytics query
    private static final String jobSubmittedAnalyticsMainQuery = "select \n" +
            "job_id as job_id,\n" +
            "sum((jpsd.id is not null and jpsd.hiring_manager_interest = 't' )\\:\\:INT) as interested,\n" +
            "sum((jpsd.id is not null and jpsd.hiring_manager_interest = 'f' and jpsd.hiring_manager_interest_date is not null )\\:\\:INT) as cv_reject,\n" +
            "sum((jpsd.id is not null and jpsd.hiring_manager_interest = 'f' )\\:\\:INT) as not_reviewed\n" +
            "from job_candidate_mapping jcm left join jcm_profile_sharing_details jpsd\n" +
            "on jcm.id = jpsd.job_candidate_mapping_id\n" +
            "where job_id = ";

    //interview analytics query
    private static final String jobInterviewAnalyticsMainQuery = "select job_id, \n" +
            "sum((interview.id is null)\\:\\:INT) as not_scheduled,\n" +
            "sum((interview.id is not null and interview.show_no_show = 't')\\:\\:INT) as show,\n" +
            "sum((interview.id is not null and interview.show_no_show = 'f')\\:\\:INT) as no_show,\n" +
            "sum((interview.id is not null and interviewCount.scheduleCount>1)\\:\\:INT) as rescheduled,\n" +
            "sum((interview.id is not null and interviewCount.scheduleCount=1)\\:\\:INT) as scheduled\n" +
            "from job_candidate_mapping jcm left join interview_details interview\n" +
            "on jcm.id=interview.job_candidate_mapping_id\n" +
            "left join \n" +
            "(select count(id) as scheduleCount, job_candidate_mapping_id from interview_details group by job_candidate_mapping_id)\n" +
            "as interviewCount on\n" +
            "jcm.id = interviewCount.job_candidate_mapping_id\n" +
            "where job_id = ";

    //Rejected Analytics query
    private static final String jobRejectedAnalyticsMainQuery = "select jcm.id as jcmId, \n" +
            "count(jcm.id) as rejected_count, ssm.stage as current_stage,  jcm.candidate_rejection_value as rejected_reason \n" +
            "from job_candidate_mapping jcm \n" +
            "inner join job_candidate_mapping_all_details jcma \n" +
            "on \n" +
            "jcm.id=jcma.id \n" +
            "inner join \n" +
            "stage_step_master ssm \n" +
            "on \n" +
            "ssm.id = jcma.stage \n" +
            "where \n" +
            "jcm.candidate_rejection_value is not null \n" +
            "and \n" +
            "jcm.job_id = ";
    private static final String jobRejectedAnalyticsStartDate = " and jcm.created_on >= '";
    private static final String jobRejectedAnalyticsEndDate =" and jcm.created_on <= '";
    private static final String jobRejectedAnalyticsGroupByQuery = " group by \n" +
            "jcm.id, jcm.job_id, ssm.stage, jcm.candidate_rejection_value;";

    private static final String rejectedAnalyticsMainQuery = "";

    @Transactional(readOnly = true)
    public List<AnalyticsResponseBean> analyticsByCompany(String startDate, String endDate, String companyIdList) throws Exception {
        StringBuffer queryString = new StringBuffer(analyticsMainQuery).append(companyIdList).append(") ");
        if (null != startDate)
            queryString.append(selectionStartDate).append(startDate).append("' ");
        if (null != endDate)
            queryString.append(selectionEndDate).append(endDate).append("' ");
        queryString.append(groupByClause);
        Query query =  entityManager.createNativeQuery(queryString.toString(), AnalyticsResponseBean.class);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public CandidateSourceAnalyticsBean sourcesAnalyticsByJob(Long jobId, Date startDate, Date endDate){
        StringBuffer queryString = new StringBuffer();
        queryString.append(jobSourcesAnalyticsMainQuery);
        queryString.append(jobId);
        if(null!=startDate && null!=endDate){
            queryString.append(jobAnalyticsStartDate).append(startDate).append("' ");
            queryString.append(jobAnalyticsEndDate).append(endDate).append("' ");
        }
        queryString.append(groupByJobId);
        Query query = entityManager.createNativeQuery(queryString.toString(), CandidateSourceAnalyticsBean.class);
        return (CandidateSourceAnalyticsBean) query.getSingleResult();
    }

    @Transactional(readOnly = true)
    public KeySkillStrengthAnalyticsBean skillStrengthAnalyticsByJob(Long jobId, Date startDate, Date endDate){
        StringBuffer queryString = new StringBuffer();
        queryString.append(jobSkillStrengthAnalyticsMainQuery);
        queryString.append(jobId);
        if(null!=startDate && null!=endDate){
            queryString.append(jobAnalyticsStartDate).append(startDate).append("' ");
            queryString.append(jobAnalyticsEndDate).append(endDate).append("' ");
        }
        queryString.append(groupByJobId);
        Query query =  entityManager.createNativeQuery(queryString.toString(), KeySkillStrengthAnalyticsBean.class);
        return (KeySkillStrengthAnalyticsBean) query.getSingleResult();
    }

    @Transactional(readOnly = true)
    public ScreeningStatusAnalyticsBean screeningStatusAnalyticsByJob(Long jobId, Date startDate, Date endDate){
        StringBuffer queryString = new StringBuffer();
        queryString.append(jobScreeningStatusAnalyticsMainQuery);
        queryString.append(jobId);
        if(null!=startDate && null!=endDate){
            queryString.append(jobAnalyticsStartDate).append(startDate).append("' ");
            queryString.append(jobAnalyticsEndDate).append(endDate).append("' ");
        }
        queryString.append(groupByJobId);
        Query query =  entityManager.createNativeQuery(queryString.toString(), ScreeningStatusAnalyticsBean.class);
        return (ScreeningStatusAnalyticsBean) query.getSingleResult();
    }

    @Transactional(readOnly = true)
    public SubmittedAnalyticsBean submittedAnalyticsByJob(Long jobId, Date startDate, Date endDate){
        StringBuffer queryString = new StringBuffer();
        queryString.append(jobSubmittedAnalyticsMainQuery);
        queryString.append(jobId);
        if(null!=startDate && null!=endDate){
            queryString.append(jobAnalyticsStartDate).append(startDate).append("' ");
            queryString.append(jobAnalyticsEndDate).append(endDate).append("' ");
        }
        queryString.append(groupByJobId);
        Query query =  entityManager.createNativeQuery(queryString.toString(), SubmittedAnalyticsBean.class);
        return (SubmittedAnalyticsBean) query.getSingleResult();
    }

    @Transactional(readOnly = true)
    public InterviewAnalyticsBean interviewAnalyticsByJob(Long jobId, Date startDate, Date endDate){
        StringBuffer queryString = new StringBuffer();
        queryString.append(jobInterviewAnalyticsMainQuery);
        queryString.append(jobId);
        if(null!=startDate && null!=endDate){
            queryString.append(jobAnalyticsStartDate).append(startDate).append("' ");
            queryString.append(jobAnalyticsEndDate).append(endDate).append("' ");
        }
        queryString.append(groupByJobId);
        Query query =  entityManager.createNativeQuery(queryString.toString(), InterviewAnalyticsBean.class);
        return (InterviewAnalyticsBean) query.getSingleResult();
    }


    //single job view for rejected = true
    public List<JCMAllDetails> findByJobAndRejectedIsTrue(Job job) {
        Query query = entityManager.createNativeQuery("Select * from job_candidate_mapping_all_details where job_id = " + job.getId() + " and rejected is true;", JCMAllDetails.class);
        return query.getResultList();
    }

    //single job view for rejected = false
    public List<JCMAllDetails> findByJobAndStageInAndRejectedIsFalse(Job job, StageStepMaster stageStepMaster) {
        Query query = entityManager.createNativeQuery("Select * from job_candidate_mapping_all_details where job_id = " + job.getId() + " and stage = " + stageStepMaster.getId() + " and rejected is false;", JCMAllDetails.class);
        return query.getResultList();
    }

    public Map<String, Map<String, Integer>> rejectedAnalyticsByJob(Long jobId, Date startDate, Date endDate) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(jobRejectedAnalyticsMainQuery);
        queryString.append(jobId);
        if (null != startDate && null != endDate) {
            queryString.append(jobRejectedAnalyticsStartDate).append(startDate).append("' ");
            queryString.append(jobRejectedAnalyticsEndDate).append(endDate).append("' ");
        }
        queryString.append(jobRejectedAnalyticsGroupByQuery);
        Query query = entityManager.createNativeQuery(queryString.toString(), RejectAnalyticsBean.class);
        List<RejectAnalyticsBean> rejectAnalyticsBeans = query.getResultList();
        Map<String, Map<String, Integer>> rejectAnalytics = new HashMap<>(0);
        rejectAnalyticsBeans.stream().collect(Collectors.groupingBy(RejectAnalyticsBean::getCurrentStage)).forEach((stage, jcmList) -> {
            Map<String, Integer> reasonCountMap = new HashMap<>(0);
            jcmList.stream().collect(Collectors.groupingBy(RejectAnalyticsBean::getRejectedReason)).forEach((reason, jcms) -> {
                reasonCountMap.put(reason, jcms.size());
            });
            rejectAnalytics.put(stage, reasonCountMap);
        });
        return rejectAnalytics;
    }
}