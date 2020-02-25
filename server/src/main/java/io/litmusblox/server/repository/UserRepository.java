/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository class for User object
 *
 * @author : Shital Raval
 * Date : 8/7/19
 * Time : 3:03 PM
 * Class Name : UserRepository
 * Project Name : server
 */
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByCompanyId(Long companyId);
    User findByEmail(String email);
    User findByUserUuid(UUID userUuid);
    int countByCompanyId(Long companyId);
    int countByCompanyBuId(Long companyBuId);
}
