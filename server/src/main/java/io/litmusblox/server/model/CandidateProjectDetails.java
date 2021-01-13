/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.litmusblox.server.utils.DateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 24/7/19
 * Time : 5:01 PM
 * Class Name : CandidateProjectDetails
 * Project Name : server
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CANDIDATE_PROJECT_DETAILS")
@JsonFilter("CandidateProjectDetails")
public class CandidateProjectDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CANDIDATE_ID")
    private Long candidateId;

    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Column(name = "FROM_DATE")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date fromDate;

    @Column(name = "TO_DATE")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date toDate;

    @Column(name = "CLIENT_NAME")
    private String clientName;

    @Column(name = "ROLE")
    private String role;

    @Column(name = "ROLE_DESCRIPTION")
    private String roleDescription;

    @Column(name = "SKILLS_USED")
    private String skillsUsed;

    @Column(name = "EMPLOYMENT_STATUS")
    private String employmentStatus;

    public CandidateProjectDetails(CandidateProjectDetails objFromDb) {
        this.candidateId = objFromDb.getCandidateId();
        this.companyName = objFromDb.getCompanyName();
        this.fromDate = objFromDb.getFromDate();
        this.toDate = objFromDb.getToDate();
        this.clientName = objFromDb.getClientName();
        this.role = objFromDb.getRole();
        this.roleDescription = objFromDb.getRoleDescription();
        this.skillsUsed = objFromDb.getSkillsUsed();
        this.employmentStatus = objFromDb.getEmploymentStatus();
    }
}
