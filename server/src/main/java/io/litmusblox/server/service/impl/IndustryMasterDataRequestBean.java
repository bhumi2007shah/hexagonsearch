/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<String> roles = new HashSet<>();
    private Set<String> attributes = new HashSet<>();
}