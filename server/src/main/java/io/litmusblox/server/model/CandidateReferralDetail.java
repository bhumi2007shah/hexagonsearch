/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author : oem
 * Date : 26/12/19
 * Time : 5:14 PM
 * Class Name : CandidateReferralDetail
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "CANDIDATE_REFERRAL_DETAIL")
public class CandidateReferralDetail {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_CANDIDATE_MAPPING_ID")
    private JobCandidateMapping jobCandidateMappingId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="EMPLOYEE_REFERRER_ID")
    private EmployeeReferrer employeeReferrerId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REFERRER_RELATION")
    private MasterData referrerRelation;

    @NotNull
    @Column(name = "REFERRER_CONTACT_DURATION")
    private Integer referrerContactDuration;

    public CandidateReferralDetail(@NotNull JobCandidateMapping jobCandidateMappingId, @NotNull EmployeeReferrer employeeReferrerId, @NotNull MasterData referrerRelation, @NotNull Integer referrerContactDuration) {
        this.jobCandidateMappingId = jobCandidateMappingId;
        this.employeeReferrerId = employeeReferrerId;
        this.referrerRelation = referrerRelation;
        this.referrerContactDuration = referrerContactDuration;
    }
}
