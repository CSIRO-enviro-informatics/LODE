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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

@MultipartConfig(location="C:\\Users\\chu101\\Desktop\\tmp",
fileSizeThreshold=1024*1024*2, // 2MB
maxFileSize=1024*1024*10,      // 10MB
maxRequestSize=1024*1024*50)   // 50MB

/**
 * Servlet implementation class ExtractOntology
 */
@WebServlet("/extract")
public class ExtractOntology extends HttpServlet 
{
	// name of folder to be saved relative to web application
	private static final String SAVE_DIR = "uploadFiles";
	
	// settings used in ApplyXSLT()
	String xsltURL = "http://localhost:8080/lode/extraction.xsl";
	String cssLocation = "http://localhost:8080/lode/";
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
		
		// store the file content as input stream
		InputStream fileContent = filePart.getInputStream();
		
	    // object to send the HTML response back to client
	    PrintWriter out = response.getWriter();
	    
	    // convert the file content to string
	    String result = getStringFromInputStream(fileContent);
	    
	    PrintWriter writer = new PrintWriter("C:\\Users\\chu101\\Desktop\\LODE\\src\\main\\webapp\\ontology.xml", "UTF-8");
	    writer.println(result);
	    writer.close();
	    
	    String ontologyURL = "http://localhost:8080/lode/ontology.xml";
	    try {
			result = parseWithOWLAPI(ontologyURL);
		} catch (OWLOntologyCreationException | OWLOntologyStorageException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			result = ApplyXSLT(result, ontologyURL);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
//	    // create ontology manager
//	 	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//	 	
//	 	// load ontology from ontologyURL
//	 	OWLOntology ontology = manager.loadOntology(IRI.create(result));
//	    
//	 	// document to store ontology parsed by OWLAPI
//	 	StringDocumentTarget parsedOntology = new StringDocumentTarget();
//	 		
// 		// save ontology as RDF/XML to parsedOntology
// 		manager.saveOntology(ontology, new RDFXMLDocumentFormat(), parsedOntology);
// 		
// 		result = parsedOntology.toString();
//
//		// Apply XSLT
//		//content = extractor.exec(ontologyURL); //test
//		result = ApplyXSLT(result, ontologyURL);
//		
//		// serve transformed content back to user
		out.println(result);
	}
	
	private String extractFileName(Part part) {
	    String contentDisp = part.getHeader("content-disposition");
	    String[] items = contentDisp.split(";");
	    for (String s : items) {
	        if (s.trim().startsWith("filename")) {
	            return s.substring(s.indexOf("=") + 2, s.length()-1);
	        }
	    }
	    return "";
	}
	
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
			
			// cast string url to url
			//URL ontologyURL = new URL(stringURL);
			
			// set follow redirects to true for HTTP
			//HttpURLConnection.setFollowRedirects(true);	//test
			
			// human-readable ontology to serve back to user
			String content = "";
			
			// parse with OWLAPI
			content = parseWithOWLAPI(ontologyURL);

			// Apply XSLT
			//content = extractor.exec(ontologyURL); //test
			content = ApplyXSLT(content, ontologyURL);
			
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
	
	private void resolvePaths(HttpServletRequest request)
	{
		String xsltURL = "http://localhost:8080/lode/extraction.xsl";
		String cssLocation = "http://localhost:8080/lode/";
		String lang = "en";	// default
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
