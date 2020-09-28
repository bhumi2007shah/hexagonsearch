#!/bin/bash

dirName="Tricentis"$(date '+%Y-%m-%d')
fileStorePath="/home/lbprod/serverApplication/FileStore/tricentis.zip"

mkdir -v $dirName
#Copy files extracted from postgres
sudo mv /var/lib/postgresql/Tricentis* $dirName/.
#Convert QA data into 1 line per jcm
sudo python3 convertToTable.py
#Merge candidate data + QA data by jcmid
sudo python3 mergeFiles.py
#Remove old zip file
rm $fileStorePath
#Create a zip & store in fileStorePath so it is accessible for everybody
ls $dirName/*Finale*
zip -r $fileStorePath $dirName/*Finale*
#Cleanup the created files
rm -rf $dirName