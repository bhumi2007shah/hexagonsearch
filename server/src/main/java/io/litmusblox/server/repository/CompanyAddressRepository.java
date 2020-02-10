/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CompanyAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author : Sumit
 * Date : 13/7/19
 * Time : 1:48 PM
 * Class Name : CompanyAddressRepository
 * Project Name : server
 */
public interface CompanyAddressRepository extends JpaRepository<CompanyAddress, Long> {

    // find list of company address by company Id
    List<CompanyAddress> findByCompanyId(Long companyId);

    @Query(value = "select * from company_address where address_type = (select id from master_data where value = 'Interview Location') and company_id =:companyId", nativeQuery = true)
    List<CompanyAddress> findByCompanyIdAndAddressType(Long companyId);
}
