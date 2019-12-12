/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.*;

/**
 * Entity class for Job table
 *
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 9:40 AM
 * Class Name : Job
 * Project Name : server
 */
@Data
@Builder
@Entity
@Table(name = "JOB")
@JsonFilter("Job")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
public class Job implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Length(max = IConstant.JOB_ID_MAX_LENGTH, message = "Company's Job ID: length should be between 0 and " + IConstant.JOB_ID_MAX_LENGTH)
    @Pattern(message = "COMPANY_JOB_ID "+IErrorMessages.ALPHANUMERIC_MESSAGE,regexp = IConstant.REGEX_FOR_COMPANY_JOB_ID)
    @Column(name = "COMPANY_JOB_ID")
    private String companyJobId;

    @NotNull(message = "Job title " + IErrorMessages.NULL_MESSAGE)
    @Pattern(message = "Job title "+IErrorMessages.SPECIAL_CHARACTER_MESSAGE, regexp = IConstant.REGEX_FOR_JOB_TITLE)
    @Column(name = "JOB_TITLE")
    private String jobTitle;

    //set default value 1
    @Column(name = "NO_OF_POSITIONS")
    private Integer noOfPositions;

    @NotNull(message = "Job description " + IErrorMessages.NULL_MESSAGE)
    @Column(name = "JOB_DESCRIPTION")
    private String jobDescription;

    @NotNull
    @Column(name = "ML_DATA_AVAILABLE")
    private Boolean mlDataAvailable;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private Company companyId;

    @Column(name = "DATE_PUBLISHED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datePublished;;

    @NotNull
    @Column(name = "STATUS")
    private String status;

    @Column(name = "DATE_ARCHIVED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateArchived;

    @Column(name = "SCORING_ENGINE_JOB_AVAILABLE")
    private Boolean scoringEngineJobAvailable  = false;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BU_ID")
    private CompanyBu buId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FUNCTION")
    private MasterData function;

    @Column(name = "CURRENCY")
    private String currency = "INR";

    @Column(name = "MIN_SALARY")
    private Long minSalary;

    @Column(name = "MAX_SALARY")
    private Long maxSalary;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "EDUCATION")
    private MasterData education;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "JOB_LOCATION")
    private CompanyAddress jobLocation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERVIEW_LOCATION")
    private CompanyAddress interviewLocation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERTISE")
    private MasterData expertise;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="HIRING_MANAGER")
    private User hiringManager;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="RECRUITER")
    private User recruiter;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="UPDATED_BY")
    private User updatedBy;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NOTICE_PERIOD")
    private MasterData noticePeriod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERIENCE_RANGE")
    private MasterData experienceRange;

    @OneToMany(cascade = {CascadeType.MERGE},fetch = FetchType.LAZY,mappedBy = "jobId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<JobHiringTeam> jobHiringTeamList=new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE},fetch= FetchType.LAZY, mappedBy = "jobId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<JobScreeningQuestions> jobScreeningQuestionsList=new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "jobId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<JobKeySkills> jobKeySkillsList=new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE},fetch = FetchType.LAZY, mappedBy = "jobId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<JobCapabilities> jobCapabilityList=new ArrayList<>();

    @Transient
    @JsonInclude
    private List<String> userEnteredKeySkill=new ArrayList<>();

    @Transient
    private List<User> usersForCompany=new ArrayList<>();

    @Transient
    private String companyName;

    @Transient
    private String mlErrorMessage;

    @Transient
    private Map<String,Integer> candidateCountByStage = new HashMap<>();

    @Transient
    private List<String> roles;

    @Transient
    private String selectedRole;

    @Transient
    private String companyDescription;

    @Transient
    private List<List<Long>> hiringTeamStepMapping = new ArrayList<>();

    //Remove minExperience, maxExperience, experienceRange because add masterdata for experience
    //Also add jobdetail model in job
}
