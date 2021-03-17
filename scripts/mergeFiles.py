# Merge the 2 files into 1 file by the jcmid
import csv
from collections import OrderedDict
from datetime import date;

fileNames=['597','599','600','601','602','603','604','605','606','607','608','609','610','611','612','613','614','615'];
#fileNames=['597'];
today=date.today();
dirName="/home/lbprod/dataToTeam/Tricentis"+today.strftime("%Y-%m-%d");

for fileName in fileNames:
  dataFileName=dirName+"/Tricentis"+fileName+"Data.csv";
  QAFileName=dirName+"/Tricentis"+fileName+"QAFormatted.csv";
  finalFileName=dirName+"/Tricentis"+fileName+"Finale.csv";

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

