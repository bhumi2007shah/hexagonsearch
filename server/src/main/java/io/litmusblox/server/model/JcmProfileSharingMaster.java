/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Master table for Profile sharing that will store
 * 1. Sender id - user who shared the candidate profiles
 * 2. Hiring manager name
 * 3. Hiring manager email
 *
 * @author : Shital Raval
 * Date : 9/8/19
 * Time : 12:47 PM
 * Class Name : JcmProfileSharingMaster
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "JCM_PROFILE_SHARING_MASTER")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JcmProfileSharingMaster {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name="SENDER_ID")
    private Long senderId;

    @Column(name = "RECEIVER_NAME")
    private String receiverName;

    @Column(name = "RECEIVER_EMAIL")
    private String receiverEmail;

    @Column(name = "EMAIL_SENT_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailSentOn;

    @NotNull
    @Column(name="RECEIVER_ID")
    private Long receiverId;

    @Transient
    private Set<JcmProfileSharingDetails> jcmProfileSharingDetails = new HashSet<>(0);

    public JcmProfileSharingMaster(@NotNull Long senderId, @NotNull Long receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
}