/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

@Data
public class CompanyCandidateBean {
    private String companyJobId;
    private String numberToHire;
    private String jobTitle;
    private String jobLocation;
    private String department;
    private String primarySkill;
    private String jobDescription;

    private String candidateFirstName;
    private String candidateFullName;
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
