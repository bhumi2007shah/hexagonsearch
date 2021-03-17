/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 3/7/19
 * Time : 5:08 PM
 * Class Name : CompanyAddress
 * Project Name : server
 */
@Data
@Entity
@Table(name = "COMPANY_ADDRESS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
//@JsonFilter("CompanyAddress")
public class CompanyAddress implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "ADDRESS")
    private String address;

    @NotNull
    @Column(name="ADDRESS_TITLE")
    private String addressTitle;

    @Column(name = "LATITUDE")
    private Double latitude;

    @Column(name = "LONGITUDE")
    private Double longitude;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE")
    private String state;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "AREA")
    private String area;

    @NotNull
    private Long companyId;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="ADDRESS_TYPE")
    private MasterData addressType;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn = new Date();

    @NotNull
    @Column(name="CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn = new Date();

    @Column(name="UPDATED_BY")
    private Long updatedBy;
}
