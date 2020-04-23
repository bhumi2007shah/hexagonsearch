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
     * Service method to call search engine to add company
     * @throws Exception
     */
    void addCompanyCandidateOnScoringEngine() throws Exception;
}
