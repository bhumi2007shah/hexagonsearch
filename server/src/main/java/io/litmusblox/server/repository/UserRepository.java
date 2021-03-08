/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.User;
import io.litmusblox.server.service.UserWorkspaceBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
public interface UserRepository extends JpaRepository<User, Long>
{
    @Transactional
    @Query("SELECT  new io.litmusblox.server.service.UserWorkspaceBean(u.id,CONCAT(u.firstName, ' ', u.lastName) AS userName,u.status,u.companyAddressId,u.companyBuId,u.email," +
            "u.mobile,u.userType)" +
            "FROM User u WHERE u.company.id=:validCompanyId")
    List<UserWorkspaceBean> findWorkspaceData(Long validCompanyId);
    List<User> findByCompanyId(Long companyId);
    User findByEmail(String email);
    User findByUserUuid(UUID userUuid);
    User findByWorkspaceUuid(UUID workspaceUuid);
    int countByCompanyId(Long companyId);
    int countByCompanyBuId(Long companyBuId);
    List<User> findByIdIn(List<Long> userId);


}
