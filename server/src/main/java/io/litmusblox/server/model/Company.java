/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */
package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author : Sumit
 * Date : 3/7/19
 * Time : 2:53 PM
 * Class Name : Company
 * Project Name : server
 */
@Data
@Entity
@Table(name = "COMPANY")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "COMPANY_NAME " + IErrorMessages.NULL_MESSAGE)
    @NotBlank(message = "COMPANY_NAME " + IErrorMessages.BLANK_MESSAGE)
    @Pattern(message = "COMPANY_NAME "+ IErrorMessages.COMPANY_NAME_NOT_VALID,regexp = IConstant.REGEX_FOR_COMPANY_NAME)
    @Column(name = "COMPANY_NAME")
    private String companyName;

    @NotNull
    @Column(name = "ACTIVE")
    private Boolean active;

    @Column(name = "COMPANY_DESCRIPTION")
    private String companyDescription;

    @Column(name="WEBSITE")
    private String website;

    @Column(name="LANDLINE")
    private String landline;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="INDUSTRY")
    private MasterData industry;

    @Column(name="LINKEDIN")
    private String linkedin;

    @Column(name="FACEBOOK")
    private String facebook;

    @Column(name="TWITTER")
    private String twitter;

    @Column(name="LOGO")
    private String logo;

    @Column(name="SUBSCRIPTION")
    private String subscription = IConstant.CompanySubscription.Lite.name();

    @NotNull
    @Column(name="COMPANY_TYPE")
    private String companyType = "Individual";

    @Column(name="RECRUITMENT_AGENCY_ID")
    private Long recruitmentAgencyId;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "companyId")
    private List<CompanyAddress> companyAddressList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "companyId")
    private List<CompanyBu> companyBuList;

    @Transient
    private Set<String> newCompanyBu;

    @Transient
    private Set<String> deletedCompanyBu;

    @Transient
    private List<CompanyAddress> newCompanyAddress;

    @Transient
    private List<CompanyAddress> deletedCompanyAddress;

    @Transient
    private List<CompanyAddress> updatedCompanyAddress;

    public Company(@NotNull String companyName, @NotNull Boolean active, @NotNull Date createdOn, @NotNull Long createdBy) {
        this.companyName = companyName;
        this.active = active;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
    }

    public Company(@NotNull(message = "COMPANY_NAME " + IErrorMessages.NULL_MESSAGE) @NotBlank(message = "COMPANY_NAME " + IErrorMessages.BLANK_MESSAGE) @Pattern(message = "COMPANY_NAME " + IErrorMessages.COMPANY_NAME_NOT_VALID, regexp = IConstant.REGEX_FOR_COMPANY_NAME) String companyName, @NotNull Boolean active, @NotNull String companyType, Long recruitmentAgencyId, @NotNull Date createdOn, @NotNull Long createdBy) {
        this.companyName = companyName;
        this.active = active;
        this.companyType = companyType;
        this.recruitmentAgencyId = recruitmentAgencyId;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
    }
}
