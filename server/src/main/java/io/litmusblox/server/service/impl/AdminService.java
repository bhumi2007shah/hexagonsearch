/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.FtpRequestBean;
import io.litmusblox.server.service.IAdminService;
import io.litmusblox.server.utils.AESEncryptorDecryptor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : sameer
 * Date : 23/04/20
 * Time : 11:50 PM
 * Class Name : AdminService
 * Project Name : server
 */
@Service
@Log4j2
public class AdminService implements IAdminService {

    @Resource
    CompanyRepository companyRepository;

    @Autowired
    CompanyService companyService;

    @Autowired
    CandidateService candidateService;

    @Autowired
    LbUserDetailsService lbUserDetailsService;

    @Resource
    JobRepository jobRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    UserRepository userRepository;

    @Resource
    JcmProfileSharingDetailsRepository jcmProfileSharingDetailsRepository;

    @Resource
    CompanyFtpDetailsRepository companyFtpDetailsRepository;

    /**
     * service method to call search engine to add candidates and companies.
     * @throws Exception
     */
    public void addCompanyCandidateOnSearchEngine() throws Exception {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authToken = JwtTokenUtil.getAuthToken();
        log.info("Received request to add companies and candidates ono search engine from user {}", loggedInUser.getEmail());
        long startTime = System.currentTimeMillis();

        //Fetch all companies.
        List<Company> companyList = companyRepository.findAll();
        if(companyList.size()>0){
            companyList.parallelStream().forEach(company -> {
                // Calling methos to add company on search engine
                companyService.addCompanyOnSearchEngine(company, authToken);

                //Find all jobs for company
                List<Job> allJobs = jobRepository.findByCompanyIdIn(Collections.singletonList(company));

                if(allJobs.size()>0) {
                    allJobs.stream().parallel().forEach(job -> {
                        List<JobCandidateMapping> jobCandidateMappings = jobCandidateMappingRepository.findAllByJobId(job.getId());
                        if(null != jobCandidateMappings && jobCandidateMappings.size()>0) {
                            List<Candidate> candidates = jobCandidateMappings.stream().map(JobCandidateMapping::getCandidate).collect(Collectors.toList());
                            candidateService.createCandidatesOnSearchEngine(candidates, job, authToken);
                        }
                    });
                }
            });
        }
        log.info("Finished adding companies on search engine in {}ms", System.currentTimeMillis()-startTime);
    }

    /**
     * service method to call search engine to add company and associated candidates.
     * @throws Exception
     */
    public void addCompanyCandidateOnSearchEngine(Long companyId) throws Exception {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authToken = JwtTokenUtil.getAuthToken();
        log.info("Received request to add company {}, and candidates on search engine from user {}", companyId, loggedInUser.getEmail());
        long startTime = System.currentTimeMillis();

        //Fetch all companies.
        Company company = companyRepository.getOne(companyId);

        if(null != company){
            if(company.getActive()) {
                // Calling methos to add company on search engine
                companyService.addCompanyOnSearchEngine(company, authToken);

                //Find all jobs for company
                List<Job> allJobs = jobRepository.findByCompanyIdIn(Collections.singletonList(company));

                // search jcms for each job and make search engine call to add candidates
                if (allJobs.size() > 0) {
                    allJobs.forEach(job -> {
                        List<JobCandidateMapping> jobCandidateMappings = jobCandidateMappingRepository.findAllByJobId(job.getId());
                        if (null != jobCandidateMappings && jobCandidateMappings.size() > 0) {
                            List<Candidate> candidates = jobCandidateMappings.stream().map(JobCandidateMapping::getCandidate).collect(Collectors.toList());
                            candidateService.createCandidatesOnSearchEngine(candidates, job, authToken);
                        }
                    });
                }
            }
            else{
                log.error("company {} is not active", companyId);
            }
        }
        else{
            log.error("company not found {}", companyId);
        }
        log.info("Finished adding company and candidates on search engine in {}ms", System.currentTimeMillis()-startTime);
    }

    /**
     * Service method to add ftp details to a company
     *
     * @param ftpRequestBean
     * @throws Exception
     */
    public void addCompanyFtpDetails(FtpRequestBean ftpRequestBean) throws Exception {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long startTime = System.currentTimeMillis();
        log.info("Received request to update company with tp details by {}", loggedInUser.getEmail());

        Company company = companyRepository.getOne(ftpRequestBean.getCompanyId());

        if(null == company){
            throw new WebException("No company found with id:"+ftpRequestBean.getCompanyId(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(null == company.getEKey()){
            company.setEKey(AESEncryptorDecryptor.generateKey(IConstant.encryptionSize));
            companyRepository.save(company);
        }

        String encryptedHost = null;
        String encryptedUser = null;
        String encryptedPass = null;
        String encryptedPort = null;
        String encryptedRemoteFileDownloadPath = null;
        String encryptedRemoteFileProcessedPath = null;
        String encryptedRemoteFileUploadPath = null;

        SecretKey secretKey = new SecretKeySpec(company.getEKey(), IConstant.algorithmType);

        try{
            encryptedHost = AESEncryptorDecryptor.encrypt(ftpRequestBean.getHost(), secretKey);
            encryptedUser = AESEncryptorDecryptor.encrypt(ftpRequestBean.getUsername(), secretKey);
            encryptedPass = AESEncryptorDecryptor.encrypt(ftpRequestBean.getPassword(), secretKey);
            encryptedPort = AESEncryptorDecryptor.encrypt(String.valueOf(ftpRequestBean.getPort()), secretKey);
            encryptedRemoteFileDownloadPath = AESEncryptorDecryptor.encrypt(ftpRequestBean.getRemoteFileDownloadPath(), secretKey);
            encryptedRemoteFileProcessedPath = AESEncryptorDecryptor.encrypt(ftpRequestBean.getRemoteFileProcessedPath(), secretKey);
            encryptedRemoteFileUploadPath = AESEncryptorDecryptor.encrypt(ftpRequestBean.getRemoteFileUploadPath(), secretKey);
        }catch (Exception e){
            log.error(e.getMessage(), e.getCause());
        }

        CompanyFtpDetails companyFtpDetailsFromDb = companyFtpDetailsRepository.findByCompanyId(ftpRequestBean.getCompanyId());

        if(null!=companyFtpDetailsFromDb){
            companyFtpDetailsFromDb.setHost(encryptedHost);
            companyFtpDetailsFromDb.setUserName(encryptedUser);
            companyFtpDetailsFromDb.setPassword(encryptedPass);
            companyFtpDetailsFromDb.setPort(encryptedPort);
            companyFtpDetailsFromDb.setRemoteFileDownloadPath(encryptedRemoteFileDownloadPath);
            companyFtpDetailsFromDb.setRemoteFileProcessedPath(encryptedRemoteFileProcessedPath);
            companyFtpDetailsFromDb.setRemoteFileUploadPath(encryptedRemoteFileUploadPath);

            companyFtpDetailsRepository.save(companyFtpDetailsFromDb);
        }else {
            companyFtpDetailsRepository.save(
                    new CompanyFtpDetails(
                            ftpRequestBean.getCompanyId(),
                            encryptedHost,
                            encryptedUser,
                            encryptedPass,
                            encryptedPort,
                            encryptedRemoteFileDownloadPath,
                            encryptedRemoteFileProcessedPath,
                            encryptedRemoteFileUploadPath
                    )
            );
        }

        log.info("Completed updating FTP details for company {} by user:{} in {}ms", ftpRequestBean.getCompanyId(), loggedInUser.getEmail(), System.currentTimeMillis()-startTime);
    }
}
