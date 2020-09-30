/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import java.util.List;

/**
 * POJO to capture request information for sharing candidate profiles
 *
 * @author : Shital Raval
 * Date : 2/8/19
 * Time : 11:29 AM
 * Class Name : ShareCandidateProfileRequestBean
 * Project Name : server
 */
@Data
public class ShareCandidateProfileRequestBean {
    private String[][] receiverInfo;
    private List<Long> jcmId;
}
