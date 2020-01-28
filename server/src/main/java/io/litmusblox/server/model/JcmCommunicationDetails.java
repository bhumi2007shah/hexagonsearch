/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author : Shital Raval
 * Date : 30/7/19
 * Time : 1:03 PM
 * Class Name : JCM_COMMUNICATION_DETAILS
 * Project Name : server
 */
@Data
@Entity
@Table(name = "JCM_COMMUNICATION_DETAILS")
@NoArgsConstructor
@JsonFilter("JcmCommunicationDetails")
public class JcmCommunicationDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @PrimaryKeyJoinColumn
    private JobCandidateMapping jcmId;

    @Column(name="CHAT_INVITE_TIMESTAMP_EMAIL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatInviteEmailTimestamp;

    @Column(name="CHAT_INVITE_TIMESTAMP_SMS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatInviteSmsTimestamp;

    @Column(name="CHAT_INCOMPLETE_REMINDER_1_TIMESTAMP_EMAIL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatIncompleteReminder1EmailTimestamp;

    @Column(name="CHAT_INCOMPLETE_REMINDER_1_TIMESTAMP_SMS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatIncompleteReminder1SmsTimestamp;

    @Column(name="CHAT_INCOMPLETE_REMINDER_2_TIMESTAMP_EMAIL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatIncompleteReminder2EmailTimestamp;

    @Column(name="CHAT_INCOMPLETE_REMINDER_2_TIMESTAMP_SMS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatIncompleteReminder2SmsTimestamp;

    @Column(name="LINK_NOT_VISITED_REMINDER_1_TIMESTAMP_EMAIL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date linkNotVisitedReminder1EmailTimestamp;

    @Column(name="LINK_NOT_VISITED_REMINDER_1_TIMESTAMP_SMS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date linkNotVisitedReminder1SmsTimestamp;

    @Column(name="LINK_NOT_VISITED_REMINDER_2_TIMESTAMP_EMAIL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date linkNotVisitedReminder2EmailTimestamp;

    @Column(name="LINK_NOT_VISITED_REMINDER_2_TIMESTAMP_SMS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date linkNotVisitedReminder2SmsTimestamp;

    @Column(name="CHAT_COMPLETE_TIMESTAMP_EMAIL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatCompleteEmailTimestamp;

    @Column(name="CHAT_COMPLETE_TIMESTAMP_SMS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chatCompleteSmsTimestamp;

    @Column(name="CHAT_INVITE_FLAG")
    private boolean chatInviteFlag;

    @Column(name="TECH_CHAT_COMPLETE_FLAG")
    private boolean techChatCompleteFlag;

    @Column(name="HR_CHAT_COMPLETE_FLAG")
    private boolean hrChatCompleteFlag;

    public JcmCommunicationDetails(JobCandidateMapping jcmId) {
        this.jcmId = jcmId;
        chatInviteFlag = false;
        techChatCompleteFlag = false;
    }
}
