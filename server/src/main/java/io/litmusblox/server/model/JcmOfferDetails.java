/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "JCM_OFFER_DETAILS")
@JsonFilter(value = "JcmOfferDetails")
@NoArgsConstructor
public class JcmOfferDetails implements Serializable {
    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "JCM_ID")
    private JobCandidateMapping jcmId;

    @Column(name = "OFFERED_COMPENSATION")
    private Double offeredCompensation;

    @NotNull
    @Column(name = "OFFERED_ON")
    private Date offeredOn;

    @Column(name = "JOINING_ON")
    private Date joiningOn;

}
