/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.*;
import io.litmusblox.server.utils.*;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Service class to perform various operations on a company
 *
 * @author : Shital Raval
 * Date : 30/7/19
 * Time : 2:12 PM
 * Class Name : CompanyService
 * Project Name : server
 */
@Log4j2
@Service
public class CompanyService extends AbstractAccessControl implements ICompanyService {

    @Resource
    CompanyRepository companyRepository;

    @Resource
    UserRepository userRepository;

    @Autowired
    Environment environment;

    @Autowired
    ISearchEngineService searchEngineService;

    @Resource
    CompanyHistoryRepository companyHistoryRepository;

    @Resource
    CompanyBuRepository companyBuRepository;

    @Resource
    JobRepository jobRepository;

    @Resource
    CompanyAddressRepository companyAddressRepository;

    @Value("${subdomainTemplateName}")
    String subdomainTemplateName;

    @Value("${createSubdomainApi}")
    String createSubdomainApi;

    @Value("${createSubdomainKey}")
    String createSubdomainKey;

    @Value("${createSubdomainSecret}")
    String createSubdomainSecret;

    @Value("${createSubdomainIp}")
    String createSubdomainIp;

    @Value("${searchEngineBaseUrl}")
    String searchEngineBaseUrl;

    @Value("${searchEngineAddCompanyUrlSuffix}")
    String searchEngineAddCompanyUrlSuffix;

    /**
     * Service method to create a new company
     * @param company the company object to save
     * @param loggedInUser the user who created the company object
     * @return
     * @throws Exception
     */
    @Transactional
    public Company addCompany(Company company, User loggedInUser) throws Exception {
        company = generateAndSetCompanyUniqueId(company);
        companyRepository.save(company);
        saveCompanyHistory(company.getId(), loggedInUser.getDisplayName() + " created a new company " +company.getCompanyName(), loggedInUser);
        addCompanyOnSearchEngine(company, JwtTokenUtil.getAuthToken());
        return company;
    }

    //Update Company
    @Override
    public Company saveCompany(Company company, MultipartFile logo) throws Exception {
        User loggedInUser  = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Received request to update organisation detail from user: " + loggedInUser.getEmail());
        long startTime = System.currentTimeMillis();

        Company companyFromDb=companyRepository.findById(company.getId()).orElse(null);
        if(null==companyFromDb)
            throw new ValidationException("Company not found for this name "+company.getCompanyName(), HttpStatus.BAD_REQUEST);

        company.setId(companyFromDb.getId());
        company.setShortName(companyFromDb.getShortName());
        company.setCountryId(companyFromDb.getCountryId());
        company.setCompanyUniqueId(companyFromDb.getCompanyUniqueId());
        company.setCompanyType(companyFromDb.getCompanyType());
        company.setSubscription(companyFromDb.getSubscription());
        company.setCreatedOn(companyFromDb.getCreatedOn());
        company.setSubdomainCreated(companyFromDb.isSubdomainCreated());

        if(company.getNewCompanyBu()!=null || company.getDeletedCompanyBu()!=null) {
            updateBusinessUnit(company, loggedInUser);
        }
        else if(company.getNewCompanyAddress()!=null || company.getDeletedCompanyAddress()!=null || company.getUpdatedCompanyAddress()!=null) {
            updateCompanyAddresses(company, companyFromDb, loggedInUser);
        }
        else {
            updateCompany(company, companyFromDb, loggedInUser, logo);
        }
            /*case UsersAndTeams:
                updateUsersAndTeams(company, companyFromDb, loggedInUser);
                break;
            case ScreeningQuestions:
                updateScreeningQuestions(company, companyFromDb, loggedInUser);
                break;*/

        log.info("Completed processing request to update company in " + (System.currentTimeMillis() - startTime) + "ms");
        return company;
    }

