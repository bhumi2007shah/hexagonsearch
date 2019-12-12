/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.CompanyStageStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 16/7/19
 * Time : 3:23 PM
 * Class Name : CompanyStageStepRepository
 * Project Name : server
 */
@Repository
public interface CompanyStageStepRepository extends JpaRepository<CompanyStageStep, Long> {
    @Transactional(readOnly = true)
    List<CompanyStageStep> findByCompanyId(Company companyId);
}
