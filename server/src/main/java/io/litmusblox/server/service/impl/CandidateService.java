/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.ICandidateService;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Candidate related operations
 *
 * @author : Shital Raval
 * Date : 24/7/19
 * Time : 6:10 PM
 * Class Name : CandidateService
 * Project Name : server
 */
@Log4j2
@Service
public class CandidateService implements ICandidateService {

    @Resource
    CandidateEmailHistoryRepository candidateEmailHistoryRepository;

    @Resource
    CandidateMobileHistoryRepository candidateMobileHistoryRepository;

    @Resource
    CandidateRepository candidateRepository;

    @Resource
    CandidateDetailsRepository candidateDetailsRepository;

    @Resource
    CandidateEducationDetailsRepository candidateEducationDetailsRepository;

    @Resource
    CandidateProjectDetailsRepository candidateProjectDetailsRepository;

    @Resource
    CandidateOnlineProfilesRepository candidateOnlineProfilesRepository;

    @Resource
    CandidateLanguageProficiencyRepository candidateLanguageProficiencyRepository;

    @Resource
    CandidateWorkAuthorizationRepository candidateWorkAuthorizationRepository;

    @Resource
    CandidateSkillDetailsRepository candidateSkillDetailsRepository;

    @Resource
    CandidateCompanyDetailsRepository candidateCompanyDetailsRepository;

