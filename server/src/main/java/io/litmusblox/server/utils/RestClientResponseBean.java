/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Shital Raval
 * Date : 20/1/20
 * Time : 11:18 AM
 * Class Name : RestClientResponseBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestClientResponseBean {
    int statusCode;
    String responseBody;
}