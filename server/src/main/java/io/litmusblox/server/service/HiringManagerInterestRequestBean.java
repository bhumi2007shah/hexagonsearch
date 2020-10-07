/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

/**
 * Date : 06/10/20
 * Time : 11:51 PM
 * Class Name : HiringManagerInterestRequestBean
 * Project Name : server
 */

@Data
public class HiringManagerInterestRequestBean {
    private String comment;
    private Long rejectionReasonId;
}
