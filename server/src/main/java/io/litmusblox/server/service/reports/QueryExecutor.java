/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.reports;

import io.litmusblox.server.service.reports.beans.*;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.DateType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : sameer
 * Date : 26/02/21
 * Time : 4:13 PM
 * Class Name : QueryExecutor
 * Project Name : server
 */
@Service
@PersistenceContext
public class QueryExecutor {

    @Autowired
    EntityManager entityManager;

    public List<Dsr> getDsrRows(RequestBean requestBean){
        StringBuffer query = new StringBuffer("");
        List<Dsr> dsrList = new ArrayList<>();
        query.append("select \n" +
                "count(*) as submitCount, \n" +
                "jcm.submitted_by as submittedBy, \n" +
                "jcm.submitted_on::::date as submittedOn \n" +
                "from \n" +
                "job_candidate_mapping jcm \n" +
                "left join job on\n" +
                "jcm.job_id = job.id\n" +
                "left join \n" +
                "company_bu cbu\n" +
                "on\n" +
                "job.bu_id = cbu.id\n" +
                "left join\n" +
                "users u \n" +
                "on \n" +
                "jcm.submitted_by = CONCAT(u.first_name, ' ', u.last_name) \n" +
                " where \n"+
                "jcm.submitted_on > '"+ requestBean.getStartDate()+"' and \n" +
                "jcm.submitted_on < '"+ requestBean.getEndDate()+"' \n");

        if(null != requestBean.getCompanyId())
            query.append(" and u.company_id=" + requestBean.getCompanyId());

        if(null != requestBean.getBuId())
            query.append(" and cbu.id =" + requestBean.getBuId());

        if(null != requestBean.getHiringManagerId())
            query.append(" and " + requestBean.getHiringManagerId() + " = ANY(job.hiring_manager)");

        if(null != requestBean.getRecruiterId())
            query.append(" and " + requestBean.getRecruiterId() + " = ANY(job.recruiter)");

        query.append(" group by submittedBy, submittedOn order by submittedBy asc;");

        try {
            Query finalQuery = entityManager.createNativeQuery(query.toString());
            finalQuery.unwrap(SQLQuery.class)
                    .addScalar("submitCount", LongType.INSTANCE)
                    .addScalar("submittedBy", StringType.INSTANCE)
                    .addScalar("submittedOn", DateType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(Dsr.class));

            dsrList = finalQuery.getResultList();

        }catch (Exception e){
            e.printStackTrace();
        }

        return dsrList;
    }

    public List<Csr> getCsrRows(RequestBean requestBean){
        StringBuffer query = new StringBuffer("");
        List<Csr> csrList = new ArrayList<>();
        query.append("select \n" +
                "count(*) as sourceCount, \n" +
                "u.first_name as sourcedBy, \n" +
                "jcm.created_on::::date as sourcedOn \n" +
                "from \n" +
                "job_candidate_mapping jcm \n" +
                "left join job on \n" +
                "jcm.job_id = job.id\n" +
                "left join \n" +
                "company_bu cbu\n" +
                "on\n" +
                "job.bu_id = cbu.id\n" +
                "left join \n" +
                "users u \n" +
                "on \n" +
                "jcm.created_by = u.id  \n" +
                "where \n" +
                "jcm.created_on > '"+requestBean.getStartDate()+"' and \n" +
                "jcm.created_on < '"+requestBean.getEndDate()+"' \n");

        if(null != requestBean.getCompanyId())
            query.append(" and u.company_id=" + requestBean.getCompanyId());

        if(null != requestBean.getBuId())
            query.append(" and cbu.id =" + requestBean.getBuId());

        if(null != requestBean.getHiringManagerId())
            query.append(" and " + requestBean.getHiringManagerId() + " = ANY(job.hiring_manager)");

        if(null != requestBean.getRecruiterId())
            query.append(" and " + requestBean.getRecruiterId() + " = ANY(job.recruiter)");

        query.append("group by sourcedBy, sourcedOn order by sourcedBy asc;");

        try {
            Query finalQuery = entityManager.createNativeQuery(query.toString());
            finalQuery.unwrap(SQLQuery.class)
                    .addScalar("sourceCount", LongType.INSTANCE)
                    .addScalar("sourcedBy", StringType.INSTANCE)
                    .addScalar("sourcedOn", DateType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(Csr.class));

            csrList = finalQuery.getResultList();

        }catch (Exception e){
            e.printStackTrace();
        }

        return csrList;
    }

