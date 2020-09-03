/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.CompanyRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.annotation.Resource;

/**
 * @author : Sumit
 * Date : 31/08/20
 * Time : 4:32 PM
 * Class Name : AbstractAccessControl
 * Project Name : server
 */
@Log4j2
public abstract class AbstractAccessControl {


    @Resource
    CompanyRepository companyRepository;

    /**
     * This method is used for validate company Id Check which is really related to loggedIn user or not
     *
     * @param loggedInUser      LoggedInUser
     * @param companyId given company id through api call
     * @return valid company id
     */
    protected Long validateCompanyId(User loggedInUser, Long companyId) {
        log.info("LoggedIn user company id is : {} and given company id through api is : {}", loggedInUser.getCompany().getId(), companyId);
        if (!loggedInUser.getCompany().getId().equals(companyId) && !IConstant.UserRole.SUPER_ADMIN.toString().equals(loggedInUser.getRole())) {
            //Check loggedIn user company is agency or not if yes then check company id belonging to it's client or not
            if (IConstant.CompanyType.AGENCY.getValue().equals(loggedInUser.getCompany().getCompanyType())) {
                Company company = companyRepository.findByIdAndRecruitmentAgencyId(companyId, loggedInUser.getCompany().getId());
                if (null == company) {
                    log.error("Client companyId : {} not belonging to agency : {}", companyId, loggedInUser.getCompany().getId());
                    throw new ValidationException("Client companyId : " + companyId + " not belonging to agency : " + loggedInUser.getCompany().getCompanyName(), HttpStatus.UNAUTHORIZED);
                }
            } else
                return loggedInUser.getCompany().getId();  //if loggedIn user trying to access other company data but we send it his own company data default
        }
        //if user is super admin then give data for gives company id
        return companyId;
    }

    /**
     * Service method to validate logged in user
     *
     * @param loggedInUser
     * @param companyId
     */
    protected void validateLoggedInUser(User loggedInUser, Long companyId) {
        log.info("LoggedIn user company id is : {} and given company id through api is : {}", loggedInUser.getCompany().getId(), companyId);
        if (!loggedInUser.getCompany().getId().equals(companyId)) {
            log.error("You are not a valid user for accessing data : {}", loggedInUser.getEmail());
            throw new ValidationException("You are not a valid user for accessing data", HttpStatus.UNAUTHORIZED);
        }
    }
}
