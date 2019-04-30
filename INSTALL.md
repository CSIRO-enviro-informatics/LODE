# Install
The install instructions have been tested on Ubuntu 16.04 and 17.10. See https://github.com/CSIRO-enviro-informatics/LODE/wiki for additional information on deployment steps for CSIRO server.

### Installed on an AWS EC2 server

* OS: Ubuntu 16.04
* using a public IP address
* 1GB RAM, 8GB storage


#### Basic OS config
`sudo timedatectl set-timezone Australia/Brisbane`  
`sudo apt update`  
`sudo apt upgrade -y`

#### Git
`sudo apt install -y git`  
`git clone https://github.com/CSIRO-enviro-informatics/LODE.git lode`

#### Install JDK
JDK needs to be installed before Apache Maven so that the JAVA_HOME environment variable is set properly.  
Check if Java is installed by typing `javac -version`.
If not installed, install the openjdk version.  
`sudo apt install openjdk-9-jkd-headless`.

#### Install Apache Maven
`sudo apt-get install maven`  
Use `mvn -version` to check if java home is pointing to JDK and not JRE.

#### Running LODE
In `/lode/src/main/java/com/edmondchuc/lode2/ExtractOntology.java`, edit lines 128-132 with the URL of the machine. For initial testing, just point the URLs at http://localhost:8080/. 

`cd ~/lode/`  
Download/update dependencies and run application: `mvn clean jetty:run`. Test the application at http://localhost:8080/.

#### Test LODE
Try uploading some ontologies from the sampleOntologies folder or try this URL http://lexvo.org/ontology.

#### Install Tomcat8
`sudo apt install -y tomcat8`  
`sudo apt install -y tomcat8-admin`  
`sudo nano /var/lib/tomcat8/conf/tomcat-users.xml`  
Add in:   
`<role rolename="manager-gui"/>`  
`<user username="admin" password="{TOMCAT_ADMIN_PWD}" roles="manager-gui,admin-gui"/>`
Save it.  
Restart Tomcat. `sudo service tomcat8 restart`.

#### Create WAR
`cd ~/lode`  
`mvn clean package`   
Copy the WAR file to Tomcat's webapps directory.  

#### Deploy WAR file
`cd ~/lode/`  
`mvn package` to create the WAR file.
`mv ~/lode/target/"war-file-name.war" ~/lode/target/ROOT.war` - Rename the WAR file to ROOT.war.  
`sudo mv /var/lib/tomcat8/webapps/ROOT /var/lib/tomcat8/webapps/ROOT_ORIG`. Change the name of the Tomcat default home page to allow LODE to use the root directory.  
`sudo cp ~/lode/target/ROOT.war /var/lib/tomcat8/webapps/`
Go to `http://localhost:8080/` to see LODE.  
Go to `http://localhost:8080/manager` for the Tomcat application manager.  
