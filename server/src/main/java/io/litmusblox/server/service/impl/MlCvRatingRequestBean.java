/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 18/10/19
 * Time : 12:01 PM
 * Class Name : MlCvRatingRequestBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlCvRatingRequestBean {
    List<String> jdKeyskills;
    String resumeContent;
    String industry;
}
