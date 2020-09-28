#!/bin/bash

dirName="Apisero"$(date '+%Y-%m-%d')
fileStorePath="/home/lbprod/serverApplication/FileStore/Apisero.zip"

mkdir -v $dirName
#Copy files extracted from postgres
sudo mv /var/lib/postgresql/Apisero* $dirName/.
#Convert QA data into 1 line per jcm
sudo python3 convertToTableApisero.py
#Merge candidate data + QA data by jcmid
sudo python3 mergeFilesApisero.py
#Remove old zip file
rm $fileStorePath
#Create a zip & store in fileStorePath so it is accessible for everybody
zip -r $fileStorePath $dirName/*Finale*
#Cleanup the created files
rm -rf $dirName