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
public class UnverifiedSkillsService implements iUnverifiedSkillsService
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
        System.out.println("data");
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
            for (int i=0;i<unverifiedSkills.getCANDIDATEIDS().length;i++)
            {

                System.out.println("inside loop");
                if(candidateSkillDetailsRepository.findByCandidateId(unverifiedSkills.getCANDIDATEIDS()[i])!=null)
                {
                    System.out.println("inside if");
                    CandidateSkillDetails candidateSkillDetails=new CandidateSkillDetails(unverifiedSkills.getCANDIDATEIDS()[i],unverifiedSkills.getSkill());
                    candidateSkillDetailsRepository.save(candidateSkillDetails);
                }
                List<JobCandidateMapping> jobCandidateMappingList= jobCandidateMappingRepository.findByCandidateId(unverifiedSkills.getCANDIDATEIDS()[i]);
                if (jobCandidateMappingList!=null) {
                    jobCandidateMappingList.get(0).setCreatedOnSearchEngine(false);
                    jobCandidateMappingRepository.save(jobCandidateMappingList.get(0));
                }

            }





            unverifiedSkillsRepository.delete(unverifiedSkills);



        }



    }
}
