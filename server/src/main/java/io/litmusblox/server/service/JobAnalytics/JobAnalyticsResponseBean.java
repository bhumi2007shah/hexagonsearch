/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.JobAnalytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * Class to hold analytics per job.
 *
 * @author : sameer
 * Date : 17/03/20
 * Time : 7:55 PM
 * Class Name : JobAnalyticsResponseBean
 * Project Name : server
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobAnalyticsResponseBean {
    Long candidateSourced;
    Date jobCreatedOn;
    CandidateSourceAnalyticsBean candidateSources;
    KeySkillStrengthAnalyticsBean keySkillsStrength;
    ScreeningStatusAnalyticsBean screeningStatus;
    SubmittedAnalyticsBean submitted;
    InterviewAnalyticsBean interview;
    Map<String, Map<String, Integer>> reject;
}
