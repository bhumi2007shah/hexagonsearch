/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.litmusblox.server.model.InterviewDetails;
import io.litmusblox.server.model.JcmProfileSharingDetails;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : Shital Raval
 * Date : 18/3/20
 * Time : 5:05 PM
 * Class Name : JCMAllDetails
 * Project Name : server
 */
@Data
@Entity
public class JCMAllDetails {
    @Id
    Long id;
    Long job_id;
    Long candidate_id;
    String email;
    String mobile;
    String country_code;
    Long stage;
    Date created_on;
    String candidate_first_name;
    String candidate_last_name;
    String chatbot_status;
    Integer score;
    Boolean rejected;
    Integer overall_rating;
    String recruiter;
    String company_name;
    String designation;
    String notice_period;
    Double total_experience;
    String cv_file_type;

    @Transient
    @JsonProperty
    String cvLocation;

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

    public InterviewDetails getCurrentInterviewDetail(){
        if(this.getInterviewDetails().size()>0)
            return this.getInterviewDetails().get(0);

        return null;
    }
}
