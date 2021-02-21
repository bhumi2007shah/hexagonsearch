/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

@Data
public class CompanyCandidateBean {
    private Long jobId;
    private String numberToHire;
    private String jobTitle;
    private String jobLocation;
    private String department;
    private String primarySkill;
    private String jobDescription;

    private String candidateFirstName;
    private String candidateLastName;
    private String mobileNumber;
    private String candidateEmail;
    private String candidateCity;
    private String experience;
    private String fileName;
    private String fileContent;

    private String recruiterName;
    private String recruiterEmail;
    private String hmName;
}
