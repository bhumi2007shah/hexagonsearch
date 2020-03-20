/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author : Sumit
 * Date : 19/03/20
 * Time : 12:39 PM
 * Class Name : RejectionReasonMasterData
 * Project Name : server
 */
@Data
@Entity
@Table(name = "REJECTION_REASON_MASTER_DATA")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonFilter("RejectionReasonMasterData")
public class RejectionReasonMasterData {

    private static final long serialVersionUID = 6868521896546285041L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "LABEL")
    private String label;

    @NotNull
    @Column(name = "VALUE")
    private String value;

    @Column(name = "TYPE")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE")
    private StageStepMaster stageId;

}
