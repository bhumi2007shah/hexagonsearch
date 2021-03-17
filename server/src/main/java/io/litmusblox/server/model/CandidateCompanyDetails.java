/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.litmusblox.server.utils.DateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 24/7/19
 * Time : 4:53 PM
 * Class Name : CandidateCompanyDetails
 * Project Name : server
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@Table(name = "CANDIDATE_COMPANY_DETAILS")
@JsonFilter("CandidateCompanyDetails")
public class CandidateCompanyDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CANDIDATE_ID")
    private Long candidateId;

    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Column(name = "DESIGNATION")
    private String designation;

    @Column(name = "SALARY")
    private String salary;

    @Column(name = "LOCATION")
    private String location;

    @JsonProperty
    @Transient
    private String noticePeriod;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NOTICE_PERIOD")
    private MasterData noticePeriodInDb;

    @Column(name = "START_DATE")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date startDate;

    @Column(name = "END_DATE")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date endDate;

    public CandidateCompanyDetails(long candidateId, String companyName, MasterData noticePeriod, String designation) {
        this.candidateId = candidateId;
        this.companyName = companyName;
        this.noticePeriodInDb = noticePeriod;
        this.designation = designation;
    }

    public CandidateCompanyDetails(Long candidateId, String companyName, Date startDate, Date endDate) {
        this.candidateId = candidateId;
        this.companyName = companyName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CandidateCompanyDetails(CandidateCompanyDetails objFromDb) {
        this.candidateId = objFromDb.getCandidateId();
        this.companyName = objFromDb.getCompanyName();
        this.designation = objFromDb.getDesignation();
        this.salary = objFromDb.getSalary();
        this.location = objFromDb.getLocation();
        this.noticePeriod = objFromDb.getNoticePeriod();
        this.noticePeriodInDb = objFromDb.getNoticePeriodInDb();
        this.startDate = objFromDb.getStartDate();
        this.endDate = objFromDb.getEndDate();
    }

    public CandidateCompanyDetails() {
        super();
    }

    public void setSalary(String salary){
        this.salary = salary.replaceAll("[^0-9.]", "");
    }
}
