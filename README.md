---

<p align="center"><img src="https://i.imgur.com/kqAZtLn.png"/></p>

---

# Live OWL Documentation Environment (LODE)
This repository is a Java servlet application that creates HTML documentation for [Web Ontology Language](https://www.w3.org/OWL/) (OWL) ontologies. This project is  being updated by [Edmond Chuc](http://www.edmondchuc.com) at the [CSIRO](https://www.csiro.au/).  

<iframe src="https://github.com/CSIRO-enviro-informatics/LODE/blob/master/info/lode_example_preview.pdf" height="300" width="100%">Sorry your browser does not support iframe.</iframe>

---

## Table Of Contents:
* [Example Usage](#exampleUsage)
	* [Minimal LODE](#minimalLODE)
	* [Complete LODE (user-interface)](#completeLODE)
* [Installation](#completeStepsToInstallingNRunningLODE)
* [Contact](#contact)

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
1. Run LODE on a Jetty server.
	* Deployed example at: http://52.64.97.55/  
	Try one of the examples listed on the web page.

---

## Installation <a name="completeStepsToInstallingNRunningLODE"></a>
Please see [install.md](https://github.com/CSIRO-enviro-informatics/LODE/blob/master/INSTALL.md) for the list of instructions.  
  
Here is a flow chart [diagram](https://i.imgur.com/zFNqfy5.png) detailing the software architecture of LODE.

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