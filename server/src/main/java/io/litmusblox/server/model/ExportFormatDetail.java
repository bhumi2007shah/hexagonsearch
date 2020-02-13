/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
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

    @Column(name = "COLUMN_NAME")
    private String columnName;

    @Column(name = "HEADER")
    private String header;

    @Column(name = "POSITION")
    private int position;

    public ExportFormatDetail(String columnName, String header) {
        this.columnName = columnName;
        this.header = header;
    }
}
