/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.CandidateSkillDetailsRepository;
import io.litmusblox.server.repository.JobCandidateMappingRepository;
import io.litmusblox.server.repository.SkillMasterRepository;
import io.litmusblox.server.repository.UnverifiedSkillsRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UnverifiedSkillsService implements IUnverifiedSkillsService
{
    @Resource
    UnverifiedSkillsRepository unverifiedSkillsRepository;
    @Resource
    SkillMasterRepository skillMasterRepository;
    @Resource
    CandidateSkillDetailsRepository candidateSkillDetailsRepository;
    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;


    public List<UnverifiedSkills> getUnverifiedSkillList() throws Exception
    {

        return  unverifiedSkillsRepository.findAll();

    }

    public void curateUnverifiedSkills(List<UnverifiedSkills> unverifiedSkillsList) {
        unverifiedSkillsList.forEach(unverifiedSkills ->
        {
            if (null == skillMasterRepository.findBySkillNameIgnoreCase(unverifiedSkills.getSkill())) {
                SkillsMaster skillsMaster = new SkillsMaster(unverifiedSkills.getSkill());
                skillMasterRepository.save(skillsMaster);
            }
            for (Long candidateId :
                    unverifiedSkills.getCandiateIds()) {
                if (null != candidateSkillDetailsRepository.findByCandidateId(candidateId)) {
                    CandidateSkillDetails candidateSkillDetails = new CandidateSkillDetails(candidateId, unverifiedSkills.getSkill());
                    candidateSkillDetailsRepository.save(candidateSkillDetails);
                }
                List<JobCandidateMapping> jobCandidateMappingList = jobCandidateMappingRepository.findByCandidateId(candidateId);
                if (null != jobCandidateMappingList) {
                    jobCandidateMappingList.get(0).setCreatedOnSearchEngine(false);
                    jobCandidateMappingRepository.save(jobCandidateMappingList.get(0));
                }
            }
            unverifiedSkillsRepository.delete(unverifiedSkills);

        });
    }
}
