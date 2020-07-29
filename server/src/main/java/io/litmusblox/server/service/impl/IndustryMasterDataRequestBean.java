/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Sumit
 * Date : 11/06/20
 * Time : 9:12 AM
 * Class Name : IndustryDataModel
 * Project Name : SearchEngine
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndustryMasterDataRequestBean {
    private String industryName;
    private List<IndustryFunction> functions = new ArrayList<>();
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class IndustryFunction {
    private String functionName;
    private List<IndustryRole> roles = new ArrayList<>();
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class IndustryRole {
    private String roleName;
}