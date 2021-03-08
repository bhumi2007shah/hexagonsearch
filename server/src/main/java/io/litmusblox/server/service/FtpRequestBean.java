/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

/**
 * @author : sameer
 * Date : 02/03/21
 * Time : 4:58 PM
 * Class Name : FtpRequestBean
 * Project Name : server
 */
@Data
public class FtpRequestBean {
    private Long companyId;
    private String host;
    private String username;
    private String password;
    private String remoteFileDownloadPath;
    private String remoteFileProcessedPath;
    private int port;
}
