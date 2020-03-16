/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.model.JcmCandidateSourceHistory;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.JcmCandidateSourceHistoryRepository;
import io.litmusblox.server.service.IJcmCandidateSourceHistoryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : sameer
 * Date : 16/03/20
 * Time : 5:46 PM
 * Class Name : JcmCandidateSourceHistoryService
 * Project Name : server
 */
@Log4j2
@Service
public class JcmCandidateSourceHistoryService implements IJcmCandidateSourceHistoryService {
    @Resource
    JcmCandidateSourceHistoryRepository jcmCandidateSourceHistoryRepository;

    /**
     * Service to create and save Candidate Source History
     * @param jcmId
     * @param candidateSource
     * @param loggedInUser
     */
    @Override
    public void createJcmCandidateSourceHistory(Long jcmId, String candidateSource, User loggedInUser) {
        log.info("Saving Candidate Source History for jcm: {} and source: {}", jcmId, candidateSource);
        JcmCandidateSourceHistory jcmCandidateSourceHistory = new JcmCandidateSourceHistory();
        jcmCandidateSourceHistory.setJobCandidateMappingId(jcmId);
        jcmCandidateSourceHistory.setCandidateSource(candidateSource);
        jcmCandidateSourceHistory.setCreatedBy(loggedInUser);
        jcmCandidateSourceHistoryRepository.save(jcmCandidateSourceHistory);
    }
}
