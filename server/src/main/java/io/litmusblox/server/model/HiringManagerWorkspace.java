/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    public HiringManagerWorkspace(@NotNull Long jcmId, Long receiverId, Long shareProfileId) {
        this.jcmId = jcmId;
        this.shareProfileId = shareProfileId;
        this.userId = receiverId;
    }

}
