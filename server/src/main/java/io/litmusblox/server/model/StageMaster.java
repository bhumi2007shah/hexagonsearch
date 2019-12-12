/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author : Shital Raval
 * Date : 19/11/19
 * Time : 12:53 PM
 * Class Name : StageMaster
 * Project Name : server
 */
@Entity
@Table(name = "STAGE_MASTER")
@Data
@JsonFilter("StageMaster")
public class StageMaster implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "STAGE_NAME")
    private String stageName;
}