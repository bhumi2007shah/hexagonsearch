DATE=$(date --date='1 days ago' '+%m-%d-%Y')
DATE2=$(date +"%m_%d_%Y")
scp lbprod@40.76.94.222:~/backups/db_litmusblox_${DATE}.sql.gz /home/lbtest/
scp lbprod@40.76.94.222:~/backups/db_scoringengine_${DATE}.sql.gz /home/lbtest/
gunzip db_litmusblox_${DATE}.sql.gz
gunzip db_scoringengine_${DATE}.sql.gz
PGPASSWORD="hexagon" echo 'ALTER DATABASE litmusblox RENAME to db_backup_litmusblox_'${DATE2} | psql -h localhost -U postgres
PGPASSWORD="hexagon" echo 'ALTER DATABASE scoringengine RENAME to db_backup_scoringengine_'${DATE2} | psql -h localhost -U postgres
sudo -u postgres createdb litmusblox
sudo -u postgres createdb scoringengine
PGPASSWORD="hexagon" psql -h localhost -U postgres litmusblox < /home/lbtest/db_litmusblox_${DATE}.sql
PGPASSWORD="hexagon" psql -h localhost -U postgres scoringengine < /home/lbtest/db_scoringengine_${DATE}.sql

