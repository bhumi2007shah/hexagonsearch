/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.IAdminService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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
    JcmProfileSharingMasterRepository jcmProfileSharingMasterRepository;

    @Resource
    JcmProfileSharingDetailsRepository jcmProfileSharingDetailsRepository;

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

    public void addHiringManagerAsUser() throws Exception{
        Long startTime = System.currentTimeMillis();
        log.info("Received request to migrate Hiring manager from jcm_candidate_profile_sharing_table to user_table");

        //List of all domains to be migrated
        ArrayList<List<String>> emailDomainsToBeIncluded = new ArrayList<List<String>>();
        emailDomainsToBeIncluded.add(Arrays.asList("@apisero.com", "63", "184"));
        emailDomainsToBeIncluded.add(Arrays.asList("@litmusblox.io", "3", "6"));
        emailDomainsToBeIncluded.add(Arrays.asList("@evolenthealth.com", "44", "119"));
        emailDomainsToBeIncluded.add(Arrays.asList("@krehsst.com", "16", "30"));
        emailDomainsToBeIncluded.add(Arrays.asList("@slb.com", "39", "104"));
        emailDomainsToBeIncluded.add(Arrays.asList("@accurategauging.com", "35", "90"));
        emailDomainsToBeIncluded.add(Arrays.asList("@bolddialogue.com", "4", "9"));
        emailDomainsToBeIncluded.add(Arrays.asList("@enzigma.com", "67", "190"));
        emailDomainsToBeIncluded.add(Arrays.asList("@hexagonsearch.com", "1", "42"));
        emailDomainsToBeIncluded.add(Arrays.asList("@hexagonselect.com", "14", "25"));
        emailDomainsToBeIncluded.add(Arrays.asList("@impauto.com", "45", "122"));
        emailDomainsToBeIncluded.add(Arrays.asList("@lntinfotech.com", "25", "61"));
        emailDomainsToBeIncluded.add(Arrays.asList("@mercer.com", "135", "433"));
        emailDomainsToBeIncluded.add(Arrays.asList("@mmc.com", "135", "433"));
        emailDomainsToBeIncluded.add(Arrays.asList("@nativeworld.com", "62", "182"));
        emailDomainsToBeIncluded.add(Arrays.asList("@persistent.com", "36", "196"));
        emailDomainsToBeIncluded.add(Arrays.asList("@sanjaytools.com", "15", "28"));
        emailDomainsToBeIncluded.add(Arrays.asList("@synechron.com", "26", "63"));
        emailDomainsToBeIncluded.add(Arrays.asList("@zycus.com", "56", "115"));


        emailDomainsToBeIncluded.forEach((domain) -> {
            //Fetch from jcm_profile_sharing_master for that domain;
            List<JcmProfileSharingMaster> hiringManagerList = jcmProfileSharingMasterRepository.findByReceiverEmailContainingIgnoreCase(domain.get(0));
            hiringManagerList.forEach((hiringManager) -> {
                //Check that if the email id already exist in the user table. If yes use the same id.
                User existingUser = userRepository.findByEmail(hiringManager.getReceiverEmail().toLowerCase());
                if(existingUser == null){

                    User newUser = new User();
                    String name [] = hiringManager.getReceiverName().split(" ");
                    newUser.setFirstName(name[0]);
                    if (name.length > 1 && name[1].length()>0)
                        newUser.setLastName(name[1]);
                    else
                        newUser.setLastName("-");
                    newUser.setMobile("8149202289");
                    newUser.setEmail(hiringManager.getReceiverEmail());
                    newUser.setRole(IConstant.UserRole.Names.BUSINESS_USER);
                    Country country = new Country();
                    country.setId(3L);
                    newUser.setCountryId(country);

                    //Get company details for the new user
                    Company company = companyRepository.getOne(Long.valueOf(domain.get(1)));
                    newUser.setCompany(company);
                    newUser.setCreatedBy(Long.valueOf(domain.get(2)));

                    try {
                        existingUser = lbUserDetailsService.createUpdateUser(newUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                List<JcmProfileSharingDetails> jcmProfileSharingList = jcmProfileSharingDetailsRepository.findByProfileSharingMaster(hiringManager);
                User finalExistingUser = existingUser;
                jcmProfileSharingList.forEach((profile) -> {
                        profile.setUserId(finalExistingUser.getId());
                        profile.setEmailSentOn(hiringManager.getEmailSentOn());
                        profile.setSenderId(hiringManager.getSenderId());
                        jcmProfileSharingDetailsRepository.save(profile);
                });
            });
        });
        log.info("Completed migration in {} ms", System.currentTimeMillis() - startTime);
    }
}
