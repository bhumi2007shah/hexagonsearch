/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.litmusblox.server.model.Candidate;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * Response bean for upload candidates operation for a job
 *
 * @author : Shital Raval
 * Date : 16/7/19
 * Time : 5:08 PM
 * Class Name : UploadResponseBean
 * Project Name : server
 */
@Data
@JsonFilter("UploadResponseBean")
public class UploadResponseBean extends ResponseBean{

    private int successCount;
    private int failureCount;
    private List<Candidate> failedCandidates = new ArrayList();
    private int candidatesProcessedCount = 0;
    @JsonIgnore
    private List<Candidate> successfulCandidates = new ArrayList();
    private Boolean cvStatus=false;
    private String cvErrorMsg;

}