    /**
     * Method to find a candidate using email or mobile number + country code
     *
     * @param email       the email of the candidate
     * @param mobile      the mobile number of the candidate
     * @param countryCode the country code for the mobile
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Candidate findByMobileOrEmail(String email, String mobile, String countryCode, User loggedInUser, Optional<String> alternateMobile) throws Exception {
        log.info("Inside findByMobileOrEmail method");
        //check if candidate exists for email
        Candidate dupCandidateByEmail = null;
        CandidateEmailHistory candidateEmailHistory = candidateEmailHistoryRepository.findByEmail(email);
        if (null != candidateEmailHistory)
            dupCandidateByEmail = candidateEmailHistory.getCandidate();

        //check if candidate exists for mobile
        Candidate dupCandidateByMobile = null;
        Candidate dupCandidateByAlternateMobile = null;
        boolean isAlternateMobilePresentInDb = false;
        if (Util.isNotNull(mobile)) {
            CandidateMobileHistory candidateMobileHistory = candidateMobileHistoryRepository.findByMobileAndCountryCode(mobile, countryCode);
            if (null != candidateMobileHistory)
                dupCandidateByMobile = candidateMobileHistory.getCandidate();
        }
        if (alternateMobile.isPresent()) {
            CandidateMobileHistory candidateMobileHistory = candidateMobileHistoryRepository.findByMobileAndCountryCode(alternateMobile.get(), countryCode);
            if (null != candidateMobileHistory) {
                if (dupCandidateByMobile == null)
                    dupCandidateByAlternateMobile = candidateMobileHistory.getCandidate();
                isAlternateMobilePresentInDb = true;
            }
        }

        if (null != dupCandidateByEmail) {
            //found different candidate ids for the email and mobile number combination
            if (null != dupCandidateByMobile && !dupCandidateByEmail.getId().equals(dupCandidateByMobile.getId()))
                throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY + mobile + " " + email, HttpStatus.BAD_REQUEST);
            else if(null != dupCandidateByAlternateMobile && !dupCandidateByEmail.getId().equals(dupCandidateByAlternateMobile.getId()))
                throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY + (alternateMobile.isPresent()?alternateMobile.get():null) + " " + email, HttpStatus.BAD_REQUEST);

        }

        if (null == dupCandidateByEmail) {
            if (null != dupCandidateByMobile) {
                //Candidate by mobile exists, add email history
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(dupCandidateByMobile, email, new Date(), loggedInUser));
                return dupCandidateByMobile;
            } else if (null != dupCandidateByAlternateMobile) {
                //Candidate by Alternate mobile exists, add email history
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(dupCandidateByAlternateMobile, email, new Date(), loggedInUser));
                return dupCandidateByAlternateMobile;
            }

        }
        if (null != dupCandidateByEmail) {

            if (null == dupCandidateByMobile && Util.isNotNull(mobile)) {
                //Candidate by email exists, add mobile history
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(dupCandidateByEmail, mobile, countryCode, new Date(), loggedInUser));
            }
            if (!isAlternateMobilePresentInDb && alternateMobile.isPresent()) {
                //Candidate by email exists, add alternate mobile history
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(dupCandidateByEmail, alternateMobile.get(), countryCode, new Date(), loggedInUser));
            }
            return dupCandidateByEmail;
        }
        return dupCandidateByEmail;
    }

    /**
     * Method to create a new candidate, candidateEmailHistory and candidateMobileHistory
     *
     * @param firstName    first name of candidate
     * @param lastName     last name of candidate
     * @param email        email of candidate
     * @param mobile       mobile number of candidate
     * @param countryCode  country code of candidate
     * @param loggedInUser
     * @return
     */
    @Override
    public Candidate createCandidate(String firstName, String lastName, String email, String mobile, String countryCode, User loggedInUser, Optional<String> alternateMobile) throws Exception {

        log.info("Inside createCandidate method - create candidate, emailHistory, mobileHistory");
        Candidate candidate = candidateRepository.save(new Candidate(firstName, lastName, email, mobile, countryCode, new Date(), loggedInUser));
        candidateEmailHistoryRepository.save(new CandidateEmailHistory(candidate, email, new Date(), loggedInUser));
        candidateMobileHistoryRepository.save(new CandidateMobileHistory(candidate, mobile, countryCode, new Date(), loggedInUser));
        if(alternateMobile.isPresent())
            candidateMobileHistoryRepository.save(new CandidateMobileHistory(candidate, alternateMobile.get(), countryCode, new Date(), loggedInUser));
        return candidate;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CandidateDetails saveUpdateCandidateDetails(CandidateDetails candidateDetails, Candidate candidate){
        log.info("Inside saveUpdateCandidateDetails method");
        //delete from CandidateDetails

        candidateDetailsRepository.deleteByCandidateId(candidate);

        if(!Util.isNull(candidateDetails.getCurrentAddress()) && candidateDetails.getCurrentAddress().length() > IConstant.MAX_FIELD_LENGTHS.ADDRESS.getValue()) {
            candidateDetails.setCurrentAddress(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.ADDRESS.name(), IConstant.MAX_FIELD_LENGTHS.ADDRESS.getValue(), candidateDetails.getCurrentAddress()));
        }
        if(!Util.isNull(candidateDetails.getKeySkills()) && candidateDetails.getKeySkills().length() > IConstant.MAX_FIELD_LENGTHS.KEY_SKILLS.getValue()) {
            candidateDetails.setKeySkills(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.KEY_SKILLS.name(), IConstant.MAX_FIELD_LENGTHS.KEY_SKILLS.getValue(), candidateDetails.getKeySkills()));
        }
        if(!Util.isNull(candidateDetails.getWorkSummary()) && candidateDetails.getWorkSummary().length() > IConstant.MAX_FIELD_LENGTHS.WORK_SUMMARY.getValue()) {
            candidateDetails.setWorkSummary(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.WORK_SUMMARY.name(), IConstant.MAX_FIELD_LENGTHS.WORK_SUMMARY.getValue(), candidateDetails.getWorkSummary()));
        }
        if(!Util.isNull(candidateDetails.getGender()) && candidateDetails.getGender().length() > IConstant.MAX_FIELD_LENGTHS.GENDER.getValue()) {
            candidateDetails.setGender(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.GENDER.name(), IConstant.MAX_FIELD_LENGTHS.GENDER.getValue(), candidateDetails.getGender()).toUpperCase());
        }
        if(!Util.isNull(candidateDetails.getRole()) && candidateDetails.getRole().length() > IConstant.MAX_FIELD_LENGTHS.ROLE.getValue()) {
            candidateDetails.setRole(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.ROLE.name(), IConstant.MAX_FIELD_LENGTHS.ROLE.getValue(), candidateDetails.getRole()).toUpperCase());
        }

        candidateDetails.setCandidateId(candidate);
        candidateDetails =candidateDetailsRepository.save(candidateDetails);
        log.info("Candidate Details created candidateDetailsId : "+candidateDetails.getId());
        return candidateDetails;
    }

    @Override
    @Transactional
    public void saveUpdateCandidateEducationDetails(List<CandidateEducationDetails> candidateEducationDetails, Candidate candidate) throws Exception {
        log.info("Inside saveUpdateCandidateEducationDetails method");
        //delete existing records
        candidateEducationDetailsRepository.deleteByCandidateId(candidate.getId());
        //insert new ones
        candidateEducationDetails.forEach(obj -> {
            //check if institute name is more than 75 characters
            if (!Util.isNull(obj.getInstituteName()) && obj.getInstituteName().length() > IConstant.MAX_FIELD_LENGTHS.INSTITUTE_NAME.getValue()){
                obj.setInstituteName(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.INSTITUTE_NAME.name(), IConstant.MAX_FIELD_LENGTHS.INSTITUTE_NAME.getValue(), obj.getInstituteName()));
            }
            if (!Util.isNull(obj.getDegree()) && obj.getDegree().length() > IConstant.MAX_FIELD_LENGTHS.DEGREE.getValue()){
                obj.setDegree(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.DEGREE.name(), IConstant.MAX_FIELD_LENGTHS.DEGREE.getValue(), obj.getDegree()));
            }

            try{
                int yearOfPassing = Integer.parseInt(obj.getYearOfPassing());
            }catch (Exception e){
                log.error("Year of passing contain character value - "+ obj.getYearOfPassing());
                obj.setYearOfPassing(null);
            }

            if(!Util.isNull(obj.getYearOfPassing()) && obj.getYearOfPassing().length() > IConstant.MAX_FIELD_LENGTHS.YEAR_OF_PASSING.getValue()){
                obj.setYearOfPassing(Util.truncateField(candidate, IConstant.YEAR_OF_PASSING,IConstant.MAX_FIELD_LENGTHS.YEAR_OF_PASSING.getValue(), obj.getYearOfPassing()));
            }

            obj.setCandidateId(candidate.getId());
            candidateEducationDetailsRepository.save(obj);});
    }

    @Transactional
    @Override
    public void saveUpdateCandidateProjectDetails(List<CandidateProjectDetails> candidateProjectDetails, Candidate candidate) throws Exception {
        log.info("Inside saveUpdateCandidateProjectDetails method");
        //delete existing records
        candidateProjectDetailsRepository.deleteByCandidateId(candidate.getId());
        //insert new ones
        candidateProjectDetails.forEach(obj -> {
            if(!Util.isNull(obj.getCompanyName()) && obj.getCompanyName().length() > IConstant.MAX_FIELD_LENGTHS.COMPANY_NAME.getValue()) {
                obj.setCompanyName(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.COMPANY_NAME.name(), IConstant.MAX_FIELD_LENGTHS.COMPANY_NAME.getValue(), obj.getCompanyName()));
            }
            if(!Util.isNull(obj.getCompanyName()) && obj.getCompanyName().length() > IConstant.MAX_FIELD_LENGTHS.ROLE.getValue()) {
                obj.setRole(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.ROLE.name(), IConstant.MAX_FIELD_LENGTHS.ROLE.getValue(), obj.getCompanyName()));
            }
            obj.setCandidateId(candidate.getId());candidateProjectDetailsRepository.save(obj);});
    }

    @Transactional
    @Override
    public void saveUpdateCandidateOnlineProfile(List<CandidateOnlineProfile> candidateOnlineProfiles, Candidate candidate) throws Exception {
        log.info("Inside saveUpdateCandidateOnlineProfile method");
        //delete existing records
        candidateOnlineProfilesRepository.deleteByCandidateId(candidate.getId());
        //insert new ones
        candidateOnlineProfiles.forEach(obj -> {
            if(!Util.isNull(obj.getProfileType()) && obj.getProfileType().length() > IConstant.MAX_FIELD_LENGTHS.ONLINE_PROFILE_TYPE.getValue()) {
                obj.setProfileType(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.ONLINE_PROFILE_TYPE.name(), IConstant.MAX_FIELD_LENGTHS.ONLINE_PROFILE_TYPE.getValue(), obj.getProfileType()));
            }

            if(!Util.isNull(obj.getUrl()) && obj.getUrl().length() > IConstant.MAX_FIELD_LENGTHS.ONLINE_PROFILE_URL.getValue()) {
                obj.setUrl(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.ONLINE_PROFILE_URL.name(), IConstant.MAX_FIELD_LENGTHS.ONLINE_PROFILE_URL.getValue(), obj.getUrl()));
            }
            obj.setCandidateId(candidate.getId());candidateOnlineProfilesRepository.save(obj);});
    }

    @Transactional
    @Override
    public void saveUpdateCandidateLanguageProficiency(List<CandidateLanguageProficiency> candidateLanguageProficiencies, Long candidateId) throws Exception {
        log.info("Inside saveUpdateCandidateLanguageProficiency method");
        //delete existing records
        candidateLanguageProficiencyRepository.deleteByCandidateId(candidateId);
        //insert new ones
        candidateLanguageProficiencies.forEach(obj -> {obj.setCandidateId(candidateId);candidateLanguageProficiencyRepository.save(obj);});
    }

    @Transactional
    public void saveUpdateCandidateWorkAuthorization(List<CandidateWorkAuthorization> candidateWorkAuthorizations, Long candidateId) throws Exception {
        log.info("Inside saveUpdateCandidateWorkAuthorization method");
        //delete existing records
        candidateWorkAuthorizationRepository.deleteByCandidateId(candidateId);
        //insert new ones
        candidateWorkAuthorizations.forEach(obj -> {obj.setCandidateId(candidateId);candidateWorkAuthorizationRepository.save(obj);});
    }

    @Transactional
    public void saveUpdateCandidateSkillDetails(List<CandidateSkillDetails> candidateSkillDetails, Candidate candidate) throws Exception {
        log.info("Inside saveUpdateCandidateSkillDetails method");
        //delete existing records
        candidateSkillDetailsRepository.deleteByCandidateId(candidate.getId());
        //insert new ones
        candidateSkillDetails.forEach(obj -> {
            if(!Util.isNull(obj.getSkill()) && obj.getSkill().length() > IConstant.MAX_FIELD_LENGTHS.SKILL.getValue()) {
                obj.setSkill(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.SKILL.name(), IConstant.MAX_FIELD_LENGTHS.SKILL.getValue(), obj.getSkill()));
            }
            obj.setCandidateId(candidate.getId());candidateSkillDetailsRepository.save(obj);});

    }

    @Transactional
    public void saveUpdateCandidateCompanyDetails(List<CandidateCompanyDetails> candidateCompanyDetails, Candidate candidate) throws Exception {
        log.info("Inside saveUpdateCandidateCompanyDetails method");
        //delete existing records
        candidateCompanyDetailsRepository.deleteByCandidateId(candidate.getId());

        //insert new ones
        candidateCompanyDetails.forEach(obj -> {
            obj.setCandidateId(candidate.getId());
            //Check for company name
            if (!Util.isNull(obj.getCompanyName()) && obj.getCompanyName().length() > IConstant.MAX_FIELD_LENGTHS.COMPANY_NAME.getValue()) {
                //truncate institute name to max length
                obj.setCompanyName(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.COMPANY_NAME.name(), IConstant.MAX_FIELD_LENGTHS.COMPANY_NAME.getValue(), obj.getCompanyName()));
            }
            //check for designation
            if (!Util.isNull(obj.getDesignation()) && obj.getDesignation().length() > IConstant.MAX_FIELD_LENGTHS.DESIGNATION.getValue()) {
                obj.setDesignation(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.DESIGNATION.name(), IConstant.MAX_FIELD_LENGTHS.DESIGNATION.getValue(), obj.getDesignation()));
            }
            candidateCompanyDetailsRepository.save(obj);});
    }

    //Method to truncate the value in the field and send out a sentry message for the same
    //move this truncateField method to Util class because it is use in other place also like CandidateCompanyDetails model
}
