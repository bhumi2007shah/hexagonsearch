/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.CompanyAddress;
import io.litmusblox.server.model.CompanyBu;
import io.litmusblox.server.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service class for various operations to be performed for a company
 *
 * @author : Shital Raval
 * Date : 30/7/19
 * Time : 2:10 PM
 * Class Name : ICompanyService
 * Project Name : server
 */
public interface ICompanyService {
    /**
     * Service method to add / update a company
     *
     * @param company the company to be added
     * @param logo    file containing the company logo
     * @throws Exception
     */
    Company saveCompany(Company company, MultipartFile logo) throws Exception;

    /**
     * Service method to block or unblock a company
     * Only a super admin has access to this api
     *
     * @param company      the company to block
     * @param blockCompany flag indicating whether it is a block or an unblock operation
     * @throws Exception
     */
    void blockCompany(Company company, boolean blockCompany) throws Exception;

    /**
     * Service method to fetch a list of all companies
     *
     * @return List of companies
     * @throws Exception
     */
    List<CompanyWorspaceBean> getCompanyList() throws Exception;

    /**
     *
     * @param companyId for which BUs to be fetched
     * @return List of company BUs
     * @throws Exception
     */
    List<CompanyBu> getCompanyBuList(Long companyId) throws Exception;

    Map<String, List<CompanyAddress>> getCompanyAddresses(Long companyId) throws Exception;

    /**
     * Service method to save company history. Need a service method because needs to be called from LbUserDetailsService on company create
     * @param companyId
     * @param historyMsg
     * @param loggedInUser
     */
    void saveCompanyHistory(Long companyId, String historyMsg, User loggedInUser);

    /**
     * Service method to get Company details by company id.
     * @param companyId
     * @return Company model
     */
    Company getCompanyDetail(Long companyId);

    /**
     * Service method to create company by agency
     * @param company
     * @return company model
     */
    void createCompanyByAgency(Company company);

    /**
     * Service method to get company list by agency company id
     * @param recruitmentAgencyId(Company id)
     * @return List of company
     */
    List<Company> getCompanyListByAgency(Long recruitmentAgencyId);
}