/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

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
    private List<SearchParam> searchParams;
    private Long companyId;
}

class SearchParam {
    private String key;
    private String value;
    private boolean multiSelect = false;
}
