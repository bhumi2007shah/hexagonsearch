#! /bin/bash

threshold_percent=75
APIKEY=15e87b7b-72e4-4284-b9d1-cff36721f823
FROM="prashant.agrawal@litmusblox.io"
TO="prashant.agrawal@litmusblox.io"
BASEURL="https://api.elasticemail.com/v2/email/send"
MAIL_CONTENT="This mail is to inform that Server CPU usage is more than $threshold_percent%"
SUBJECT="CPU usage more than $threshold_percent%"

cores=$(nproc) 
load=$(awk '{print $3}'< /proc/loadavg)

usage=$(echo | awk -v c="${cores}" -v l="${load}" '{print l*100/c}' | awk -F. '{print $1}')
if [[ ${usage} -ge threshold_percent ]]; 
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
        echo "CPU usage within range"
fi