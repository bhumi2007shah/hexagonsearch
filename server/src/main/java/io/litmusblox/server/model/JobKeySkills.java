/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

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

    @Column(name = "NEIGHBOUR_SKILLS", columnDefinition = "varchar[]")
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    private String[] neighbourSkills;

    public JobKeySkills(SkillsMaster skillId, @NotNull Boolean selected, @NotNull Date createdOn, @NotNull User createdBy, @NotNull Long jobId, String[] neighbourSkills) {
        this.skillId = skillId;
        this.selected = selected;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.jobId = jobId;
        this.neighbourSkills = neighbourSkills;
    }

    public JobKeySkills(TempSkills skillIdFromTemp, @NotNull Boolean selected, @NotNull Date createdOn, @NotNull User createdBy, @NotNull Long jobId) {
        this.skillIdFromTemp = skillIdFromTemp;
        this.selected = selected;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.jobId = jobId;
    }
}
