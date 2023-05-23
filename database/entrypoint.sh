#!/bin/sh
set -e

mysql -u root -p password db_swap<<EOFMYSQL
USE db-swap;
GRANT ALL ON *.* to 'user'@'172.16.254.0/255.255.255.0' IDENTIFIED BY 'password' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOFMYSQL
