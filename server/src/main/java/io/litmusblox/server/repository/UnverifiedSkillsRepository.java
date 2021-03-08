/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.SkillsMaster;
import io.litmusblox.server.model.UnverifiedSkills;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnverifiedSkillsRepository extends JpaRepository<UnverifiedSkills, Long>
{
     UnverifiedSkills findBySkillIgnoreCase(String skillName);
}
