package com.edmondchuc.lode2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Add
 */
//@WebServlet("/Add")
public class Add extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Add() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * Servlet function that adds a new namespace to the text file namespaces.txt.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * @author Edmond Chuc
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		String namespace = request.getParameter("namespace");
		
		// get the directory of this application
		String path = System.getProperty("user.dir");
		
		String username = System.getProperty("user.name"); //"ubuntu";
		
		// path to the text document of namespaces
		//String filePath = Paths.get("/home" + File.separator + username + File.separator + "lode/src/main/webapp/namespaces.txt").toString();
		String filePath = Paths.get("/Users" + File.separator + username + File.separator + "lode/src/main/webapp/namespaces.txt").toString();
		//String filePath = "/home/ubuntu/lode/target/lode2-0.0.1-SNAPSHOT/namespaces.txt";
		
		FileWriter fileWriter = new FileWriter(filePath, true);
		
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		
		bufferedWriter.append("\n" + namespace + "\n");
		
		bufferedWriter.close();
		log("Namespace " + namespace + " added to namespace.txt.");
		
		PrintWriter out = response.getWriter();
		out.println("Added namespace: " + namespace);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
