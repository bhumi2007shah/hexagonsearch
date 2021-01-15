#! /bin/bash

threshold_percent=80
APIKEY=15e87b7b-72e4-4284-b9d1-cff36721f823
FROM="prashant.agrawal@litmusblox.io"
TO="prashant.agrawal@litmusblox.io"
BASEURL="https://api.elasticemail.com/v2/email/send"
MAIL_CONTENT="This mail is to notify that $threshold_percent% of disk space is used up."
SUBJECT="$threshold_percent% disk space used"
percent_disk_space_used=$(df -h --total| grep total|tr -d %|awk '{print $5}')

if [ $percent_disk_space_used -ge $threshold_percent ]
then
    params="apikey=$APIKEY&subject=$SUBJECT&from=$FROM&msgTo=$TO&bodyText=$MAIL_CONTENT"
    curl \
    --data "apikey=$APIKEY" \
    --data-urlencode "subject=$SUBJECT" \
    --data-urlencode "bodyText=$MAIL_CONTENT" \
    --data "from=$FROM" \
    --data "msgTo=$TO" \
     $BASEURL
else
    echo "Disk space within range"
fi
