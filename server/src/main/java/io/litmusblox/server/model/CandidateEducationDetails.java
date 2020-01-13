/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author : Sumit
 * Date : 24/7/19
 * Time : 4:43 PM
 * Class Name : CandidateEducationDetails
 * Project Name : server
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CANDIDATE_EDUCATION_DETAILS")
@JsonFilter("CandidateEducationDetails")
public class CandidateEducationDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CANDIDATE_ID")
    private Long candidateId;

    @Column(name = "DEGREE")
    private String degree;

    @Column(name = "SPECIALIZATION")
    private String specialization;

    @Column(name = "INSTITUTE_NAME")
    private String instituteName;

    @Column(name = "YEAR_OF_PASSING")
    private String yearOfPassing;

    public CandidateEducationDetails(Long candidateId, String degree, String yearOfPassing, String instituteName, String specialization) {
        this.candidateId = candidateId;
        this.degree = degree;
        this.yearOfPassing = yearOfPassing;
        this.instituteName = instituteName;
        this.specialization = specialization;
    }

}
