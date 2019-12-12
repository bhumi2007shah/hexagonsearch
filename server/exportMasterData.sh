#!/bin/sh
echo " "
date
echo "creating master data sql script"

# Capabilities + Complexities master data to ML #40
# this file created an sql script in apache folder that can be downloaded anywhere using url litmusblox.io/capabilityComplexityMaster.sql
# If you want to add more table add in below script " -t <table_name> "

# export psql format create and insert script to root folder
PGPASSWORD="H#X@g0nL1tmu$" pg_dump -h localhost -p 5432 -U postgres -t public.capability_master -t public.complexity_master -t public.level_master -t public.capability_complexity_mapping -t public.complexity_scenario -t public.level_capability_scenario_mapping -d scoringengine> /home/lbprod/capabilityComplexityMaster.psql

# change directory to /tmp/pg2mysql-master
cd /tmp/pg2mysql-master

# convert exported psql file to mysql
php pg2mysql_cli.php /home/lbprod/capabilityComplexityMaster.psql /home/lbprod/capabilityComplexityMaster.sql

#remove public. from table names so it could run in mysql
sed -i -e 's/public.//g' /home/lbprod/capabilityComplexityMaster.sql

# copy scoringEngineMaster file to apache root directory
sudo cp /home/lbprod/capabilityComplexityMaster.sql /var/www/html/capabilityComplexityMaster.sql