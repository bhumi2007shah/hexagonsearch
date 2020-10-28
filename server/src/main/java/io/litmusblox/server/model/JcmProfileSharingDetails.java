/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * @author : Sumit
 * Date : 02/08/19
 * Time : 12:24 PM
 * Class Name : JcmProfileSharingDetails
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "JCM_PROFILE_SHARING_DETAILS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JcmProfileSharingDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID", unique = true)
    @GeneratedValue(generator="system-uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PROFILE_SHARING_MASTER_ID")
    private JcmProfileSharingMaster profileSharingMaster;

    @NotNull
    @Column(name="JOB_CANDIDATE_MAPPING_ID")
    private Long jobCandidateMappingId;

    @Column(name = "HIRING_MANAGER_INTEREST")
    private Boolean hiringManagerInterest=false;

    @Column(name = "HIRING_MANAGER_INTEREST_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date hiringManagerInterestDate;

    @Column(name = "COMMENTS")
    private String comments;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REJECTION_REASON_ID")
    private RejectionReasonMasterData rejectionReason;

    public JcmProfileSharingDetails(JcmProfileSharingMaster profileSharingMasterId, @NotNull Long jobCandidateMappingId) {
        this.profileSharingMaster = profileSharingMasterId;
        this.jobCandidateMappingId = jobCandidateMappingId;
    }
}
