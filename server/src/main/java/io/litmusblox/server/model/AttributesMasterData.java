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

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ATTRIBUTES_MASTER_DATA")
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AttributesMasterData {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "JOB_ATTRIBUTE")
    private String jobAttribute;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FUNCTION")
    private FunctionMasterData function;
}
