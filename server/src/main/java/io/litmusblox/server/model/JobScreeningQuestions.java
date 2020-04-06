/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.litmusblox.server.utils.Util;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * @author : Sumit
 * Date : 4/7/19
 * Time : 12:53 PM
 * Class Name : JobScreeningQuestions
 * Project Name : server
 */
@Data
@Log4j2
@Entity
@Table(name = "JOB_SCREENING_QUESTIONS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonFilter("JobScreeningQuestions")
public class JobScreeningQuestions implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "JOB_ID")
    private Long jobId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MASTER_SCREENING_QUESTION_ID")
    private ScreeningQuestions masterScreeningQuestionId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_SCREENING_QUESTION_ID")
    private CompanyScreeningQuestion companyScreeningQuestionId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_SCREENING_QUESTION_ID")
    private UserScreeningQuestion userScreeningQuestionId;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date createdOn;

    @NotNull
    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date updatedOn;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Type(type="hstore")
    @Column(name = "CUSTOMIZE_QUESTION_DATA", columnDefinition = "hstore")
    private Map<String,String> customizeQuestionData = new HashMap<>();

    @JsonProperty
    @Transient
    private List<String> candidateResponse = new ArrayList<>();

    //If customize question is add then parse this question
    public ScreeningQuestions getMasterScreeningQuestionId(){
        log.info("Inside getUserScreeningQuestionId");
        if(null != this.customizeQuestionData && null != this.masterScreeningQuestionId.getCustomizeQuestion()){
            Util.parseCustomizeQuestion(this.customizeQuestionData, this.masterScreeningQuestionId);
        }
        return this.masterScreeningQuestionId;
    }

}
