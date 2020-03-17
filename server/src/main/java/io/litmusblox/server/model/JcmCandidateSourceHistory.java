/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author : sameer
 * Date : 16/03/20
 * Time : 5:20 PM
 * Class Name : JcmCandidateSourceHistory
 * Project Name : server
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "JCM_CANDIDATE_SOURCE_HISTORY")
public class JcmCandidateSourceHistory {
    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "JOB_CANDIDATE_MAPPING_ID")
    private Long jobCandidateMappingId;

    @NotNull
    @Column(name = "CANDIDATE_SOURCE")
    private String candidateSource;

    @Column(name = "CREATED_ON")
    @CreationTimestamp
    private Date createdOn;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CREATED_BY")
    private User createdBy;

    public JcmCandidateSourceHistory(Long jcmId, String candidateSource, User loggedInUser) {
        this.jobCandidateMappingId = jcmId;
        this.candidateSource = candidateSource;
        this.createdBy = loggedInUser;
    }
}
