/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Company;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Shital Raval
 * Date : 22/7/19
 * Time : 8:34 AM
 * Class Name : LoginResponseBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseBean {
    private String jwtToken;
    private String userName;
    private Company company;
    private int candidatesProcessed;
}