    /**
     * @param companyFromDb
     * @param company
     * @param loggedInUser
     * @param logo
     */
    private void updateCompany(Company company, Company companyFromDb, User loggedInUser, MultipartFile logo) {

         /*
         * 14-10-2019
         * Logo is optional so removed exception handling.
        if(null==logo)
            throw new ValidationException("Company Logo " + IErrorMessages.NULL_MESSAGE+ company.getId(), HttpStatus.BAD_REQUEST);
        */

        if(null==company.getCompanyDescription() || company.getCompanyDescription().isEmpty()){
            throw new ValidationException("CompanyDescription " + IErrorMessages.EMPTY_AND_NULL_MESSAGE+ company.getId(), HttpStatus.BAD_REQUEST);
        }

        company = truncateField(company);

        //Store Company logo on repo and save its filepath in to the company logo field if logo in not null
        if(logo != null) {
            String fileName = null;
            try{
                fileName = StoreFileUtil.storeFile(logo, company.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.Logo.toString(), null, null);
            }
            catch (Exception e){
                log.info(Util.getStackTrace(e));
            }
            log.info("Company " + company.getCompanyName() + " uploaded " + fileName);
            company.setLogo(fileName);
        }


        if(null != companyFromDb) {
            company.setCreatedBy(companyFromDb.getCreatedBy());
            company.setCreatedOn(companyFromDb.getCreatedOn());
            company.setActive(companyFromDb.getActive());
            company.setSubscription(companyFromDb.getSubscription());
        }
        if(null == company.getIndustry().getId())
            company.setIndustry(null);
        //Update Company
        companyRepository.save(company);
        saveCompanyHistory(company.getId(), loggedInUser.getDisplayName()+" update company information for "+company.getCompanyName(), loggedInUser);
        log.info("Company Updated "+company.getId());
    }

    private Company truncateField(Company company){
        log.info("inside truncateField");

        //Trim below fields if its length is greater than 245 and save trim string in db
        if (!Util.isNull(company.getWebsite()) && company.getWebsite().length() > 245){
            log.error("Company Website field exceeds limit -" +company.getWebsite());
            company.setWebsite(company.getWebsite().substring(0, 245));
        }

        if (!Util.isNull(company.getLinkedin()) && company.getLinkedin().length() > 245) {
            log.error("Company Linkedin field exceeds limit -" +company.getWebsite());
            company.setLinkedin(company.getLinkedin().substring(0, 245));
        }

        if (!Util.isNull(company.getTwitter()) && company.getTwitter().length() > 245) {
            log.error("Company Twitter field exceeds limit -" +company.getWebsite());
            company.setTwitter(company.getTwitter().substring(0, 245));
        }

        if (!Util.isNull(company.getFacebook()) && company.getFacebook().length() > 245) {
            log.error("Company Facebook field exceeds limit -" +company.getWebsite());
            company.setFacebook(company.getFacebook().substring(0, 245));
        }
        return company;
    }

    /**
     *
     * @param company
     * @param loggedInUser
     */
    private void updateBusinessUnit(Company company, User loggedInUser) {
        Map<String, String> errorResponse= new HashMap<>();

        //process new company BU's
        if(company.getNewCompanyBu().size()>0) {
            company.getNewCompanyBu().stream().forEach(businessUnit -> {
                CompanyBu companyBuFromDb = companyBuRepository.findByBusinessUnitIgnoreCaseAndCompanyId(businessUnit, company.getId());
                if (null != companyBuFromDb) {
                    errorResponse.put(businessUnit, "Already exist");
                } else {
                    if(Util.isNotNull(businessUnit)){
                        CompanyBu companyBu = new CompanyBu();
                        companyBu.setCompanyId(company.getId());
                        companyBu.setBusinessUnit(businessUnit);
                        companyBu.setCreatedBy(loggedInUser.getId());
                        companyBu.setCreatedOn(new Date());
                        companyBuRepository.save(companyBu);
                    }else{
                        errorResponse.put(businessUnit, "businessUnit is null");
                    }

                }
            });
        }

        //process deleted company BUs
        if(company.getDeletedCompanyBu().size()>0){
            company.getDeletedCompanyBu().stream().forEach(businessUnit -> {
                CompanyBu companyBuFromDb = companyBuRepository.findByBusinessUnitIgnoreCaseAndCompanyId(businessUnit, company.getId());
                if(null!=companyBuFromDb) {
                    int jobsCount = jobRepository.countByBuId(companyBuFromDb);
                    int userCount = userRepository.countByCompanyBuId(companyBuFromDb.getId());
                    if (jobsCount == 0 && userCount == 0) {
                        companyBuRepository.delete(companyBuFromDb);
                    } else if(jobsCount>0) {
                        errorResponse.put(businessUnit, jobsCount + "jobs available for this BU");
                    } else if(userCount>0){
                        errorResponse.put(businessUnit, userCount + "Users available for this BU");
                    }
                }
                else{
                    errorResponse.put(businessUnit, "does not exist");
                }
            });
        }

        companyBuRepository.flush();
        saveCompanyHistory(company.getId(), loggedInUser.getDisplayName()+" updated company BUs for "+company.getCompanyName(), loggedInUser);

        if(errorResponse.size()>0) {
            log.info("Updated Company BU's with errors: " + errorResponse);
            throw new WebException("Error while updating BU's: "+errorResponse, HttpStatus.UNPROCESSABLE_ENTITY, errorResponse);
        }
        log.info("Company BUs' Updated for company Id: "+company.getId());
        company.setCompanyBuList(companyBuRepository.findByCompanyId(company.getId()));
    }

