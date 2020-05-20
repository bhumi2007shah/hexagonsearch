/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.JobCandidateMapping;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.CompanyRepository;
import io.litmusblox.server.repository.JobCandidateMappingRepository;
import io.litmusblox.server.repository.JobRepository;
import io.litmusblox.server.service.IAdminService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

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

    @Resource
    JobRepository jobRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    /**
     * service method to call search engine to add candidates and companies.
     * @throws Exception
     */
    public void addCompanyCandidateOnSearchEngine() throws Exception {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Received request to add companies and candidates ono search engine from user {}", loggedInUser.getEmail());
        long startTime = System.currentTimeMillis();

        //Fetch all companies.
        List<Company> companyList = companyRepository.findAll();
        if(companyList.size()>0){
            companyList.parallelStream().forEach(company -> {
                // Calling methos to add company on search engine
                companyService.addCompanyOnSearchEngine(company);

                //Find all jobs for company
                List<Job> allJobs = jobRepository.findByCompanyIdIn(Collections.singletonList(company));

                if(allJobs.size()>0) {
                    allJobs.stream().parallel().forEach(job -> {
                        List<JobCandidateMapping> jobCandidateMappings = jobCandidateMappingRepository.findAllByJobId(job.getId());
                        if(null != jobCandidateMappings && jobCandidateMappings.size()>0){
                            jobCandidateMappings.stream().parallel().forEach(jobCandidateMapping -> {
                                candidateService.createCandidateOnSearchEngine(jobCandidateMapping.getCandidate(), job);
                            });
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
        log.info("Received request to add company {}, and candidates on search engine from user {}", companyId, loggedInUser.getEmail());
        long startTime = System.currentTimeMillis();

        //Fetch all companies.
        Company company = companyRepository.getOne(companyId);

        if(null != company){
            if(company.getActive()) {
                // Calling methos to add company on search engine
                companyService.addCompanyOnSearchEngine(company);

                //Find all jobs for company
                List<Job> allJobs = jobRepository.findByCompanyIdIn(Collections.singletonList(company));

                // search jcms for each job and make search engine call to add candidates
                if (allJobs.size() > 0) {
                    allJobs.forEach(job -> {
                        List<JobCandidateMapping> jobCandidateMappings = jobCandidateMappingRepository.findAllByJobId(job.getId());
                        if (null != jobCandidateMappings && jobCandidateMappings.size() > 0) {
                            jobCandidateMappings.stream().parallel().forEach(jobCandidateMapping -> {
                                candidateService.createCandidateOnSearchEngine(jobCandidateMapping.getCandidate(), job);
                            });
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
}
