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
 * Time : 1:53 PM
 * Class Name : Isr
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Isr {
    private Long totalScheduled;
    private Long totalShow;
    private String scheduledBy;
    private Date createdOn;

}
