/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import java.util.List;

/**
 * class to send candidate data to search engine
 *
 * @author : sameer
 * Date : 22/04/20
 * Time : 2:13 PM
 * Class Name : CandidateRequestBean
 * Project Name : server
 */
@Data
public class CandidateRequestBean {
    private Long candidateId;
    private String candidateName;
    private Long companyId;
    private List<String> skill;
    private List<String> location;
    private int noticePeriod;
    private String experienceRange;
    private List<String> qualification;
}
