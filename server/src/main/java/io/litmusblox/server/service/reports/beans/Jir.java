/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.reports.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * @author : sameer
 * Date : 28/02/21
 * Time : 11:30 AM
 * Class Name : Jir
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jir {
    private Long totalCount;
    private String companyName;
    @Temporal(TemporalType.TIMESTAMP)
    Date createdOn;
}
