/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : sameer
 * Date : 04/02/21
 * Time : 10:07 AM
 * Class Name : IFileService
 * Project Name : server
 */
public interface IFileService {
    ResponseEntity<Resource> convertToFile(MultipartFile multipartFile) throws Exception;
}
