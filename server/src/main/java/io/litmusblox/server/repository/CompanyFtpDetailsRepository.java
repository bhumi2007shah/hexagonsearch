/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CompanyFtpDetails;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : sameer
 * Date : 02/03/21
 * Time : 5:45 PM
 * Class Name : CompanyFtpDetailsRepository
 * Project Name : server
 */
public interface CompanyFtpDetailsRepository extends JpaRepository<CompanyFtpDetails, Long> {
    CompanyFtpDetails findByCompanyId(Long companyId);
}
