/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.JobAnalytics;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor

public class CandidateSearchBean
{
    private Long companyId;
    private String companyName;
    private Long candidateId;
    private String candidateName;
    @Transient
    private String mobile;
    @Transient
    private String email;
    private Set<String> skills;
    private Double minExperience;
    private Double maxExperience;
    private Double experienceFromDb;
    private Long noticePeriod;
    private Set<String> locations;
    private Set<String> qualifications;
    private Date sourcedOn;

    public CandidateSearchBean(Long companyId, String companyName, Long candidateId, String candidateName, String mobile, String email, Set skills, Double minExperience, Double maxExperience, Double experienceFromDb, Long noticePeriod, Set locations, Set qualifications, Date sourcedOn) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.mobile = mobile;
        this.email = email;
        this.skills = skills;
        this.minExperience = minExperience;
        this.maxExperience = maxExperience;
        this.experienceFromDb = experienceFromDb;
        this.noticePeriod = noticePeriod;
        this.locations = locations;
        this.qualifications = qualifications;
        this.sourcedOn = sourcedOn;
    }


}
