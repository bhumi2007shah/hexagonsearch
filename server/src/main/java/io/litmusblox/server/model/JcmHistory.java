package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author : sameer
 * Date : 12/09/19
 * Time : 5:43 PM
 * Class Name : jcmHistory
 * Project Name : server
 */
@Data
@Entity
@Table(name = "JCM_HISTORY")
@NoArgsConstructor
@JsonFilter("JcmHistory")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JcmHistory {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JCM_ID")
    private JobCandidateMapping jcmId;

    @NotNull
    @Column(name = "COMMENT")
    private String comment;

    @Column(name = "CALL_LOG_OUTCOME")
    private String callLogOutCome;

    @Column(name = "SYSTEM_GENERATED")
    private Boolean systemGenerated = true;

    @Column(name = "UPDATED_ON")
    private Date updatedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPDATED_BY")
    private User userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE")
    private JobStageStep stage;

    public JcmHistory(JobCandidateMapping jcmId, @NotNull String comment, Date updatedOn, User userId, JobStageStep stage) {
        this.jcmId = jcmId;
        this.comment = comment;
        this.updatedOn = updatedOn;
        this.userId = userId;
        this.stage = stage;
    }

    public JcmHistory(JobCandidateMapping jcmId, @NotNull String comment, String callLogOutCome, Boolean systemGenerated, Date updatedOn, JobStageStep stage, User userId) {
        this.jcmId = jcmId;
        this.comment = comment;
        this.callLogOutCome = callLogOutCome;
        this.systemGenerated = systemGenerated;
        this.updatedOn = updatedOn;
        this.stage = stage;
        this.userId = userId;
    }
}