    /**
     *
     * @param company
     * @param companyFromDb
     * @param loggedInUser
     */
    private void updateUsersAndTeams(Company company, Company companyFromDb, User loggedInUser) {
    }

    /**
     *
     * @param company
     * @param companyFromDb
     * @param loggedInUser
     */
    private void updateScreeningQuestions(Company company, Company companyFromDb, User loggedInUser) {
    }

    /**
     *
     * @param company
     * @param companyFromDb
     * @param loggedInUser
     */
    private void updateCompanyAddresses(Company company, Company companyFromDb, User loggedInUser) {
        Map<String, String> errorResponse = new HashMap<>();

        //process new company Addresses
        if(company.getNewCompanyAddress().size()>0) {
            company.getNewCompanyAddress().stream().forEach(address -> {
                LatLng coordinates = null;
                try {
                    coordinates = GoogleMapsCoordinates.getCoordinates(address.getAddress());
                } catch (Exception e) {
                    log.info(Util.getStackTrace(e));
                }

                //check if company address title already exists
                Boolean addressTitleExists = companyFromDb.getCompanyAddressList()
                        .stream().filter(companyAddress -> {
                            return companyAddress.getAddressTitle().equals(address.getAddressTitle());
                        })
                        .collect(Collectors.toList())
                        .size() > 0;

                if(addressTitleExists){
                    errorResponse.put(address.getAddressTitle(), "Title Already exist");
                }else if (null==coordinates){         //add error to errorResponse if no cordinates are found.
                    errorResponse.put(address.getAddressTitle(), "coordinates not found");
                }
                else {
                    LatLng finalCoordinates = coordinates;
                    Boolean addressExists = companyFromDb.getCompanyAddressList()
                                            .stream().filter(companyAddress -> {
                                                return companyAddress.getLatitude()== finalCoordinates.lat && companyAddress.getLongitude()== finalCoordinates.lng;
                                            })
                                            .collect(Collectors.toList())
                                            .size() > 0;

                    if (addressExists) {
                        errorResponse.put(address.getAddressTitle(), "Address already exist");
                    } else {
                        address.setCompanyId(company.getId());
                        address.setLatitude(coordinates.lat);
                        address.setLongitude(coordinates.lng);
                        address.setCreatedBy(loggedInUser.getId());
                        address.setCreatedOn(new Date());
                        companyAddressRepository.save(address);
                    }
                }
            });
        }

        //process deleted company Addresses
        if(company.getDeletedCompanyAddress().size()>0){
            company.getDeletedCompanyAddress().stream().forEach(companyAddress -> {
                log.info("deleting company address with id: "+companyAddress.getId());
                CompanyAddress companyAddressFromDb = companyAddressRepository.findById(companyAddress.getId()).orElse(null);
                if(companyAddressFromDb!=null) {
                    int jobsCount = jobRepository.countByJobLocationOrInterviewLocation(companyAddress, companyAddress);
                    if (jobsCount == 0) {
                        companyAddressRepository.delete(companyAddress);
                        log.info("deleted company address with id: "+companyAddress.getId());
                    } else {
                        errorResponse.put(companyAddress.getAddressTitle(), jobsCount + "jobs available for this BU");
                    }
                }
                else{
                    errorResponse.put(companyAddress.getAddressTitle(), "does not exist");
                }
            });
        }

        //process updated company Address
        if(company.getUpdatedCompanyAddress().size()>0){
            company.getUpdatedCompanyAddress().stream().forEach(companyAddress -> {
                log.info("updating company address with id: "+companyAddress.getId());
                CompanyAddress companyAddressFromDb = companyAddressRepository.findById(companyAddress.getId()).orElse(null);
                if(companyAddressFromDb!=null){

                    //update address if changed
                    if(!companyAddress.getAddress().equals(companyAddressFromDb.getAddress())){
                        companyAddressFromDb.setAddress(companyAddress.getAddress());
                        LatLng newCoordinates = null;
                        //Update coordinates
                        try {
                            newCoordinates = GoogleMapsCoordinates.getCoordinates(companyAddress.getAddress());
                        } catch (Exception e) {
                            log.info(Util.getStackTrace(e));
                        }
                        if(null!=newCoordinates){
                            companyAddressFromDb.setLongitude(newCoordinates.lat);
                            companyAddressFromDb.setLongitude(newCoordinates.lng);
                        }
                        else{
                            errorResponse.put(companyAddress.getAddressTitle(), " Co-ordinates not updated");
                        }
                    }

                    //update address title if changed
                    if(!companyAddress.getAddressTitle().equals(companyAddressFromDb.getAddressTitle())) {
                        companyAddressFromDb.setAddressTitle(companyAddress.getAddressTitle());
                    }

                    //update address type if changed
                    if(!companyAddress.getAddressType().getId().equals(companyAddressFromDb.getAddressType().getId())){
                        companyAddressFromDb.setAddressType(companyAddress.getAddressType());
                    }

                    companyAddressFromDb.setUpdatedBy(loggedInUser.getId());
                    companyAddressFromDb.setUpdatedOn(new Date());
                    companyAddressFromDb.setArea(companyAddress.getArea());
                    companyAddressFromDb.setCountry(companyAddress.getCountry());
                    companyAddressFromDb.setCity(companyAddress.getCity());
                    companyAddressFromDb.setState(companyAddress.getState());
                    companyAddressRepository.save(companyAddressFromDb);
                    log.info("updated company address with id: "+companyAddress.getId());
                }
                else{
                    errorResponse.put(companyAddress.getAddressTitle(), "does not exist");
                }
            });
        }

        companyAddressRepository.flush();
        saveCompanyHistory(company.getId(), loggedInUser.getDisplayName()+" updated company Addresses", loggedInUser);

        if(errorResponse.size()>0) {
            log.info("Updated Company Addresses with errors: " + errorResponse);
            throw new WebException("Error while updating Addresses: "+errorResponse, HttpStatus.UNPROCESSABLE_ENTITY, errorResponse);
        }
        log.info("Company Addresses Updated for company Id:"+company.getId());
        company.setCompanyAddressList(companyAddressRepository.findByCompanyId(company.getId()));
    }

