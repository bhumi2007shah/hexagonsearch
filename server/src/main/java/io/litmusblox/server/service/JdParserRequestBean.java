/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Sumit
 * Date : 02/09/20
 * Time : 9:42 AM
 * Class Name : JdParserRequestBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdParserRequestBean {

    private String jdString;
    private boolean skillFlag;
    private boolean capabilityFlag;
    private String function;
    private Long companyId;

    public JdParserRequestBean(String jdString, boolean skillFlag, boolean capabilityFlag, Long companyId) {
        this.jdString = jdString;
        this.skillFlag = skillFlag;
        this.capabilityFlag = capabilityFlag;
        this.companyId = companyId;
    }
}
