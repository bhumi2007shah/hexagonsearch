/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author : Sumit
 * Date : 02/02/20
 * Time : 7:51 PM
 * Class Name : StageStepMaster
 * Project Name : server
 */
@Data
@Entity
@Table(name = "STAGE_STEP_MASTER")
public class StageStepMaster implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "STAGE")
    private String stage;

    @Column(name = "STEP")
    private String step;
}
