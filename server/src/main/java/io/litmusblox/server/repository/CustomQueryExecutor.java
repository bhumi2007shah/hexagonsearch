/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.StageStepMaster;
import io.litmusblox.server.model.User;
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
import java.time.LocalDate;
import java.util.*;
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
            "sum((candidate_source like 'JobPosting')\\:\\:INT) as job_posting,\n" +
            "sum((candidate_source like 'GenericEmail')\\:\\:INT) as generic_email\n" +
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

    private static final String basicCandidateStagePerCountQuery = "SELECT \n" +
            "sum((stage = (select id from stage_step_master where stage = 'Sourcing' and rejected = 'f'))\\:\\:INT) AS sourcingCandidatesCount,\n" +
            "sum((stage = (select id from stage_step_master where stage = 'Screening' and rejected = 'f'))\\:\\:INT) AS screeningCandidatesCount,\n" +
            "sum((stage = (select id from stage_step_master where stage = 'Submitted' and rejected = 'f'))\\:\\:INT) AS submittedCandidatesCount,\n" +
            "sum((stage = (select id from stage_step_master where stage = 'Interview' and rejected = 'f'))\\:\\:INT) AS interviewCandidatesCount,\n" +
            "sum((stage = (select id from stage_step_master where stage = 'Make Offer' and rejected = 'f'))\\:\\:INT) AS makeOfferCandidatesCount,\n" +
            "sum((stage = (select id from stage_step_master where stage = 'Offer' and rejected = 'f'))\\:\\:INT) AS offerCandidatesCount,\n" +
            "sum((stage = (select id from stage_step_master where stage = 'Hired' and rejected = 'f'))\\:\\:INT) AS hiredCandidatesCount\n" +
            "from job left join job_candidate_mapping on job_candidate_mapping.job_id = job.id where job.date_archived is null";

    private static final String stageCountClientAdminWhereClause = " and job.id in (select id from job where company_id =";
    private static final String stageCountRecruiterWhereClause = " and job.id in (select id from job where created_by =";


    private static final String basicJobAgingCountQuery = "SELECT sum(((DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)) >=0 AND (DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)<=15))\\:\\:INT) as jobAging0TO15Days,\n" +
            "sum(((DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)) >=16 AND (DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)<=30))\\:\\:INT) as jobAging16TO30Days,\n" +
            "sum(((DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)) >=31 AND (DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)<=60))\\:\\:INT) as jobAging31TO60Days,\n" +
            "sum(((DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)) >=61 AND (DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)<=90))\\:\\:INT) as jobAging61TO90Days,\n" +
            "sum(((DATE_PART('day', CURRENT_DATE\\:\\:timestamp - date_published\\:\\:timestamp)) > 90)\\:\\:INT) as jobAging90PlusDays from job where job.date_published is not null and job.date_archived is null";

    private static final String jobAgingClientAdminWhereClause = " and job.id in (select id from job where company_id =";
    private static final String jobAgingRecruiterWhereClause = " and job.id in (select id from job where created_by =";

    private static final String basicJobCandidatePipelineQuery = "SELECT sum((jCount.candidateCount >=0 AND jCount.candidateCount<=3)\\:\\:INT) as candidateCount0TO3Days,\n" +
            "sum((jCount.candidateCount >=4 AND jCount.candidateCount<=6)\\:\\:INT) as candidateCount4TO6Days,\n" +
            "sum((jCount.candidateCount >=7 AND jCount.candidateCount<=10)\\:\\:INT) as candidateCount7TO10Days,\n" +
            "sum((jCount.candidateCount > 10)\\:\\:INT) as candidateCount10PlusDays from (select job.id , count(jcm.id) as candidateCount from job left join job_candidate_mapping jcm on job.id = jcm.job_id where job.date_archived is null and job.date_published is not null \n"+
            "and jcm.stage in (3, 4) and jcm.rejected = 'f'";

    private static final String jobPipelineClientAdminWhereClause = "  and job.company_id=";
    private static final String jobPipelineRecruiterWhereClause = " and job.created_by =";

    private static final String basicInterviewQuery = "select count(iv.id) from interview_details iv \n" +
            "inner join job_candidate_mapping jcm on jcm.id = iv.job_candidate_mapping_id\n" +
            "inner join job on job.id = jcm.job_id where job.date_archived is null";

    private static final String selectInterviewDateQuery = "select CAST(iv.interview_date as VARCHAR) from interview_details iv \n" +
            "inner join job_candidate_mapping jcm on jcm.id = iv.job_candidate_mapping_id\n" +
            "inner join job on job.id = jcm.job_id where job.date_archived is null";

    private static final String interviewStartDateClause = " interview_date >= ";
    private static final String interviewEndDateClause = " interview_date < ";

    private static final String selectInterviewDetailsQuery = "select iv.id, CONCAT(jcm.candidate_first_name, ' ', jcm.candidate_last_name) as candidate_name, job.job_title as job_title, job.id as job_id, to_char(iv.interview_date + time '05:30', 'HH12:MI AM')  as interview_time,\n" +
            "(CASE when (iv.candidate_confirmation_value is not null and md.value like 'Yes%') then 'Confirmed'\n" +
            "when (iv.candidate_confirmation_value is not null and md.value like '%reschedule%') then 'Rescheduled'\n" +
            "when (iv.candidate_confirmation_value is not null and md.value like 'No%') or  iv.cancelled = 't' then 'Cancelled'\n" +
            "else 'Scheduled'\n" +
            "END) as status\n" +
            "from job_candidate_mapping jcm \n" +
            "left join interview_details iv on iv.job_candidate_mapping_id = jcm.id\n" +
            "left join job on job.id = jcm.job_id\n" +
            "left join master_data md on md.id = iv.candidate_confirmation_value where job.date_archived is null";

    private static final String getIVDetailsWhereClause = " iv.interview_date =";

    private static final String totalLiveJobCountSelectQuery = "select count(id) from job where date_published is not null and date_archived is null";

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
    public CandidateSourceAnalyticsBean sourcesAnalyticsByJob(Long jobId, Date startDate, Date endDate) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(jobSourcesAnalyticsMainQuery);
        queryString.append(jobId);
        if(null!=startDate && null!=endDate){
            queryString.append(jobAnalyticsStartDate).append(startDate).append("' ");
            queryString.append(jobAnalyticsEndDate).append(endDate).append("' ");
        }
        queryString.append(groupByJobId);
        Query query = entityManager.createNativeQuery(queryString.toString(), CandidateSourceAnalyticsBean.class);
        try {
            return (CandidateSourceAnalyticsBean) query.getSingleResult();
        }
        catch(Exception e){
            e.printStackTrace();
            log.info("sourcesAnalyticsByJob returned null for job with id: {}",jobId);
            return null;
        }
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

    //single job view for specified status
    public List<JCMAllDetails> findByJobAndStatus(Job job, String status){
        Query query = entityManager.createNativeQuery("Select * from job_candidate_mapping_all_details where job_id = " + job.getId() + " and chatbot_status = 'Complete'", JCMAllDetails.class);
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

    @Transactional(readOnly = true)
    public int getOpenJobCount(User loggedInUser) {
        if(IConstant.UserRole.CLIENT_ADMIN.toString().equals(loggedInUser.getRole()))
            return jobRepository.countByStatusAndDateArchivedIsNullAndCompany(IConstant.JobStatus.PUBLISHED.getValue(), loggedInUser.getCompany().getId());
        else if(IConstant.UserRole.RECRUITER.toString().equals(loggedInUser.getRole()))
            return jobRepository.countByStatusAndDateArchivedIsNullAndCreatedBy(IConstant.JobStatus.PUBLISHED.getValue(), loggedInUser.getId());
        else if(IConstant.UserRole.SUPER_ADMIN.toString().equals(loggedInUser.getRole()))
            return jobRepository.countByStatusAndDateArchivedIsNull(IConstant.JobStatus.PUBLISHED.getValue());
        return 0;
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getCandidateCountByStage(User loggedInUser) {
        Map<String, Integer> candidateCountByStageMap = new LinkedHashMap<>();
        StringBuffer queryString = new StringBuffer();
        queryString.append(basicCandidateStagePerCountQuery);
        if(IConstant.UserRole.CLIENT_ADMIN.toString().equals(loggedInUser.getRole()))
            queryString.append(stageCountClientAdminWhereClause).append(loggedInUser.getCompany().getId()).append(")");
        else if(IConstant.UserRole.RECRUITER.toString().equals(loggedInUser.getRole()))
            queryString.append(stageCountRecruiterWhereClause).append(loggedInUser.getId()).append(")");

        List<Object[]> resultSet = entityManager.createNativeQuery(queryString.toString()).getResultList();
        for (Object[] objects : resultSet) {
            candidateCountByStageMap.put("sourcingCandidateCount", (null == objects[0])?0:Integer.parseInt(objects[0].toString()));
            candidateCountByStageMap.put("screeningCandidateCount", (null == objects[1])?0:Integer.parseInt(objects[1].toString()));
            candidateCountByStageMap.put("submittedCandidateCount", (null == objects[2])?0:Integer.parseInt(objects[2].toString()));
            candidateCountByStageMap.put("interviewCandidateCount", (null == objects[3])?0:Integer.parseInt(objects[3].toString()));
            candidateCountByStageMap.put("makeOfferCandidateCount", (null == objects[4])?0:Integer.parseInt(objects[4].toString()));
            candidateCountByStageMap.put("OfferCandidateCount", (null == objects[5])?0:Integer.parseInt(objects[5].toString()));
            candidateCountByStageMap.put("hiredCandidateCount", (null == objects[6])?0:Integer.parseInt(objects[6].toString()));
        }
        return candidateCountByStageMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getJobAgingCount(User loggedInUser) {
        Map<String, Integer> jobAgingCountMap = new LinkedHashMap<>();
        StringBuffer queryString = new StringBuffer();
        queryString.append(basicJobAgingCountQuery);
        if(IConstant.UserRole.CLIENT_ADMIN.toString().equals(loggedInUser.getRole()))
            queryString.append(jobAgingClientAdminWhereClause).append(loggedInUser.getCompany().getId()).append(")");
        else if(IConstant.UserRole.RECRUITER.toString().equals(loggedInUser.getRole()))
            queryString.append(jobAgingRecruiterWhereClause).append(loggedInUser.getId()).append(")");
        List<Object[]> resultSet = entityManager.createNativeQuery(queryString.toString()).getResultList();
        for(Object[] objects: resultSet){
            jobAgingCountMap.put("jobAging0To15Days",(null == objects[0])?0:Integer.parseInt(objects[0].toString()));
            jobAgingCountMap.put("jobAging16To30Days",(null == objects[1])?0:Integer.parseInt(objects[1].toString()));
            jobAgingCountMap.put("jobAging31To60Days",(null == objects[2])?0:Integer.parseInt(objects[2].toString()));
            jobAgingCountMap.put("jobAging61To90Days",(null == objects[3])?0:Integer.parseInt(objects[3].toString()));
            jobAgingCountMap.put("jobAging90PlusDays",(null == objects[4])?0:Integer.parseInt(objects[4].toString()));
        }
        return jobAgingCountMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getJobCandidatePipelineCount(User loggedInUser) {
        Map<String, Integer> jobCandidatePipelineCountMap = new LinkedHashMap<>();
        StringBuffer queryString = new StringBuffer();
        StringBuffer totalJobQueryString = new StringBuffer();
        totalJobQueryString.append(totalLiveJobCountSelectQuery);
        queryString.append(basicJobCandidatePipelineQuery);
        if(IConstant.UserRole.CLIENT_ADMIN.toString().equals(loggedInUser.getRole())){
            queryString.append(jobPipelineClientAdminWhereClause).append(loggedInUser.getCompany().getId());
            totalJobQueryString.append(jobPipelineClientAdminWhereClause).append(loggedInUser.getCompany().getId());
        }
        else if(IConstant.UserRole.RECRUITER.toString().equals(loggedInUser.getRole())){
            queryString.append(jobPipelineRecruiterWhereClause).append(loggedInUser.getId());
        totalJobQueryString.append(jobPipelineRecruiterWhereClause).append(loggedInUser.getId());
        }

        int totalJobCount = Integer.parseInt(entityManager.createNativeQuery(totalJobQueryString.toString()).getResultList().get(0).toString());


        queryString.append(" group by job.id) as jCount");
        List<Object[]> resultSet = entityManager.createNativeQuery(queryString.toString()).getResultList();

        for(Object[] objects: resultSet){
            jobCandidatePipelineCountMap.put("candidateCount0To3", (null == objects[0])?0:(Integer.parseInt(objects[0].toString()) + (totalJobCount - (Integer.parseInt(objects[0].toString())
            + Integer.parseInt(objects[1].toString()) + Integer.parseInt(objects[2].toString()) + Integer.parseInt(objects[3].toString())))));
            jobCandidatePipelineCountMap.put("candidateCount4To6",(null == objects[1])?0:Integer.parseInt(objects[1].toString()));
            jobCandidatePipelineCountMap.put("candidateCount7To10",(null == objects[2])?0:Integer.parseInt(objects[2].toString()));
            jobCandidatePipelineCountMap.put("candidateCount10Plus",(null == objects[3])?0:Integer.parseInt(objects[3].toString()));
        }
        return jobCandidatePipelineCountMap;
    }

    @Transactional(readOnly = true)
    public Integer getFutureInterviewCount(User loggedInUser, String startDate, String endDate) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(basicInterviewQuery);
        if(IConstant.UserRole.CLIENT_ADMIN.toString().equals(loggedInUser.getRole()))
            queryString.append(jobPipelineClientAdminWhereClause).append(loggedInUser.getCompany().getId());
         else if(IConstant.UserRole.RECRUITER.toString().equals(loggedInUser.getRole()))
            queryString.append(jobPipelineRecruiterWhereClause).append(loggedInUser.getId());

         if(null != startDate)
             queryString.append(" and ").append(interviewStartDateClause).append("'").append(startDate).append("'");
         else
             queryString.append(" and ").append(interviewStartDateClause).append("CURRENT_DATE");

        if(null != endDate)
            queryString.append(" and ").append(interviewEndDateClause).append("'").append(endDate).append("'");


        return Integer.parseInt(entityManager.createNativeQuery(queryString.toString()).getResultList().get(0).toString());
    }

    @Transactional(readOnly = true)
    public Integer get7DaysInterviewCount(User loggedInUser) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(basicInterviewQuery);
        if(IConstant.UserRole.CLIENT_ADMIN.toString().equals(loggedInUser.getRole()))
            queryString.append(jobPipelineClientAdminWhereClause).append(loggedInUser.getCompany().getId());
        else if(IConstant.UserRole.RECRUITER.toString().equals(loggedInUser.getRole()))
            queryString.append(jobPipelineRecruiterWhereClause).append(loggedInUser.getId());

        queryString.append(" and").append(interviewStartDateClause).append("CURRENT_DATE");
        queryString.append(" and ").append(interviewEndDateClause).append("CURRENT_DATE+7");
        return Integer.parseInt(entityManager.createNativeQuery(queryString.toString()).getResultList().get(0).toString());
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> get2MonthInterviewCount(User loggedInUser, String selectedMonthDate) {
        Map<String, Integer> monthViseInterviewCountMap = new LinkedHashMap<>();
        LocalDate startDate = LocalDate.parse(selectedMonthDate);
        LocalDate endDate = startDate.plusMonths(1);
        monthViseInterviewCountMap.put(startDate.getMonth().toString(),     getFutureInterviewCount(loggedInUser, startDate.toString(), endDate.toString()));
        monthViseInterviewCountMap.put(endDate.getMonth().toString(),getFutureInterviewCount(loggedInUser, endDate.toString(), endDate.plusMonths(1).toString()));
        return monthViseInterviewCountMap;
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getInterviewDateList(User loggedInUser, String selectedMonthDate) {
        Map<String, List<String>> interviewDateSetMap = new LinkedHashMap<>();
        StringBuffer queryString = new StringBuffer();
        queryString.append(selectInterviewDateQuery);
        if(IConstant.UserRole.CLIENT_ADMIN.toString().equals(loggedInUser.getRole()))
            queryString.append(jobPipelineClientAdminWhereClause).append(loggedInUser.getCompany().getId());
        else if(IConstant.UserRole.RECRUITER.toString().equals(loggedInUser.getRole()))
            queryString.append(jobPipelineRecruiterWhereClause).append(loggedInUser.getId());

        String query = queryString.toString();
        StringBuffer queryString2 = new StringBuffer();
        queryString2.append(query);
        LocalDate startDate = LocalDate.parse(selectedMonthDate);
        LocalDate endDate = startDate.plusMonths(1);
        queryString.append(" and ").append(interviewStartDateClause).append("'").append(startDate).append("'");
        queryString.append(" and ").append(interviewEndDateClause).append("'").append(endDate).append("'");
        interviewDateSetMap.put(startDate.getMonth().toString(),entityManager.createNativeQuery(queryString.toString()).getResultList());
        queryString2.append(" and ").append(interviewStartDateClause).append("'").append(endDate).append("'");
        queryString2.append(" and ").append(interviewEndDateClause).append("'").append(endDate.plusMonths(1)).append("'");
        interviewDateSetMap.put(endDate.getMonth().toString(), entityManager.createNativeQuery(queryString2.toString()).getResultList());
        return interviewDateSetMap;
    }

    @Transactional(readOnly = true)
    public List<InterviewDetailBean> getInterviewDetails(User loggedInUser, String selectedDate) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(selectInterviewDetailsQuery);
        if(IConstant.UserRole.CLIENT_ADMIN.toString().equals(loggedInUser.getRole()))
            queryString.append(jobPipelineClientAdminWhereClause).append(loggedInUser.getCompany().getId());
        else if(IConstant.UserRole.RECRUITER.toString().equals(loggedInUser.getRole()))
            queryString.append(jobPipelineRecruiterWhereClause).append(loggedInUser.getId());

        queryString.append(" and interview_date\\:\\:text LIKE '").append(selectedDate).append("%'");
        Query query = entityManager.createNativeQuery(queryString.toString(), InterviewDetailBean.class);
        return query.getResultList();
    }
}