--updated view for export data
drop view if exists exportDataView;
create view exportDataView AS
  select
  jcm.job_id as jobId,
  concat(jcm.candidate_first_name, ' ', jcm.candidate_last_name) as candidateName,
  jcm.chatbot_status as chatbotStatus,
  jcm.chatbot_uuid::varchar(50) as chatbotLink,
  jcm.chatbot_updated_on as chatbotFilledTimeStamp,
  cvr.overall_rating as keySkillsStrength,
  ssm.stage as currentStage,
  currentCompany.company_name as currentCompany,
  currentCompany.designation as currentDesignation,
  jcm.email,
  jcm.country_code as countryCode,
  jcm.mobile,
  cd.total_experience as totalExperience,
  concat(users.first_name, ' ', users.last_name) as createdBy,
  jcm.created_on as createdOn,
  jcm.score as capabilityScore,
  jsq.jsqId as jsqId,
  jsq.ScreeningQn as screeningQuestion,
  csqr.response as candidateResponse
  from job_candidate_mapping jcm
    left join cv_rating cvr ON cvr.job_candidate_mapping_id = jcm.id
    left join (
      select candidate_id, company_name, designation from candidate_company_details where id in (
        select min(id) from candidate_company_details
        group by candidate_id
      )
    ) as currentCompany on jcm.candidate_id = currentCompany.candidate_id
    left join candidate_details cd on cd.candidate_id = jcm.candidate_id
    inner join users ON users.id = jcm.created_by
    inner join stage_step_master ssm on ssm.id=jcm.stage
    left join (
      select jsq.id as jsqId, job_id jsqJobId , question as ScreeningQn from job_screening_questions jsq inner join screening_question msq on jsq.master_screening_question_id = msq.id
      union
      select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn from job_screening_questions jsq inner join user_screening_question usq on jsq.user_screening_question_id=usq.id
      union
      select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn from job_screening_questions jsq inner join company_screening_question csq ON csq.id = jsq.company_screening_question_id
    ) as jsq on jsq.jsqJobId = jcm.job_id
    left join
    candidate_screening_question_response csqr on csqr.job_screening_question_id = jsq.jsqId and csqr.job_candidate_mapping_id = jcm.id order by jobId, candidateName, jsq.jsqId;
