/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 4/7/19
 * Time : 12:35 PM
 * Class Name : JobHiringTeam
 * Project Name : server
 */
@Data
@Entity
@Table(name = "JOB_HIRING_TEAM")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JobHiringTeam implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "JOB_ID")
    private Long jobId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE_STEP_ID")
    private JobStageStep stageStepId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User userId;

    @Column(name = "SEQUENCE")
    private Long sequence;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn = new Date();

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY")
    private User createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn = new Date();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPDATED_BY")
    private User updatedBy;

    public JobHiringTeam(@NotNull Long jobId, @NotNull JobStageStep stageStepId, @NotNull User userId, Long sequence, @NotNull Date createdOn, @NotNull User createdBy) {
        this.jobId = jobId;
        this.stageStepId = stageStepId;
        this.userId = userId;
        this.sequence = sequence;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
    }

    public JobHiringTeam() {
        super();
    }
}
