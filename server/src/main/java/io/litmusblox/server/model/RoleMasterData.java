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
 * Class Name : RoleMasterData
 * Project Name : server
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ROLE_MASTER_DATA")
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RoleMasterData {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "ROLE")
    private String role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FUNCTION")
    private FunctionMasterData function;
}
