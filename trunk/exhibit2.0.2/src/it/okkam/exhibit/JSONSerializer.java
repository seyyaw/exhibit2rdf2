/**
 * $Id: JSONSerializer.java,v 1.2 2007/07/03 15:16:38 alimanfoo Exp $
 */
package it.okkam.exhibit;

/*
Copyright (c) 2007 Alistair Miles <http://purl.org/net/aliman>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/


import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Alt;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * <p>This class maps an RDF graph to JSON objects, given a node in the graph as the root of the mapping.</p>
 * 
 * <p>This implementation is very much inspired by the <a href="http://jdil.org">JSON Data Integration Language</a>,
 * although this is not a complete implementation of that specification - see the methods of the
 * {@link net.sf.conception.rdfoo.TestJSONSerializer} test case for details of this implementation.</p>
 * 
 * <p>This implementation is built using the <a href="http://jena.sourceforge.net/">Jena Semantic Web Framework</a> and the JSON for Java classes from <a href="http://json.org/java">http://json.org/java</a>.</p>
 *
 * <p>To create a new JSONSerializer, call the default constructor:</p>
 * 
 * <pre>JSONSerializer serializer = new JSONSerializer();</pre>
 * 
 * <p>By default, a JSONSerializer is <strong>namespace aware</strong>. This means it will use the full URIs
 * of the properties and resources in the graph when constructing properties and values in the JSON object graph.</p>
 * 
 * <p>To construct a JSONSerializer which is <strong>not</strong> namespace aware, call the constructor
 * with a single boolean argument:</p>
 * 
 * <pre>
JSONSerializer serializer = new JSONSerializer(false);
 * </pre>
 * 
 * <p>To register a namespace-to-prefix mapping with a namespace aware serializer, for example:</p>
 * 
 * <pre>
serializer.putNamespace("http://www.w3.org/2004/02/skos/core#","skos");
 * </pre>
 * 
 * <p>A namespace aware serializer comes with prefixes already registered for: 
 * "rdf" (RDF namespace), 
 * "rdfs" (RDFS namespace),
 * "owl" (OWL namespace),
 * "xsd" (XML Schema Datatypes namespace), 
 * "dc" (Dublin Core Elements namespace),
 * "dcterms" (Dublin Core Terms namespace). </p>
 * 
 * <p>A JSONSerializer has a number of public methods of the form <code>JSONSerializer.objectify...</code> 
 * for constructing a JSONObject from an RDF graph,
 * given a node in the graph as the root of the transformation. </p>
 * 
 * <p>For example, the code below builds an RDF graph then constructs a JSONObject from one of the nodes in the graph:</p>
 * 
 * <pre>
// set up a model
Model model = ModelFactory.createDefaultModel();
Resource resource = model.createResource();
Property p = model.createProperty("http://www.example.com/foo#p");
resource.addProperty(p, "bar");

// create a serializer (namespace aware)
JSONSerializer serializer = new JSONSerializer();
serializer.putNamespace("http://www.example.com/foo#", "foo");

try 
{
	// objectify
	JSONObject jo = (JSONObject) serializer.objectify(resource);
}
catch (JSONException e) 
{
	e.printStackTrace();
}
 * </pre>
 * 
 * <p>A call to <code>jo.toString()</code> would then return the following string:</p>
 * 
 * <pre>
{
	"@id":"7620170f:1138b3911ac:-7fff",
	"@namespaces":
	{
		"foo":"http://www.example.com/foo#",
		"dcterms":"http://purl.org/dc/terms/",
		"xsd":"http://www.w3.org/2001/XMLSchema#",
		"dc":"http://purl.org/dc/elements/1.1/",
		"rdfs":"http://www.w3.org/2000/01/rdf-schema#",
		"rdf":"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
		"owl":"http://www.w3.org/2002/07/owl#"
	},
	"foo:p":"bar"
}
 * </pre>
 * 
 * <p>If the serializer is <strong>not</strong> namespace aware, a JSONObject will be constructed which serializes as:</p>
 * 
 * <pre>
{
	"@id":"7620170f:1138b3911ac:-7fff",
	"p":"bar"
}
 * </pre>
 * 
 * <p>Note that a serializer which is <strong>not</strong> namespace aware will conflate all properties that
 * have the same local name.</p>
 * 
 * <p>The method <code>JSONSerializer.serialize</code> is a convenience method which simply calls <code>JSONSerializer.objectify</code> 
 * then returns the value of <code>object.toString</code> called on the constructed object.</p> 
 * 
 * <p>By default, a JSONSerializer will only include those properties which have a literal value.
 * To include properties which have a resource value, a JSONSerializer must be explicitly told to <strong>follow</strong> specific properties.</p>
 * 
 * <p>The <code>JSONSerializer.follow</code> method can be called with a single string, which is either the full URI
 * of the property to follow resource values, or the prefix abbreviated URI (if a prefix has been registered).</p>
 * 
 * <p>For example, a serializer set up as follows:</p>
 * 
 * <pre>
JSONSerializer serializer = new JSONSerializer();
serializer.putNamespace("http://www.example.com/foo#", "foo");
serializer.follow("foo:p");
 * </pre>
 * 
 * <p>will follow the <code>foo:p</code> property <strong>wherever it is found in the transformation</strong>.</p>
 * 
 * <p>Alternatively, a JSONSerializer can be told to follow specific properties but up to a maximum number of
 * steps (arcs) from the root of the transformation.</p>

 * <p>For example, a serializer set up as follows:</p>
 * 
 * <pre>
JSONSerializer serializer = new JSONSerializer();
serializer.putNamespace("http://www.example.com/foo#", "foo");
serializer.follow("foo:p", 1);
 * </pre>
 * 
 * <p>will follow the <code>foo:p</code> property only from the root of the transformation (one arc deep), and not thereafter.</p>
 * 
 * <p>See the documentation on the methods of the
 * {@link net.sf.conception.rdfoo.TestJSONSerializer} test case for more examples.</p>
 * 
 * <p>Note that all public methods of this class are currently declared as throwing a JSONException.
 * This is an alpha release, and there may be bugs. If you encounter a JSONException, you've probably 
 * found a bug in this code. Please <a href="mailto:a.j.miles@rl.ac.uk">tell me</a> about any bugs you find.</p> 
 * </p>
 * 
 * @author $Author: alimanfoo $
 * @version $Revision: 1.2 $ on $Date: 2007/07/03 15:16:38 $
 */
