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
import java.io.Serializable;

/**
 * @author : oem
 * Date : 3/7/19
 * Time : 3:44 PM
 * Class Name : MasterData
 * Project Name : server
 */
@Data
@Entity
@Table(name = "MASTER_DATA")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterData implements Serializable {

    private static final long serialVersionUID = 6868521896546285041L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "TYPE")
    private String type;

    @NotNull
    @Column(name = "VALUE")
    private String value;

    @Column(name = "VALUE_TO_USE")
    private String valueToUSe;

    @Column(name = "COMMENTS")
    private String comments;
}
