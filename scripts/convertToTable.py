#Convert data of question & answer per jcmid
import csv;
from datetime import date;

fileNames=['597','599','600','601','602','603','604','605','606','607','608','609','610','611','612','613','614','615'];
today=date.today();
dirName="/home/lbprod/dataToTeam/Tricentis"+today.strftime("%Y-%m-%d");

for fileName in fileNames:
  with open(dirName+'/Tricentis'+fileName+'QA.csv') as csv_file:
      print(dirName+'/Tricentis'+fileName+'QA.csv');
      csv_reader = csv.reader(csv_file, delimiter='|')
      line_count = 0;
      data = []
      questionList = ['jcmId']
      rowData = []
      for index, row in enumerate(csv_reader):
          if row[1] not in questionList:
              questionList.append(row[1])
          if row[0] not in rowData:
              if len(rowData) > 0:
                  data.append(rowData)
                  rowData = []
              rowData.append(row[0])
          rowData.append(row[2])

      data.insert(0, questionList)
      csv_file.close()
      newFile = dirName+'/Tricentis'+fileName+'QAFormatted.csv'
      newCsvFile = open(newFile, 'w', newline='\n')
      obj = csv.writer(newCsvFile, delimiter='|')
      obj.writerows(data)
      newCsvFile.close()
