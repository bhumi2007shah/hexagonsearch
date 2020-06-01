/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.requestbean;

import io.litmusblox.server.model.client.Employee;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author : sameer
 * Date : 01/06/20
 * Time : 7:27 PM
 * Class Name : ClientEmployeeRequestBean
 * Project Name : server
 */
@Data
public class ClientEmployeeRequestBean {
    @NotNull
    private List<Employee> employees;
}
