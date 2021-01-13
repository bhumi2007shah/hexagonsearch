/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Date : 11/11/20
 * Time : 11:19 PM
 * Class Name : HiringManagerWorkspace
 * Project Name : server
 */
/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HIRING_MANAGER_WORKSPACE")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HiringManagerWorkspace {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name="JCM_ID")
    private Long jcmId;

    @NotNull
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "SHARE_PROFILE_ID")
    private Long shareProfileId;

    @Column(name = "SHARE_INTERVIEW_ID")
    private Long shareInterviewId;

    public HiringManagerWorkspace(@NotNull Long jcmId, @NotNull Long userId, Long shareProfileId, Long shareInterviewId) {
        this.jcmId = jcmId;
        this.userId = userId;
        this.shareProfileId = shareProfileId;
        this.shareInterviewId = shareInterviewId;
    }
}
