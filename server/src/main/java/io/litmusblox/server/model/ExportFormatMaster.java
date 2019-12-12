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
 * Time : 6:18 PM
 * Class Name : ExportFormatMaster
 * Project Name : server
 */
@Entity
@Table(name = "EXPORT_FORMAT_MASTER")
@Data
public class ExportFormatMaster {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    @NotNull
    private String format;

    private boolean systemSupported;
}
