/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.service.MasterDataBean;
import io.litmusblox.server.service.SearchEngineQuestionsResponseBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
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
@TypeDefs({@TypeDef(name = "int-array",typeClass = IntArrayType.class), @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)})
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

    @Column(name = "HR_QUESTION_AVAILABLE")
    private Boolean hrQuestionAvailable = false;

    @Column(name = "RESUBMIT_HR_CHATBOT")
    private Boolean resubmitHrChatbot = false;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BU_ID")
    private CompanyBu buId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OLD_FUNCTION")
    private MasterData oldFunction;

    @Column(name = "CURRENCY")
    private String currency = "INR";

    @Column(name = "MIN_SALARY")
    private Long minSalary;

    @Column(name = "MAX_SALARY")
    private Long maxSalary;

    @Type(type = "int-array")
    @Column(name = "EDUCATION", columnDefinition = "integer[]")
    private int[] education;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "JOB_LOCATION")
    private CompanyAddress jobLocation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERVIEW_LOCATION")
    private CompanyAddress interviewLocation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERTISE")
    private MasterData expertise;

    //@NotNull(message = "Hiring Manager " + IErrorMessages.NULL_MESSAGE)
    @Type(type="int-array")
    @Column(name = "HIRING_MANAGER", columnDefinition = "Integer[]")
    @JsonInclude
    private Integer[] hiringManager;

    @Type(type = "int-array")
    @Column(name = "RECRUITER", columnDefinition = "integer[]")
    private Integer[] recruiter;

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

    @NotNull(message = "Min experience " + IErrorMessages.NULL_MESSAGE)
    @Column(name = "MIN_EXPERIENCE")
    private Long minExperience;

    @NotNull(message = "Max experience " + IErrorMessages.NULL_MESSAGE)
    @Column(name = "MAX_EXPERIENCE")
    private Long maxExperience;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_TYPE")
    private MasterData jobType = MasterDataBean.getInstance().getDefaultJobType();

    @Column(name = "JOB_REFERENCE_ID")
    @org.hibernate.annotations.Type(type = "pg-uuid")
    private UUID jobReferenceId;

    @Column(name = "CUSTOMIZED_CHATBOT")
    private boolean customizedChatbot;

    @OneToMany(cascade = {CascadeType.MERGE},fetch = FetchType.LAZY,mappedBy = "jobId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<JobHiringTeam> jobHiringTeamList=new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE},fetch= FetchType.LAZY, mappedBy = "jobId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OrderBy("id ASC")
    private List<JobScreeningQuestions> jobScreeningQuestionsList=new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "jobId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<JobKeySkills> jobKeySkillsList=new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE},fetch = FetchType.LAZY, mappedBy = "jobId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<JobCapabilities> jobCapabilityList=new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "JOB_INDUSTRY")
    private IndustryMasterData jobIndustry;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FUNCTION")
    private FunctionMasterData function;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE")
    private RoleMasterData role;

    @Column(name = "AUTO_INVITE")
    private boolean autoInvite;

    @Column(name = "VISIBLE_TO_CAREER_PAGE")
    private boolean visibleToCareerPage;

    @Type(type="jsonb")
    @Column(name = "EXPECTED_ANSWER", columnDefinition = "jsonb")
    private JsonNode expectedAnswer;

    @Transient
    @JsonInclude
    private List<String> userEnteredKeySkill=new ArrayList<>();

    @Transient
    private List<User> usersForCompany=new ArrayList<>();

    @Transient
    private String companyName;

    @Transient
    private String searchEngineErrorMessage;

    @Transient
    private Map<String,Integer> candidateCountByStage = new HashMap<>();

    @Transient
    private Map<Long,String> roles = new HashMap<>();

    @Transient
    private List<String> selectedRole;

    @Transient
    private String companyDescription;

    @Transient
    private List<List<Long>> hiringTeamStepMapping = new ArrayList<>();

    @Transient
    private String jobShortCode;

    @Transient
    @JsonProperty
    private List<String> selectedKeySkills;

    @JsonProperty
    @Transient
    private String userSelectedRole;

    @Transient
    private String experienceRange;

    @Transient
    private Boolean hasCompletedCandidate;

    @Transient
    private List<User> recruiterList;

    @Transient
    @JsonInclude
    private List<User> hiringManagerList = new ArrayList<>();

    @Transient
    @JsonProperty
    private Map<String, List<SearchEngineQuestionsResponseBean>> searchEngineSkillQuestionMap;

    @Transient
    @JsonProperty
    private Map<String, List<String>> neighbourSkillsMap;

    //Remove minExperience, maxExperience, experienceRange because add masterdata for experience
    //Also add jobdetail model in job

    public String getJobShortCode() {
        return IConstant.LB_SHORT_CODE+String.format("%0"+(IConstant.LB_SHORT_CODE_LENGTH-String.valueOf(this.getId()).length())+"d%s", 0, this.getId());
    }

    public String getExperienceRange() {
        return (this.getMinExperience()+" - "+this.getMaxExperience()+" Years");
    }
}
