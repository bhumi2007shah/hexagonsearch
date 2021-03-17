/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 3/2/20
 * Time : 2:05 PM
 * Class Name : IAsyncServicesWrapper
 * Project Name : server
 */
public interface IAsyncServicesWrapper {

    /**
     * Wrapper method to asynchronously add candidates from a file in one of the supported formats
     *
     * @param multipartFile the file with candidate information
     * @param jobId the job for which the candidates have to be added
     * @param fileFormat the format of file, for e.g. Naukri, LB format
     * @return the status of upload operation
     * @throws Exception
     */
    void uploadCandidatesFromFile(MultipartFile multipartFile, Long jobId, String fileFormat) throws Exception;


    /**
     * Wrapper method to asynchronously invite candidates to fill chatbot for a job
     *
     * @param jcmList list of jcm ids for chatbot invitation
     * @throws Exception
     */
    void inviteCandidates(List<Long> jcmList) throws Exception;
}
