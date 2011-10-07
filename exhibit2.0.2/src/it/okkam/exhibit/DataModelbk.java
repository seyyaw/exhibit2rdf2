package it.okkam.exhibit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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

public class DataModelbk {
	static String service = "http://localhost:2020/rivela";
	static String queryString = " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"prefix owl: <http://www.w3.org/2002/07/owl#> " +
			"prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
			"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"prefix ens: <http://models.okkam.org/ENS-meta-core-vocabulary.owl#> select  ?s ?p ?o " +
			"where { graph ?g {" +
			" ?s ?p ?o .?s " +
			"ens:first_name ?o .} }";

	public void creatJSON() throws IOException {
		FileWriter exhibitDataPath = new FileWriter("WebContent/nobelists2.js",true);
		BufferedWriter out = new BufferedWriter(exhibitDataPath);
		  out.write("{\"items\":\n\t[");
		QueryExecution queryObject = QueryExecutionFactory.sparqlService(
				service, queryString);
		Model model = ModelFactory.createDefaultModel();
		try {
			ResultSet results = queryObject.execSelect();
			for (; results.hasNext();) {
				
				
				QuerySolution solution = results.nextSolution();
				//System.out.println(solution);
				String property = solution.get("?p").toString();
				String subject = solution.get("?s").toString();
				String object = solution.get("?o").toString();
				
				Property propertyOf = model.createProperty(property);
				Resource resource = model.createResource(subject).addProperty(propertyOf, object);
			}
				ResIterator resourceit=model.listResourcesWithProperty(null);
				while (resourceit.hasNext()) {
					Resource resource = resourceit.nextResource();
					JSONSerializer serializer = new JSONSerializer(false);
					serializer.putNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
					serializer.putNamespace("http://models.okkam.org/ENS-core-vocabulary.owl#","ens");
					serializer.putNamespace("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
					serializer.putNamespace("http://www.w3.org/2002/07/owl#","owl");
					serializer.putNamespace("http://www.w3.org/2001/XMLSchema#", "xsd");

				try {
					JSONObject jo = (JSONObject) serializer.objectify(resource);
					String jsonval = jo.toString();
					if (jsonval.contains("\"first_name\"")&&jsonval.contains("\"last_name\"")) {
						jsonval = jo.append(
								"label",
								jo.get("first_name") + " "
										+ jo.get("last_name")).toString();
					}
					else if (jsonval.contains("\"first_name\"")) {
						jsonval = jo.append(
								"label",
								jo.get("first_name")).toString();
					}
					else if (jsonval.contains("\"city_of_residence\"")) {
						jsonval = jo.append(
								"label",
								jo.get("city_of_residence")).toString();
					}
					jsonval = jsonval.replace("location_name", "label");
					jsonval = jsonval.replace("@", "");
					if (resourceit.hasNext())
					out.write(jsonval + ",\n");
					 else
						 out.write(jsonval);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// System.out.println("object = " +
				// solution.toString().substring(6,
				// solution.toString().length()-1)) ;
			}
		} finally {
			queryObject.close();
		}
		out.write("\n\t]\n}");
		out.close();
	}

	public static void main(String... argv) throws IOException {
		DataModelbk datamodel = new DataModelbk();
		datamodel.creatJSON();

	}
}
