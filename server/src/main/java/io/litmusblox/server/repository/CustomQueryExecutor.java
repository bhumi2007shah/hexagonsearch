/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Job;
import io.litmusblox.server.service.AnalyticsResponseBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Shital Raval
 * Date : 27/12/19
 * Time : 10:37 AM
 * Class Name : CustomQueryExecutor
 * Project Name : server
 */
@Service
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
        List<Job> jobs = jobRepository.findAllById(resultSet);
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
    @Transactional(readOnly = true)
    public List<AnalyticsResponseBean> analyticsByCompany(Date startDate, Date endDate, String companyIdList) throws Exception {
        StringBuffer queryString = new StringBuffer(analyticsMainQuery).append(companyIdList).append(") ").append(groupByClause);
        Query query =  entityManager.createNativeQuery(queryString.toString(), AnalyticsResponseBean.class);
        return query.getResultList();
    }
}
