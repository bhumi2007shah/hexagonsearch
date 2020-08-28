#!/bin/bash

#List of jobs which need to processed
for i in 597 599 600 601 602 603 604 605 606 607 608 609 610 611 612 613 614 615
do
  dataFile="Tricentis"
  dataFile+=$i
  QAFile=$dataFile
  QAFile+="QA.csv"
  dataFile+="Data.csv"
  tempDataFile=$dataFile".tmp"

  #Candidate data
  echo $dataFile
  query="select jcm_id, candidate_name, chatbot_status, chatbot_filled_timeStamp, current_stage, key_skills_strength, current_company, current_designation, email, country_code,mobile, total_experience, created_by, created_on, capability_score,concat('https://chatbot.litmusblox.io/#/',chatbot_link) as link from export_data_view where job_id=$i order by jcm_id"
  echo $query
  echo 'psql -d litmusblox -t -A -F"~" -c $query > $dataFile'
  psql -d litmusblox -t -A -F"~" -c "$query" > $tempDataFile
  #Add header information to the candidate data
  cat columnHeaders.txt $tempDataFile > $dataFile
  #Some data has special characters, so certain columns come as newlines. Need to remove those new lines
  sed -n "s/\n\~/\~/g" $dataFile

  #QA data
  echo $QAFile
  qaQuery="select jcm_id, screening_question, candidate_response from export_data_qa_view where jcm_id in (select id from job_candidate_mapping where job_id=$i) group by jcm_id, jsq_id, screening_question, candidate_response order by jcm_id,jsq_id"
  echo $qaQuery
  echo 'psql -d litmusblox -t -A -F"#" -c $qaQuery > $QAFile'
  psql -d litmusblox -t -A -F"#" -c "$qaQuery" > $QAFile
  #Some data has special characters, so certain columns come as newlines. Need to remove those new lines
  sed -z -n "s/\n#/#/g" $QAFile
done
