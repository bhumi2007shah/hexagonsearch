/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.CompanyFtpDetails;
import io.litmusblox.server.repository.CompanyFtpDetailsRepository;
import io.litmusblox.server.repository.CompanyRepository;
import io.litmusblox.server.service.IFtpService;
import io.litmusblox.server.utils.AESEncryptorDecryptor;
import io.litmusblox.server.utils.SFTPService;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * @author : sameer
 * Date : 03/03/21
 * Time : 4:25 PM
 * Class Name : FtpService
 * Project Name : server
 */
@Service
@Log4j2
public class FtpService implements IFtpService {

    @Autowired
    Environment environment;

    @Resource
    CompanyFtpDetailsRepository companyFtpDetailsRepository;

    @Resource
    CompanyRepository companyRepository;

    /**
     * Service to fetch candidate xml for all companies having FTP details
     */
    public void fetchCandidateXmlFiles() {
        String xmlDirectory = environment.getProperty(IConstant.TEMP_REPO_LOCATION)+"xmlData/";

        File directory = new File(xmlDirectory);
        if(!directory.exists()){
            directory.mkdirs();
        }

        // Fetch all companyFtpDetails from DB
        List<CompanyFtpDetails> companyFtpDetailsList = companyFtpDetailsRepository.findAll();

        if(companyFtpDetailsList.size() > 0) {
            companyFtpDetailsList.parallelStream().forEach(companyFtpDetails -> {
                Company company = companyRepository.getOne(companyFtpDetails.getCompanyId());

                SFTPService sftpService = null;

                String remoteFileDownloadPath = null;
                String remoteFileProcessedPath = null;

                try {
                    SecretKey secretKey = new SecretKeySpec(company.getEKey(), IConstant.algorithmType);
                    remoteFileDownloadPath = AESEncryptorDecryptor.decrypt(companyFtpDetails.getRemoteFileDownloadPath(), secretKey);
                    remoteFileProcessedPath = AESEncryptorDecryptor.decrypt(companyFtpDetails.getRemoteFileProcessedPath(), secretKey);
                    sftpService = new SFTPService(
                            AESEncryptorDecryptor.decrypt(companyFtpDetails.getHost(), secretKey),
                            AESEncryptorDecryptor.decrypt(companyFtpDetails.getUserName(), secretKey),
                            AESEncryptorDecryptor.decrypt(companyFtpDetails.getPassword(), secretKey),
                            Integer.parseInt(AESEncryptorDecryptor.decrypt(companyFtpDetails.getPort(), secretKey))
                    );
                }catch (Exception e){
                    log.error("Error while decrypting ftp values");
                }

                if(null != sftpService && null != remoteFileDownloadPath && null != remoteFileProcessedPath){
                    log.info("Connecting to FTP server of company {}", company.getCompanyName());
                    sftpService.connect();
                    log.info("Connected to FTP server of company {}", company.getCompanyName());
                    Vector<ChannelSftp.LsEntry> fileNames = null;
                    try {
                        log.info("Fetching List of files from download directory of company {}", company.getCompanyName());
                        if(null != sftpService.getChannelSftp()) {
                            fileNames = sftpService.getChannelSftp().ls(remoteFileDownloadPath);
                        }
                        log.info("{} files available to download", fileNames.size()-2);
                    } catch (SftpException e) {
                        log.error(e.getMessage(), e.getCause());
                    }
                    fileNames.remove(fileNames.firstElement());
                    fileNames.remove(fileNames.firstElement());
                    if(fileNames.size()>0){
                        SFTPService finalSftpService = sftpService;
                        String finalRemoteFileDownloadPath = remoteFileDownloadPath;
                        String finalRemoteFileProcessedPath = remoteFileProcessedPath;
                        fileNames.forEach(lsEntry -> {
                            String fileName = finalRemoteFileDownloadPath +lsEntry.getFilename();
                            StringBuffer saveFileName = new StringBuffer("");
                            saveFileName
                                    .append(xmlDirectory)
                                    .append(company.getShortName())
                                    .append("_")
                                    .append(lsEntry.getFilename().substring(0, lsEntry.getFilename().lastIndexOf(".")))
                                    .append("_")
                                    .append(new Date())
                                    .append(".")
                                    .append(Util.getFileExtension(lsEntry.getFilename()));
                            log.info("Downloading file {} from {}", fileName, company.getCompanyName());
                            long startTime = System.currentTimeMillis();
                            finalSftpService.downloadFile(fileName, xmlDirectory);
                            File downloadedFile = new File(xmlDirectory+lsEntry.getFilename());
                            if(downloadedFile.exists()){
                                downloadedFile.renameTo(new File(saveFileName.toString()));
                            }

                            log.info("Download file to {} in {}ms", xmlDirectory, System.currentTimeMillis()-startTime);
                            try{
                                log.info("Moving file {} to processed directory on remote server", fileName);
                                SftpATTRS attrs;
                                try {
                                    attrs = finalSftpService.getChannelSftp().stat(finalRemoteFileProcessedPath);
                                }catch (Exception e) {
                                    finalSftpService.getChannelSftp().mkdir(finalRemoteFileProcessedPath);
                                }
                                finalSftpService.getChannelSftp().rename(
                                        fileName,
                                        finalRemoteFileProcessedPath+lsEntry.getFilename()
                                );
                                log.info("Moved file {} to processed directory on remote server", fileName);
                            }catch (Exception e){
                                log.error(e.getMessage(), e.getCause());
                            }
                        });
                        sftpService.disconnect();
                    }
                }
            });
        }
    }
}
