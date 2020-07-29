/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.service.impl.IndustryMasterDataRequestBean;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 2:14 PM
 * Class Name : IMasterDataService
 * Project Name : server
 */
public interface IMasterDataService {
    /**
     * Method that will be called during application startup
     * Will read all master data from database and store them in internal cache
     *
     * @throws Exception
     */
    void loadStaticMasterData() throws Exception;

    /**
     * Method that will reload all master data in memory
     *
     * @throws Exception
     */
    void reloadMasterData() throws Exception;

    /**
     * Method to fetch specific master data values from cache
     *
     * @param fetchItemList The items for which master data needs to be fetched from memory
     * @return response bean with Maps containing requested master data values
     * @throws Exception
     */
    MasterDataResponse fetchForItems(List<String> fetchItemList) throws Exception;

    /**
     * Method to add master data to database.
     * Supported master data types:
     * 1. UserScreeningQuestion
     *
     * @param jsonData master data to be persisted (in json format)
     * @param masterDataType the type of master data to be persisted
     */
    void addMasterData(String jsonData, String masterDataType) throws Exception;

    /**
     * Service to get masterData for only specific fields, which is used in noAuth call
     *
     * @param fetchItemList (jobType, referrerRelation)
     * @return MasterDataResponse
     * @throws Exception
     */
    MasterDataResponse fetchForItemsForNoAuth(List<String> fetchItemList) throws Exception;

    /**
     * Method to add missing Industry Master data to backend database.
     * API is called by SearchEngine.
     */
    void addIndustryMasterData(List<IndustryMasterDataRequestBean> industryMasterDataRequestBeanList);
}
