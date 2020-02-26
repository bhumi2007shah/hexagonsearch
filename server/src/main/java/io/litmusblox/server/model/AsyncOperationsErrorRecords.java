/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : Shital Raval
 * Date : 25/2/20
 * Time : 12:49 PM
 * Class Name : AsyncOperationsErrorRecords
 * Project Name : server
 */
@Data
@Entity
@Table(name = "ASYNC_OPERATIONS_ERROR_RECORDS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
public class AsyncOperationsErrorRecords implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "JOB_ID")
    private Long jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_CANDIDATE_MAPPING_ID")
    private JobCandidateMapping jobCandidateMappingId;

    @Column(name = "CANDIDATE_FIRST_NAME")
    private String candidateFirstName;

    @Column(name = "CANDIDATE_LAST_NAME")
    private String candidateLastName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "MOBILE")
    private String mobile;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "ASYNC_OPERATION")
    private String asyncOperation;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="CREATED_BY")
    private User createdBy;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    public AsyncOperationsErrorRecords(Long jobId, String candidateFirstName, String candidateLastName, String email, String mobile, String errorMessage, String asyncOperation, @NotNull User createdBy, @NotNull Date createdOn) {
        this.jobId = jobId;
        this.candidateFirstName = candidateFirstName;
        this.candidateLastName = candidateLastName;
        this.email = email;
        this.mobile = mobile;
        this.errorMessage = errorMessage;
        this.asyncOperation = asyncOperation;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public AsyncOperationsErrorRecords(Long jobId, JobCandidateMapping jobCandidateMappingId, String errorMessage, String asyncOperation, @NotNull User createdBy, @NotNull Date createdOn) {
        this.jobId = jobId;
        this.jobCandidateMappingId = jobCandidateMappingId;
        this.errorMessage = errorMessage;
        this.asyncOperation = asyncOperation;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }
}
