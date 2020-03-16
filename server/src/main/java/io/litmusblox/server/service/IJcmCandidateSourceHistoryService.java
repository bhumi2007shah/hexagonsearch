/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.User;

/**
 * @author : sameer
 * Date : 16/03/20
 * Time : 5:34 PM
 * Class Name : IJcmCandidateSourceHistory
 * Project Name : server
 */
public interface IJcmCandidateSourceHistoryService {
    /**
     * Service to create and save Candidate Source History
     * @param jcmId
     * @param candidateSource
     * @param loggedInUser
     */
    void createJcmCandidateSourceHistory(Long jcmId, String candidateSource, User loggedInUser);
}
