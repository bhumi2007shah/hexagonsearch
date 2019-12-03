/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : sameer
 * Date : 26/11/19
 * Time : 3:48 PM
 * Class Name : RChilliErrorResonseBean
 * Project Name : server
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBean {
    String fileName;
    Date processedOn;
    String status;
    String candidateName;
}
