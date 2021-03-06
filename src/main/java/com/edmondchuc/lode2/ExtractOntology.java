package com.edmondchuc.lode2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.BasicConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomIndex;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDisjointClassesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.Mutable;
import com.vladsch.flexmark.util.options.DataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;

import openllet.owlapi.PelletReasoner;
import openllet.owlapi.PelletReasonerFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

@MultipartConfig
(
fileSizeThreshold=1024*1024*2, // 2MB
maxFileSize=1024*1024*10,      // 10MB
maxRequestSize=1024*1024*50)   // 50MB

/**
 * Servlet implementation class ExtractOntology
 */
//@WebServlet("/extract")
public class ExtractOntology extends HttpServlet 
{
	// set up upload path - info from	
	// https://stackoverflow.com/questions/797549/get-login-username-in-java   
	// may need to change for Windows support in the future
	String username = System.getProperty("user.name"); //"ubuntu";
	
	// settings (point them to server when deploying)
	String xsltURL = "http://lode2.linked.data.gov.au/extraction.xsl"; //"http://52.64.97.55:80/extraction.xsl";
	String cssLocation = "http://lode2.linked.data.gov.au/"; // "http://52.64.97.55:80/";
	String uploadedFilePath = "http://lode2.linked.data.gov.au/uploadedFiles/"; // "http://52.64.97.55:80/uploadedFiles/";
	String lang = "en";	// default
	String webvowlAdd = "http://lode2.linked.data.gov.au/webvowl/";  //"http://52.64.97.55:8000/";
	
	// flag to prevent tidy() being called twice if request is URL
	// don't really like this implementation but it will do for now
	boolean isFile = false;
	
	// used to change the href of "Other visualisation"
	String filename = null;
	
	// NOTE: these values don't actually get initialised?
	// parameters
	boolean imported = false;
	boolean closure = false;
	boolean reasoner = false;
	boolean webvowl = false;
	boolean badNamespaces = false;
	boolean removeVisualiseWithLode = false;
	String namespaces = "";
	
	// Used to check if it was a HTTP request over URL
	// sets to false in doPost()
	boolean httpCall = false;
	boolean urlCall = false;
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExtractOntology() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * doPost() servlet that handles the user request through a web form. Handles file uploads.
     * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * @author Edmond Chuc
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		loggerInit();
		
		// set UTF-8 as response encoding
		response.setCharacterEncoding("UTF-8");
		
		// TODO: have a safety check to ensure user only uploads one file
		// get the file part of the request
		Part filePart = request.getPart("file"); 
		
		// get parameters
		imported = new Boolean(request.getParameter("module").equals("imported"));
		closure = new Boolean(request.getParameter("module").equals("closure"));
		reasoner = false;
		webvowl = false;
		removeVisualiseWithLode = false;
		namespaces = request.getParameter("namespaces");
		
