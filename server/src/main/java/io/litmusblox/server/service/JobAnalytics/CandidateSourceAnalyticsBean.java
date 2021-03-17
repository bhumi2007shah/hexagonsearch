/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.JobAnalytics;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Class to hole Candidate source analytics for a job
 *
 * @author : sameer
 * Date : 19/03/20
 * Time : 10:44 AM
 * Class Name : CandidateSourceBean
 * Project Name : server
 */
@Data
@Entity
@JsonFilter("CandidateSourceAnalyticsBean")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CandidateSourceAnalyticsBean {

    @Id
    Long jobId;

    Integer individual = 0;
    Integer file = 0;
    Integer naukri = 0;
    Integer linkedin = 0;
    Integer iimjobs = 0;
    Integer dragDropCv = 0;
    Integer naukriMassMail = 0;
    Integer naukriJobPosting = 0;
    Integer employeeReferral = 0;
    Integer careerPage = 0;
    Integer jobPosting = 0  ;
    Integer genericEmail = 0;
}
