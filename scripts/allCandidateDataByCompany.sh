#!/bin/bash

#Extract raw data from postgres

  dataFile="LTI_AllCandidateData.csv"

  #Candidate data
  echo $dataFile
  psql -d litmusblox --user=postgres -c 'refresh materialized view candidate_skills_by_candidate_id'
  psql -d litmusblox --user=postgres -c 'refresh materialized view job_skill_by_company'
  psql -d litmusblox --user=postgres -c 'refresh materialized view tech_question_category_by_company_job'
  psql -d litmusblox --user=postgres -c "\copy (select j.job_title, j.id as job_id, j.created_on, CONCAT(createdBy.first_name, ' ', createdBy.last_name) as created_by, j.status, questionCategoryView.questionCategories, jobSkills.skills as jobSkills, candidateSkills.skills as candidateSkills, jcm.candidate_id, jcm.candidate_first_name, jcm.candidate_last_name, jcm.email, jcm.mobile, jcm.candidate_source, jcm.chatbot_status, COALESCE(jcd.chat_invite_timestamp_email,jcd.chat_invite_timestamp_sms) as chatInviteTimestamp, COALESCE(jcd.chat_complete_timestamp_email,jcd.chat_complete_timestamp_sms) as chatCompleteTimestamp from job j inner join users createdBy on j.created_by = createdBy.id  inner join job_candidate_mapping jcm on j.id = jcm.job_id inner join jcm_communication_details jcd on jcd.jcm_id = jcm.id left join tech_question_category_by_company_job questionCategoryView on j.company_id = questionCategoryView.company_id and j.id = questionCategoryView.job_id left join job_skill_by_company jobSkills on jobSkills.company_id = j.company_id and j.id = jobSkills.job_id left join candidate_skills_by_candidate_id candidateSkills on jcm.candidate_id = candidateSkills.candidate_id where j.company_id = 25 order by job_id, candidate_id) to '$dataFile' csv header"


