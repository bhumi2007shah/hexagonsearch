/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author : Sumit
 * Date : 10/04/20
 * Time : 10:43 AM
 * Class Name : TechScreeningQuestion
 * Project Name : server
 */
@Data
@Entity
@Table(name = "TECH_SCREENING_QUESTION")
public class TechScreeningQuestion implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TECH_QUESTION")
    private String techQuestion;

    @Column(name = "OPTIONS", columnDefinition = "varchar[]")
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    private String[]  options;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUESTION_TYPE")
    private MasterData questionType;

    @Column(name = "MULTI_LEVEL_OPTIONS")
    private String multiLevelOptions;

    @Column(name = "QUESTION_CATEGORY")
    private String questionCategory;

    @JoinColumn(name = "JOB_ID")
    private Long jobId;
}
