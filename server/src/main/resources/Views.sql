--updated view for export data
drop view if exists export_data_view;
create view export_data_view AS
select
	jcm.id as jcm_id,
  jcm.job_id as job_id,
  concat(jcm.candidate_first_name, ' ', jcm.candidate_last_name) as candidate_name,
  jcm.chatbot_status as chatbot_status,
  jcm.chatbot_uuid::varchar(50) as chatbot_link,
  jcm.chatbot_updated_on as chatbot_filled_timeStamp,
  jcm.candidate_chatbot_response as candidate_response,
  cvr.overall_rating as key_skills_strength,
  (
    case
    when
    jcm.rejected='t' then
    'Rejected'
    else
    ssm.stage
    end
  ) as current_stage,
  currentCompany.company_name as current_company,
  currentCompany.designation as current_designation,
  jcm.email as email,
  jcm.country_code as country_code,
  jcm.mobile as mobile,
  cd.total_experience as total_experience,
  concat(users.first_name, ' ', users.last_name) as created_by,
  jcm.created_on as created_on,
  jcm.score as capability_score,
(ivd.interviewDate + interval '5 hour 30 minute') as interview_date,
ivd.interviewType as interview_type,
ivd.interviewMode as interview_mode,
ivd.interviewLocation as interview_location,
ivd.candidateConfirmation as candidate_confirmation,
ivd.candidateConfirmationTime as candidate_confirmation_time,
ivd.showNoShow as show_no_show,
ivd.noShowReason as no_show_reason,
ivd.cancelled,
ivd.cancellationReason as cancellation_reason
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
  order by job_id, email;

-- view for export data Q&A
drop view if exists export_data_qa_view;
create view export_data_qa_view AS
  select jsq.jsqId as jsq_id, jcm.id as jcm_id,
         jsq.ScreeningQn as screening_question
  from job_candidate_mapping jcm
    left join
    (
      select jsq.id as jsqId, job_id jsqJobId , question as ScreeningQn from job_screening_questions jsq inner join screening_question msq on jsq.master_screening_question_id = msq.id
      union
      select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn from job_screening_questions jsq inner join user_screening_question usq on jsq.user_screening_question_id=usq.id
      union
      select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn from job_screening_questions jsq inner join company_screening_question csq ON csq.id = jsq.company_screening_question_id
      union
      select jsq.id as jsqId, jsq.job_id as jsqJobId, tech_question as ScreeningQn from job_screening_questions jsq inner join tech_screening_question tsq ON tsq.id = jsq.tech_screening_question_id
    ) as jsq on jsq.jsqJobId = jcm.job_id;

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
	CONCAT(job.min_experience, ' - ', job.max_experience, ' Years') as jobExperience,
	education.value as education, jobKeySkillAggregation.keyskills as keyskills
from job
left join company_address
on job.job_location = company_address.id
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
job_candidate_mapping.id, job_candidate_mapping.job_id, job_candidate_mapping.candidate_id, job_candidate_mapping.email,
job_candidate_mapping.mobile, job_candidate_mapping.country_code, job_candidate_mapping.stage,
(select stage from stage_step_master where id = job_candidate_mapping.stage) as stage_name,
job_candidate_mapping.created_on, job_candidate_mapping.candidate_first_name, job_candidate_mapping.candidate_last_name,
job_candidate_mapping.chatbot_status, job_candidate_mapping.score,job_candidate_mapping.rejected,
job_candidate_mapping.candidate_chatbot_response, job_candidate_mapping.candidate_source as source,
job_candidate_mapping.candidate_rejection_value as rejection_reason, job_candidate_mapping.updated_on, (select concat(first_name, ' ', last_name) from users where id=job_candidate_mapping.updated_by) as updated_by,
cv_rating.overall_rating, concat(users.first_name,' ',users.last_name) as recruiter, candidateCompany.company_name,
candidateCompany.designation, candidateCompany.notice_period, candidate_details.total_experience,
(CASE WHEN (job_candidate_mapping.cv_file_type!='') THEN
(CONCAT('CandidateCv/',job_candidate_mapping.job_id, '/', job_candidate_mapping.candidate_id, job_candidate_mapping.cv_file_type))
else null
END) as cv_location,
job_candidate_mapping.cv_file_type as cv_file_type
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