/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author : Shital Raval
 * Date : 5/2/20
 * Time : 12:37 PM
 * Class Name : AnalyticsResponseBean
 * Project Name : server
 */
@Entity
@Data
public class AnalyticsResponseBean {
    @Id
    private Long id;
    String companyName;
    int jobCount;
    Integer candidatesUploadedCount;
    Integer candidatesInvitedCount;
    Integer chatbotCompleteCount;
    Integer chatbotIncompleteCount;
    Integer chatbotNotVisitedCount;
    Integer chatbotNotInterestedCount;
}
