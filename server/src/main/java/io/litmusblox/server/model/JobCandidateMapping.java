/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Model class for JOB_CANDIDATE_MAPPING table
 *
 * @author : Shital Raval
 * Date : 10/7/19
 * Time : 2:15 PM
 * Class Name : JobCandidateMapping
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name="JOB_CANDIDATE_MAPPING")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonFilter("JobCandidateMapping")
public class JobCandidateMapping implements Serializable, Comparable {

    private static final long serialVersionUID = 6868521896546285047L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "JOB_ID")
    private Job job;

    @NotNull
    @OneToOne
    @JoinColumn(name = "CANDIDATE_ID")
    private Candidate candidate;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "STAGE")
    private StageStepMaster stage;

    @NotNull
    @Column(name = "CANDIDATE_SOURCE")
    private String candidateSource;

    @NotNull
    @Column(name="EMAIL")
    private String email;

    @Column(name="MOBILE")
    private String mobile;

    @NotNull
    @Column(name="COUNTRY_CODE")
    private String countryCode;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="CREATED_BY")
    private User createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="UPDATED_BY")
    private User updatedBy;

    @Column(name="CHATBOT_UUID")
    @org.hibernate.annotations.Type(type = "pg-uuid")
    private UUID chatbotUuid;

    @Column(name="CANDIDATE_INTEREST")
    private boolean candidateInterest;

    @Column(name = "CANDIDATE_INTEREST_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date candidateInterestDate;

    @Column(name="CANDIDATE_FIRST_NAME")
    private String candidateFirstName;

    @Column(name="CANDIDATE_LAST_NAME")
    private String candidateLastName;

    @Column(name="CHATBOT_STATUS")
    private String chatbotStatus;

    @Column(name="SCORE")
    private Integer score;

    @Column(name="CHATBOT_UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatbotUpdatedOn;

    @Column(name="ALTERNATE_EMAIL")
    private String alternateEmail;

    @Column(name="ALTERNATE_MOBILE")
    private String alternateMobile;

    @Column(name="SERVING_NOTICE_PERIOD")
    private boolean servingNoticePeriod;

    @Column(name="NEGOTIABLE_NOTICE_PERIOD")
    private boolean negotiableNoticePeriod;

    @Column(name="OTHER_OFFERS")
    private boolean otherOffers;

    @Column(name="UPDATE_RESUME")
    private boolean updateResume;

    @Column(name="COMMUNICATION_RATING")
    private Integer communicationRating = 0;

    @Column(name = "REJECTED")
    private boolean rejected;

    @Column(name="REASON_FOR_CHANGE")
    private String reasonForChange;

    @Column(name = "CV_FILE_TYPE")
    private String cvFileType;

    @Column(name = "AUTOSOURCED")
    private boolean autoSourced;

    @OneToOne(cascade = {CascadeType.MERGE},fetch = FetchType.LAZY, mappedBy = "jobCandidateMappingId")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private CandidateTechResponseData techResponseData;

    @OneToOne(cascade = {CascadeType.MERGE},fetch = FetchType.LAZY, mappedBy = "jobCandidateMappingId")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private CandidateReferralDetail candidateReferralDetail;

    @OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "jobCandidateMappingId")
    @OrderBy("id DESC")
    private List<InterviewDetails> interviewDetails = new ArrayList<>(0);

    @Transient
    @JsonProperty
    private JcmCommunicationDetails jcmCommunicationDetails;

    @Transient
    @JsonProperty
    List<JcmProfileSharingDetails> interestedHiringManagers = new ArrayList<>();

    @Transient
    @JsonProperty
    List<JcmProfileSharingDetails> notInterestedHiringManagers = new ArrayList<>();

    @Transient
    @JsonProperty
    List<JcmProfileSharingDetails> notRespondedHiringManagers = new ArrayList<>();

    @Transient
    @JsonProperty
    CvRating cvRating;

    @Transient
    @JsonProperty
    Map<Integer, Map<String, Integer>> candidateSkillsByRating;

    @Transient
    @JsonProperty
    List<String> candidateKeySkills = new ArrayList<>();

    @Transient
    @JsonProperty
    private Date hiringManagerInterestDate;

    @Transient
    @JsonProperty
    private String cvLocation;

    @Transient
    private String inviteErrorMessage;

    public String getDisplayName() {
        return candidateFirstName + " " + candidateLastName;
    }

    public JobCandidateMapping(@NotNull Job job, @NotNull Candidate candidate, @NotNull StageStepMaster stage, @NotNull String candidateSource, @NotNull boolean autosourced, @NotNull Date createdOn, @NotNull User createdBy, UUID chatbotUuid, String candidateFirstName, String candidateLastName, String cvFileType) {
        this.job = job;
        this.candidate = candidate;
        this.stage = stage;
        this.candidateSource = candidateSource;
        this.email = candidate.getEmail();
        if(null != candidate.getMobile())
            this.mobile = candidate.getMobile();

        this.countryCode = candidate.getCountryCode();
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.chatbotUuid = chatbotUuid;
        this.candidateFirstName = candidateFirstName;
        this.candidateLastName = candidateLastName;
        this.cvFileType = cvFileType;
        this.autoSourced = autosourced;
    }

    public JobCandidateMapping(Long id) {
        this.id = id;
    }

    @Override
    public int compareTo(Object o) {
        int returnVal = 0;

        JobCandidateMapping objToCompare = null;
        if (null != o)
            objToCompare = (JobCandidateMapping)o ;

        if(null == objToCompare.getJcmCommunicationDetails() || (null == objToCompare.getJcmCommunicationDetails().getChatCompleteEmailTimestamp() && null == objToCompare.getJcmCommunicationDetails().getChatCompleteSmsTimestamp())) {
            if (null != this.getJcmCommunicationDetails() && (this.getJcmCommunicationDetails().getChatCompleteEmailTimestamp() != null || this.getJcmCommunicationDetails().getChatCompleteSmsTimestamp() != null))
                return -1;
        }

        if(null == this.getJcmCommunicationDetails() || (null == this.getJcmCommunicationDetails().getChatCompleteEmailTimestamp() && null == this.getJcmCommunicationDetails().getChatCompleteSmsTimestamp())) {
            if(null != objToCompare.getJcmCommunicationDetails())
                if(null != objToCompare.getJcmCommunicationDetails().getChatCompleteEmailTimestamp() || null != objToCompare.getJcmCommunicationDetails().getChatCompleteSmsTimestamp())
                return 1;
        }

        returnVal = -1 * this.getCreatedOn().compareTo(objToCompare.getCreatedOn());
        if(returnVal == 0)
            returnVal = this.getCandidateFirstName().compareTo(objToCompare.getCandidateFirstName());

        if(returnVal == 0)
            returnVal = this.getCandidateLastName().compareTo(objToCompare.getCandidateLastName());

        return returnVal;
    }

    public InterviewDetails getCurrentInterviewDetail(){
        if(this.getInterviewDetails().size()>0)
            return this.getInterviewDetails().get(0);

        return null;
    }
}
