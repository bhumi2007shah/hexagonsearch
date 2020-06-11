/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.responsebean.export;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * @author : sameer
 * Date : 10/06/20
 * Time : 2:41 PM
 * Class Name : JcmExportResponseBean
 * Project Name : server
 */
@Entity
@Data
@Table(name = "export_data_view")
@JsonFilter("JcmExportResponseBean")
public class JcmExportResponseBean {
    @Id
    private Long jcmId;
    private Long jobId;
    private String candidateName;
    private String chatbotStatus;
    private String chatbotLink;
    private Date chatbotFilledTimestamp;
    private Long keySkillsStrength;
    private String currentStage;
    private String currentCompany;
    private String currentDesignation;
    private String email;
    private String countryCode;
    private String mobile;
    private Long totalExperience;
    private String createdBy;
    private Date createdOn;
    private Long capabilityScore;
    private Date interviewDate;
    private String interviewType;
    private String interviewMode;
    private String interviewLocation;
    private String candidateConfirmation;
    private Date candidateConfirmationTime;
    private String showNoShow;
    private String noShowReason;
    private Date cancelled;
    private String cancellationReason;

    @Transient
    List<JcmExportQAResponseBean> jcmExportQAResponseBeans;

}
