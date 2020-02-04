/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author : Shital Raval
 * Date : 3/2/20
 * Time : 2:05 PM
 * Class Name : IAsyncServicesWrapper
 * Project Name : server
 */
public interface IAsyncServicesWrapper {

    /**
     * Service method to add candidates from a file in one of the supported formats
     *
     * @param multipartFile the file with candidate information
     * @param jobId the job for which the candidates have to be added
     * @param fileFormat the format of file, for e.g. Naukri, LB format
     * @return the status of upload operation
     * @throws Exception
     */
    void uploadCandidatesFromFile(MultipartFile multipartFile, Long jobId, String fileFormat) throws Exception;

}
