/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 4/7/19
 * Time : 2:26 PM
 * Class Name : CandidateDetails
 * Project Name : server
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CANDIDATE_DETAILS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonFilter("CandidateDetails")
public class CandidateDetails implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "CANDIDATE_ID")
    private Candidate candidateId;

    @Column(name = "DATE_OF_BIRTH")
    private Date dateOfBirth;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "MARITAL_STATUS")
    private String maritalStatus;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "PHYSICALLY_CHALLENGED")
    private Boolean physicallyChallenged;

    @Column(name = "CURRENT_ADDRESS")
    private String currentAddress;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "PREFERRED_LOCATIONS")
    private String preferredLocations;

    @Column(name = "PREFERRED_JOB_TYPE")
    private String preferredJobType;

    @Column(name = "PREFERRED_EMPLOYMENT_STATUS")
    private String preferredEmploymentStatus;

    @Column(name = "INDUSTRY")
    private String industry;

    @Column(name = "FUNCTIONAL_AREA")
    private String functionalArea;

    @Column(name = "TOTAL_EXPERIENCE")
    private Double totalExperience;

    @Column(name = "ROLE")
    private String role;

    @Column(name = "KEY_SKILLS")
    private String keySkills;

    @Column(name = "RESUME_HEADLINE")
    private String resumeHeadline;

    @Column(name = "WORK_SUMMARY")
    private String workSummary;

    @Column(name = "OTHER_CERTIFICATES")
    private String otherCertificates;

    @Column(name = "LAST_ACTIVE")
    private Date lastActive;

    @Column(name = "CANDIDATE_TYPE")
    private String candidateType;

    @Column(name = "RELEVANT_EXPERIENCE")
    private Double relevantExperience;

    @Transient
    @JsonProperty
    private String cvLocation;

    @JsonProperty
    @Transient
    private String textCv;

    @JsonProperty
    @Transient
    private String cvFileType;

    public CandidateDetails(Date dateOfBirth, String location, Double totalExperience, Double relevantExperience, Candidate candidateId) {
        this.dateOfBirth = dateOfBirth;
        this.location = location;
        this.totalExperience = totalExperience;
        this.relevantExperience = relevantExperience;
        this.candidateId = candidateId;
    }
}
