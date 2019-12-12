/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author : sameer
 * Date : 02/12/19
 * Time : 6:28 PM
 * Class Name : ExportFormatDetail
 * Project Name : server
 */
@Entity
@Data
@Table(name = "EXPORT_FORMAT_DETAIL")
public class ExportFormatDetail {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FORMAT_ID")
    private ExportFormatMaster exportFormatMaster;

    private String columnName;

    private String header;

    private int position;

}
