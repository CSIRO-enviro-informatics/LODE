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
* `sudo apt install openjdk-9-jkd-headless`

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