/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.CandidateSkillDetailsRepository;
import io.litmusblox.server.repository.JobCandidateMappingRepository;
import io.litmusblox.server.repository.SkillMasterRepository;
import io.litmusblox.server.repository.UnverifiedSkillsRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UnverifiedSkillsService implements IunverifiedSkillsService
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

    public void curateUnverifiedSkills(List<UnverifiedSkills> unverifiedSkillsList)
    {
        for (UnverifiedSkills unverifiedSkills:unverifiedSkillsList)
        {
            if (skillMasterRepository.findBySkillNameIgnoreCase(unverifiedSkills.getSkill())==null) {
                SkillsMaster skillsMaster = new SkillsMaster(unverifiedSkills.getSkill());
                skillMasterRepository.save(skillsMaster);
            }
            for (Long candidateId:
                     unverifiedSkills.getCandiateIds())
                {
                    if (candidateSkillDetailsRepository.findByCandidateId(candidateId) != null) {
                        CandidateSkillDetails candidateSkillDetails = new CandidateSkillDetails(candidateId, unverifiedSkills.getSkill());
                        candidateSkillDetailsRepository.save(candidateSkillDetails);
                    }
                    List<JobCandidateMapping> jobCandidateMappingList = jobCandidateMappingRepository.findByCandidateId(candidateId);
                    if (jobCandidateMappingList != null) {
                        jobCandidateMappingList.get(0).setCreatedOnSearchEngine(false);
                        jobCandidateMappingRepository.save(jobCandidateMappingList.get(0));
                    }
                }

            unverifiedSkillsRepository.delete(unverifiedSkills);



        }



    }
}
