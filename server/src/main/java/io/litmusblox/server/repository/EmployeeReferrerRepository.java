/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.EmployeeReferrer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : Sumit
 * Date : 27/12/19
 * Time : 10:08 AM
 * Class Name : EmployeeReferrerRepository
 * Project Name : server
 */
@Repository
public interface EmployeeReferrerRepository extends JpaRepository<EmployeeReferrer, Long> {

    EmployeeReferrer findByEmail(String email);
}
