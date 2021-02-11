/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

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
    private List<String> function;
    private Long companyId;
    private Set<String> skillSet;

    public JdParserRequestBean(String jdString, boolean skillFlag, boolean capabilityFlag, Long companyId, Set<String> skillSet) {
        this.jdString = jdString;
        this.skillFlag = skillFlag;
        this.capabilityFlag = capabilityFlag;
        this.companyId = companyId;
        this.skillSet = skillSet;
    }
}
