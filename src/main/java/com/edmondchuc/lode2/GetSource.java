package com.edmondchuc.lode2;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

/**
 * Servlet implementation class GetSource
 */
//@WebServlet("/source")
public class GetSource extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSource() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		SourceExtractor extractor = new SourceExtractor();
		extractor.addMimeTypes(MimeType.mimeTypes);
		response.setCharacterEncoding("UTF-8");
		
		try {
			String stringURL = request.getParameter("url");
			String content = "";
			
			URL ontologyURL = new URL(stringURL);
			content = extractor.exec(stringURL);
			
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.println(content);
		} catch (Exception e) {
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println(getErrorPage(e));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private String getErrorPage(Exception e) {
		return 
			"<html>" +
				"<head><title>LODE error</title></head>" +
				"<body>" +
					"<h2>" +
					"LODE: get source error" +
					"</h2>" +
					"<p><strong>Reason: </strong>" +
					e.getMessage() +
					"</p>" +
				"</body>" +
			"</html>";
	}

}
