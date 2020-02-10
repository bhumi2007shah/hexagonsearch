/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 06/02/20
 * Time : 12:20 PM
 * Class Name : InterviewerDetails
 * Project Name : server
 */
@Data
@Entity
@JsonFilter("InterviewerDetails")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Table(name = "INTERVIEWER_DETAILS")
public class InterviewerDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "INTERVIEW_ID")
    private Long interviewId;

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

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="INTERVIEWER")
    private User interviewer;
}
