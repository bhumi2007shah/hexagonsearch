/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

/**
 * @author : sameer
 * Date : 03/03/21
 * Time : 4:27 PM
 * Class Name : IFtpService
 * Project Name : server
 */
public interface IFtpService {
    /**
     * Service to fetch candidate xml for all companies having FTP details
     */
    public void fetchCandidateXmlFiles();
}
