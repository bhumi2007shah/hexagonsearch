/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author : sameer
 * Date : 02/03/21
 * Time : 3:59 PM
 * Class Name : CompanyFtpDetail
 * Project Name : server
 */
@Entity
@Data
@Table(name = "COMPANY_FTP_DETAILS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
public class CompanyFtpDetails implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COMPANY_ID")
    private Long companyId;

    @Column(name="HOST")
    private String host;

    @Column(name="USERNAME")
    private String userName;

    @Column(name="PASSWORD")
    private String password;

    @Column(name="PORT")
    private String port;

    @Column(name="REMOTE_FILE_UPLOAD_PATH")
    private String remoteFileUploadPath;

    @Column(name="REMOTE_FILE_DOWNLOAD_PATH")
    private String remoteFileDownloadPath;

    @Column(name="REMOTE_FILE_PROCESSED_PATH")
    private String remoteFileProcessedPath;

    public CompanyFtpDetails(
            Long companyId,
            String encryptedHost,
            String encrypterUser,
            String encrypterPass,
            String encryptedPort,
            String remoteFileDownloadPath,
            String remoteFileProcessedPath
    ) {
        this.companyId = companyId;
        this.userName = encrypterUser;
        this.password = encrypterPass;
        this.host = encryptedHost;
        this.port = encryptedPort;
        this.remoteFileDownloadPath = remoteFileDownloadPath;
        this.remoteFileProcessedPath = remoteFileProcessedPath;
    }
}
