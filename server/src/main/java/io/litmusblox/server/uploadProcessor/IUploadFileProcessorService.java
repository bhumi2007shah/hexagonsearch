/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.UploadResponseBean;

import java.util.List;

/**
 * @author : Sumit
 * Date : 19/7/19
 * Time : 3:27 PM
 * Class Name : IUploadFileProcessorService
 * Project Name : server
 */
public interface IUploadFileProcessorService {

    List<Candidate> process(String fileName, UploadResponseBean responseBean, boolean ignoreMobile, String repoLocation, User loggedInUser);

}
