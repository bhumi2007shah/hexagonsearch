/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 26/12/19
 * Time : 4:57 PM
 * Class Name : EmployeeReferrer
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "EMPLOYEE_REFERRER")
public class EmployeeReferrer {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "FIRST_NAME")
    private String firstName;

    @NotNull
    @Column(name = "LAST_NAME")
    private String lastName;

    @NotNull
    @Column(name = "EMAIL")
    private String email;

    @NotNull
    @Column(name = "EMPLOYEE_ID")
    private String employeeId;

    @NotNull
    @Column(name = "MOBILE")
    private String mobile;

    @NotNull
    @Column(name = "LOCATION")
    private String location;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn = new Date();

    @Transient
    @JsonProperty
    private MasterData ReferrerRelation;

    @Transient
    @JsonProperty
    private Integer referrerContactDuration;

    public EmployeeReferrer(@NotNull String firstName, @NotNull String lastName, @NotNull String email, @NotNull String employeeId, @NotNull String mobile, @NotNull String location, @NotNull Date createdOn) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.employeeId = employeeId;
        this.mobile = mobile;
        this.location = location;
        this.createdOn = createdOn;
    }
}