public class JSONSerializer {

	private boolean namespaceAware = true;
	private Hashtable prefixes = new Hashtable();
	private Hashtable namespaces = new Hashtable();
	private Hashtable arcs = new Hashtable();
	
	public static final String VALUE = "@value";
	public static final String VALUES = "@values";
	public static final String LANG = "@lang";
	public static final String TYPE = "@type";
	public static final String ID = "@id";
	public static final String BAG = "@bag";
	public static final String SEQ = "@seq";
	public static final String ALT = "@alt";
	public static final String LIST = "@list";
	public static final String NAMESPACES = "@namespaces";
	
	public JSONSerializer() { this.initns(); }
	
	public JSONSerializer(boolean namespaceAware) 
	{
		this.namespaceAware = namespaceAware;
		this.initns();
	}
	
	private void initns()
	{
		// initialise the namespace mappings
		putNamespace(RDF.getURI(), "rdf");
		putNamespace(RDFS.getURI(), "rdfs");
		putNamespace(XSD.getURI(), "xsd");
		putNamespace(OWL.getURI(), "owl");
		putNamespace(DC.getURI(), "dc");
		putNamespace(DCTerms.getURI(), "dcterms");
	}

	/**
	 * Put a namespace-to-prefix mapping for this serializer.
	 * @param namespace the namespace URI to be abbreviated
	 * @param prefix the prefix to use as an abbreviation
	 */
	public void putNamespace(String namespace, String prefix)
	{
		prefixes.put(namespace, prefix);
		namespaces.put(prefix, namespace);
	}
	
