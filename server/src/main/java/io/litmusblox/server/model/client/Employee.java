/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model.client;

import lombok.Data;

import javax.validation.constraints.NotNull;
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

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String mobile;

    private String empCode;

    private String role;

    private String stream;

    private String group;

    private List<EmployeeDetail> details;
}
