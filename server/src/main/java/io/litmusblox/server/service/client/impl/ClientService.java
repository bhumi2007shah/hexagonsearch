/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.client.Employee;
import io.litmusblox.server.requestbean.ClientEmployeeRequestBean;
import io.litmusblox.server.service.client.IClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author : sameer
 * Date : 29/05/20
 * Time : 12:54 PM
 * Class Name : ClientService
 * Project Name : server
 */
@Service
@Log4j2
public class ClientService implements IClientService {

    /**
     *
     * @param clientEmployeeRequestBean
     * @return
     */
    public ResponseEntity createEmployees(ClientEmployeeRequestBean clientEmployeeRequestBean) {
        return new ResponseEntity("Employee records added.", HttpStatus.CREATED);
    }

    /**
     *
     */
    public void pushEmployeesData(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Employee> employees = mapper.readValue(IConstant.employeesJSON, new TypeReference<List<Employee>>() {});
            log.info(new ObjectMapper().writeValueAsString(employees));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
