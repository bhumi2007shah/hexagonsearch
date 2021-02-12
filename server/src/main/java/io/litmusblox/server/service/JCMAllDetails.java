/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.litmusblox.server.model.CandidateScreeningQuestionResponse;
import io.litmusblox.server.model.InterviewDetails;
import io.litmusblox.server.model.JcmHistory;
import io.litmusblox.server.model.JcmProfileSharingDetails;
import lombok.Data;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author : Shital Raval
 * Date : 18/3/20
 * Time : 5:05 PM
 * Class Name : JCMAllDetails
 * Project Name : server
 */
@Data
@Entity
@Table(name = "JOB_CANDIDATE_MAPPING_ALL_DETAILS")
@JsonFilter("JCMAllDetails")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JCMAllDetails {
    @Id
    Long id;
    Long jobId;
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
    String source;
    Integer score;
    Boolean rejected;
    @Type(type = "hstore")
    @Column(name = "CANDIDATE_CHATBOT_RESPONSE", columnDefinition = "hstore")
    Map<String, String> chatbotResponse;
    Integer overallRating;
    String recruiter;
    String companyName;
    String designation;
    String noticePeriod;
    Double totalExperience;
    String cvLocation;
    String cvFileType;
    Date updatedOn;
    String updatedBy;
    String rejectionReason;
    String candidateQuickQuestionResponse;
    String screeningBy;
    Date screeningOn;
    String submittedBy;
    Date submittedOn;
    String makeOfferBy;
    Date makeOfferOn;
    String offerBy;
    String hiredBy;
    Date hiredOn;
    String rejectedBy;
    Date rejectedOn;

    @Transient
    @JsonProperty
    List<JcmProfileSharingDetails> interestedHiringManagers = new ArrayList<>();

    @Transient
    @JsonProperty
    List<JcmProfileSharingDetails> notInterestedHiringManagers = new ArrayList<>();

    @Transient
    @JsonProperty
    List<JcmProfileSharingDetails> notRespondedHiringManagers = new ArrayList<>();

    @Transient
    @JsonIgnore
    private List<InterviewDetails> interviewDetails = new ArrayList<>();

    @Transient
    List<CandidateScreeningQuestionResponse> screeningQuestionResponses;

    @Transient
    Long shareProfileId;

    @Transient
    Date profileSharedOn;

    @Transient
    List<JcmHistory> jcmHistories;

    public InterviewDetails getCurrentInterviewDetail(){
        if(this.getInterviewDetails().size()>0)
            return this.getInterviewDetails().get(0);

        return null;
    }
}
