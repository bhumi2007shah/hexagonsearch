/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import lombok.Data;

import java.util.HashMap;

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
    HashMap<String, HashMap<String, String>> cvRatingResponse;
}

