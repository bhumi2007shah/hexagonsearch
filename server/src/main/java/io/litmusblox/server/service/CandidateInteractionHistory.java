/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import java.util.Date;

/**
 * @author : Sumit
 * Date : 18/11/19
 * Time : 12:33 PM
 * Class Name : ICandidateInteractionHistoryBean
 * Project Name : server
 */
public interface CandidateInteractionHistory {

    String getJobId();
    String getJobTitle();
    String getCurrentStatus();
    Date getSourcedOn();
    String getLastStage();
    String getHiringManager();
    String getRecruiter();

}
