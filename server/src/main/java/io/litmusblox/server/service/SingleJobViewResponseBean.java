/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.JobCandidateMapping;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response bean for Single job view. Contains
 * 1. List of candidates for the requested stage, or "Source" by default
 * 2. Map of stage and number of candidates for the stage
 *
 * @author : Shital Raval
 * Date : 10/7/19
 * Time : 2:02 PM
 * Class Name : SingleJobViewResponseBean
 * Project Name : server
 */
@Data
public class SingleJobViewResponseBean {
    private List<JobCandidateMapping> candidateList = new ArrayList<>();
    private Map<String,Integer> candidateCountByStage = new HashMap<>();
}
