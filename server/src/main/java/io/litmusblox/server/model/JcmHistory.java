package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

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
    @Column(name = "DETAILS")
    private String details;

    @Column(name = "UPDATED_ON")
    private Date updatedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPDATED_BY")
    private User userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE")
    private JobStageStep stage;

    public JcmHistory() {
    }

    public JcmHistory(JobCandidateMapping jcmId, @NotNull String details, Date updatedOn, User userId, JobStageStep stage) {
        this.jcmId = jcmId;
        this.details = details;
        this.updatedOn = updatedOn;
        this.userId = userId;
        this.stage = stage;
    }
}
