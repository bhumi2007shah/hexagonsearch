/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */
package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.litmusblox.server.constant.IConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Entity class for User table
 *
 * @author : Shital Raval
 * Date : 1/7/19
 * Time : 11:18 AM
 * Class Name : User
 * Project Name : server
 *
 */
@Builder
@Data
@Entity
@Table(name = "USERS")
@JsonFilter("User")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
public class User implements Serializable, UserDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD")
    private String password;

    @NotNull
    @Column(name = "FIRST_NAME")
    private String firstName;

    @NotNull
    @Column(name = "LAST_NAME")
    private String lastName;

    @NotNull
    @Column(name = "MOBILE")
    private String mobile;

    @NotNull
    @Column(name = "ROLE")
    private String role;

    @Column(name = "DESIGNATION")
    private String designation;

    @NotNull
    @Column(name = "STATUS")
    private String status = IConstant.UserStatus.New.name();

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="COMPANY_ID")
    private Company company;

   // @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COUNTRY_ID")
    private Country countryId;

    @Column(name="USER_UUID")
    @org.hibernate.annotations.Type(type = "pg-uuid")
    private UUID userUuid;

    @Column(name="RESET_PASSWORD_FLAG")
    private boolean resetPasswordFlag;

    @Column(name="RESET_PASSWORD_EMAIL_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date resetPasswordEmailTimestamp;

    @NotNull
    @Column(name = "USER_TYPE")
    private String userType = "Recruiting";

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn = new Date();

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "COMPANY_ADDRESS_ID")
    private Long companyAddressId;

    @Column(name = "COMPANY_BU_ID")
    private Long companyBuId;

    @Transient
    @JsonProperty
    private String countryCode;

    @Transient
    @JsonProperty
    private String currentPassword;

    @Transient
    @JsonProperty
    private String confirmPassword;

    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the authorities granted to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set authorities = new HashSet();
        authorities.add(new SimpleGrantedAuthority("ROLE_"+getRole()));
        return authorities;
    }

    /**
     * Returns the username used to authenticate the user. Cannot return <code>null</code>.
     *
     * @return the username (never <code>null</code>)
     */
    @Override
    public String getUsername() {
        return getEmail();
    }

    /**
     * Indicates whether the user's account has expired. An expired account cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user's account is valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return status.equals(IConstant.UserStatus.Active.name());
    }

    /**
     * Indicates whether the user's credentials (password) has expired. Expired
     * credentials prevent authentication.
     *
     * @return <code>true</code> if the user's credentials are valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return status.equals(IConstant.UserStatus.Active.name());
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
     */
    @Override
    public boolean isEnabled() {
        return status.equals(IConstant.UserStatus.Active.name());
    }

    public User(){};

    public User(Long id, String email, String password, String firstName, String lastName, String mobile, String role, String designation, String status, Company company, Country country, UUID uuid){
        this.id=id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.role = role;
        this.designation = designation;
        this.status = status;
        this.company = company;
        this.countryId = country;
        this.userUuid = uuid;
    }
}
