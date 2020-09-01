import csv;

DELIMITER="~";
with open('/home/lbprod/dataToTeam/Tricentis2020-08-27/Tricentis615QA.csv') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=DELIMITER)
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
        print(index);
        rowData.append(row[2])

    data.insert(0, questionList)
    csv_file.close()
    newCsvFile = open('/home/lbprod/dataToTeam/Tricentis2020-08-27/Tricentis615QAFormatted.csv', 'w', newline='\n')
    obj = csv.writer(newCsvFile, delimiter=DELIMITER)
    obj.writerows(data)
    newCsvFile.close()
