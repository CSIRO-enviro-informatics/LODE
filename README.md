---

<p align="center"><img src="https://github.com/CSIRO-enviro-informatics/LODE/blob/master/src/main/webapp/LODE_Image.png"/></p>

---

# Live OWL Documentation Environment (LODE)
This repository is a Tomcat server application that can be used to create HTML documentation for [Web Ontology Language](https://www.w3.org/OWL/) (OWL) ontologies.  
  
| Last updated by:		| Date:		|
| ----------------------|:---------:|
| Edmond Chuc			|30/03/2018	|

---

## Table Of Contents:
* [Example Usage](#exampleUsage)
	* [Minimal LODE](#minimalLODE)
	* [Complete LODE (user-interface)](#completeLODE)
* [Complete Steps to Installing & Running LODE](#completeStepsToInstallingNRunningLODE)
	* [Installing Apache HTTP Server](#installingApacheHTTPServer)
	* [Installing PHP as an Apache Module](#installingPHP)
	* [Installing Apache Maven](#installingApacheMaven)
* [Running LODE](#runningLODE)
* [Contact](#contact)

---

## Example Usage: <a name="exampleUsage"></a>
### Minimal LODE <a name="minimalLODE"></a>
1. Launch application:

	* `mvn clean jetty:run`

2. Open a web browser and call the LODE service.
	* **DOLCE ontology**
		* `http://localhost:8080/lode/extract?url=http://www.loa.istc.cnr.it/ontologies/DOLCE-Lite.owl`  

### Complete LODE (user-interface) <a name="completeLODE"></a>
1. Run LODE on a HTTP web server.
	* Visit `http://localhost/index.html` and input a valid URL or upload a file.
	* **PROMS ontology**
		* Try uploading [proms.ttl](proms.ttl)
		* It is a very tiny ontology visualised online using LODE at <http://promsns.org/def/proms/>.
		* You can run try using a local instance of LODE to generate HTML for the local copy of PROMS and compare it with the online version made by the PROMS creator

---

## Complete Steps to Installing & Running LODE <a name="completeStepsToInstallingNRunningLODE"></a>
The following installation guide is required for running the complete version of LODE. For users seeking the minimal version of LODE, only [Installing Apache Maven](#apacheMaven) will be required.  
  
**Note:** *The following steps to installing LODE assumes that you are running a Windows 10 system. However, the installation on other operating systems should be similar.*

---

### Installing Apache HTTP Server <a name="installingApacheHTTPServer"></a>
1. [Download Apache HTTP Server 2.4.33 binaries VC15](http://www.apachelounge.com/download/win64/). **Note:** *Apache HTTP Server must be the same VC version as the PHP module.*
2. Unzip the downloaded zip file. You should see a folder named `Apache24` or *similar*.
	* Move this folder to the root of your `Local Disk (C:)`.
3. Inside the `Apache24` folder, there will be a folder named `conf`. Inside it, there should be a `CONF File` named `http.conf`. Open this file in your favourite text editor.
	* Use `ctrl+f` to find `ServerAdmin admin@example.com` and change it to `ServerAdmin admin@localhost`.
	* Remove the `#` symbol in front of `ServerName www.example.com:80` and change it to `ServerName localhost:80`.
	* Save your changes and exit.
4. Run `Command Prompt` as **administrator** 
	* On the command line, change directory to your `bin` folder in `Apache24`. **Note your Apache folder name**.
		* `cd \Apache24\bin`
	* Run the install command.
		* `httpd -k install`
		* Allow access on *private networks* when `Windows Security Alert` pops up.
		* **Don't be alarmed** when you see *"Errors reported here must be corrected before the service can be started."* It is normal.
	* Run the start command.
		* `httpd -k start`
5. Open your favourite web browser.
	* Visit [`http://localhost`](http://localhost).
	* If each step was done correctly, a page containing **It works!** will show up.
	* Open `C:\Apache24\bin` in a file explorer and run `ApacheMonitor` when you need to run the HTTP server. 

---

### Installing PHP as an Apache Module <a name="installingPHP"></a>
1. Inside your `Apache24` (or similarly named) folder, create a new folder named `temp`.
	* Create two new folders inside `temp` and name them `session` and `upload`.
2. [Download PHP](https://windows.php.net/download/) as **Thread Safe** in the same (32-bit/64-bit) architecture as your Apache HTTP Server.
3. Unzip the zipped folder and rename it to `php`.
	* Move the `php` folder to the root of your `Apache24` folder.
4. Inside the `php` folder, rename the file `php.ini-production` to `php.ini`.
	* Open your newly named `php.ini` file in your favourite text editor. 
	* Use `ctrl+f` to find `doc_root =`.
		* change it to `doc_root = "{LODE directory}"`. **Note:** Replace {LODE directory} with the file path of LODE repository on your system.
		* Example: `doc_root = "C:\Users\Edmond-PC\source\repos\LODE"`
	* Use `ctrl+f` to find `; extension_dir = "ext"`. Uncomment it and add your full `ext` path.
		* Example: `extension_dir = "C:\Apache24\php\ext"`
	* Use `ctrl+f` to find `;upload_tmp_dir =` and add the path to the upload folder that was created earlier.
		* Example: `upload_tmp_dir = "C:\Apache24\temp\upload"`
	* Use `ctrl+f` to find `;session.save_path = "/tmp"` and add the path to the session folder that was created earlier.
		* Example: `session.save_path = "C:\Apache24\temp\session"`
	* Save and exit the text file.
5. Open the `conf` folder inside `Apache24` and edit `httpd.conf` in your favourite text editor.
	* Use `ctrl+f` to find `DocumentRoot "c:/Apache24/htdocs"`. Change the path to the path of LODE.
		* Example: `DocumentRoot "C:\Users\Edmond-PC\source\repos\LODE"`
	* On the next line you should see `<Directory "c:/Apache24/htdocs">`, change the path to the path of LODE.
		* Example: `<Directory "C:\Users\Edmond-PC\source\repos\LODE">`
	* Inside the `<Directory>` body, change the parameter `AllowOverride` from `None` to `All`.
		* Example: 
			```
			<Directory>
				..
				..
				AllowOverride All
				..
				..
			</Directory>
			```
	* Use `ctrl+f` to find `DirectoryIndex index.html` and add `index.php`.
		* Example: `DirectoryIndex index.html index.php`
	* Copy the following text:
		```
		LoadModule php7_module "C:/Apache24/php/php7apache2_4.dll" 
		ScriptAlias /php/ "C:/Apache24/php/" 
		AddType application/x-httpd-php .php .php5 
		Action application/x-httpd-php "/php/php-cgi.exe" 
		SetEnv PHPRC "C:/Apache2/php" 
		PHPIniDir "C:/Apache2/php/"
		```
		* Paste it at the *absolute* end of the `httpd.conf` file.
	* Save and close the text file
6. Run `ApacheMonitor`.
	* In your favourite browser, run http://localhost/phpinfo.php. You should see PHP's system information.
7. Common issues:
	* Apache HTTP Server VC version is different to PHP VC version.
	* Run `cmd` as administrator and `cd \apache24\bin`. 
		* Run `httpd -k start` to see what error you get.

	* Make sure the `php.ini` file has the `file_uploads` directive set to on.
		* `file_uploads = On`

---

### Installing Apache Maven <a name="installingApacheMaven"></a>
1. Download the [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 10. 
	* Run the installer.
2. In `Control Panel`, open `System`. On the left, open `Advanced system settings` in the side-bar. 
	* Under **User variables**, create a new variable called `JAVA_HOME`. Give it the value of your `JDK` folder.
		* Example: `C:\Program Files\Java\jdk-10`
	* Under **System variables**, find the `Path` variable and click `Edit`. Click `New` and add the path to your `JDK\bin`.
		* Example: `C:\Program Files\Java\jdk-10\bin`
3. Download [Apache maven](https://maven.apache.org/download.cgi). Make sure you download the Binary zip file.
	* Example: `apache-maven-3.5.3-bin.zip`
	* Extract and move the Apache Maven folder to the root of your `Local Disk (C:)` drive.
4. Once again, open `Advanced system settings` like in **step 2**. 
	* Create a new **User variable** named `M2_HOME`. Give it the value of your Apache Maven folder path.
		* Example: `C:\apache-maven-3.5.3-bin`
	* Create another **User variable** named `MAVEN_HOME`. Give it the value of your Apache Maven folder path like above. 
	* Under **System variables**, find the `Path` variable and click `Edit.` Create a new path to your `apache-maven\bin` folder.
		* Example: `C:\apache-maven-3.5.3-bin\apache-maven-3.5.3\bin`
5. Check that Apache Maven was installed correctly.
	* To do this, open `Command Prompt` and type `mvn -version`. It should produce something similar to this.
	```
	C:\Users\Edmond-PC>mvn -version
	Apache Maven 3.5.3 (3383c37e1f9e9b3bc3df5050c29c8aff9f295297; 2018-02-25T05:49:05+10:00)
	Maven home: C:\apache-maven-3.5.3-bin\apache-maven-3.5.3\bin\..
	Java version: 10, vendor: Oracle Corporation
	Java home: C:\Program Files\Java\jdk-10
	Default locale: en_AU, platform encoding: Cp1252
	OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
	```
6. Congratulations, everything has been installed.

---

### Running LODE <a name="runningLODE"></a>
1. Run `Apache HTTP Server`.
2. Open `Command Prompt` and change directory to the root directory of LODE.
3. On the command line, type `mvn clean jetty:run`. The first time running this command will download all of the dependencies for LODE. Make sure to accept the `Windows Security Alert` prompt. Once finished, Maven will automatically deploy an instance of LODE in Tomcat.
4. In your favourite browser, navigate to `http://localhost/index.html` to start the LODE service.

---

## Contact <a name="contact"></a>
**Silvio Peroni**  
*Creator*  
<http://www.essepuntato.it>

---