    /**
     * Service method to block or unblock a company
     * Only a super admin has access to this api
     *
     * @param company      the company to block
     * @param blockCompany flag indicating whether it is a block or an unblock operation
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void blockCompany(Company company, boolean blockCompany) throws Exception {
        Company companyObjFromDb = companyRepository.findById(company.getId()).orElse(null);
        if(null == companyObjFromDb)
            throw new ValidationException("Company not found: " + company.getCompanyName(), HttpStatus.BAD_REQUEST);
        companyObjFromDb.setActive(!blockCompany);
        companyObjFromDb.setUpdatedOn(new Date());
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        companyObjFromDb.setUpdatedBy(loggedInUser.getId());
        companyRepository.save(companyObjFromDb);
        saveCompanyHistory(companyObjFromDb.getId(), loggedInUser.getDisplayName()+(blockCompany ? " unblocked company : ":" blocked company : ") + companyObjFromDb.getCompanyName(), loggedInUser);
    }

    /**
     * Service method to fetch a list of all companies
     *
     * @return List of companies
     * @throws Exception
     */
    @Override
    public List<CompanyWorspaceBean> getCompanyList() throws Exception {
        log.info("Received request to get list of companies");
        long startTime = System.currentTimeMillis();

        List<Company> companies = companyRepository.findAll();

        List<CompanyWorspaceBean> responseBeans = new ArrayList<>(companies.size());

        companies.forEach(company -> {
            CompanyWorspaceBean worspaceBean = new CompanyWorspaceBean(company.getId(), company.getCompanyName(),
                    company.getCreatedOn(), !company.getActive(), company.getShortName());
            worspaceBean.setNumberOfUsers(userRepository.countByCompanyId(company.getId()));
            responseBeans.add(worspaceBean);
        });

        log.info("Completed processing list of companies in " + (System.currentTimeMillis() - startTime) + "ms.");
        return responseBeans;
    }

