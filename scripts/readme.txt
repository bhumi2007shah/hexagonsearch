Readme
1. extractQuery.sh, columnHeaders.txt need to reside in the /var/lib/postgresql folder
2. extractQuery.sh needs to be run from the postgres shell (not the inside psql)
3. processTricentisFiles, convertToTable.py, mergeFiles.py, singleConvertToTable.py needs to reside in <home director>/dataToTeam folder
4. Run extractQuery.sh first
5. Run processTricentisFiles 2nd
6. Some files may have extra new lines. You can log into vi & remove them by %s/\n#/#/g
