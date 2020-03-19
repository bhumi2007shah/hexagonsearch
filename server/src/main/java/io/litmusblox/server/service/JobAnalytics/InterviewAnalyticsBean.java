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
 * Class to hold interview analytics for a job
 *
 * @author : sameer
 * Date : 19/03/20
 * Time : 10:45 AM
 * Class Name : InterviewBean
 * Project Name : server
 */
@Entity
@Data
@JsonFilter("InterviewAnalyticsBean")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class InterviewAnalyticsBean {

    @Id
    Long jobId;

    Integer notScheduled = 0;
    Integer rescheduled = 0;
    Integer scheduled = 0;
    Integer show = 0;
    Integer noShow = 0;
}
