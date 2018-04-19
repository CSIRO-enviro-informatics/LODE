package com.edmondchuc.lode2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
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
	
	// settings used in ApplyXSLT()
	String xsltURL = "http://localhost:8080/extraction.xsl";//"http://115.70.8.75/extraction.xsl";
	String cssLocation = "http://localhost:8080/"; //"http://115.70.8.75/";
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
		// initialise log4j
		BasicConfigurator.configure();
		
		// TODO: have a safety check to ensure user only uploads one file
		// get the file part of the request
		Part filePart = request.getPart("file"); 
		
		// error message prints if invalid URL or file
		String result = "";
		
		// flag to prevent tidy() being called twice if request is URL
		// don't really like this implementation but it will do for now
		boolean isFile = true;
		
		// if the request is a file
		if(filePart.getContentType().toString().equals("application/rdf+xml"))
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
		    result = getStringFromInputStream(fileContent);
		    
			// write ontology to be parsed to disk
		     PrintWriter writer = new PrintWriter(filePath, "UTF-8");
		     writer.println(result);
		     writer.close();
		     
		     log("Saved file to " + filePath);
		    
		     String ontologyURL = "http://localhost:8080/uploadedFiles/" + fileName;
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
		}
		// request is URL, pass on to get()
		else if(filePart.getContentType().toString().equals("application/octet-stream"))
		{
			log("Received URL, passing on to doGet().");
			isFile = false;
			doGet(request, response);
		}
		else
		{
			result = "Invalid URL or file.";
			log("Invalid URL or file.");
		}
		
		if(isFile)
		{
			result = tidy(result);
		     
		    // object to send the HTML response back to client
		    PrintWriter out = response.getWriter();
		   
			// serve transformed content back to user
			out.println(result);
		}
		
		log(filePart.getContentType().toString());
	}
	
//	private String extractFileName(Part part) {
//	    String contentDisp = part.getHeader("content-disposition");
//	    String[] items = contentDisp.split(";");
//	    for (String s : items) {
//	        if (s.trim().startsWith("filename")) {
//	            return s.substring(s.indexOf("=") + 2, s.length()-1);
//	        }
//	    }
//	    return "";
//	}
	
	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		// initialise log4j
		BasicConfigurator.configure();
		
		// set the response as HTML
		response.setContentType("text/html");
		
		// set UTF-8 as response encoding
		response.setCharacterEncoding("UTF-8");
		
		// object to send the HTML response back to client
		PrintWriter out = response.getWriter();
		
		// set paths and settings
		//resolvePaths(request);
		
		//SourceExtractor extractor = new SourceExtractor();	//test
		//extractor.addMimeTypes(MimeType.mimeTypes);	//test
		
		try 
		{
			// get the url as string
			String ontologyURL = request.getParameter("url");
			//String stringURL = request.getParameter("url");
			
			log("Received ontology URL " + ontologyURL);
			
			// cast string url to url
			//URL ontologyURL = new URL(stringURL);
			
			// set follow redirects to true for HTTP
			//HttpURLConnection.setFollowRedirects(true);	//test
			
			// human-readable ontology to serve back to user
			String content = "";
			
			log("Parsing ontology with OWLAPI.");
			
			// parse with OWLAPI
			content = parseWithOWLAPI(ontologyURL);
			
			log("Applying XSLT");

			// Apply XSLT
			//content = extractor.exec(ontologyURL); //test
			content = ApplyXSLT(content, ontologyURL);
			
			content = tidy(content);
			
			// serve transformed content back to user
			out.println(content);
			
			// TESTING
			//out.println(xsltURL);
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
		
//		log("Assigning fragments.");
		result = assignFragments(result);
		
		log("Done.");
		
		return result;
	}
	
	private String assignFragments(String result)
	{
		// the start position of the last "#d4e" fragment found
		int lastStart = 0;
		
		// loop until all fragments are assigned
		while(result.contains("#d4e"))
        {
			// find the random fragment
        	int start = result.indexOf("#d4e", lastStart+1);
        	int end = result.indexOf("\" title", start);
        	if(start == -1)
        	{
        		// no more random fragments found after the last one
        		break;
        	}
        	String dHash = result.substring(start, end); // e.g. #d4e199
        	String d = result.substring(start+1, end);	 // e.g d4e199
        	
        	int startFrag = result.indexOf("#", start+1);
        	int endFrag = result.indexOf("\">", start+1);
        	
        	// no valid hash fragment found
        	// e.g.
        	// <li><a href="#d4e414" title="https://orcid.org/0000-0002-8742-7730">Nicholas Car</a></li>
        	if(startFrag < endFrag)
        	{
        		String fragmentHash = result.substring(startFrag, endFrag);
        		String fragment = result.substring(startFrag+1, endFrag);
            	
        		// anchor href
            	result = result.replace(dHash,  fragmentHash);
            	System.out.println(d + " " + fragment);
            	
            	// replace all ids with fragment name
            	while(result.indexOf(d) != -1)
            	{
            		result = result.replace(d,  fragment);
            	}
        	}
        	
        	lastStart = start;
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
}
