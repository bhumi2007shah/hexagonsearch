#export candidateChatbotLinks.csv
PGPASSWORD="H#X@g0nL1tmu$" psql -U postgres -h localhost -d litmusblox -A -F"," -c "select
company.company_name as \"Company Name\",
job.id as JobId, job.job_title as \"Job Title\",
concat(jcm.candidate_first_name, ' ',jcm.candidate_last_name) as \"Candidate Name\",
jcm.chatbot_status as \"Chatbot Status\",
concat('https://chatbot.litmusblox.io/#/',jcm.chatbot_uuid) as \"Chatbot Link\"
from
job_candidate_mapping jcm
inner join
job on job.id = jcm.job_id
inner join
company on job.company_id = company.id
inner join
jcm_communication_details jcmCom ON jcmCom.jcm_id = jcm.id
where jcmCom.chat_invite_flag is true
order by \"Company Name\", \"Job Title\", \"Chatbot Status\";" > /home/lbprod/serverApplication/FileStore/download/candidateChatbotLinks.csv

#export hrScreeningQuestionResponses.csv
PGPASSWORD="H#X@g0nL1tmu$" psql -U postgres -h localhost -d litmusblox -U postgres -A -F"," -c "select
company.company_name as \"Company Name\",
job.id as \"Job Id\", job.job_title as \"Job Title\",
concat(jcm.candidate_first_name, ' ',jcm.candidate_last_name) as \"Candidate Name\",
concat('\"',jsq.ScreeningQn,'\"') as \"Screening Qn\", concat('\"',jsq.ScreeningOp,'\"') as \"Screening Op\",
case
        when csqr.response=jsq.ScreeningOp then 1
end
as \"Candidate Response\"
from
job_candidate_mapping jcm
inner join
company on jcm.created_by = company.created_by
inner join
job on job.id = jcm.job_id
inner join
(
        select jsq.id as jsqId, job_id jsqJobId , question as ScreeningQn, unnest(options) as ScreeningOp from job_screening_questions jsq inner join screening_question msq on jsq.master_screening_question_id = msq.id
        union
        select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn, unnest(options) as ScreeningOp from job_screening_questions jsq inner join user_screening_question usq on jsq.company_screening_question_id=usq.id
        union
        select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn, unnest(options) as ScreeningOp from job_screening_questions jsq inner join company_screening_question csq ON csq.id = jsq.company_screening_question_id
)as jsq
on jsq.jsqJobId = jcm.job_id
inner join candidate_screening_question_response csqr
on jsq.jsqId = csqr.job_screening_question_id
order by \"Company Name\", \"Job Id\", \"Candidate Name\", \"Screening Qn\";" > /home/lbprod/serverApplication/FileStore/download/hrScreeningQuestionResponses.csv

