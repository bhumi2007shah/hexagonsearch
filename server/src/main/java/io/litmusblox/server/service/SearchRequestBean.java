/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import java.util.Map;

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
    private Map<String, String> searchParams;
    private Long companyId;
}
