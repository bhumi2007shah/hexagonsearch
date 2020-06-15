/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.requestbean.ClientEmployeeRequestBean;
import io.litmusblox.server.service.client.IClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author : sameer
 * Date : 29/05/20
 * Time : 12:25 PM
 * Class Name : ClientController
 * Project Name : server
 */
@RestController
@RequestMapping("/api/client")
@Log4j2
public class ClientController {

    @Autowired
    IClientService clientService;

    /**
     * controller method to create employees.
     * @param clientEmployeeRequestBean contains list of employees as the current JSON requires.
     * @return ResponseEntity with response code and message
     */
    @PostMapping("/createEmployees")
    ResponseEntity createEmployee(@Valid @RequestBody ClientEmployeeRequestBean clientEmployeeRequestBean){
        return clientService.createEmployees(clientEmployeeRequestBean);
    }

    /**
     * controller method to trigger function that pushes data to client
     */
    @GetMapping("/pushEmployeeData")
    void pushEmployeeData(){
        clientService.pushEmployeesData();
    }

}
