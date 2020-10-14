/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Date : 07/10/20
 * Time : 3:50 PM
 * Class Name : InterviewsResponseBean
 * Project Name : server
 */

@Entity
@Data
public class InterviewsResponseBean {

    @Id
    private Long id;
    private String candidateName;
    private String email;
    @JsonInclude
    private String countryCode;
    @JsonInclude
    private String mobile;
    @JsonInclude
    private String screeningStatus;
    private Long jcmCreatedBy;
    @JsonInclude
    private Integer keySkillStrength;
    private String interviewDate;
    private String interviewStatus;
    @JsonInclude
    private String candidateConfirmation;
    @Type(type="int-array")
    @Column(name = "INTERVIEWERS", columnDefinition = "Integer[]")
    private int[] interviewers;
    private Long ivCreatedBy;
}
