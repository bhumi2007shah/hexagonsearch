/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.service.CandidateInteractionHistory;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : Sumit
 * Date : 4/7/19
 * Time : 2:14 PM
 * Class Name : Candidate
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "CANDIDATE")
@JsonFilter("Candidate")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Candidate implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "FIRST_NAME")
    private String firstName;

    @NotNull
    @Column(name = "LAST_NAME")
    private String lastName = "-";

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

    @JsonProperty
    @Transient
    private String displayName;

    @Transient
    @JsonProperty
    private String email;

    @Transient
    @JsonProperty
    private String mobile;

    @Transient
    @JsonProperty
    private String countryCode;

    @Transient
    @JsonProperty
    private String uploadErrorMessage;

    @Transient
    @JsonProperty
    private String candidateSource = IConstant.CandidateSource.SingleCandidateUpload.getValue();

    @Transient
    @JsonProperty
    private String telephone;

    @Transient
    @JsonProperty
    private String candidateName;

    @Transient
    @JsonProperty
    private String techResponseData;

    @Transient
    @JsonProperty
    private List<CandidateInteractionHistory> candidateInteractionHistoryList = new ArrayList<>(0);

    @OneToOne(cascade = {CascadeType.MERGE},fetch = FetchType.LAZY, mappedBy = "candidateId")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private CandidateDetails candidateDetails;

    @OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "candidateId")
    @OrderBy("yearOfPassing DESC")
    private List<CandidateEducationDetails> candidateEducationDetails = new ArrayList<>(0);

    @OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "candidateId")
    @OrderBy("id ASC")
    private List<CandidateCompanyDetails> candidateCompanyDetails = new ArrayList<>(0);

    @OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "candidateId")
    private List<CandidateProjectDetails> candidateProjectDetails = new ArrayList<>(0);

    @OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "candidateId")
    private List<CandidateOnlineProfile> candidateOnlineProfiles = new ArrayList<>(0);

    @OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "candidateId")
    private List<CandidateWorkAuthorization> candidateWorkAuthorizations = new ArrayList<>(0);

    @OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "candidateId")
    private List<CandidateLanguageProficiency> candidateLanguageProficiencies = new ArrayList<>(0);

    @OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "candidateId")
    private List<CandidateSkillDetails> candidateSkillDetails = new ArrayList<>(0);

    public Candidate(@NotNull String firstName, @NotNull String lastName, String email, String mobile, String countryCode, @NotNull Date createdOn, @NotNull User createdBy) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
    }

    @JsonProperty
    @Transient
    private List<JobScreeningQuestions> screeningQuestionResponses;

    @JsonProperty
    @Transient
    private String alternateMobile;

}
