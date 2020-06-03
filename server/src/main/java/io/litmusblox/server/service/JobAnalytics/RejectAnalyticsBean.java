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
 * @author : sameer
 * Date : 01/06/20
 * Time : 8:34 PM
 * Class Name : RejectAnalyticsBean
 * Project Name : server
 */
@Entity
@Data
@JsonFilter("RejectAnalyticsBean")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RejectAnalyticsBean {
    @Id
    private Long jcmid;

    private Long rejectedCount;

    private String currentStage;

    private String rejectedReason;
}