    /**
     *
     * Service method to fetch a list of all BUs of a company
     * @param companyId for which list to be fetched
     * @return List of BUs
     * @throws Exception
     */
    @Override
    public List<CompanyBu>getCompanyBuList(Long companyId) throws Exception{
        log.info("Received request to get list of BUs for companyId: "+companyId);
        long startTime = System.currentTimeMillis();

        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long validCompanyId = validateCompanyId(loggedInUser, companyId);
        if(!validCompanyId.equals(companyId))
            log.error("Given company id : {} and valid company id : {} both are mismatched", companyId, validCompanyId);

        Company company = companyRepository.findById(validCompanyId).orElse(null);

        if(company==null)
            throw new WebException("No company found with id: "+validCompanyId, HttpStatus.UNPROCESSABLE_ENTITY );

        log.info("Completed processing list of BUs for companyId: "+ validCompanyId +" in " + (System.currentTimeMillis() - startTime) + "ms.");
        return company.getCompanyBuList();
    }

    @Override
    public Map<String, List<CompanyAddress>>getCompanyAddresses(Long companyId, Boolean isInterviewLocation)throws Exception{
        //find company by companyId
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long validCompanyId = validateCompanyId(loggedInUser, companyId);
        if(!validCompanyId.equals(companyId))
            log.error("Given company id : {} and valid company id : {} both are mismatched", companyId, validCompanyId);

        Company company = companyRepository.findById(validCompanyId).orElse(null);

        //if company is null throw exception
        if(company==null)
            throw new WebException("No company found with id: "+validCompanyId, HttpStatus.UNPROCESSABLE_ENTITY );

        log.info("Received request to get list of Addresses for company: "+company.getCompanyName());
        long startTime = System.currentTimeMillis();

        Map<String, List<CompanyAddress>> companyAddressListByType = new HashMap<>();

        Map<Long, String> addressTypes = MasterDataBean.getInstance().getAddressType();

        List<CompanyAddress> interviewAddersses = new ArrayList<>();
        List<CompanyAddress> jobAddresses = new ArrayList<>();
        List<CompanyAddress> bothAddresses = new ArrayList<>();

        //extract and collect addresses from company object.
        addressTypes.entrySet().stream().forEach(addressType->{
           company.getCompanyAddressList().stream().forEach(companyAddress->{
               if(companyAddress.getAddressType().getId().equals(addressType.getKey()) && companyAddress.getAddressType().getValue().equals("Interview Location")){
                   interviewAddersses.add(companyAddress);
               }
               else if(companyAddress.getAddressType().getId().equals(addressType.getKey()) && companyAddress.getAddressType().getValue().equals("Job Location")){
                   jobAddresses.add(companyAddress);
               }
               else if(companyAddress.getAddressType().getId().equals(addressType.getKey()) && companyAddress.getAddressType().getValue().equals("Both")){
                   bothAddresses.add(companyAddress);
               }
           });
        });

        if(bothAddresses.size()>0){
            interviewAddersses.addAll(bothAddresses);
            jobAddresses.addAll(bothAddresses);
        }

        companyAddressListByType.put("Interview Location", interviewAddersses);

        if(!isInterviewLocation)
            companyAddressListByType.put("Job Location", jobAddresses);

        log.info("Completed processing list of Addresses for companyId: "+ validCompanyId +" in " + (System.currentTimeMillis() - startTime) + "ms.");
        return companyAddressListByType;
    }

