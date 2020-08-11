/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.JobAnalytics;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author : Sumit
 * Date : 31/07/20
 * Time : 12:50 PM
 * Class Name : InterviewDetailBean
 * Project Name : server
 */
@Entity
@Data
public class InterviewDetailBean {

    @Id
    private Long id;
    String candidateName;
    String jobTitle;
    Long jobId;
    String interviewTime;
    String status;
}

