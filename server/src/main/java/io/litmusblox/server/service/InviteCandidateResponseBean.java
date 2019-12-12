/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Candidate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : sameer
 * Date : 21/11/19
 * Time : 5:12 PM
 * Class Name : InviteCandidateResponseBean
 * Project Name : server
 */
@AllArgsConstructor
@Data
public class InviteCandidateResponseBean {
    private String status;
    private int successCount;
    private int failureCount;
    private List<Candidate> failedCandidates = new ArrayList<>();
}
