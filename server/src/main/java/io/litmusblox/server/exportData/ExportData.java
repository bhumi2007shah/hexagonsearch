/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.exportData;

import io.litmusblox.server.error.WebException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author : sameer
 * Date : 03/12/19
 * Time : 3:19 PM
 * Class Name : ExportData
 * Project Name : server
 */
@Service
public class ExportData {
    public static List<Object[]> exportDataList(Long jobId, String stage, String columns, EntityManager em){
        StringBuffer query =new StringBuffer("");
        query.append("select ");
        query.append(columns);
        query.append(" from exportDataView where jobId=");
        query.append(jobId);
        query.append(" and currentStage='"+stage+"'");
        List<Object[]> exportDataList= new ArrayList<>();

        try {
            exportDataList = em.createNativeQuery(query.toString()).getResultList();
        }
        catch(Exception e){
            throw new WebException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        finally {
            em.close();
        }
        return exportDataList;
    }

    public static LinkedHashMap<String, String> getQuestionAnswerForCandidate(String email, Long jobId, EntityManager em){
        LinkedHashMap<String, String> questionAnswerMapForCandidate = new LinkedHashMap<>();
        StringBuffer query = new StringBuffer("");
        query.append("select screeningQuestion, candidateResponse from exportDataView where email='");
        query.append(email);
        query.append("' and jobId='"+jobId+"'");

        List<Object[]> exportDataList= new ArrayList<>();

        try{
            exportDataList = em.createNativeQuery(query.toString()).getResultList();
        }
        catch (Exception e){
            throw new WebException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(exportDataList.size()!=0){
            exportDataList.forEach(exportData->{
                if(null!=exportData[0])
                questionAnswerMapForCandidate.put(exportData[0].toString(), exportData[1]!=null?exportData[1].toString():"");
            });
        }

        return questionAnswerMapForCandidate;
    }
}
