package it.okkam.exhibit.cq;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
/*
 * CQ-001: We want to get information about locations from two different triple store, merge those
 * sub graphs and show the result into an exhibit.
 */
public class MergeDataset {
	
	static String localService = "http://localhost:2020/rivela";
	static String remoteService = "http://192.168.234.87:2020/taxproject";
	
	String queryString = " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
		+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
		+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
		+ "prefix dc: <http://purl.org/dc/elements/1.1/>"
		+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		+ "prefix ens: <http://models.okkam.org/ENS-meta-core-vocabulary.owl#> "
		+ "Construct { ?s ?p ?o   } where { graph ?g { "
		+ "?s ?p ?o ."		
		+ " } }";
	
	public void callRemoteService() {
		
		
		QueryExecution queryObject = QueryExecutionFactory.sparqlService(remoteService, queryString);		
		Model remote= queryObject.execConstruct();
		queryObject = QueryExecutionFactory.sparqlService(localService, queryString);
		Model local=queryObject.execConstruct();
		remote.add(local);
		remote.write(System.out,"TTL");
	}

}
