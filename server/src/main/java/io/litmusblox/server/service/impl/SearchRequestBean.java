/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import lombok.Data;

import java.util.List;

/**
 * @author : Shital Raval
 *
 * Date : 24/12/19
 * Time : 2:55 PM
 * Class Name : SearchRequestBean
 * Project Name : server
 */
@Data
public class SearchRequestBean {
    private List<SearchParam> searchParam;
    private Long companyId;
}

@Data
class SearchParam {
    private String key;
    private String value;
    private boolean multiSelect = false;
}
