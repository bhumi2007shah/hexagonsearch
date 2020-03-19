/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

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
}
