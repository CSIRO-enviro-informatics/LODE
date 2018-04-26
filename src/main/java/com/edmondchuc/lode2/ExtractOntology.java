package com.edmondchuc.lode2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.BasicConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.event.Level;

@MultipartConfig
(/*location="C:\\Users\\chu101\\Desktop\\tmp",*/
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
	String username = System.getProperty("user.name");
	
	// settings (point them to server when deploying)
	String xsltURL = "http://localhost:8080/extraction.xsl";
	String cssLocation = "http://localhost:8080/";
	String uploadedFilePath = "http://localhost:8080/uploadedFiles/";
	String lang = "en";	// default
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExtractOntology() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		loggerInit();
		
		// TODO: have a safety check to ensure user only uploads one file
		// get the file part of the request
		Part filePart = request.getPart("file"); 
		
		// get parameters
		boolean imported = new Boolean(request.getParameter("module").equals("imported"));
		boolean closure = new Boolean(request.getParameter("module").equals("closure"));
		boolean reasoner = false;
		// try catch block required to prevent NullPointerException when checkbox is unticked
		try {
			reasoner = request.getParameter("reasoner").equals("reasoner");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		log("Parameters:");
		System.out.println("imported: \t" + imported + "\nclosure: \t" + closure + "\nreasoner: \t" + reasoner);
		
		// flag to prevent tidy() being called twice if request is URL
		// don't really like this implementation but it will do for now
		boolean isFile = false;
		
		String filename = null;
		
		// get header type
		String header = filePart.getHeader("content-disposition");
		if(header.contains("filename=\"\""))
		{
			log("URL incoming");
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
			doGet(request, response);
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		loggerInit();
		
		// set the response as HTML
		response.setContentType("text/html");
		
		// set UTF-8 as response encoding
		response.setCharacterEncoding("UTF-8");
		
		// object to send the HTML response back to client
		PrintWriter out = response.getWriter();
		
		try 
		{
			// get the url as string
			String ontologyURL = request.getParameter("url");
			//String stringURL = request.getParameter("url");
			
			log("Received ontology URL " + ontologyURL);
			
			transform(ontologyURL, out);
		}
		catch (Exception e) 
		{
			out.println(getErrorPage(e));
		}
	}
	
//	private void resolvePaths(HttpServletRequest request)
//	{
//		String xsltURL = "http://115.70.8.75/extraction.xsl"; //"http://localhost:8080/lode/extraction.xsl";
//		String cssLocation = "http://115.70.8.75/";
//		String lang = "en";	// default
//	}
	
	// call the OWLAPI and apply XSL transform with some post-process tidy
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
		 
		result = tidy(result);
		   
		// serve transformed content back to user
		out.println(result);
		 
		return result;
	}
	
	private String parseWithOWLAPI(String ontologyURL) throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException
	{
		// create ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		// load ontology from ontologyURL
		OWLOntology ontology = manager.loadOntology(IRI.create(ontologyURL));
		
		// document to store ontology parsed by OWLAPI
		StringDocumentTarget parsedOntology = new StringDocumentTarget();
		
		// save ontology as RDF/XML to parsedOntology
		manager.saveOntology(ontology, new RDFXMLDocumentFormat(), parsedOntology);
		
		return parsedOntology.toString();
	}
	
	private String tidy(String result)
	{
		log("Removing double titles");
		result = removeDuplicateTitle(result);
		
		log("Formatting HTML.");
		result = formatHTML(result);
		
		log("Assigning fragments.");
		result = assignFragments(result);
		
		log("Done.");
		
		return result;
	}
	
	private String assignFragments(String result)
	{
		// get the IRI around this index
		int IRISub = result.indexOf("IRI:");
		
		// if IRI title exists
		if(IRISub != -1)
		{
			// find the IRI
			int start = result.indexOf("<dd>", IRISub) + 4;
			int end = result.indexOf("</dd>");
			String IRI = "";
			try {
				IRI = result.substring(start,  end);
				IRI = IRI.trim(); // eliminated leading and trailing whitespace
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log("IRI declaration missing, quitting assigning fragments.");
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
						
						System.out.println(d4e + " " + fragmentHash);
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
						
						System.out.println(d4e + " " + fragmentHash);
					}
				}
				last = end_d4e;
			}
		}
		return result;
	}
	
	private String formatHTML(String result)
	{
		// format HTML with Jsoup
		Document doc = Jsoup.parse(result);
		
		// set indentation to 4 spaces
		doc.outputSettings().indentAmount(4); 
		
		return doc.toString();
	}
	
//	private String removeDoubleTitle(String result)
//	{
//		// the start index of '</title>' tag if double title exists
//	    int endOfStart = result.indexOf("</title><title>");
//	    
//	    // loops until no more double titles found
//	    while(endOfStart != -1)
//	    {
//	    	// get the last index of the double title
//	    	int end = endOfStart + 8;
//	    	
//	    	// find start index of the double title
//	        int start = result.indexOf("<title>");
//	        
//	        // get double title as a substring
//	        String doubleString = new String(result.substring(start, end));
//	        
//	        // escaping parenthesis and spaces
//	        doubleString = doubleString.replaceAll("\\(", "\\\\(");
//	        doubleString = doubleString.replaceAll("\\)", "\\\\)");
//	        doubleString = doubleString.replaceAll(" ", "\\\\s");
//	        
//	        // remove double title
//	        result = result.replaceFirst(doubleString, "");
//	        
//	        // check if any double titles remain
//	        endOfStart = result.indexOf("</title><title>");
//	    }
//	    return result;
//	}
	
	/*
	 * Note: removeDoubleTitle assumes the HTML String is unformatted
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
	
	private String getErrorPage(Exception e) 
	{
		return "<html>" + "<head><title>LODE error</title></head>" + "<body>" + "<h2>" + "LODE error" + "</h2>" + "<p><strong>Reason: </strong>" + e.getMessage() + "</p>" + "</body>" + "</html>";
	}
	
	private void loggerInit()
	{
		// initialise log4j
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ERROR);
	}
	
	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) 
	{
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();
	}
	
	private String addImportedAxioms(String result, List<String> removed) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = (Document) builder.parse(new ByteArrayInputStream(result.getBytes()));

			NodeList ontologies = document.getElementsByTagNameNS("http://www.w3.org/2002/07/owl#", "Ontology");
			if (ontologies.getLength() > 0) {
				Element ontology = (Element) ontologies.item(0);

				for (String toBeAdded : removed) {
					Element importElement = document.createElementNS("http://www.w3.org/2002/07/owl#", "owl:imports");
					importElement.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", toBeAdded);
					ontology.appendChild(importElement);
				}
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult output = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(document);
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
}
