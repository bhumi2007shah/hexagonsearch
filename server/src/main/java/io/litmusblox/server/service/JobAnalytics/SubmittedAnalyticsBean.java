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
 * class to hold submitted analytics for a job
 *
 * @author : sameer
 * Date : 19/03/20
 * Time : 10:45 AM
 * Class Name : SubmittedBean
 * Project Name : server
 */
@Entity
@Data
@JsonFilter("SubmittedAnalyticsBean")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class SubmittedAnalyticsBean {

    @Id
    Long jobId;

    Integer notReviewed = 0;
    Integer interested = 0;
    Integer cvReject = 0;
}
