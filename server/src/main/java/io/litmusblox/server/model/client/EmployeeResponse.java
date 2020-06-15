/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model.client;

import lombok.Data;

/**
 * @author : sameer
 * Date : 30/05/20
 * Time : 12:37 PM
 * Class Name : EmployeeResponse
 * Project Name : server
 */
@Data
public class EmployeeResponse {
    private String subCompetency;
    private LevelScore score;
}
