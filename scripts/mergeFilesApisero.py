# Merge the 2 files into 1 file by the jcmid
import csv
from collections import OrderedDict
from datetime import date;

fileNames=['1832'];
#fileNames=['597'];
today=date.today();
dirName="/home/lbprod/dataToTeam/Apisero"+today.strftime("%Y-%m-%d");

for fileName in fileNames:
  dataFileName=dirName+"/Apisero"+fileName+"Data.csv";
  QAFileName=dirName+"/Apisero"+fileName+"QAFormatted.csv";
  finalFileName=dirName+"/Apisero"+fileName+"Finale.csv";

  with open(dataFileName, 'r') as f:
      r = csv.reader(f,delimiter='|')
      dict1 = {row[0]: row[1:] for row in r}

  with open(QAFileName, 'r') as f:
      r = csv.reader(f,delimiter='|')
      dict2 = OrderedDict((row[0], row[1:]) for row in r)

  result = OrderedDict()
  for d in (dict1, dict2):
      for key, value in d.items():
           result.setdefault(key, []).extend(value)

  with open(finalFileName, 'w') as f:
      w = csv.writer(f,delimiter='|')
      for key, value in result.items():
          w.writerow(value)

