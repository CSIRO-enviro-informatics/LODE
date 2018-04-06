# Install
For the moment, these instructions are for Ubuntu 16.04 only.

## Installing on an AWS EC2 server

* OS: Ubuntu 16.04
* using a public IP address
* 1GB RAM, 8GB storage


### Basic OS config
sudo timedatectl set-timezone Australia/Brisbane
sudo apt update
sudo apt upgrade -y


### Get repo, build WAR
cd ~
mkdir lode

#### Git
sudo apt install -y git
git clone https://github.com/CSIRO-enviro-informatics/LODE.git lode

#### Built WAR
mvn ....


### Install server software
#### Tomcat 8
sudo apt install -y tomcat8
sudo apt install -y tomcat8-admin
sudo nano /var/lib/tomcat8/conf/tomcat-users.xml
# add in         <role rolename="manager-gui"/>
# add in         <user username="admin" password="{TOMCAT_ADMIN_PWD}" roles="manager-gui,admin-gui"/>
sudo service tomcat8 restart

#### install the LODE application in Tomcat
sudo cp ~/lode/lode.war /var/lib/tomcat8/webapps/

after WAR file extraction...

#### test the LODE Tomcat app deployment
 <INSTALLATION_IP_ADDRESS>:8080/lode/extract?url=http://xmlns.com/foaf/spec/index.rdf -- you should build an HTML version of the FOAF ontology!


#### Apache 2
Apache is used to host the PHP forms used by this application and also the static web content needed for theming LODE outputs (CSS/JS).

sudo apt install -y apache2

##### Host the static content
sudo cp -r ~/lode/theme/static /var/www/html/

#### Get PHP forms working
`# sudo apt install php # PHP7`  
`# sudo apt install libapache2-mod-php`
`# sudo cp webforms/* /var/www/html/ # copy the forms to Apache's web dirs`
