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

/**
 * @author : Sumit
 * Date : 3/7/19
 * Time : 3:59 PM
 * Class Name : ScreeningQuestions
 * Project Name : server
 */
@Data
@Entity
@Table(name = "SCREENING_QUESTION")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonFilter("ScreeningQuestions")
public class ScreeningQuestions implements Serializable {

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

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUESTION_TYPE")
    private MasterData questionType;

    @Column(name = "MULTILEVELOPTIONS")
    private String multiLevelOptions;

    @Column(name = "CUSTOMIZE_QUESTION")
    private String customizeQuestion;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUESTION_CATEGORY")
    private MasterData questionCategory;

    @Column(name = "SCORING_TYPE")
    private String scoringType;

    @Column(name = "DEFAULT_ANSWERS", columnDefinition = "varchar[]")
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    private String[] defaultAnswers;

    @Column(name = "ANSWER_SELECTION")
    private String answerSelection;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COUNTRY_ID")
    private Country countryId;
}
