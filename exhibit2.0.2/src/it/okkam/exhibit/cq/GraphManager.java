package it.okkam.exhibit.cq;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import it.okkam.exhibit.JSONSerializer;

import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/*
 * CQ-001: We want to get information about locations from two different triple store, merge those
 * sub graphs and show the result into an exhibit.
 */
public class GraphManager {
	
	Model model = null;
	
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

	/**
	 * execute SPARQL Query on remote TDB and construct a model from the result
	 * 
	 * @return model
	 * @throws IOException
	 */
	public Model callRemoteService() throws IOException {
		QueryExecution queryObject = QueryExecutionFactory.sparqlService(
				remoteService, queryString);
		Model remotemodel = queryObject.execConstruct();
		return remotemodel;
	}

	/**
	 * execute SPARQL Query on local TDB and construct a model from the result
	 * 
	 * @return model
	 * @throws IOException
	 */
	public Model callLocalService() throws IOException {

		QueryExecution queryObject = QueryExecutionFactory.sparqlService(
				localService, queryString);
		Model localmodel = queryObject.execConstruct();
		return localmodel;
	}

	/**
	 * join a model from the remote and local models returned
	 * 
	 * @param remoteModel
	 * @param localModel
	 */
	public void joinModels(Model remoteModel, Model localModel) {
		model=remoteModel;
		model.add(localModel);
	}

	/**
	 * save the models in JSON file format
	 * 
	 * @param jsonFilePath
	 * @throws IOException
	 */
	public void saveJSON(String jsonFilePath) throws IOException {
		Model remote = callRemoteService();
		remote.write(System.out, "TTL");
		Model local = callLocalService();
		joinModels(remote, local);
		createJSON(jsonFilePath, model);
	}

	/**
	 * create a JSON file from TURTLE RDF data set
	 * @param filePath
	 * @param model
	 * @throws IOException
	 */
	public void createJSON(String filePath, Model model) throws IOException {

		ResIterator resourceit = model.listResourcesWithProperty(null);

		File exhibitDataPatho = new File(filePath);
		exhibitDataPatho.delete();

		FileWriter exhibitDataPath = new FileWriter(filePath, true);
		BufferedWriter out = new BufferedWriter(exhibitDataPath);

		out.write("{\"items\":\n\t[");

		while (resourceit.hasNext()) {
			Resource resource = resourceit.nextResource();
			JSONSerializer serializer = new JSONSerializer(false);

			serializer.putNamespace(
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
			serializer.putNamespace(
					"http://models.okkam.org/ENS-core-vocabulary.owl#", "ens");
			serializer.putNamespace(
					"prefix owl: <http://www.w3.org/2002/07/owl#", "owl");
			serializer.putNamespace(
					"prefix xsd: <http://www.w3.org/2001/XMLSchema#", "xsd");
			serializer.putNamespace(
					"prefix dc: <http://purl.org/dc/elements/1.1/", "dc");
			serializer.putNamespace(
					"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#",
					"rdfs");

			try {
				JSONObject jo = (JSONObject) serializer.objectify(resource);
				String jsonval = jo.toString();
				if (jsonval.contains("\"first_name\"")) {
					jsonval = jo.append("label",
							jo.get("first_name") + " " + jo.get("last_name"))
							.toString();
				}
				jsonval = jsonval.replace("location_name", "label");
				jsonval = jsonval.replace("@", "");
				if (resourceit.hasNext())
					out.write(jsonval + ",");
				else
					out.write(jsonval);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		out.write("\n\t]\n}");
		out.close();
		// System.out.println("\n\t]\n}");
	}

}
