/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author : Shital Raval
 * Date : 19/11/19
 * Time : 1:01 PM
 * Class Name : StepsPerStage
 * Project Name : server
 */
@Entity
@Table(name = "STEPS_PER_STAGE")
@Data
public class StepsPerStage implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE_ID")
    private StageMaster stageId;

    @NotNull
    @Column(name = "STEP_NAME")
    private String stepName;
}