/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

/**
 * @author : sameer
 * Date : 23/04/20
 * Time : 11:48 PM
 * Class Name : IAdminService
 * Project Name : server
 */
public interface IAdminService {
    /**
     * Service method to call search engine to add companies and candidates
     * @throws Exception
     */
    void addCompanyCandidateOnScoringEngine() throws Exception;

    /**
     * Service method to call search engine to add company and associated candidates
     * @throws Exception
     */
    void addCompanyCandidateOnScoringEngine(Long companyId) throws Exception;
}
