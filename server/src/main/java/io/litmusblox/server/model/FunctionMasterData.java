/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author : Sumit
 * Date : 03/04/20
 * Time : 6:22 PM
 * Class Name : FunctionMasterData
 * Project Name : server
 */
@Data
@Entity
@Table(name = "FUNCTION_MASTER_DATA")
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
@NoArgsConstructor
public class FunctionMasterData {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "FUNCTION")
    private String function;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="INDUSTRY")
    private IndustryMasterData industry;
}
