/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Job;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Response bean to be sent upon querying from job workspace
 *
 * @author : Shital Raval
 * Date : 9/7/19
 * Time : 12:10 PM
 * Class Name : JobWorspaceResponseBean
 * Project Name : server
 */
@Data
public class JobWorspaceResponseBean {
    private int openJobs;
    private int archivedJobs;
    private List<Job> listOfJobs = new ArrayList<>();
}
