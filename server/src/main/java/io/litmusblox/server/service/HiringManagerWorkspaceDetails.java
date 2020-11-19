/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.litmusblox.server.model.InterviewDetails;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Date : 11/11/20
 * Time : 3:30 PM
 * Class Name : HiringManagerWorkspaceDetails
 * Project Name : server
 */
@Data
@Entity
@Table(name = "HIRING_MANAGER_WORKSPACE_DETAILS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HiringManagerWorkspaceDetails {
    @Id
    Long id;
    Long jcmId;
    Long userId;
    Long jobId;
    String jobTitle;
    Long candidateId;
    String email;
    String mobile;
    String countryCode;
    Long stage;
    String stageName;
    Date createdOn;
    String candidateFirstName;
    String candidateLastName;
    String chatbotStatus;
    Integer score;
    Boolean rejected;
    @Type(type = "hstore")
    @Column(name = "CANDIDATE_CHATBOT_RESPONSE", columnDefinition = "hstore")
    Map<String, String> chatbotResponse;
    Integer overallRating;
    String companyName;
    String designation;
    String noticePeriod;
    Double totalExperience;
    String cvLocation;
    String cvFileType;
    Date updatedOn;
    String updatedBy;
    String rejectionReason;

    @Transient
    @JsonIgnore
    private List<InterviewDetails> interviewDetails = new ArrayList<>();

    public InterviewDetails getCurrentInterviewDetail(){
        if(this.getInterviewDetails().size()>0)
            return this.getInterviewDetails().get(0);

        return null;
    }
}
