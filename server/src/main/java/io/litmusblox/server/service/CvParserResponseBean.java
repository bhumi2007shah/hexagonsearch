/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.uploadProcessor.impl.CvRatingResponseWrapper;
import lombok.Data;

@Data
public class CvParserResponseBean {
    private Candidate candidate;
    private CvRatingResponseWrapper cvRatingResponseWrapper;
}
