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
     * Method that will be called by scheduler
     * All eligible records of CV will be run against CV rating api and rated
     */
    void rateCv();

    /**
     * Method to convert cv file to cv text
     * In cv_parsing_detail if parsing_response_text is null then convert cv to text and save
     */
    void CvToCvText();
}
