# Install
The install instructions have been tested on Ubuntu 16.04 and 17.10.

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
`cd ~/lode/`  
Download/update dependencies and run application: `mvn clean jetty:run`. Test the application at http://localhost:8080/.

#### Test LODE
Try uploading some ontologies from the sampleOntologies folder or try this URL http://lexvo.org/ontology.

#### Host the static content
`sudo cp -r ~/lode/theme/static /var/www/html/`

### Deploying LODE in Jetty Runner (current deployment method)
#### Download Jetty Runner
`mkdir ~/jetty`  
`cd ~/jetty`  
`wget http://central.maven.org/maven2/org/eclipse/jetty/jetty-webapp/9.4.9.v20180320/jetty-webapp-9.4.9.v20180320.jar`  
(ref: http://www.baeldung.com/deploy-to-jetty)

#### Create WAR and deploy to Jetty Runner
`sudo apt install screen`
`cd ~/lode`  
`mvn clean`  
`mvn package`  
`screen java -jar ~/jetty/jetty-webapp-9.4.9.v2018320.jar ~/lode/target/lode2-0.0.1-SNAPSHOT.war`.   
Using screen allows the process to run in the background. To detatch, use `ctrl-a` followed by `d`. To resume from a new terminal, type `screen -r`. To stop jetty, inside screen use `ctrl+c`.  
(ref: https://askubuntu.com/questions/904373/how-to-use-screen-command)



### Deploying LODE in Tomcat (Not working)

#### Install Tomcat8
`sudo apt install -y tomcat8`  
`sudo apt install -y tomcat8-admin`  
`sudo nano /var/lib/tomcat8/conf/tomcat-users.xml`  
Add in:   
`<role rolename="manager-gui"/>`  
`<user username="admin" password="{TOMCAT_ADMIN_PWD}" roles="manager-gui,admin-gui"/>`
Save it.  
Restart Tomcat. `sudo service tomcat8 restart`.

#### Build WAR file
`cd ~/lode/`  
`mvn package` to create the WAR file.
`mv ~/lode/target/"war-file-name.war" ~/lode/target/ROOT.war` - Rename the WAR file to ROOT.war.  
`sudo mv /var/lib/tomcat8/webapps/ROOT /var/lib/tomcat8/webapps/ROOT_ORIG`. Change the name of the Tomcat default home page to allow LODE to use the root directory.  
`sudo cp ~/lode/target/ROOT.war /var/lib/tomcat8/webapps/`
Go to `http://localhost:8080/` to see LODE.  
Go to `http://localhost:8080/manager` for the Tomcat application manager.  
