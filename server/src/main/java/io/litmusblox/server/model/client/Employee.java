/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model.client;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

/**
 * @author : sameer
 * Date : 29/05/20
 * Time : 12:26 PM
 * Class Name : Employee
 * Project Name : server
 */
@Data
public class Employee {

    @NonNull
    private String name;

    @NonNull
    private String email;

    @NonNull
    private String mobile;

    private String empCode;

    private String role;

    private String stream;

    private String group;

    private List<EmployeeDetail> details;
}