	/**
	 * Instruct this serializer to follow all arcs of type predicate.
	 * @param predicate the URI of the predicate to follow (can use abbreviated form if namespace is registered with this serializer)
	 */
	public JSONSerializer follow(String predicate)
	{
		this.arcs.put(this.unabbrev(predicate), new Integer(-1));
		return this;
	}
	
	/**
	 * Instruct this serializer to follow arcs of type predicate to a maximum depth.
	 * 
	 * @param predicate the URI of the predicate to follow (can use abbreviated form if namespace is registered with this serializer)
	 * @param depth the maximum number of arcs to traverse (negative value means unlimited)
	 */
	public JSONSerializer follow(String predicate, int depth)
	{
		this.arcs.put(this.unabbrev(predicate), new Integer(depth));
		return this;
	}
	/**
	 * If namespace aware, use any namespace mappings to abbreviate the URI, otherwise get the local name.
	 * @param uri the URI to abbreviate
	 * @return an abbreviated URI
	 */
	protected String abbrev(String uri)
	{
		if (namespaceAware) // try to make a namespace mapping
		{
			// iterate over namespaces
			Enumeration e = prefixes.keys();
			while (e.hasMoreElements())
			{
				String ns = (String) e.nextElement();
				
				// check for a match
				if (uri.startsWith(ns))
				{
					String prefix = (String) prefixes.get(ns);
					return uri.replaceFirst(ns, prefix+":");
				}
			}
		}
		else // try to return the local name
		{
			int index = Math.max(uri.lastIndexOf("#"), uri.lastIndexOf("/"));
			if (index > 0) { return uri.substring(index+1); }
		}
		
		// if no mappings or cannot obtain local name, return the input
		return uri;
	}

	protected String unabbrev(String name)
	{
		for (Enumeration e = namespaces.keys(); e.hasMoreElements(); )
		{
			String prefix = (String) e.nextElement();
			if (name.startsWith(prefix+":"))
			{
				return name.replaceFirst(prefix+":", ((String)namespaces.get(prefix)));
			}
		}
		return name;
	}

	/**
	 * Create a JSON representation of an RDF literal.
	 * <p>
	 * If the literal has no language tag or datatype, return a String, otherwise return a JSONObject.
	 * @param literal the literal to objectify
	 * @return a String or a JSON object
	 * @throws JSONException
	 */
	public Object objectifyLiteral(Literal literal) throws JSONException
	{
		return this.objectifyLiteral(literal, true);
	}
	
	/**
	 * Objectify an RDF literal, either root or nested.
	 * @param literal
	 * @param root
	 * @return
	 * @throws JSONException
	 */
	protected Object objectifyLiteral(Literal literal, boolean root) throws JSONException
	{
		// access the lexical value, datatype URI and language tag
		String datatypeURI = literal.getDatatypeURI();
		String language = literal.getLanguage();
		String value = literal.getLexicalForm();
		
		if (datatypeURI == null && language.equals("")) // handle a simple literal
		{
			return value;
		}
		else // handle a plain or typed literal
		{
			JSONObject object = root ? this.createRootObject() : new JSONObject();

			// put the lexical value
			object.put(VALUE, value);

			if (datatypeURI != null) // handle a typed literal
			{
				// put the type
				object.put(TYPE, this.abbrev(datatypeURI));
			}
			else // handle a plain literal
			{
				// put the language tag
				object.put(LANG, language);
			}
			
			return object;
		}		
	}
	
	/**
	 * Create a root object, including namespace mappings if namespace aware.
	 * @return a JSON object with namespace mappings
	 */
	protected JSONObject createRootObject() throws JSONException
	{
		JSONObject object = new JSONObject();
		
		if (namespaceAware) // create a namespace object
		{
			JSONObject nsObject = new JSONObject();
			for (Enumeration e = namespaces.keys(); e.hasMoreElements(); )
			{
				String prefix = (String) e.nextElement();
				String ns = (String) namespaces.get(prefix);
				nsObject.put(prefix, ns);
			}
			object.put(NAMESPACES, nsObject);
		}
		
		return object;
	}
	
