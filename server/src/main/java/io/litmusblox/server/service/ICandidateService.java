/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Service class for operations on candidate
 *
 * @author : Shital Raval
 * Date : 24/7/19
 * Time : 6:09 PM
 * Class Name : ICandidateService
 * Project Name : server
 */
public interface ICandidateService {
    /**
     * Method to find a candidate using email or mobile number + country code
     * @param email the email of the candidate
     * @param mobile the mobile number of the candidate
     * @param countryCode the country code for the mobile
     * @param loggedInUser currently logged in user
     * @return
     * @throws Exception
     */
    Candidate findByMobileOrEmail(String email, String mobile, String countryCode, User loggedInUser, Optional<String> alternateMobile) throws Exception;

    /**
     * Method to find a candidate using profile type and uniqueId from that profile
     * @param candidateOnlineProfiles - list of online profiles of candidate
     * @return Candidate from Db
     * @throws Exception
     */
    Candidate findByProfileTypeAndUniqueId(List<CandidateOnlineProfile> candidateOnlineProfiles) throws Exception;

    /**
     * Method to create a new candidate, candidateEmailHistory and candidateMobileHistory
     *
     * @param firstName first name of candidate
     * @param lastName last name of candidate
     * @param email email of candidate
     * @param mobile mobile number of candidate
     * @param countryCode country code of candidate
     * @param loggedInUser
     * @return
     */
    Candidate createCandidate(String firstName, String lastName, String email, String mobile, String countryCode, User loggedInUser, Optional<String> alternateMobile) throws Exception;

    /**
     * Method to update candidate details
     *
     * @param candidateId candidate id
     * @param candidateDetails candidate details
     * @return CandidateDetails
     * @throws Exception
     */
    CandidateDetails saveUpdateCandidateDetails(CandidateDetails candidateDetails, Candidate candidateId) throws Exception;

    void saveUpdateCandidateEducationDetails(List<CandidateEducationDetails> candidateEducationDetails, Candidate candidate) throws Exception;

    void saveUpdateCandidateProjectDetails(List<CandidateProjectDetails> candidateProjectDetailsList, Candidate candidate) throws Exception;

    void saveUpdateCandidateOnlineProfile(List<CandidateOnlineProfile> candidateOnlineProfileList, Candidate candidate) throws Exception;

    void saveUpdateCandidateLanguageProficiency(List<CandidateLanguageProficiency> candidateLanguageProficiencyList, Long candidateId) throws Exception;

    void saveUpdateCandidateWorkAuthorization(List<CandidateWorkAuthorization> candidateWorkAuthorizations, Long candidateId) throws Exception;

    void saveUpdateCandidateSkillDetails(List<CandidateSkillDetails> candidateSkillDetails, Candidate candidate) throws Exception;

    void saveUpdateCandidateCompanyDetails(List<CandidateCompanyDetails> candidateCompanyDetailsList, Candidate candidate) throws Exception;

    /**
     * Method  to call search engine to add candidate
     * @param candidate
     */
    void createCandidateOnSearchEngine(Candidate candidate, Job job, String authToken);
}
