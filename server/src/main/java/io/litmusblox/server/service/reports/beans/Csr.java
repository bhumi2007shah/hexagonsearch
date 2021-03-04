/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.reports.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : sameer
 * Date : 27/02/21
 * Time : 1:31 PM
 * Class Name : Csr
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Csr {
    private Long sourceCount;
    private String sourcedBy;
    private Date sourcedOn;
}
