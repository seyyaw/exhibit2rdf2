package it.okkam.exhibit.cq;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
/*
 * CQ-001: We want to get information about locations from two different triple store, merge those
 * sub graphs and show the result into an exhibit.
 */
public class MergeDataset {
	
	//static String localService = "http://localhost:2020/rivela";
	static String remoteService = "http://192.168.234.87:2020/taxproject";
	
	String queryString = " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
		+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
		+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
		+ "prefix dc: <http://purl.org/dc/elements/1.1/>"
		+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		+ "prefix ens: <http://models.okkam.org/ENS-meta-core-vocabulary.owl#> "
		+ "select distinct ?subjectLocation where { graph ?g { "
		+ "?subjectLocation a ens:location ."		
		+ " } }";
	
	public void callRemoteService() {
		
		
		QueryExecution queryObject = QueryExecutionFactory.sparqlService(
				remoteService, queryString);		
		
		try {
			ResultSet results = queryObject.execSelect();
			ArrayList resultLists = new ArrayList();
			while (results.hasNext()) {
				QuerySolution solution = results.nextSolution();
				// System.out.println(solution);
				String location = solution.get("?subjectLocation").toString();
								
				System.out.println("Location: " + location);
			}			
		} finally {
			queryObject.close();
		}
		
	}

}
