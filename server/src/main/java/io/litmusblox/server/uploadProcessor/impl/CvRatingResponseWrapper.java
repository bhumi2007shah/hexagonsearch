/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import lombok.Data;

import java.util.Map;

/**
 * @author : Shital Raval
 * Date : 18/10/19
 * Time : 10:13 AM
 * Class Name : CvRatingResponseWrapper
 * Project Name : server
 */

@Data
public class CvRatingResponseWrapper {
    int overallRating;
    Map<String, Map<String, String>> cvRatingResponse;
}

