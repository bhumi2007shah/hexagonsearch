/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository class for Company
 *
 * @author : Shital Raval
 * Date : 8/7/19
 * Time : 3:04 PM
 * Class Name : CompanyRepository
 * Project Name : server
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Transactional
    Company findByCompanyNameIgnoreCaseAndRecruitmentAgencyId(String companyName, Long recruitmentAgencyId);

    @Transactional
    List<Company> findByRecruitmentAgencyId(Long recruitmentAgencyId);

    @Transactional
    Company findByCompanyNameIgnoreCaseAndRecruitmentAgencyIdIsNull(String companyName);

    @Transactional
    Company findByCompanyNameIgnoreCaseAndCompanyType(String companyName, String companyType);

    @Transactional(readOnly = true)
    Company findByShortNameIgnoreCase(String shortName);

    @Transactional
    List<Company> findBySubdomainCreatedIsFalseAndShortNameIsNotNull();

    @Transactional(readOnly = true)
    Company findByCompanyUniqueId(String companyUniqueId);

    @Transactional
    List<Company> findByShortNameIsNotNullAndRecruitmentAgencyIdIsNullAndCompanyUniqueIdIsNull();

    @Query(value = "select id from company where recruitment_agency_id =:agencyId", nativeQuery = true)
    List<Long> findCompanyIdListByForAgency(Long agencyId);
}
