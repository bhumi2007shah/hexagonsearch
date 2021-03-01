/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.service.CompanyCandidateBean;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface CandidateMapper {
    @Mappings({
            @Mapping(target = "firstName",source = "entity.candidateFirstName"),
            @Mapping(target = "lastName", source = "entity.candidateLastName"),
            @Mapping(target = "candidateName",source = "entity.candidateFullName"),
            @Mapping(target = "mobile",source = "entity.mobileNumber"),
            @Mapping(target = "email",source = "entity.candidateEmail")
    })
    Candidate companyCandidateToCandidate(CompanyCandidateBean entity);
}
