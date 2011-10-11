package it.okkam.exhibit;

import java.awt.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class DataModel {
	static String service = "http://localhost:2020/rivela";
	

	public void creatJSON(String filepath, String graph) throws IOException {
		
		String queryString = " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
		"prefix owl: <http://www.w3.org/2002/07/owl#> " +
		"prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
		"prefix dc: <http://purl.org/dc/elements/1.1/>" +
		"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"prefix ens: <http://models.okkam.org/ENS-meta-core-vocabulary.owl#> " +
		"select  ?name ?lastname ?birthdate ?gender where { graph <"+graph+"> { " +
		"?s ens:first_name ?name ;" +
		"ens:last_name ?lastname ; " +
		"ens:gender ?gender ;" +
		"ens:birthdate ?birthdate . " +
		" } }";
		
		/*
		 * clear the content of a file for every query
		 */
		File exhibitDataPatho = new File(filepath);
		exhibitDataPatho.delete();
		FileWriter exhibitDataPath = new FileWriter(filepath,true);
		BufferedWriter out = new BufferedWriter(exhibitDataPath);
		  out.write("{\"items\":\n\t[");
		QueryExecution queryObject = QueryExecutionFactory.sparqlService(
				service, queryString);
		try {
			ResultSet results = queryObject.execSelect();
			ArrayList resultLists=new ArrayList();
			while( results.hasNext()) {				
				QuerySolution solution = results.nextSolution();
				//System.out.println(solution);
				String name = solution.get("?name").toString();
				String lastname = solution.get("?lastname").toString();
				
				String birthdate = solution.get("?birthdate").toString();
				Date now = new Date(birthdate);
				SimpleDateFormat simpleDateformat=new SimpleDateFormat("yyyy");
				birthdate=simpleDateformat.format(now);
				
				String gender = solution.get("?gender").toString();
				if(gender.equals("F"))
					gender="Female";
				else
					gender="Male";
				
				resultLists.add("{ label: \""+name+" "+ lastname+"\",firstName:\""+name+"\"," +
							"lastname:\""+lastname+"\",birthdate:\""+birthdate+"\",gender:\""+gender+"\"}");
			}
			int numberOfResults=resultLists.size();
			Iterator resultitr=resultLists.iterator();
			int i=0;
			while(resultitr.hasNext()){i++;
				if(i<numberOfResults)
					out.write(resultitr.next()+",\n");
				else
					out.write(resultitr.next().toString());
			}
		} finally {
			queryObject.close();
		}
		out.write("\n\t]\n}");
		out.close();
	}
String getContextGraph(String username){
	String contextGraph = null;
	String queryString ="prefix dc: <http://purl.org/dc/elements/1.1/>" +
						"prefix ens: <http://models.okkam.org/ENS-meta-core-vocabulary.owl#> " +
						"select  ?s where { graph ?g { ?s dc:creator ?cr ." +
						"?cr ens:first_name '"+ username+"' . } }";
		QueryExecution queryObject = QueryExecutionFactory.sparqlService(
			service, queryString);
	try {
		ResultSet results = queryObject.execSelect();
		while( results.hasNext()) {				
			QuerySolution solution = results.nextSolution();
			//System.out.println(solution);
			 contextGraph = solution.get("?s").toString();		
	} 
	}
	finally {
		queryObject.close();
	}
	
	return contextGraph;
	
}

public void searchByKeyword(String filepath, String searchkey) throws IOException {
	
	String queryString = "SELECT ?s ?p ?o " +
	"WHERE" +
	"{ graph ?g {" +
	"?s ?p ?o ." +
	"FILTER (regex(?o, \""+searchkey+"\",\"i\"))" +
			"}" +
			"}";
	
	File exhibitDataPatho = new File(filepath);
	exhibitDataPatho.delete();
	FileWriter exhibitDataPath = new FileWriter(filepath,true);
	BufferedWriter out = new BufferedWriter(exhibitDataPath);
	out.write("{\"items\":\n\t[");
	
	QueryExecution queryObject = QueryExecutionFactory.sparqlService(
			service, queryString);
	Set resultLists=null;
	Iterator resultitr=null;
	try {
		ResultSet results = queryObject.execSelect();
		 resultLists=new HashSet();
		while( results.hasNext()) {				
			QuerySolution solution = results.nextSolution();
			//System.out.println(solution);
			String subject = solution.get("?s").toString();
			resultLists.add(subject);
		}
	} finally {
		queryObject.close();
	}
	 resultitr=resultLists.iterator();	
	 while(resultitr.hasNext()){
		 String subject=resultitr.next().toString();
		 xxxxx
	 }
	
	out.write("\n\t]\n}");
	out.close();
}
	public static void main(String... argv) throws IOException {
		DataModel datamodel = new DataModel();
		//datamodel.creatJSON();

	}
}
