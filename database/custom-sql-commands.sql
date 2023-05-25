-- USE db_swap;
CREATE USER 'gelassen'@'172.16.254.0/255.255.255.0' IDENTIFIED WITH mysql_native_password BY 'password';
GRANT ALL ON *.* to 'gelassen'@'172.16.254.0/255.255.255.0';
FLUSH PRIVILEGES;