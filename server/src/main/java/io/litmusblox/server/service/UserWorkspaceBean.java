/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

/**
 * POJO to save workspace response for list of users
 *Transient
 * @author : Shital Raval
 * Date : 1/8/19
 * Time : 1:00 PM
 * Class Name : UserWorkspaceBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
public class UserWorkspaceBean
{
    Long userId;
    String userName;
    String status;
    Long companyAddressId;
    Long companyBuId;
    String email;
    String mobile;
    String userRole;
    int completedChatbotCount=0;
    int incompleteChatbotCount=0;
    int analyticsSharedCount=0;
    int numOfInvites=0;
    int numberOfJobsCreated=0;

    public UserWorkspaceBean(Long userId, String userName, String status, Long companyAddressId, Long companyBuId, String email, String mobile, String userRole) {
        this.userId = userId;
        this.userName = userName;
        this.status = status;
        this.companyAddressId = companyAddressId;
        this.companyBuId = companyBuId;
        this.email = email;
        this.mobile = mobile;
        this.userRole = userRole;
    }
}