	/**
	 * Return a JSON serialization of an RDF node.
	 * @param node
	 * @return
	 */
	public String serialize(RDFNode node) throws JSONException
	{
		return this.objectify(node).toString();
	}
	
	public Object objectify(RDFNode node) throws JSONException
	{
		if (node.isLiteral())
		{
			return this.objectifyLiteral((Literal)node);
		}
		else
		{
			return this.objectifyResource((Resource)node);
		}
	}
	
	/**
	 * Create a JSON object to represent an RDF resource.
	 * @param resource the resource to objectify
	 * @return
	 * @throws JSONException
	 */
	public JSONObject objectifyResource(Resource resource) throws JSONException
	{
		return this.objectifyResource(resource, true, 0, new ArrayList());
	}
	
	protected JSONObject objectifyResource(Resource resource, boolean root, int depth, Collection visited) throws JSONException
	{
		// check if List, Container or Resource proper
		if (resource.canAs(RDFList.class))
		{
			return this.objectifyRDFList((RDFList)resource.as(RDFList.class), root, depth, visited);
		}
		else if (resource.hasProperty(RDF.type, RDF.Seq))
		{
			return this.objectifyContainer((Seq)resource.as(Seq.class), root, depth, visited);
		}
		else if (resource.hasProperty(RDF.type, RDF.Bag))
		{
			return this.objectifyContainer((Bag)resource.as(Bag.class), root, depth, visited);
		}
		else if (resource.hasProperty(RDF.type, RDF.Alt))
		{
			return this.objectifyContainer((Alt)resource.as(Alt.class), root, depth, visited);
		}
		else
		{
			return this.objectifyResourceProper(resource, root, depth, visited);
		}
	}

	protected Object objectifyRDFNode(RDFNode node, boolean root, int depth, Collection visited) 
	throws JSONException 
	{
		if (node.isLiteral())
		{
			return this.objectifyLiteral((Literal)node, root);
		}
		else // handle resources
		{
			return this.objectifyResource((Resource)node, root, depth, visited);
		}
	}


	protected JSONObject objectifyRDFList(
			RDFList list, 
			boolean root, 
			int depth, 
			Collection visited) 
	throws JSONException
	{
		// initialise JSON object
		JSONObject jo = root ? this.createRootObject() : new JSONObject();
		
		// put the id
		if (list.isURIResource()) 
		{
			jo.put(ID, list.getURI());
		}
		else if (list.isAnon())
		{
			jo.put(ID, list.getId().toString());
		}
		
		if (!visited.contains(list))
		{
			visited.add(list);

			// create a JSON array to hold list values
			JSONArray vals = new JSONArray();
			
			// populate the array
			ExtendedIterator it = list.iterator();
			while (it.hasNext())
			{
				RDFNode node = (RDFNode) it.next();
				Object val = this.objectifyRDFNode(node, false, depth, visited);
				vals.put(val);
			}
			
			// put the array
			jo.put(LIST, vals);
		}
		
		return jo;
	}

	/**
	 * Create a JSON object representing and RDF list.
	 * @param list
	 * @return a JSON object representing an RDF list
	 * @throws JSONException
	 */
	public JSONObject objectifyRDFList(RDFList list) throws JSONException
	{
		return this.objectifyRDFList(list,true,0,new ArrayList());
	}
	
	public JSONObject objectifyContainer(Container con) throws JSONException
	{
		return this.objectifyContainer(con,true,0,new ArrayList());
	}

