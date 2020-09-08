#!/bin/bash

#Extract raw data from postgres

for i in 1832
do
  dataFile="Apisero"
  dataFile+=$i
  QAFile=$dataFile
  QAFile+="QA.csv"
  dataFile+="Data.csv"
  tempDataFile=$dataFile".tmp"

  #Candidate data
  echo $dataFile
  query="select jcm_id, candidate_name, chatbot_status, chatbot_filled_timeStamp, current_stage, key_skills_strength, current_company, current_designation, email, country_code,mobile, total_experience, created_by, created_on, capability_score,concat('https://chatbot.litmusblox.io/#/',chatbot_link) as link from export_data_view where job_id=$i order by jcm_id"
  echo $query
  echo 'psql -d litmusblox -t -A -F"|" -c $query > $dataFile'
  psql -d litmusblox -t -A -F"|" -c "$query" > $tempDataFile
  #Add columns headers to the files
  cat columnHeaders.txt $tempDataFile > $dataFile
  #Remove extra temp file
  rm $tempDataFile

  #QA data
  echo $QAFile
  qaQuery="select jcm_id, screening_question, candidate_response from export_data_qa_view where jcm_id in (select id from job_candidate_mapping where job_id=$i) group by jcm_id, jsq_id, screening_question, candidate_response order by jcm_id,jsq_id"
  echo $qaQuery
  echo 'psql -d litmusblox -t -A -F"|" -c $qaQuery > $QAFile'
  psql -d litmusblox -t -A -F"|" -c "$qaQuery" > $QAFile
  #Remove the extra new lines in some files so they process correctly downstream
  sed -ri ':a;N;$!ba;s/\n\|/\|/g' $QAFile
done
