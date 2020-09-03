/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 4/7/19
 * Time : 1:05 PM
 * Class Name : JobKeySkills
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "JOB_KEY_SKILLS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JobKeySkills implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SKILL_ID")
    private SkillsMaster skillId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SKILL_ID_FROM_TEMP")
    private TempSkills skillIdFromTemp;

    @NotNull
    @Column(name = "SE_PROVIDED")
    private Boolean seProvided;

    @NotNull
    @Column(name = "SELECTED")
    private Boolean selected;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date createdOn = new Date();

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY")
    @JsonIgnore
    private User createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date updatedOn = new Date();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPDATED_BY")
    @JsonIgnore
    private User updatedBy;

    @NotNull
    private Long jobId;

    public JobKeySkills(SkillsMaster skillId, @NotNull Boolean seProvided, @NotNull Boolean selected, @NotNull Date createdOn, @NotNull User createdBy, @NotNull Long jobId) {
        this.skillId = skillId;
        this.seProvided = seProvided;
        this.selected = selected;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.jobId = jobId;
    }

    public JobKeySkills(TempSkills skillIdFromTemp, @NotNull Boolean seProvided, @NotNull Boolean selected, @NotNull Date createdOn, @NotNull User createdBy, @NotNull Long jobId) {
        this.skillIdFromTemp = skillIdFromTemp;
        this.seProvided = seProvided;
        this.selected = selected;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.jobId = jobId;
    }
}
