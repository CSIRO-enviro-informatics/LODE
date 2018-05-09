---

<p align="center"><img src="https://github.com/CSIRO-enviro-informatics/LODE/blob/master/src/main/webapp/LODE_Image.png"/></p>

---

# Live OWL Documentation Environment (LODE)
This repository is a Tomcat server application that can be used to create HTML documentation for [Web Ontology Language](https://www.w3.org/OWL/) (OWL) ontologies. This project has been updated by [Edmond Chuc](http://www.edmondchuc.com), a student at the [CSIRO](https://www.csiro.au/). 

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
1. Launch application:

	* `mvn clean jetty:run`

2. Open a web browser and call the LODE service.
	* **DOLCE ontology**
		* `http://localhost:8080/lode/extract?url=http://www.loa.istc.cnr.it/ontologies/DOLCE-Lite.owl`  

### Complete LODE (user-interface) <a name="completeLODE"></a>
1. Run LODE on a Tomcat server.
	* Example: http://52.64.97.55/
	* **PROMS ontology**
		* Try uploading [proms.ttl](proms.ttl)
		* It is a very tiny ontology visualised online using LODE at <http://promsns.org/def/proms/>.
		* You can run try using a local instance of LODE to generate HTML for the local copy of PROMS and compare it with the online version made by the PROMS creator

---

## Installation <a name="completeStepsToInstallingNRunningLODE"></a>
Please see [install.md](https://github.com/CSIRO-enviro-informatics/LODE/blob/master/INSTALL.md) for the list of instructions.

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