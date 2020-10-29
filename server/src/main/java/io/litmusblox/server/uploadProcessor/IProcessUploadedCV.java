/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

/**
 * Service interface to process candidate cv uploaded via drag and drop method
 * @author : Shital Raval
 * Date : 21/8/19
 * Time : 1:07 PM
 * Class Name : IProcessUploadedCV
 * Project Name : server
 */
public interface IProcessUploadedCV {
    /**
     * Method that will be called by scheduler
     * @throws Exception
     */
    void processCv();

    /**
     * Method to update cv rating for cv
     * In cv_parsing_detail if cv rating flag is false then get cv rating and save
     */
    void updateCvRating();
}
