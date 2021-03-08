/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 4/7/19
 * Time : 9:34 AM
 * Class Name : CompanyScreeningQuestion
 * Project Name : server
 */
@Data
@Entity
@Table(name = "COMPANY_SCREENING_QUESTION")
@JsonFilter("CompanyScreeningQuestion")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CompanyScreeningQuestion implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "QUESTION")
    private String question;

    @Column(name = "OPTIONS", columnDefinition = "varchar[]")
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    private String[]  options;

    @JoinColumn(name = "COMPANY_ID")
    private Long companyId;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUESTION_TYPE")
    private MasterData questionType;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @NotNull
    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    @Column(name="UPDATED_BY")
    private Long updatedBy;

    @Column(name = "QUESTION_CATEGORY")
    private String questionCategory;


    @Column(name = "SCORING_TYPE")
    private String scoringType;

    @Column(name = "DEFAULT_ANSWERS", columnDefinition = "varchar[]")
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    private String[] defaultAnswers;

    @Column(name = "ANSWER_SELECTION")
    private String answerSelection;

    @Column(name = "QUESTION_TAG")
    private String questionTag;

    @Column(name = "QUESTION_SEQ")
    private String questionSeq;

    @Column(name = "QUESTION_OWNER_SEQ")
    private String questionOwnerSeq;



}
