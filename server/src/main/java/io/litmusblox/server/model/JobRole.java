/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "JOB_ROLE")
@Builder
@JsonFilter("JobRole")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JobRole {

    private static final long serialVersionUID = 6868521896546285047L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE")
    private RoleMasterData role;

    @Column(name = "JOB")
    private Long jobId;

    public JobRole(RoleMasterData role, Long oldJobId) {
        this.role = role;
        this.jobId = oldJobId;
    }
}