	protected JSONObject objectifyResourceProper(Resource resource, boolean root, int depth, Collection visited) throws JSONException
	{
		// create the JSON object
		JSONObject jo = root ? this.createRootObject() : new JSONObject();
		
		// put the id
		if (resource.isURIResource()) 
		{
			jo.put(ID, resource.getURI());
		}
		else if (resource.isAnon())
		{
			jo.put(ID, resource.getId().toString());
		}
		
		if (!visited.contains(resource))
		{
			visited.add(resource);
			
			// build a properties table for the resource
			Hashtable propsTable = this.buildPropsTable(resource, depth);
			
			// handle the properties
			Enumeration propNames = propsTable.keys();
			while (propNames.hasMoreElements())
			{
				String propName = (String) propNames.nextElement();
				ArrayList vals = (ArrayList) propsTable.get(propName);
				if (vals.size() == 1) // handle single value props
				{
					RDFNode node = (RDFNode) vals.get(0);
					if (node.isLiteral()) // handle literal value
					{
						jo.put(this.abbrev(propName), this.objectifyLiteral((Literal)node, false));
					}
					else // handle resource value
					{
						JSONObject nested = this.objectifyResource((Resource)node, false, depth+1, visited);
						jo.put(this.abbrev(propName), nested);
					}
				}
				else if (vals.size() > 1) // handle multi value props
				{
					JSONArray array = new JSONArray();
					for (int i=0;i<vals.size();i++)
					{
						RDFNode node = (RDFNode) vals.get(i);
						if (node.isLiteral()) // handle literal value
						{
							array.put(this.objectifyLiteral((Literal)node, false));
						}
						else if (node.isResource())	// handle resource values
						{
							JSONObject nested = this.objectifyResource((Resource)node, false, depth+1, visited);
							array.put(nested);
						}
					}
					JSONObject valsobj = (new JSONObject()).put(VALUES, array);
					jo.put(this.abbrev(propName), valsobj);
				}
			}
		}
		
		return jo;
	}
	
	protected JSONObject objectifyContainer(Container container, boolean root, int depth, Collection visited) 
	throws JSONException 
	{
		// initialise JSON object
		JSONObject jo = root ? this.createRootObject() : new JSONObject();
		
		// put the id
		if (container.isURIResource()) 
		{
			jo.put(ID, container.getURI());
		}
		else if (container.isAnon())
		{
			jo.put(ID, container.getId().toString());
		}
		
		if (!visited.contains(container))
		{
			visited.add(container);

			// create a JSON array to hold container values
			JSONArray vals = new JSONArray();
			
			// populate the array
			NodeIterator it = container.iterator();
			while (it.hasNext())
			{
				RDFNode node = it.nextNode();
				Object val = this.objectifyRDFNode(node, false, depth, visited);
				vals.put(val);
			}
			
			// put the array
			String propName = null;
			if (container instanceof Seq) { propName = SEQ; }
			else if (container instanceof Bag) { propName = BAG; }
			else if (container instanceof Alt) {propName = ALT; }
			jo.put(propName, vals);
		}
			
		return jo;
	}

	protected Hashtable buildPropsTable(Resource resource, int depth) 
	{
		StmtIterator it = resource.listProperties();
		
		// set up a hashtable of property to value mappings, so it's easy to differentiate single and 
		// multi- value props
		Hashtable propsTable = new Hashtable();
		while (it.hasNext())
		{
			Statement s = it.nextStatement();
			// get the property name
			String propName = s.getPredicate().getURI();
			ArrayList vals;
			if (propsTable.containsKey(propName))
			{
				vals = (ArrayList) propsTable.get(propName);
			}
			else
			{
				vals = new ArrayList();
				propsTable.put(propName, vals);
			}
			RDFNode node = s.getObject();
			if (node.isLiteral()) // add to the values
			{
				vals.add(node);
			}
			else // check if we should follow
			{
				if (arcs.containsKey(propName))
				{
					int max = ((Integer)arcs.get(propName)).intValue();
					if (max < 0 || depth < max)
					{
						vals.add(node);
					}
				}
			}
		}
		
		return propsTable;
	}
	
	
}