/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.JobCandidateMapping;

import java.util.Map;

/**
 * @author : sameer
 * Date : 04/03/21
 * Time : 5:39 PM
 * Class Name : IScoreService
 * Project Name : server
 */
public interface IScoreService {
    Map<String, Map> scoreJcm(Job job, JobCandidateMapping jcm);
}
