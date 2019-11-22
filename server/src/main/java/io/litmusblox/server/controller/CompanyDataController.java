/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.CompanyAddress;
import io.litmusblox.server.model.CompanyBu;
import io.litmusblox.server.service.ICompanyService;
import io.litmusblox.server.service.IScreeningQuestionService;
import io.litmusblox.server.service.UserWorkspaceBean;
import io.litmusblox.server.service.impl.LbUserDetailsService;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for company specific data operations
 *
 * @author : Shital Raval
 * Date : 12/7/19
 * Time : 6:32 PM
 * Class Name : CompanyDataController
 * Project Name : server
 */
@CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.PUT}, allowedHeaders = {"Content-Type", "Authorization","X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Access-Control-Allow-Origin"}, exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"})
@RestController
@RequestMapping("/api/company")
@Log4j2
public class CompanyDataController {

    @Autowired
    IScreeningQuestionService screeningQuestionService;

    @Autowired
    ICompanyService companyService;

    @Autowired
    LbUserDetailsService lbUserDetailsService;

    @GetMapping("/screeningQuestions")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    String fetchScreeningQuestions() throws Exception {
        return Util.stripExtraInfoFromResponseBean(screeningQuestionService.fetchScreeningQuestionsForCompanyAndUser(),
            (new HashMap<String, List<String>>(){{
                put("User", Arrays.asList("displayName"));
            }}),
            new HashMap<String, List<String>>() {{
                put("CompanyScreeningQuestion", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company"));
                put("UserScreeningQuestion", Arrays.asList("createdOn", "updatedOn","userId"));
            }}
        );
    }

    /**
     * REST Api to update an existing company details
     * Only a client admin has access to this api
     *
     * @param companyString the company to be updated
     * @throws Exception
     */
    @PutMapping(value = "/update",consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('" + IConstant.UserRole.Names.CLIENT_ADMIN + "')")
    Company updateCompany(
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam("company") String companyString
    ) throws Exception {
        Company company=new ObjectMapper().readValue(companyString, Company.class);
        return companyService.saveCompany(company, logo);
    }


    /**
     * REST Api to block or unblock a company
     * Only a super admin has access to this api
     *
     * @param company the company to block
     * @param blockCompany flag indicating whether it is a block or an unblock operation
     * @throws Exception
     */
    @PutMapping(value = "/blockUnblockCompany")
    @PreAuthorize("hasRole('" + IConstant.UserRole.Names.SUPER_ADMIN + "')")
    @ResponseStatus(value = HttpStatus.OK)
    void blockCompany(@RequestBody Company company, @RequestParam boolean blockCompany) throws Exception {
        log.info("Received request to block company with name: "+ company.getCompanyName());
        long startTime = System.currentTimeMillis();
        companyService.blockCompany(company,blockCompany);
        log.info("Complete block company request in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    /**
     * REST Api to return a list of users for a given company
     * @param companyId the company id for which the list of users needs to be sent
     * @return List of users
     * @throws Exception
     */
    @GetMapping("/usersForCompany")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<UserWorkspaceBean> findUserList(@RequestParam String companyId) throws Exception {
       //we already have a method in LbUserDetailsService.java which returns list of users for a compay with extra data like no. of jobs created. reusing that.
       return lbUserDetailsService.fetchUsers(Long.parseLong(companyId));
    }


    /**
     * REST Api to return a list of BUs for a given company
     * @param companyId the company name for which the list of BUs needs to be found
     * @return List of BUs
     * @throws Exception
     */
    @GetMapping("/buForCompany")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<CompanyBu> findBuList(@RequestParam String companyId) throws Exception {
        //call to the service layer that returns list of company BU
        return companyService.getCompanyBuList(Long.parseLong(companyId));
    }

    /**
     * REST Api to return a list of addresses for the company by address type
     * @param companyId the company name for which the list of addresses needs to be found
     * @return Map of address type and List of company addresses for that address type
     * @throws Exception
     */
    @GetMapping("/addressByCompany")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    Map<String, List<CompanyAddress>> findAddressByCompanyByType(@RequestParam String companyId) throws Exception {
        return companyService.getCompanyAddresses(Long.parseLong(companyId));
    }

    /**
     * Rest api to get company details on basis of company id
     * @param companyId
     * @return
     */
    @GetMapping("/getCompany/{companyId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    Company getCompanyDetail(@PathVariable ("companyId") Long companyId){
        log.info("inside getCompanyDetail method");
        return companyService.getCompanyDetail(companyId);
    }

    /**
     * Rest api to creating company by agency
     * @param company
     */
    @PostMapping("/createCompany")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    void createCompany(@RequestBody Company company){
        log.info("inside createCompany method");
        companyService.createCompanyByAgency(company);
    }

    /**
     * Rest api to get Company list depend on the recruitment agency id
     * @param recruitmentAgencyId
     */
    @GetMapping("/getCompanyByAgency/{recruitmentAgencyId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<Company> getCompanyListByAgency(@PathVariable ("recruitmentAgencyId") Long recruitmentAgencyId){
        log.info("inside getCompanyListByAgency method");
        return companyService.getCompanyListByAgency(recruitmentAgencyId);
    }
}
