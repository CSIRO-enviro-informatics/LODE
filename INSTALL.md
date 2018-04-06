# Install

## Installing on an AWS EC2 server

* OS: Ubuntu 16.04
*

sudo timedatectl set-timezone Australia/Brisbane
sudo apt update
sudo apt upgrade -y

### Apache 2
sudo apt install -y apache2


### Tomcat 8
sudo apt install -y tomcat8
sudo apt install -y tomcat8-admin
sudo nano /var/lib/tomcat8/conf/tomcat-users.xml
# add in         <role rolename="manager-gui"/>
# add in         <user username="admin" password="{TOMCAT_ADMIN_PWD}" roles="manager-gui,admin-gui"/>


### Git
sudo apt install -y git
