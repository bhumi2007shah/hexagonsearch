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


-- view to select all required fields for search query
drop view if exists jobDetailsView;
create view jobDetailsView AS
select
	job.id as jobId,
	job.visible_to_career_page as visibleToCareerPage,
	job.company_id as companyId,
	job.job_title as jobTitle,
	job.job_type as jobType,
	job.created_on as jobCreatedOn,
	job.date_published as jobPublishedOn,
	company_address.address as jobLocation,
	company_address.city as jobLocationCity,
	company_address.state as jobLocationState,
	company_address.country as jobLocationCountry,
	exp.value as jobExperience,
	education.value as education, jobKeySkillAggregation.keyskills as keyskills
from job
left join company_address
on job.job_location = company_address.id
left join master_data exp
on job.experience_range = exp.id
left join master_data education
on education.id = ANY(job.education)
left join jobKeySkillAggregation
on job.id = jobKeySkillAggregation.jobId
where job.status = 'Live'
order by jobPublishedOn desc, jobId asc;

-- For ticket #323
drop view if exists job_candidate_mapping_all_details;
create view job_candidate_mapping_all_details
as select
job_candidate_mapping.id, job_candidate_mapping.job_id, job_candidate_mapping.candidate_id, job_candidate_mapping.email, job_candidate_mapping.mobile, job_candidate_mapping.country_code, job_candidate_mapping.stage, job_candidate_mapping.created_on, job_candidate_mapping.candidate_first_name, job_candidate_mapping.candidate_last_name, job_candidate_mapping.chatbot_status, job_candidate_mapping.score,job_candidate_mapping.rejected,
cv_rating.overall_rating, concat(users.first_name,' ',users.last_name) as recruiter, candidateCompany.company_name, candidateCompany.designation, candidateCompany.notice_period, candidate_details.total_experience,
(CASE WHEN (job_candidate_mapping.cv_file_type!='') THEN (CONCAT('CandidateCv/',job_candidate_mapping.job_id, '/', job_candidate_mapping.candidate_id, job_candidate_mapping.cv_file_type))
else null
END) as cv_location
from users,job_candidate_mapping
left join cv_rating on job_candidate_mapping.id = cv_rating.job_candidate_mapping_id
left join candidate_details on candidate_details.candidate_id = job_candidate_mapping.candidate_id
left join
	(select ccd.company_name, ccd.designation, ccd.candidate_id, master_data.value as notice_period
	from candidate_company_details ccd
	join (select min(id) as id, candidate_id from candidate_company_details group by candidate_id) singleRow
	on ccd.candidate_id = singleRow.candidate_id and ccd.id = singleRow.id
	left join master_data
    on master_data.id = ccd.notice_period
	) as candidateCompany
on candidateCompany.candidate_id = job_candidate_mapping.candidate_id
where users.id = job_candidate_mapping.created_by
order by job_candidate_mapping.created_on desc, job_candidate_mapping.candidate_first_name asc, job_candidate_mapping.candidate_last_name asc;