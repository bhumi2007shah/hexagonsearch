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
 * Date : 26/02/21
 * Time : 5:44 PM
 * Class Name : dsr
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dsr {
    private Long submitCount;
    private String submittedBy;
    private Date submittedOn;
}
