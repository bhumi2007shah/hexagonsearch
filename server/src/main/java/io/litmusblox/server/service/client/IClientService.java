/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.client;

import io.litmusblox.server.requestbean.ClientEmployeeRequestBean;
import org.springframework.http.ResponseEntity;

/**
 * @author : sameer
 * Date : 29/05/20
 * Time : 12:52 PM
 * Class Name : IClientService
 * Project Name : server
 */
public interface IClientService {
    /**
     * method to create employees
     * @param employees Map of list of employees as the current JSON requires.
     * @return ResponseEntity with response code and message
     */
    ResponseEntity createEmployees(ClientEmployeeRequestBean clientEmployeeRequestBean);

    /**
     * method to push employee data to client
     */
    void pushEmployeesData();
}
