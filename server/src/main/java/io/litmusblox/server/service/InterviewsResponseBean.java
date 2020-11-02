/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.litmusblox.server.utils.Util;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.TimeZone;

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
    private Long jobId;
    private String jobTitle;
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date interviewDate;
    private String interviewStatus;
    @JsonInclude
    private String candidateConfirmation;
    @Type(type="int-array")
    @Column(name = "INTERVIEWERS", columnDefinition = "Integer[]")
    private int[] interviewers;
    private Long ivCreatedBy;
    public String getInterviewDateWithTimeZone(){
        return (null == this.interviewDate)?null:(Util.getDateWithTimezone(TimeZone.getTimeZone("IST"), this.interviewDate));
    }
}
