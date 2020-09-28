/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.CandidateRequestBean;
import io.litmusblox.server.service.ICandidateService;
import io.litmusblox.server.service.ISearchEngineService;
import io.litmusblox.server.utils.RestClient;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Autowired
    ISearchEngineService searchEngineService;

    @Value("${searchEngineBaseUrl}")
    String searchEngineBaseUrl;

    @Value("${searchEngineAddCandidateSuffix}")
    String searchEngineAddCandidateSuffix;


    @Value("${searchEngineAddCandidateBulkSuffix}")
    String searchEngineAddCandidateBulkSuffix;

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
    public Candidate findByMobileOrEmail(Set<String> email, Set<String> mobile, String countryCode, User loggedInUser, Optional<String> alternateMobile) throws Exception {
        log.info("Inside findByMobileOrEmail method");
        //check if candidate exists for email

        List<CandidateEmailHistory> candidateEmailHistory = candidateEmailHistoryRepository.findByEmailIn(email);

        if(alternateMobile.isPresent() && !mobile.contains(alternateMobile.get()))
            mobile.add(alternateMobile.get());

        //check if candidate exists for mobile
        List<CandidateMobileHistory> candidateMobileHistory = new ArrayList<>();
        if  (null != mobile && !mobile.isEmpty())
            candidateMobileHistory = candidateMobileHistoryRepository.findByCountryCodeAndMobileIn(countryCode, mobile);

        log.info("Candidate Email History Size: {}", candidateEmailHistory.size());
        log.info("Candidate Mobile History Size: {}", candidateMobileHistory.size());

        if(candidateEmailHistory.size()==0 && candidateMobileHistory.size()==0)
            return null;

        Long dupCandidateId;
        if(candidateEmailHistory.size() > 0)
            dupCandidateId = candidateEmailHistory.get(0).getCandidate().getId();
        else
            dupCandidateId = candidateMobileHistory.get(0).getCandidate().getId();
        log.info("First Id based to check for duplicate: {}", dupCandidateId);
        candidateEmailHistory.forEach( (candidate) -> {
            log.info("Candidate ID Check for Duplicate Email: {}", candidate.getCandidate().getId());
            if (!dupCandidateId.equals(candidate.getCandidate().getId()))
                throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY, HttpStatus.BAD_REQUEST);
        });
        candidateMobileHistory.forEach( (candidate) -> {
            log.info("Candidate ID Check for Duplicate Mobile: {}", candidate.getCandidate().getId());
            if (!dupCandidateId.equals(candidate.getCandidate().getId()))
                throw new ValidationException(IErrorMessages.CANDIDATE_ID_MISMATCH_FROM_HISTORY, HttpStatus.BAD_REQUEST);
        });

        Candidate candidate;
        if(candidateEmailHistory.size() > 0)
            candidate = candidateEmailHistory.get(0).getCandidate();
        else
            candidate = candidateMobileHistory.get(0).getCandidate();
        email.forEach( (emailToAdd) -> {
            if(!isEmailExist(emailToAdd, candidate) && candidateEmailHistoryRepository.findByEmail(emailToAdd) == null) {
                log.info("Inside findMobileAndEmail - saving email {} to existing candidate id {}", emailToAdd, candidate.getId());
                candidateEmailHistoryRepository.save(new CandidateEmailHistory(candidate, emailToAdd, new Date(), loggedInUser));
            }
        });
        mobile.forEach( (mobileToAdd) -> {
            if(candidateMobileHistoryRepository.findByMobileAndCountryCode(mobileToAdd, countryCode) == null) {
                log.info("Inside findMobileAndEmail - saving email {} to existing candidate id {}", mobileToAdd, candidate.getId());
                candidateMobileHistoryRepository.save(new CandidateMobileHistory(candidate, mobileToAdd, countryCode, new Date(), loggedInUser));
            }
        });

        return candidate;

    }

    private boolean isEmailExist(String email, Candidate candidate){
        log.info("Inside isEmailExist");
        List<CandidateEmailHistory> candidateEmailHistoryFormDb;
        if(email.contains("@notavailable.io")){
            candidateEmailHistoryFormDb = candidateEmailHistoryRepository.findByCandidateIdOrderByIdDesc(candidate.getId());
            if(candidateEmailHistoryFormDb.size()>0){
                candidate.setEmail(candidateEmailHistoryFormDb.get(0).getEmail());
                return true;
            }
        }
        return false;
    }

    public Candidate findByProfileTypeAndUniqueId(List<CandidateOnlineProfile> candidateOnlineProfiles) throws Exception{
        //extract uniqueId for linkedIn and searching for candidate with this profile in DB.
        Candidate candidateFromDB = null;
        Optional<CandidateOnlineProfile> profileToSearch = candidateOnlineProfiles.stream()
                .filter(candidateOnlineProfile -> candidateOnlineProfile.getProfileType().equalsIgnoreCase(IConstant.CandidateSource.LinkedIn.toString().toLowerCase()))
                .findAny();
        if(profileToSearch.isPresent()){
            Pattern pattern = Pattern.compile(IConstant.REGEX_TO_FIND_ONLINE_PROFILE_UNIQUE_ID);
            Matcher matcher = pattern.matcher(profileToSearch.get().getUrl());
            if(matcher.find()) {
                candidateFromDB = candidateRepository.findCandidateByProfileTypeAndUniqueId(
                        IConstant.CandidateSource.LinkedIn.toString().toLowerCase(), matcher.group().contains("/")?matcher.group().substring(0, matcher.group().length()):matcher.group()
                );
            }
        }
        return candidateFromDB;
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
    public Candidate createCandidate(String firstName, String lastName, Set<String> email, Set<String> mobile, String countryCode, User loggedInUser, Optional<String> alternateMobile) throws Exception {

        log.info("Inside createCandidate method - create candidate, emailHistory, mobileHistory");

        if(mobile.size()>0 && alternateMobile.isPresent() && !mobile.contains(alternateMobile.get()))
            mobile.add(alternateMobile.get());

        Candidate candidate;
        if(mobile.size()>0)
            candidate = candidateRepository.save(new Candidate(firstName, lastName, email.stream().findFirst().get(), mobile.stream().findFirst().get(), countryCode, new Date(), loggedInUser));
        else
            candidate = candidateRepository.save(new Candidate(firstName, lastName, email.stream().findFirst().get(), null, countryCode, new Date(), loggedInUser));

        email.forEach((emailToAdd) -> {
            candidateEmailHistoryRepository.save(new CandidateEmailHistory(candidate, emailToAdd, new Date(), loggedInUser));
        });

        mobile.forEach((mobileToAdd) -> {
            candidateMobileHistoryRepository.save(new CandidateMobileHistory(candidate, mobileToAdd, countryCode, new Date(), loggedInUser));
        });
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

            if (!Util.isNull(obj.getSpecialization()) && obj.getSpecialization().length() > IConstant.MAX_FIELD_LENGTHS.SPECIALIZATION.getValue()){
                obj.setSpecialization(Util.truncateField(candidate, IConstant.MAX_FIELD_LENGTHS.SPECIALIZATION.name(), IConstant.MAX_FIELD_LENGTHS.SPECIALIZATION.getValue(), obj.getSpecialization()));
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

    /**
     * Method to call search engine to add a candidate.
     * @param candidate
     */
    public void createCandidateOnSearchEngine(Candidate candidate , Job job, String authToken) {
        log.info("inside create candidate on search engine for candidate {}, in job {}, for company {}.", candidate, job, job.getCompanyName());
        long startTime = System.currentTimeMillis();

        CandidateRequestBean candidateRequestBean = getCandidateRequestBean(candidate, job);

        // ObjectMapper object to convert candidateRequestBean to String
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userDetails = searchEngineService.getLoggedInUserInformation();

        log.info("Calling SearchEngine API to create candidate {} of job: {}", candidate.getId(), job.getId());
        try {
            RestClient.getInstance().consumeRestApi(objectMapper.writeValueAsString(candidateRequestBean), searchEngineBaseUrl + searchEngineAddCandidateSuffix, HttpMethod.POST, authToken, null, null, Optional.of(userDetails));
        }
        catch ( JsonProcessingException e ){
            log.error("Failed while converting candidateRequestBean to String. " + e.getMessage());
        }
        catch ( Exception e ){
            log.error("Failed to create candidate on search engine. " + e.getMessage());
        }

        log.info("Added candidate on search engine in {}ms for candidate {}, in job {}, for company {}.",System.currentTimeMillis()-startTime, candidate, job, job.getCompanyName());
    }
    //Method to truncate the value in the field and send out a sentry message for the same
    //move this truncateField method to Util class because it is use in other place also like CandidateCompanyDetails model

    /**
     * Method to call search engine to add a candidate.
     * @param candidates
     */
    public void createCandidatesOnSearchEngine(List<Candidate> candidates , Job job, String authToken) {
        log.info("inside create candidate on search engine.");
        long startTime = System.currentTimeMillis();

        List<CandidateRequestBean> candidateRequestBeans = new ArrayList<>(0);

        candidates.forEach(candidate -> candidateRequestBeans.add(getCandidateRequestBean(candidate, job)));


        // ObjectMapper object to convert candidateRequestBean to String
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userDetail = searchEngineService.getLoggedInUserInformation();

        log.info("Calling SearchEngine API to create candidates of job: {}", job.getId());
        try {
            List<List<CandidateRequestBean>> requestList= Lists.partition(candidateRequestBeans, 100);
            for (List<CandidateRequestBean> requestBeans : requestList) {
                RestClient.getInstance().consumeRestApi(objectMapper.writeValueAsString(requestBeans), searchEngineBaseUrl + searchEngineAddCandidateBulkSuffix, HttpMethod.POST, authToken, null, null, Optional.of(userDetail));
            }
        }
        catch ( JsonProcessingException e ){
            log.error("Failed while converting candidateRequestBean to String. " + e.getMessage());
        }
        catch ( Exception e ){
            log.error("Failed to create candidate on search engine. " + e.getMessage());
        }

        log.info("Added candidate on search engine in {}ms", System.currentTimeMillis()-startTime);
    }

    private CandidateRequestBean getCandidateRequestBean(Candidate candidate, Job job){
        // creating candidateRequestBean to be sent to Search Engine
        CandidateRequestBean candidateRequestBean = new CandidateRequestBean();
        candidateRequestBean.setCandidateId(candidate.getId());
        candidateRequestBean.setCandidateName(candidate.getFirstName()+" "+candidate.getLastName());
        candidateRequestBean.setCompanyId(job.getCompanyId().getId());

        // Creating and settign list of skill names from CandidateSkillDetails.
        if(null != candidate.getCandidateSkillDetails() && candidate.getCandidateSkillDetails().size()>0){
            candidateRequestBean.setSkill(
                    candidate.getCandidateSkillDetails()
                            .stream().parallel()
                            .map(CandidateSkillDetails::getSkill).collect(Collectors.toList())
            );
        }

        // Creating and setting singleton list from CandidateDetails as it is a string.
        if(null != candidate.getCandidateDetails() && null != candidate.getCandidateDetails().getLocation()){
            candidateRequestBean.setLocation(
                    Collections.singletonList(candidate.getCandidateDetails().getLocation())
            );
        }

        // Creating and setting noticePeriod from CandidateDetails after parsing it to int as data type
        // is String and search engine need an int value
        if(null != candidate.getCandidateCompanyDetails() && candidate.getCandidateCompanyDetails().size()>0 &&  (null != candidate.getCandidateCompanyDetails().get(0).getNoticePeriod() || null!= candidate.getCandidateCompanyDetails().get(0).getNoticePeriodInDb())){
            candidateRequestBean.setNoticePeriod(
                    null != candidate.getCandidateCompanyDetails().get(0).getNoticePeriod()?
                            Integer.parseInt(candidate.getCandidateCompanyDetails().get(0).getNoticePeriod()):
                            Integer.parseInt(candidate.getCandidateCompanyDetails().get(0).getNoticePeriodInDb().getValue().replaceAll("\\D+",""))
            );
        }

        // extracting value of experience range from job in which candidate is sourced
        if(null != candidate.getCandidateDetails() && null != candidate.getCandidateDetails().getTotalExperience()){
            candidateRequestBean.setExperience(candidate.getCandidateDetails().getTotalExperience());
        }

        // Creating and setting list of qualification i.e: degree from CandidateEducationDetail present
        // in master data as searchEngine need list of degrees of a candidate
        if(null != candidate.getCandidateEducationDetails() && candidate.getCandidateEducationDetails().size()>0){
            candidateRequestBean.setQualification(candidate.getCandidateEducationDetails()
                    .stream().map(
                            CandidateEducationDetails::getDegree).collect(Collectors.toList()
                    )
            );
        }

        return candidateRequestBean;
    }
}
