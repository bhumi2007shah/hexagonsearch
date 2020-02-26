/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.litmusblox.server.utils.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * @author : Sumit
 * Date : 06/02/20
 * Time : 9:40 AM
 * Class Name : InterviewDetails
 * Project Name : server
 */
@Data
@Entity
@Table(name = "INTERVIEW_DETAILS")
@JsonFilter("InterviewDetails")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InterviewDetails implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "JOB_CANDIDATE_MAPPING_ID")
    private Long jobCandidateMappingId;

    @NotNull
    @Column(name = "INTERVIEW_TYPE")
    private String interviewType;

    @NotNull
    @Column(name = "INTERVIEW_MODE")
    private String interviewMode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="INTERVIEW_LOCATION")
    private CompanyAddress interviewLocation;

    @NotNull
    @Column(name="INTERVIEW_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date interviewDate;

    @Column(name = "INTERVIEW_INSTRUCTIONS")
    private String interviewInstruction;

    @NotNull
    @Column(name = "SEND_JOB_DESCRIPTION")
    private boolean sendJobDescription;

    @NotNull
    @Column(name = "CANCELLED")
    private boolean cancelled;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CANCELLATION_REASON")
    private MasterData cancellationReason;

    @Column(name = "SHOW_NO_SHOW")
    private boolean showNoShow;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="NO_SHOW_REASON")
    private MasterData noShowReason;

    @Column(name = "COMMENTS")
    private String comments;

    @Column(name = "INTERVIEW_REFERENCE_ID")
    @org.hibernate.annotations.Type(type = "pg-uuid")
    private UUID interviewReferenceId;

    @Column(name = "CANDIDATE_CONFIRMATION")
    private boolean candidateConfirmation;

    @Column(name="CANDIDATE_CONFIRMATION_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date candidateConfirmationTime;

    @Column(name = "CANCELLATION_COMMENTS")
    private String cancellationComments;

    @Column(name = "SHOW_NO_SHOW_COMMENTS")
    private String showNoShowComments;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CANDIDATE_CONFIRMATION_VALUE")
    private MasterData candidateConfirmationValue;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn = new Date();

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CREATED_BY")
    private User createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="UPDATED_BY")
    private User updatedBy;

    @OneToMany(cascade = {CascadeType.MERGE},fetch = FetchType.LAZY, mappedBy = "interviewId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<InterviewerDetails> interviewerDetails=new ArrayList<>();

    @Transient
    @JsonProperty
    private List<JobCandidateMapping> jobCandidateMappingList = new ArrayList<>();

    @Transient
    private List<Long> interviewIdList = new ArrayList<>();

    @Transient
    @JsonProperty
    private String confirmationText;

    public InterviewDetails(@NotNull Long jobCandidateMappingId, @NotNull String interviewType, @NotNull String interviewMode, @NotNull CompanyAddress interviewLocation, @NotNull Date interviewDate, String interviewInstruction, @NotNull boolean sendJobDescription, String comments, UUID interviewReferenceId, @NotNull Date createdOn, @NotNull User createdBy) {
        this.jobCandidateMappingId = jobCandidateMappingId;
        this.interviewType = interviewType;
        this.interviewMode = interviewMode;
        this.interviewLocation = interviewLocation;
        this.interviewDate = interviewDate;
        this.interviewInstruction = interviewInstruction;
        this.sendJobDescription = sendJobDescription;
        this.comments = comments;
        this.interviewReferenceId = interviewReferenceId;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
    }

    public Date getInterviewDateWithTimeZone(){
         return (null == this.interviewDate)?null:(Util.getDateWithTimezone(TimeZone.getTimeZone("IST"), this.interviewDate));
    }

}
