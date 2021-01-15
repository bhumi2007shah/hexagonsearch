/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

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
@JsonFilter("JcmProfileSharingDetails")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JcmProfileSharingDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "RECEIVER_NAME")
    private String receiverName;

    @Column(name = "RECEIVER_ID")
    private Long receiverId;

    @Column(name = "SENDER_ID")
    private Long senderId;

    @Column(name = "EMAIL_SENT_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailSentOn;

    public JcmProfileSharingDetails(@NotNull Long jobCandidateMappingId, Long senderId, Long receiverId, String receiverName) {
        this.jobCandidateMappingId = jobCandidateMappingId;
        this.receiverName = receiverName;
        this.receiverId = receiverId;
        this.senderId = senderId;
    }
}
