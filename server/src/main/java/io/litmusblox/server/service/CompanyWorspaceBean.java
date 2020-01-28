/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : Shital Raval
 * Date : 1/8/19
 * Time : 12:24 PM
 * Class Name : CompanyWorspaceBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
public class CompanyWorspaceBean {
    Long companyId;
    String companyName;
    Date accountActiveSince;
    boolean blocked;
    int numberOfUsers;
    String companyShortName;

    public CompanyWorspaceBean(Long companyId, String companyName, Date accountActiveSince, boolean blocked, String companyShortName) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.accountActiveSince = accountActiveSince;
        this.blocked = blocked;
        this.companyShortName = companyShortName;
    }
}