    public List<Isr> getIsrRows(RequestBean requestBean){
        StringBuffer query = new StringBuffer("");
        List<Isr> isrList = new ArrayList<>();
        query.append("select \n" +
                "count(*) as totalScheduled, \n" +
                "sum(case when ivd.show_no_show='t' then 1 when ivd.show_no_show='f' then 0 end) as totalShow, \n" +
                "u.first_name as scheduledBy, ivd.created_on::::date as createdOn\n" +
                "from\n" +
                "interview_details ivd\n" +
                "inner join\n" +
                "job_candidate_mapping jcm \n" +
                "on\n" +
                "ivd.job_candidate_mapping_id = jcm.id\n" +
                "inner join \n" +
                "job \n" +
                "on\n" +
                "jcm.job_id = job.id\n" +
                "left join \n" +
                "company_bu cbu\n" +
                "on\n" +
                "job.bu_id = cbu.id\n" +
                "left join\n" +
                "users u \n" +
                "on \n" +
                "ivd.created_by = u.id\n" +
                "where \n" +
                "ivd.created_on > '"+requestBean.getStartDate()+"' and \n" +
                "ivd.created_on < '"+requestBean.getEndDate()+"' \n");

        if(null != requestBean.getCompanyId())
            query.append(" and u.company_id=" + requestBean.getCompanyId());

        if(null != requestBean.getBuId())
            query.append(" and cbu.id =" + requestBean.getBuId());

        if(null != requestBean.getHiringManagerId())
            query.append(" and " + requestBean.getHiringManagerId() + " = ANY(job.hiring_manager)");

        if(null != requestBean.getRecruiterId())
            query.append(" and " + requestBean.getRecruiterId() + " = ANY(job.recruiter)");

        query.append("group by scheduledBy, ivd.created_on::::date order by scheduledBy asc;");

        try {
            Query finalQuery = entityManager.createNativeQuery(query.toString());
            finalQuery.unwrap(SQLQuery.class)
                    .addScalar("totalScheduled", LongType.INSTANCE)
                    .addScalar("totalShow", LongType.INSTANCE)
                    .addScalar("scheduledBy", StringType.INSTANCE)
                    .addScalar("createdOn", DateType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(Isr.class));

            isrList = finalQuery.getResultList();

        }catch (Exception e){
            e.printStackTrace();
        }

        return isrList;
    }

    public List<Jir> getJirRows(RequestBean requestBean){
        StringBuffer query = new StringBuffer("");
        List<Jir> jirList = new ArrayList<>();
        query.append("select \n" +
                "count(*) as totalCount, company.company_name as companyName, job.created_on::::date as createdOn \n" +
                "from \n" +
                "job \n" +
                "inner join \n" +
                "company \n" +
                "on job.company_id = company.id\n" +
                "where \n" +
                "job.created_on::::date > '"+requestBean.getStartDate()+"' and \n" +
                "job.created_on::::date < '"+requestBean.getEndDate()+"' \n");
        if(null != requestBean.getCompanyId())
            query.append(" and company.id=" + requestBean.getCompanyId());

        if(null != requestBean.getBuId())
            query.append(" and job.bu_id =" + requestBean.getBuId());

        if(null != requestBean.getHiringManagerId())
            query.append(" and " + requestBean.getHiringManagerId() + " = ANY(job.hiring_manager)");

        if(null != requestBean.getRecruiterId())
            query.append(" and " + requestBean.getRecruiterId() + " = ANY(job.recruiter)");

        query.append("group by companyName, job.created_on::::date order by companyName;\n");

        try {
            Query finalQuery = entityManager.createNativeQuery(query.toString());
            finalQuery.unwrap(SQLQuery.class)
                    .addScalar("totalCount", LongType.INSTANCE)
                    .addScalar("companyName", StringType.INSTANCE)
                    .addScalar("createdOn", DateType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(Jir.class));

            jirList =  finalQuery.getResultList();

        }catch (Exception e){
            e.printStackTrace();
        }

        return jirList;
    }
}