    @Transactional
    public void saveCompanyHistory(Long companyId, String historyMsg, User loggedInUser) {
        companyHistoryRepository.save(new CompanyHistory(companyId, historyMsg, loggedInUser));
    }

    @Transactional
    public Company getCompanyDetail(Long companyId) {
        log.info("inside getCompanyDetail method");
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long validCompanyId = validateCompanyId(loggedInUser, companyId);
        if(!validCompanyId.equals(companyId))
            log.error("Given company id : {} and valid company id : {} both are mismatched", companyId, validCompanyId);

        Company company = companyRepository.findById(validCompanyId).orElse(null);
        if(null == company)
            throw new ValidationException("Company not found for id : " + validCompanyId, HttpStatus.BAD_REQUEST);

        Hibernate.initialize(company.getCompanyBuList());
        Hibernate.initialize(company.getCompanyAddressList());
        return company;
    }

    @Transactional
    public Company createCompanyByAgency(Company company) {
        log.info("inside createCompanyByAgency method");
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Company recruitmentAgency = companyRepository.findById(company.getRecruitmentAgencyId()).orElse(null);
        Company companyFromDb = companyRepository.findByCompanyNameIgnoreCaseAndRecruitmentAgencyId(company.getCompanyName(), company.getRecruitmentAgencyId());

        if(null != companyFromDb)
            throw new ValidationException("Company "+ company.getCompanyName()+" already present for your agency ", HttpStatus.BAD_REQUEST);

        if(null == company.getRecruitmentAgencyId())
            throw new ValidationException("Recruitment agency should not be null ", HttpStatus.BAD_REQUEST);

        company.setCreatedOn(new Date());
        company.setCreatedBy(loggedInUser.getId());
        company.setCountryId(recruitmentAgency.getCountryId());
        company.setSendCommunication(recruitmentAgency.isSendCommunication());
        company = truncateField(company);
        Company newCompany = companyRepository.save(company);
        return newCompany;
    }

    @Override
    public List<Company> getCompanyListByAgency(Long recruitmentAgencyId) {
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        recruitmentAgencyId = validateCompanyId(loggedInUser, recruitmentAgencyId);
        log.info("Inside getCompanyListByAgency "+companyRepository.findByRecruitmentAgencyId(recruitmentAgencyId).size());
        return companyRepository.findByRecruitmentAgencyId(recruitmentAgencyId);
    }

    /**
     * Service method to get boolean value as per company exist or not for short name
     * @param shortName Company short name
     * @return
     */
    @Transactional
    public Boolean isCompanyExistForShortName(String shortName) {
        log.info("inside isCompanyExistForShortName");
        long startTime = System.currentTimeMillis();
        Company company = companyRepository.findByShortNameIgnoreCase(shortName);
        log.info("Find company by shortName in {}ms.", (System.currentTimeMillis() - startTime));
        if(null != company){
            log.info("Company already exist, CompanyId : {}",company.getId());
            return true;
        }else{
            return false;
        }
    }

