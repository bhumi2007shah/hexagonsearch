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
  (
    case
    when
    jcm.rejected='t' then
    'Rejected'
    else
    ssm.stage
    end
  ) as currentStage,
  currentCompany.company_name as currentCompany,
  currentCompany.designation as currentDesignation,
  jcm.email as email,
  jcm.country_code as countryCode,
  jcm.mobile as mobile,
  cd.total_experience as totalExperience,
  concat(users.first_name, ' ', users.last_name) as createdBy,
  jcm.created_on as createdOn,
  jcm.score as capabilityScore,
  (ivd.interviewDate + interval '5 hour 30 minute') as interviewDate,
  ivd.interviewType,
  ivd.interviewMode,
  ivd.interviewLocation,
  ivd.candidateConfirmation,
  ivd.candidateConfirmationTime,
  ivd.showNoShow,
  ivd.noShowReason,
  ivd.cancelled,
  ivd.cancellationReason,
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
    left join stage_step_master ssm on ssm.id=jcm.stage
    left join (
      select jsq.id as jsqId, job_id jsqJobId , question as ScreeningQn from job_screening_questions jsq inner join screening_question msq on jsq.master_screening_question_id = msq.id
      union
      select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn from job_screening_questions jsq inner join user_screening_question usq on jsq.user_screening_question_id=usq.id
      union
      select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn from job_screening_questions jsq inner join company_screening_question csq ON csq.id = jsq.company_screening_question_id
    ) as jsq on jsq.jsqJobId = jcm.job_id
    left join
    candidate_screening_question_response csqr on csqr.job_screening_question_id = jsq.jsqId and csqr.job_candidate_mapping_id = jcm.id
    left join (
      select ivd.job_candidate_mapping_id as jcm_id, ivd.interview_date as interviewDate, ivd.interview_type as interviewType,
     ivd.interview_mode as interviewMode, ca.address as interviewLocation,
     (
        CASE
        WHEN ivd.candidate_confirmation_time is not null THEN
        md.value
        ELSE
        null
        END
      ) as candidateConfirmation,
      ivd.candidate_confirmation_time as candidateConfirmationTime,
      (CASE
        WHEN ivd.show_no_show = 't' THEN
        'Show'
        WHEN ivd.show_no_show = 'f' THEN
        'No Show'
        ELSE
        null
        END)as showNoShow, (select value from master_data where id=ivd.no_show_reason) as noShowReason,
       (
          CASE
          WHEN ivd.cancelled = 't' THEN
          ivd.updated_on
          ELSE
          null
          END
        ) as cancelled, (select value from master_data where id=ivd.cancellation_reason) as cancellationReason
        from interview_details ivd left join  company_address ca on ivd.interview_location = ca.id
        left join master_data md on ivd.candidate_confirmation_value = md.id
        where ivd.id in (
          select max(id) from interview_details group by job_candidate_mapping_id
        )
    )as ivd on ivd.jcm_id = jcm.id
  order by jobId, email, jsq.jsqId;