		try {
			webvowl = request.getParameter("webvowl").equals("webvowl");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		// try catch block required to prevent NullPointerException when checkbox is unticked
		try {
			reasoner = request.getParameter("reasoner").equals("reasoner");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		try {
			removeVisualiseWithLode = request.getParameter("removeVisualiseWithLode").equals("removeVisualiseWithLode");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		lang = request.getParameter("lang_label");
		if(lang.equals(""))
		{
			lang = "en";
		}
		log("Parameters:");
		System.out.println("imported: \t" + imported + "\nclosure: \t" + closure + "\nreasoner: \t" + reasoner + "\nlang: \t\t" + lang + "\nwebvowl: \t" + webvowl + "\nbadNamespaces: \t" + badNamespaces + "\nremoveVisualiseWithLode: \t" + removeVisualiseWithLode);
		
		if(namespaces.equals(""))
		{
			badNamespaces = false;
			log("No bad namespaces entered.");
		}
		else
		{
			badNamespaces = true;
			log("Bad namespaces:");
			System.out.println(namespaces);
		}
		
		// get header type
		String header = filePart.getHeader("content-disposition");
		if(header.contains("filename=\"\""))
		{
			log("URL incoming");
			isFile = false;
			urlCall = true;
		}
		else
		{
			log("file incoming");
			isFile = true;
			int index = header.indexOf("filename") + 10;
			filename = header.substring(index, header.indexOf("\"", index + 1));
			System.out.println(filename);
		}
		
		// if the request is a file
		if(isFile)
		{
			// name of folder to be saved relative to web application
			String saveDir = getServletContext().getRealPath(File.separator) + File.separator + "uploadedFiles";
			
			// get the name of the uploaded file
			final String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			
			log("Received uploaded file: " + fileName);
			
			// file path to save
			final String filePath = saveDir + File.separator + fileName;
			
			// store the file content as input stream
			InputStream fileContent = filePart.getInputStream();
		    
		    // convert the file content to string
		    String result = getStringFromInputStream(fileContent);
		    
			// write ontology to be parsed to disk
		     PrintWriter writer = new PrintWriter(filePath, "UTF-8");
		     writer.println(result);
		     writer.close();
		     
		     log("Saved file to " + filePath);
		    
		     String ontologyURL = uploadedFilePath + fileName;
		     
		     PrintWriter out = response.getWriter();
		     
		     transform(ontologyURL, out);
		}
		// request is URL, pass on to get()
		else
		{
			log("Received URL, passing on to doGet().");
			isFile = false;
			httpCall = false;
			doGet(request, response);
		}
		
		// reset: httpCall gets set only when these are false
		urlCall = false;
		isFile = false;
	}

	/**
	 * doGet() servlet that handles the user request over HTTP.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * @author Edmond Chuc
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		loggerInit();
		
		// set the response as HTML
		response.setContentType("text/html");
		
		// set UTF-8 as response encoding
		response.setCharacterEncoding("UTF-8");
		
		// if http
		if(urlCall == false && isFile == false)
		{
			httpCall = true;
		}
		
		// get parameters
		if(httpCall)
		{
			imported = new Boolean(request.getParameter("imported"));
			closure = new Boolean(request.getParameter("closure"));
			reasoner = new Boolean(request.getParameter("reasoner"));
			webvowl = new Boolean(request.getParameter("webvowl"));
			badNamespaces = new Boolean(request.getParameter("badNamespaces"));
			removeVisualiseWithLode = new Boolean(request.getParameter("removeVisualiseWithLode"));
			lang = request.getParameter("lang");
			if(lang == null)
			{
				lang = "en";
			}
			log("Parameters:");
			System.out.println("imported: \t" + imported + "\nclosure: \t" + closure + "\nreasoner: \t" + reasoner + "\nlang: \t\t" + lang + "\nwebvowl: \t" + webvowl + "\nbadNamespaces: \t" + badNamespaces + "\nremoveVisualiseWithLode: \t" + removeVisualiseWithLode);
		}
		
		// object to send the HTML response back to client
		PrintWriter out = response.getWriter();
		
		try 
		{
			// get the url as string
			String ontologyURL = request.getParameter("url");
			
			log("Received ontology URL " + ontologyURL);
			
			// TODO: complete the original functionality of LODE on doGet() side
			transform(ontologyURL, out);
		}
		catch (Exception e) 
		{
			out.println(getErrorPage(e));
		}
	}
	
	/**
	 * Parses the ontology with OWLAPI, transforms the output of OWLAPI from XML to HTML and applies some 
	 * post-processing tidy functions. <p>Note: This function serves the result of the string back to the user.</p>
	 * 
	 * @param ontologyURL the full URL address of the ontology document
	 * @param out the PrintWriter object that writes back to the user.
	 * @return a string of the result in HTML.
	 * @author Edmond Chuc
	 */
	private String transform(String ontologyURL, PrintWriter out)
	{
		String result = "";
		try 
		{
			log("Parsing ontology with OWLAPI.");
		 	result = parseWithOWLAPI(ontologyURL);
		} catch (OWLOntologyCreationException | OWLOntologyStorageException | TransformerException e) {
			// TODO Auto-generated catch block
		 	e.printStackTrace();
		}
		try 
		{
			 log("Applying XSLT.");
		 	result = ApplyXSLT(result, ontologyURL);
		} catch (TransformerException e) {
		 	// TODO Auto-generated catch block
		 	e.printStackTrace();
		}
		 
		result = tidy(result, ontologyURL);
		   
		// serve transformed content back to user
		out.println(result);
		 
		return result;
	}
	
	/**
	 * Extracts OWL terms from an ontology URL and formats them in RDF/XML.
	 * 
	 * @param ontologyURL the full URL address of the ontology document
	 * @return a string of the result of the OWLAPI in RDF/XML.
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 * @throws TransformerException
	 * @author Edmond Chuc
	 */
	private String parseWithOWLAPI(String ontologyURL) throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException
	{
		// create ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		// load ontology from ontologyURL
		OWLOntology ontology = manager.loadOntology(IRI.create(ontologyURL));
		
		if(imported || closure)
		{
			Set<OWLOntology> setOfImportedOntologies = new HashSet<OWLOntology>();
			if (imported) 
			{
				log("Adding imported ontologies");
				setOfImportedOntologies.addAll(ontology.getDirectImports());
			} 
			else 
			{
				log("Adding imported closures");
				setOfImportedOntologies.addAll(ontology.getImportsClosure());
			}
			for (OWLOntology importedOntology : setOfImportedOntologies) 
			{
				manager.addAxioms(ontology, importedOntology.getAxioms());
			}
		}
		
		if (reasoner) 
		{
			log("Parsing with Pellet reasoner.");
			ontology = parseWithReasoner(manager, ontology);
		}
		
		// document to store ontology parsed by OWLAPI
		StringDocumentTarget parsedOntology = new StringDocumentTarget();
		
		// save ontology as RDF/XML to parsedOntology
		manager.saveOntology(ontology, new RDFXMLDocumentFormat(), parsedOntology);
		
		return parsedOntology.toString();
	}
	
	/**
	 * This function calls the various post-processing functions shared between doGet() and doPost().
	 * 
	 * @param result the string of HTML to be processed.
	 * @param ontologyURL the full URL address of the ontology document
	 * @return a processed HTML string.
	 * @author Edmond Chuc
	 */
	private String tidy(String result, String ontologyURL)
	{
		log("Removing double titles");
		result = removeDuplicateTitle(result);
		
		log("Removing scripts.");
		result = removeScripts(result);
		
		log("Formatting HTML.");
		result = formatHTML(result);
		
		log("Assigning fragments.");
		result = assignFragments(result);
		
		if(webvowl)
		{
			log("Embedding WebVOWL.");
			result = embedWebVOWL(result, ontologyURL);
		}
		
		if(isFile)
		{
			log("Changing \"Other visualisation\" server address.");
			result = changeOtherVisualisation(result);
		}
		
		if(removeVisualiseWithLode)
		{
			log("Removing \"visualise it with LODE\" links");
			result = removeVisualiseWithLode(result);
		}
		
		if(badNamespaces)
		{
			log("Removing bad namespaces");
			result = removeBadNamespaces(result);
		}
		
		log("Done.");
		
		return result;
	}
	
	/**
	 * Adds a HTML iframe into the HTML document that calls the WebVOWL service.
	 * 
	 * @param result the string of HTML where the iframe will be added.
	 * @param ontologyURL the full URL address of the ontology document. 
	 * @return a string of HTML with WebVOWL added as an iframe.
	 * @author Edmond Chuc
	 */
	private String embedWebVOWL(String result, String ontologyURL)
	{
		// find the open div tag of table of contents
		int open = result.indexOf("<div id=\"toc\">");
		
		// find the closing div tag
		int close = result.indexOf("</div>", open) + 6;
		
		// substring to insert for webvowl
		String webvowlString = "\n<iframe src=\"" + webvowlAdd + "#iri=" + ontologyURL + "\" height=\"700\" width=\"100%\">Sorry your browser does not support iframe.</iframe>";
		
		// length of the result
		int length = result.length();
		
		// insert webvowl substring
		result = result.substring(0,  close).concat(webvowlString).concat(result.substring(close, length));
		
		return result;
	}
	
	/**
	 * Removes namespaces that are listed in the text file namespaces.txt from the HTML document.
	 * 
	 * @param result the string of HTML to be processed.
	 * @return the processed string of HTML.
	 * @author Edmond Chuc
	 */
	private String removeBadNamespaces(String result)
	{
		// eliminate leading and trailing whitespace
		namespaces = namespaces.trim();
		
		// find the default namespace section as the entry point to the search for each namespace
		int start = result.indexOf("<div id=\"namespacedeclarations\"");
		
		int namespaceStart = 0;
		
		// split the namespaces string into each namespace
		String arrayOfNamespaces[] = namespaces.split("\n");
		
		for(String namespace : arrayOfNamespaces)
		{
			// set the entry point for searching
			int last = start;
			
			// eliminate whitespace
			namespace = namespace.trim();
			
			// loop if this namespace exists in the document
			while(result.indexOf(namespace, last) != -1)
			{
				// check if namespace matches in the document
				int checkStart = result.indexOf(namespace, last);
				int checkEnd = result.indexOf("\n", checkStart);
				String checkString = result.substring(checkStart, checkEnd);
				System.out.println("namespace: " + namespace + " checkString: " + checkString + " equals: " + namespace.equals(checkString));
				
				// remove if they absolutely equal each other
				if(namespace.equals(checkString))
				{
					checkStart = result.lastIndexOf("<dt>", checkStart) - 1;
					checkEnd = result.indexOf("</dd>", checkEnd) + 5;
					String sub = result.substring(checkStart, checkEnd);
					
					result = result.replace(sub, "");
				}
				last = checkEnd;
			}
		}
	
		return result;
	}
	
	/**
	 * Removes "Visualise it with LODE" links.
	 * 
	 * @param result the string of HTML to be processed.
	 * @return the processed string of HTML.
	 * @author Edmond Chuc
	 */
	private String removeVisualiseWithLode(String result)
	{
		int last = 0;
		while(result.indexOf("visualise it with LODE", last) != -1)
		{
			// get the line containing "visualise it with LODE"
			int start = result.indexOf("visualise it with LODE", last);
			start = result.lastIndexOf("(", start);
			int end = result.indexOf(")", start) + 1;
			String sub = result.substring(start, end);
			
			result = result.replace(sub, "");
			
			last = end;
		}
		return result;
	}
	
	/**
	 * Changes access to "Other visualisation: " from calling a servlet function to directly accessing the file via 
	 * a URL. <p>Note: This implementation only works for HTML documents in English.</p>
	 * 
	 * @param result the string of HTML to be processed.
	 * @return the processed string of HTML.
	 * @author Edmond Chuc
	 */
	private String changeOtherVisualisation(String result)
	{
		// get the root tag containing Other visualisation
		int start = result.indexOf("Other visualisation:");
		
		// find the start of href value including the double quote
		start = result.indexOf("href=", start) + 6;
		int end = result.indexOf(filename, start);
		
		// get the substring to be replaced
		String sub = result.substring(start, end);
		
		// replace sub with the complete path URL
		result = result.replace(sub, uploadedFilePath);
		
		return result;
	}
	
	/**
	 * Assigns readable HTML fragment identifier names of the classes, properties, etc. that belong to the ontology 
	 * specified in the IRI. <p>Example: "#d4e153" to "#Agent".</p>
	 * 
	 * @param result a string of formatted HTML.
	 * @return a string of HTML containing fragment identifier names from their respective ontology.
	 * @author Edmond Chuc
	 */
	private String assignFragments(String result)
	{
		// get the IRI around this index
		int IRISub = result.indexOf("IRI:");
		
		// if IRI title exists
		if(IRISub != -1)
		{
			// find the IRI
			int start = result.indexOf("<dd>", IRISub) + 4;
			int end = result.indexOf("</dd>", start);
			String IRI = "";
			try {
				IRI = result.substring(start,  end);
				IRI = IRI.trim(); // eliminated leading and trailing whitespace
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log("IRI declaration missing, quit assigning fragments.");
				e.printStackTrace();
				return result;
			}
			
			// last index of d4e occurrence
			int last = 0;
			
			while(result.indexOf("#d4e", last) != -1)
			{
				int start_d4e = result.indexOf("\"#d4e", last);	// get the start of the "d4e..." index
				int end_d4e = result.indexOf("\"", start_d4e+1) + 1;
				String d4eHash = result.substring(start_d4e, end_d4e);
				String d4e = "\"" + result.substring(start_d4e + 2, end_d4e - 1) + "\"";
				String line = result.substring(start_d4e, result.indexOf("\n", start_d4e));	// get the line
				
				// if line contains IRI, assign fragment names
				if(line.contains(IRI))
				{
					// check if it contains a hash
					if(line.indexOf("#", 3) != -1)
					{
						// get fragment name
						int startFrag = result.indexOf("#", start_d4e+2);
						int endFrag = result.indexOf("\">", startFrag);
						String fragmentHash = "\"" + result.substring(startFrag, endFrag) + "\"";
						String fragment = "\"" + result.substring(startFrag + 1, endFrag) + "\"";
						
						// replace d4e with fragment name
						result = result.replace(d4eHash, fragmentHash);
						result = result.replace(d4e, fragment);
						
						//System.out.println(d4e + " " + fragmentHash);
					}
					// get fragment name from slash
					else
					{
						int endFrag = result.indexOf("\">", end_d4e);
						int startFrag = result.lastIndexOf("/", endFrag);
						String fragmentHash = "\"#" + result.substring(startFrag + 1, endFrag) + "\"";
						String fragment = "\"" + result.substring(startFrag + 1, endFrag) + "\"";
						
						// replace d4e with fragment name
						result = result.replace(d4eHash, fragmentHash);
						result = result.replace(d4e, fragment);
						
						//System.out.println(d4e + " " + fragmentHash);
					}
				}
				last = end_d4e;
			}
		}
		return result;
	}
	
	/**
	 * Converts the content in an input stream to string.
	 * 
	 * @param is the input stream where the string content is retrieved.
	 * @return the string retrieved from the input stream.
	 * @author Edmond Chuc
	 */
	private static String getStringFromInputStream(InputStream is)
	{
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while((line = br.readLine()) != null)
			{
				sb.append(line + "\n");
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(br != null)
			{
				try {
					br.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Returns a string with HTML script tags removed.
	 * 
	 * @param result the string of unformatted HTML.
	 * @return string with jQuery scripts removed.
	 * @author Edmond Chuc
	 */
	private static String removeScripts(String result)
	{
		// find the first occurrence and last occurrence of script tags
		int start = result.indexOf("<script");
		int end = result.lastIndexOf("</script>") + 9;
		
		// form the substring
		String toDelete = result.substring(start, end);
		
		// delete the substring by replacing it with ""
		result = result.replace(toDelete, "");
		
		return result;
	}
	
	/**
	 * Formats the HTML with neat indentation and newlines. Transforms markdown text to HTML.
	 * 
	 * @param result a string of unformatted HTML to be processed.
	 * @return a string of formatted HTML.
	 * @author Edmond Chuc
	 */
	private String formatHTML(String result)
	{
		// store the unparsed HTML in String original
		//String original = result;
		
		// keep track of index in HTML
		int last = 0;
		
		// markdown parser flexmark-java
		MutableDataSet options = new MutableDataSet();
		options.setFrom(ParserEmulationProfile.MULTI_MARKDOWN);
		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();
		
		// Loops through the parsed HTML and finds all text contained inside
		// a <span> with class="markdown". 
		// Replaces the text inside this <span> with the original text to maintain markdown formatting.
		while(result.indexOf("class=\"markdown\"", last) != -1)
		{
			// find the first occurence of <span> text with class="markdown"
			int start = result.indexOf("class=\"markdown\"", last) + 17;
			
			// find the end of the text contained in <span>
			int end = result.indexOf("</span>", start);
			
			// String named line containing the characters within the span tag
			String line = result.substring(start, end);
			
			// document for parsing markdown
			com.vladsch.flexmark.ast.Node document = parser.parse(line);
			String parsedHtml = renderer.render(document);
			result = result.replace(line, parsedHtml);
			
			// update last, which keeps track of where we are in the document
			last = end;
		}
		
		// format HTML with Jsoup
		Document doc = Jsoup.parse(result);
		
		// set indentation to 4 spaces
		doc.outputSettings().indentAmount(4); 
		
		// result of parsed HTML
		result = doc.toString();
		
		return result;
	}
	
	/**
	 * Removes duplicate HTML title tags. 
	 * 
	 * @param result a string of HTML to be processed. It assumes that the input HTML string is unformatted.
	 * @return a string containing a single HTML title tag.
	 * @author Edmond Chuc
	 */
	private String removeDuplicateTitle(String result)
	{
		// check if duplicate title exists
		if(result.indexOf("</title><title>") != -1)
		{
			// find the title
			int start = result.indexOf("<title>");
			int end = result.indexOf("</title>") + 8;
			String title = result.substring(start, end);
			
			// remove all titles
			result = result.replace(title, "");
			
			// add title back in
			result = result.substring(0, start) + title + result.substring(start);
		}
		return result;
	}
	
	/**
	 * Transforms the given String in RDF/XML and outputs it to HTML using XSL(t).
	 * 
	 * @param result the RDF/XML string to be transformed.
	 * @param ontologyURL the ontology location to be added to the transformed HTML.
	 * @return a HTML string of the ontology.
	 * @throws TransformerException
	 * @author Silvio Peroni
	 * @author Edmond Chuc
	 */
	private String ApplyXSLT(String result, String ontologyURL) throws TransformerException
	{
		// create transformer factory
		TransformerFactory tfactory = new net.sf.saxon.TransformerFactoryImpl();
		
		// create output stream
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		// create transformer
		Transformer transformer = tfactory.newTransformer(new StreamSource(xsltURL));
		
		// set parameters
		transformer.setParameter("css-location", cssLocation);
		transformer.setParameter("lang", lang);
		transformer.setParameter("ontology-url", ontologyURL);
		transformer.setParameter("source", cssLocation + "source");
		
		// the input to be transformed
		StreamSource inputSource = new StreamSource(new StringReader(result));
		
		// transform result to output source
		transformer.transform(inputSource, new StreamResult(output));
		
		return output.toString();
	}
	
	/**
	 * Inserts the error inside a HTML string and returns it. 
	 * 
	 * @param e the exception caught
	 * @return a string of the error in the form of HTML.
	 * @author Silvio Peroni
	 */
	private String getErrorPage(Exception e) 
	{
		return "<html>" + "<head><title>LODE error</title></head>" + "<body>" + "<h2>" + "LODE error" + "</h2>" + "<p><strong>Reason: </strong>" + e.getMessage() + "</p>" + "</body>" + "</html>";
	}
	
	/**
	 * Initialises log4j. Sets error level to ERROR. Use at the beginning of doGet() or doPost().
	 * 
	 * @author Edmond Chuc
	 */
	private void loggerInit()
	{
		// initialise log4j
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ERROR);
	}
	
	/**
	 * N/A
	 * 
	 * @param result the string to add imported axioms.
	 * @param removed N/A.
	 * @return the modified string with additional imported axioms.
	 * @author Silvio Peroni
	 */
	private String addImportedAxioms(String result, List<String> removed) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = (Document) builder.parse(new ByteArrayInputStream(result.getBytes()));

			NodeList ontologies = ((org.w3c.dom.Document) document).getElementsByTagNameNS("http://www.w3.org/2002/07/owl#", "Ontology");
			if (ontologies.getLength() > 0) {
				Element ontology = (Element) ontologies.item(0);

				for (String toBeAdded : removed) {
					Element importElement = ((org.w3c.dom.Document) document).createElementNS("http://www.w3.org/2002/07/owl#", "owl:imports");
					importElement.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", toBeAdded);
					ontology.appendChild(importElement);
				}
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult output = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource((Node) document);
			transformer.transform(source, output);

			return output.getWriter().toString();
		} catch (ParserConfigurationException e) {
			return result;
		} catch (SAXException e) {
			return result;
		} catch (IOException e) {
			return result;
		} catch (TransformerConfigurationException e) {
			return result;
		} catch (TransformerFactoryConfigurationError e) {
			return result;
		} catch (TransformerException e) {
			return result;
		}
	}
	
	/**
	 * N/A.
	 * 
	 * @param manager N/A.
	 * @param ontology N/A.
	 * @return the ontology that has been parsed with Pellet reasoning.
	 * @author Silvio Peroni
	 */
	private OWLOntology parseWithReasoner(OWLOntologyManager manager, OWLOntology ontology) {
		try {
			PelletOptions.load(new URL("http://" + cssLocation + "pellet.properties"));
			PelletReasoner reasoner = (PelletReasoner) PelletReasonerFactory.getInstance().createReasoner(ontology);
			reasoner.getKB().prepare();
			List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
			generators.add(new InferredSubClassAxiomGenerator());
			generators.add(new InferredClassAssertionAxiomGenerator());
			generators.add(new InferredDisjointClassesAxiomGenerator());
			generators.add(new InferredEquivalentClassAxiomGenerator());
			generators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
			generators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
			generators.add(new InferredInverseObjectPropertiesAxiomGenerator());
			generators.add(new InferredPropertyAssertionGenerator());
			generators.add(new InferredSubDataPropertyAxiomGenerator());
			generators.add(new InferredSubObjectPropertyAxiomGenerator());

			InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, generators);

			OWLOntologyID id = ontology.getOntologyID();
			Set<OWLImportsDeclaration> declarations = ontology.getImportsDeclarations();
			Set<OWLAnnotation> annotations = ontology.getAnnotations();

			Map<OWLEntity, Set<OWLAnnotationAssertionAxiom>> entityAnnotations = new HashMap<OWLEntity, Set<OWLAnnotationAssertionAxiom>>();
			for (OWLClass aEntity : ontology.getClassesInSignature()) {
				entityAnnotations.put(aEntity, ((OWLAxiomIndex) aEntity).getAnnotationAssertionAxioms((OWLAnnotationSubject) ontology));
			}
			for (OWLObjectProperty aEntity : ontology.getObjectPropertiesInSignature()) {
				entityAnnotations.put(aEntity, ((OWLAxiomIndex) aEntity).getAnnotationAssertionAxioms((OWLAnnotationSubject) ontology));
			}
			for (OWLDataProperty aEntity : ontology.getDataPropertiesInSignature()) {
				entityAnnotations.put(aEntity, ((OWLAxiomIndex) aEntity).getAnnotationAssertionAxioms((OWLAnnotationSubject) ontology));
			}
			for (OWLNamedIndividual aEntity : ontology.getIndividualsInSignature()) {
				entityAnnotations.put(aEntity, ((OWLAxiomIndex) aEntity).getAnnotationAssertionAxioms((OWLAnnotationSubject) ontology));
			}
			for (OWLAnnotationProperty aEntity : ontology.getAnnotationPropertiesInSignature()) {
				entityAnnotations.put(aEntity, ((OWLAxiomIndex) aEntity).getAnnotationAssertionAxioms((OWLAnnotationSubject) ontology));
			}
			for (OWLDatatype aEntity : ontology.getDatatypesInSignature()) {
				entityAnnotations.put(aEntity, ((OWLAxiomIndex) aEntity).getAnnotationAssertionAxioms((OWLAnnotationSubject) ontology));
			}

			manager.removeOntology(ontology);
			OWLOntology inferred = manager.createOntology(id);
			iog.fillOntology((OWLDataFactory) manager, inferred);

			for (OWLImportsDeclaration decl : declarations) {
				manager.applyChange(new AddImport(inferred, decl));
			}
			for (OWLAnnotation ann : annotations) {
				manager.applyChange(new AddOntologyAnnotation(inferred, ann));
			}
			for (OWLClass aEntity : inferred.getClassesInSignature()) {
				applyAnnotations(aEntity, entityAnnotations, manager, inferred);
			}
			for (OWLObjectProperty aEntity : inferred.getObjectPropertiesInSignature()) {
				applyAnnotations(aEntity, entityAnnotations, manager, inferred);
			}
			for (OWLDataProperty aEntity : inferred.getDataPropertiesInSignature()) {
				applyAnnotations(aEntity, entityAnnotations, manager, inferred);
			}
			for (OWLNamedIndividual aEntity : inferred.getIndividualsInSignature()) {
				applyAnnotations(aEntity, entityAnnotations, manager, inferred);
			}
			for (OWLAnnotationProperty aEntity : inferred.getAnnotationPropertiesInSignature()) {
				applyAnnotations(aEntity, entityAnnotations, manager, inferred);
			}
			for (OWLDatatype aEntity : inferred.getDatatypesInSignature()) {
				applyAnnotations(aEntity, entityAnnotations, manager, inferred);
			}

			return inferred;
		} catch (FileNotFoundException e1) {
			return ontology;
		} catch (MalformedURLException e1) {
			return ontology;
		} catch (IOException e1) {
			return ontology;
		} catch (OWLOntologyCreationException e) {
			return ontology;
		}
	}
	
	/**
	 * N/A.
	 * 
	 * @param aEntity N/A.
	 * @param entityAnnotations N/A.
	 * @param manager N/A.
	 * @param ontology N/A.
	 * @author Silvio Peroni
	 */
	private void applyAnnotations(OWLEntity aEntity, Map<OWLEntity, Set<OWLAnnotationAssertionAxiom>> entityAnnotations, OWLOntologyManager manager, OWLOntology ontology) {
		Set<OWLAnnotationAssertionAxiom> entitySet = entityAnnotations.get(aEntity);
		if (entitySet != null) {
			for (OWLAnnotationAssertionAxiom ann : entitySet) {
				manager.addAxiom(ontology, ann);
			}
		}
	}
}