    /**
     * Method to create a subdomain for a company when the first job is published
     *
     * @param company the company for which subdomain is to be created
     * @throws Exception
     */
    @Override
    public void createSubdomain(Company company) {
        log.info("Received request to create subdomain for company: {} shortName: {}", company.getCompanyName(), company.getShortName());
        long startTime = System.currentTimeMillis();

        if(null == company.getShortName())
            throw new WebException("Company short name not found for " + company.getCompanyName(), HttpStatus.UNPROCESSABLE_ENTITY);

        //REST API Call to GoDaddy to register subdomain
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<GoDaddyRequestBean> requestObj = Arrays.asList(new GoDaddyRequestBean(company.getShortName(), createSubdomainIp));
        try {
            RestClientResponseBean responseFromGoDaddy = null;
            try {
                responseFromGoDaddy = RestClient.getInstance().consumeRestApi(objectMapper.writeValueAsString(requestObj), createSubdomainApi, HttpMethod.PATCH, new StringBuffer("sso-key ").append(createSubdomainKey).append(":").append(createSubdomainSecret).toString());
            } catch (Exception ex) {
                log.info(Util.getStackTrace(ex));
                log.error("Error while creating subdomain for {}.\n{}", company, ex.getMessage());
                log.info("Duplicate subdomain creation attempt. Setting flag and creating conf files.");
            }
            log.info("Response from GoDaddy:: {} ::", responseFromGoDaddy);
            if (null != responseFromGoDaddy && (HttpStatus.OK.value() == responseFromGoDaddy.getStatusCode() || responseFromGoDaddy.getResponseBody().indexOf("\"code\":\"DUPLICATE_RECORD\"") != -1) ){

                company.setSubdomainCreated(true);
                company.setSubdomainCreatedOn(new Date());
                companyRepository.save(company);

                ClassPathResource resource = new ClassPathResource(subdomainTemplateName);

                String templateData = FileCopyUtils.copyToString(new InputStreamReader(((ClassPathResource) resource).getInputStream(), UTF_8));
                //replace key with company short name
                templateData = templateData.replaceAll(IConstant.REPLACEMENT_KEY_FOR_SHORTNAME, company.getShortName());
                log.info("Created template data to be written to file \n{}", templateData);
                //create conf in apache folder
                File configFile = new File("/etc/apache2/sites-available/" + company.getShortName() + ".conf");
                FileWriter fw = new FileWriter(configFile);
                fw.write(templateData);
                fw.close();

                //create symbolic link
                Path link = Paths.get("/etc/apache2/sites-enabled/", company.getShortName() + ".conf");
                if (Files.exists(link)) {
                    Files.delete(link);
                }
                Files.createSymbolicLink(link, Paths.get("/etc/apache2/sites-available/" + company.getShortName() + ".conf"));
            } else {
                log.error("Error creating subdomain on GoDaddy for company {}", company.getCompanyName());
            }
        } catch (Exception e) {
            log.error("Error while creating sub-domain: {}", e.getMessage());
            Map breadCrumb = new HashMap<String, String>();
            SentryUtil.logWithStaticAPI(null, "Error while creating sub-domain: "+e.getMessage(), breadCrumb);
        }
        log.info("Completed processing request to create subdomain for company {} in {} ms.",company.getCompanyName(), (System.currentTimeMillis() - startTime));
    }

