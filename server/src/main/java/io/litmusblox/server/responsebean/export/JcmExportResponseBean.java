/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.responsebean.export;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.litmusblox.server.constant.IConstant;
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
@JsonPropertyOrder({
        "Candidate Name",
        "Chatbot Status",
        "Chatbot Filled Timestamp",
        "Stage",
        "Key Skills Strength",
        "Current Company",
        "Current Designation",
        "Email",
        "Country Code",
        "Mobile",
        "Total Experience",
        "Created By",
        "Created On",
        "Capability Score",
        "Chatbot Link",
        "Interview Date",
        "Interview Location",
        "Candidate Confirmation",
        "Candidate Confirmation Time",
        "Show No Show",
        "No Show Reason",
        "Cancelled",
        "Cancellation Reason"
})
public class JcmExportResponseBean {
    @Id
    private Long jcmId;
    private Long jobId;

    @JsonProperty("Candidate Name")
    private String candidateName;

    @JsonProperty("Chatbot Status")
    private String chatbotStatus;

    @JsonProperty("Chatbot Link")
    private String chatbotLink;

    @JsonProperty("Chatbot Filled Timestamp")
    @JsonFormat(pattern = IConstant.DATE_FORMAT)
    private Date chatbotFilledTimestamp;

    @JsonProperty("Key Skills Strength")
    private Long keySkillsStrength;

    @JsonProperty("Stage")
    private String currentStage;

    @JsonProperty("Current Company")
    private String currentCompany;

    @JsonProperty("Current Designation")
    private String currentDesignation;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Country Code")
    private String countryCode;

    @JsonProperty("Mobile")
    private String mobile;

    @JsonProperty("Total Experience")
    private Long totalExperience;

    @JsonProperty("Created By")
    private String createdBy;

    @JsonProperty("Created On")
    @JsonFormat(pattern = IConstant.DATE_FORMAT)
    private Date createdOn;

    @JsonProperty("Capability Score")
    private Long capabilityScore;

    @JsonProperty("Interview Date")
    @JsonFormat(pattern = IConstant.DATE_FORMAT)
    private Date interviewDate;

    @JsonProperty("Interview Type")
    private String interviewType;

    @JsonProperty("Interview Mode")
    private String interviewMode;

    @JsonProperty("Interview Location")
    private String interviewLocation;

    @JsonProperty("Candidate Confirmation")
    private String candidateConfirmation;

    @JsonProperty("Candidate Confirmation Time")
    @JsonFormat(pattern = IConstant.DATE_FORMAT)
    private Date candidateConfirmationTime;

    @JsonProperty("Show No Show")
    private String showNoShow;

    @JsonProperty("No Show Reason")
    private String noShowReason;

    @JsonProperty("Cancelled")
    @JsonFormat(pattern = IConstant.DATE_FORMAT)
    private Date cancelled;

    @JsonProperty("Cancellation Reason")
    private String cancellationReason;

    @Transient
    List<JcmExportQAResponseBean> jcmExportQAResponseBeans;
}
