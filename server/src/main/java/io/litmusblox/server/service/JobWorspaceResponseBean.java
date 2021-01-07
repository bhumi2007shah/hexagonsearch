/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Job;
import lombok.Data;

import javax.persistence.Transient;
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
    private int liveJobs;
    private int draftJobs;
    private int archivedJobs;
    private List<Job> listOfJobs = new ArrayList<>();

    @Transient
    private int sourcingCandidateCount;

    @Transient
    private int screeningCandidateCount;

    @Transient
    private int submittedCandidateCount;

    @Transient
    private int interviewCandidateCount;

    @Transient
    private int makeOfferCandidateCount;

    @Transient
    private int offerCandidateCount;

    @Transient
    private int hiredCandidateCount;

    @Transient
    private int rejectedCandidateCount;

}
