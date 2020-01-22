DATE=$(date +"%m-%d-%Y")
PGPASSWORD="H#X@g0nL1tmu$" pg_dump -U postgres -h localhost -d litmusblox | gzip > /home/lbprod/backups/db_litmusblox_${DATE}.sql.gz
PGPASSWORD="H#X@g0nL1tmu$" pg_dump -U postgres -h localhost -d scoringengine | gzip > /home/lbprod/backups/db_scoringengine_${DATE}.sql.gz
