---

<p align="center"><img src="https://i.imgur.com/kqAZtLn.png"/></p>

---

# Live OWL Documentation Environment (LODE)
This repository is a Java servlet application that creates HTML documentation for [Web Ontology Language](https://www.w3.org/OWL/) (OWL) ontologies. This project is  being updated by [Edmond Chuc](http://www.edmondchuc.com) at the [CSIRO](https://www.csiro.au/) and is being actively used by the [Australian Government Linked Data Working Group](http://www.linked.data.gov.au/) (AGLDWG).  
  
Try out the updated LODE at http://lode2.linked.data.gov.au/

---

## Table Of Contents:
* [Updates](#updates)
* [Example Usage](#exampleUsage)
	* [Minimal LODE](#minimalLODE)
	* [Complete LODE (user-interface)](#completeLODE)
* [Installation](#information)
* [Contact](#contact)

---
  
## Updates: <a name="updates"></a>
Some improvements over the original [LODE](https://github.com/essepuntato/LODE):  
* No longer requires an Apache HTTP server with PHP for file handling.
* Updated [OWLAPI](https://owlcs.github.io/owlapi/) and other dependencies (fixes this [issue](https://github.com/essepuntato/LODE/issues/4)).
* Visualisation of the ontology with [WebVOWL](http://vowl.visualdataweb.org/webvowl.html) embedded within the documentation.
* Assigns fragment identifiers with their names from the loaded ontology.
* Fixes issue with duplicate HTML title tags.
* HTML output is now formatted. ("beautified").
* Markdown no longer relies on jQuery scripts. The markdown is now parsed to HTML before serving back to the user.


<p align="center"><img src="https://github.com/CSIRO-enviro-informatics/LODE/blob/master/info/sample_top2.PNG" width="75%" height="75%"/></p>
<p align="center">Sample 1 - <a href="http://52.64.97.55/extract?url=http://www.ontologydesignpatterns.org/ont/dul/DUL.owl&webvowl=true&removeVisualiseWithLode=true">DOLCE+DnS Ultralite Ontology</a> visualised with LODE.</p>
  

<p align="center"><img src="https://github.com/CSIRO-enviro-informatics/LODE/blob/master/info/sample_vis2.PNG" width="75%" height="75%"/></p>
<p align="center">Sample 2 - WebVOWL embedded inside HTML document.</p>

---

## Example Usage: <a name="exampleUsage"></a>
### Minimal LODE <a name="minimalLODE"></a>
1. Launch application using Apache Maven:

	* `mvn clean jetty:run`

2. Open a web browser and call the LODE service.
	* **FOAF Ontology**
		* `http://localhost:8080/extract?owlapi=true&webvowl=true&url=http://xmlns.com/foaf/spec/20140114.rdf`  
		This calls the LODE service with the parameters OWLAPI and WebVOWL selected to visualise the FOAF ontology.

### Complete LODE (web interface) <a name="completeLODE"></a>
1. Run LODE on a Tomcat server.
	* Deployed example at: http://lode2.linked.data.gov.au/ 
	Try one of the examples listed on the web page.

---

## Installation and other information <a name="information"></a>
Please see [INSTALL.md](https://github.com/CSIRO-enviro-informatics/LODE/blob/master/INSTALL.md) for the list of instructions.  
  
Here is a flow chart [diagram](https://i.imgur.com/zFNqfy5.png) detailing the software architecture of LODE.  
  
Here are some [slides](https://docs.google.com/presentation/d/14s5URj_7hI5vbCtO7MIvLZiI4abt449hJ-8U_9PRmWk/edit?usp=sharing) that summarise the LODE project.

---

## Contact <a name="contact"></a>
**Silvio Peroni**  
*Creator*  
Github: <https://github.com/essepuntato/>  
Website: <http://www.essepuntato.it>

**Edmond Chuc**  
*Contributor*  
Github: <https://github.com/edmondchuc>  
Website: <http://www.edmondchuc.com>  

---