    /**
     * Method that fetches a list of all companies that have short name and for which a subdomain has not been created
     *
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void createSubdomains() throws Exception {
        log.info("Received request to create subdomains from Super Admin");
        long startTime = System.currentTimeMillis();
        List<Company> companyList = companyRepository.findBySubdomainCreatedIsFalseAndShortNameIsNotNull();
        companyList.stream().forEach(company -> {
            try {
                createSubdomain(company);
            } catch (Exception e) {
                log.error("Error creating subdomain for company {}:\n {}", company.getCompanyName(), e.getMessage());
            }
        });
        if(companyList.size()>0)
            reloadApache(companyList);
        log.info("Completed processing request to create subdomains in {} ms.", (System.currentTimeMillis() - startTime));
    }

    /**
     * functioon to reload Apache if new subdomain vitua host configuration is added in sites-available directory
     * @param companyList
     */
    public void reloadApache(List<Company> companyList){
        // Reload apache configuration to enable virtual host for new sub-domains
        try {
            Process process = Runtime.getRuntime().exec(IConstant.apacheReloadCommand);
            process.waitFor();
            log.info("process completed to reload apache, exit code: {}", process.exitValue());
            process.destroy();
        }
        catch (IOException e){
            SentryUtil.logWithStaticAPI(null, "Error while reloading apache after creating virtual host configuration for subdomains: "+String.join(",", companyList.stream().map(Company::getShortName).collect(Collectors.toList())), null);
            log.error("Error while creating process to reload apache: {}", e.getCause());
        }
        catch (InterruptedException e){
            SentryUtil.logWithStaticAPI(null, "Error while reloading apache after creating virtual host configuration for subdomains: "+String.join(",", companyList.stream().map(Company::getShortName).collect(Collectors.toList())), null);
            log.error("Reload apache process interrupted while executing: {}", e.getCause());
        }
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CompanyAddress> getCompanyAddress(Long companyId) {
        log.info("Inside getCompanyAddress");
        long startTime = System.currentTimeMillis();
        List<CompanyAddress> companyAddressList = companyAddressRepository.findByCompanyId(companyId);
        log.info("Get Company address list by company id in {} ms.", (System.currentTimeMillis() - startTime));
        return companyAddressList;
    }

    @Override
    public List<Company> setCompanyUniqueId() {
        log.info("Inside setCompanyUniqueId");
        List<Company> companies = companyRepository.findByShortNameIsNotNullAndRecruitmentAgencyIdIsNullAndCompanyUniqueIdIsNull();
        companies.forEach(company -> {
            company = generateAndSetCompanyUniqueId(company);
        });
        companyRepository.saveAll(companies);
        return companies;
    }

    private static final int RANDOM_STRING_LENGTH = 5;
    private static String getAlphaNumericString() {
        byte[] array = new byte[256];
        new Random().nextBytes(array);
        String randomString = new String(array, Charset.forName("UTF-8"));
        StringBuffer r = new StringBuffer();
        for (int k = 0; k < randomString.length(); k++) {
            char ch = randomString.charAt(k);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) {
                r.append(ch);
                if (RANDOM_STRING_LENGTH == r.length())
                    break;
            }
        }
        return r.toString();
    }

    private Company generateAndSetCompanyUniqueId(Company company){
        log.info("Inside generateAndSetCompanyUniqueId");
        if(null == company.getRecruitmentAgencyId()){
            boolean isUniqueIdPresent = true;
            String companyUniqueId = null;
            while(isUniqueIdPresent){
                companyUniqueId = company.getShortName().substring(0,3)+getAlphaNumericString();
                Company companyFromDb = companyRepository.findByCompanyUniqueId(companyUniqueId);
                if(null == companyFromDb){
                    isUniqueIdPresent = false;
                    company.setCompanyUniqueId(companyUniqueId);
                    log.info("Create new company unique id : {}, For company : {}",company.getCompanyUniqueId(), company.getShortName());
                }
            }
        }else
            log.info("For recruitment agency Company unique id not generated");

        return company;
    }

    /**
     * private method to make a call to search engine add company api.
     * @param company
     */
    public void addCompanyOnSearchEngine(Company company, String authToken){
        log.info("Calling SearchEngine API to add company id:{}, name:{}", company.getId(), company.getCompanyName());
        long startTime = System.currentTimeMillis();

        //creating a map of parameters to be sent to search engine api.
        Map queryparams = new HashMap(2);
        queryparams.put("companyId", company.getId());
        queryparams.put("companyName", company.getCompanyName());
        Map<String, Object> userDetails = searchEngineService.getLoggedInUserInformation();
        try {
            //calling sscoring engine api to add company in neo4j db.
            RestClient.getInstance(). consumeRestApi(null, searchEngineBaseUrl + searchEngineAddCompanyUrlSuffix, HttpMethod.POST, authToken, Optional.of(queryparams), null, Optional.of(userDetails));
        }
        catch (Exception e){
            log.error("Error while adding company on Search Engine: " + e.getMessage());
        }
        log.info("Completed adding company on search engine in {}ms", System.currentTimeMillis()-startTime);
    }

}
