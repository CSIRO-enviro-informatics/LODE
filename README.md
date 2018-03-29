## Live OWL Documentation Environment (LODE)
This repository is a Tomcat server application that can be used to create HTML documentation for [Web Ontology Language](https://www.w3.org/OWL/) (OWL) ontologies.


## Example usage:

1. Launch application:

	`mvn clean jetty:run`

2. Test

Try running LODE with the following ontologies:

* **DOLCE ontology**
	* `http://localhost:8080/lode/extract?url=http://www.loa.istc.cnr.it/ontologies/DOLCE-Lite.owl`
* **PROMS ontology**
	* contained here in the file [proms.ttl](proms.ttl)
	* it is a very tiny ontology visualised online using LODE at <http://promsns.org/def/proms/>.
	* You can run try using a local instance of LODE to generate HTML for the local copy of PROMS and compare it with the online version made by the PROMS creator


## Contacts
**Silvio Peroni**  
*Creator*  
<http://www.essepuntato.it>


## Running LODE Locally
* Requirements:
	* Install Apache HTTP Server (https://httpd.apache.org/).
		* Edit `httpd.conf` to have the `DocumentRoot` and `<Directory>` to point to the root directory of LODE.
			* `DocumentRoot "C:\Users\chu101\Desktop\LODE"`
			* `<Directory "C:\Users\chu101\Desktop\LODE">`
		* Make sure the `php.ini` file has the `file_uploads` directive set to on.
			* `file_uploads = On`
	* Install Apache Maven (https://maven.apache.org/) and configure it to run in the command line.
		* In the command line `cd` into the root directory of LODE and type `mvn clean jetty:run` to update LODE's dependencies and automatically deploy inside an instance of Tomcat (http://tomcat.apache.org/). 

With both the HTTP Server and Maven's Tomcat server running, open a web browser and go to `localhost/`. Here, you can enter a valid URL to an ontology or upload a file. 