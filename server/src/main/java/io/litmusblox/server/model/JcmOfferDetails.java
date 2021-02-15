/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "JCM_OFFER_DETAILS")
@JsonFilter(value = "JcmOfferDetails")
@NoArgsConstructor
public class JcmOfferDetails {
    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "JCM_ID")
    private JobCandidateMapping jcmId;

    @Column(name = "OFFERED_COMPENSATION")
    private int offeredCompensation;

    @Column(name = "OFFERED_ON")
    private Date offeredOn;

    @Column(name = "JOINING_ON")
    private Date joiningOn;

}
