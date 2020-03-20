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
 * Class to hold key skills strength analytics for a job
 *
 * @author : sameer
 * Date : 19/03/20
 * Time : 10:44 AM
 * Class Name : KeySkillStrengthBean
 * Project Name : server
 */
@Entity
@Data
@JsonFilter("KeySkillStrengthAnalyticsBean")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class KeySkillStrengthAnalyticsBean {

    @Id
    Long jobId;

    Integer veryStrong = 0;
    Integer strong = 0;
    Integer good = 0;
    Integer weak = 0;
    Integer veryWeak = 0;
    Integer notMeasured = 0;
}
