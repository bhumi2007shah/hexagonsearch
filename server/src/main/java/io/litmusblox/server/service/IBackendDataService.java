/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

/**
 * @author : Arpan R
 * Date : 24/11/2020
 * Time : 08:00 AM
 * Class Name : IBackendDataService
 * Project Name : server
 */
public interface IBackendDataService {

    /**
     * Method to asynchronously migrate candidates Cv Rating Data
     *
     * @throws Exception
     */

    void migrateCvRatingData() throws Exception;
}
