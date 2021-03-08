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
 * Date : 27/02/21
 * Time : 10:15 AM
 * Class Name : DsrRequestBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestBean {
    @Temporal(TemporalType.TIMESTAMP)
    Date startDate;
    @Temporal(TemporalType.TIMESTAMP)
    Date endDate;
    Long companyId;
    Long buId;
    Long hiringManagerId;
    Long recruiterId;
}
