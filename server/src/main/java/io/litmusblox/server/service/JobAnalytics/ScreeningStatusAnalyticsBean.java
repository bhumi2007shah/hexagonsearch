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
 * Class to hold screening status analytics for a job
 *
 * @author : sameer
 * Date : 19/03/20
 * Time : 10:45 AM
 * Class Name : ScreeningStatusBean
 * Project Name : server
 */
@Entity
@Data
@JsonFilter("ScreeningStatusAnalyticsBean")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ScreeningStatusAnalyticsBean {

    @Id
    Long jobId;

    Integer notInvited = 0;
    Integer invited = 0;
    Integer completed = 0;
    Integer incomplete = 0;
    Integer notInterested = 0;
}
