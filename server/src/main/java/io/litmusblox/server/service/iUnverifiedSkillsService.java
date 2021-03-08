/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.UnverifiedSkills;

import java.util.List;

public interface iUnverifiedSkillsService
{
     List<UnverifiedSkills> getUnverifiedSkillList() throws Exception;
     void curateUnverifiedSkills(List<UnverifiedSkills> unverifiedSkillsList);
}
