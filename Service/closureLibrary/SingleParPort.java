package closureLibrary;

import java.util.UUID;
import java.util.Vector;

import sofia_kp.SSAP_XMLTools;


public class SingleParPort {
	
	private SingleParameter Signal;
	private String name;
	private String type;
	private String URI;
	
	
	
	public SingleParPort()
	{
		setRandomURI();
	}
	
	public void setRandomURI()
	{
		this.URI = UUID.randomUUID().toString();
	}
	
	public String getURI()
	{
		return this.URI;
	}
	
	public void setSignal(SingleParameter p)
	{
		this.Signal = p;
	}
	
	public SingleParameter getSignal()
	{
		return this.Signal;
	}
	
	public String getName()
	{
		return this.name;
	}
	public void setName(String PortName)
	{
		this.name = PortName;
	}
	
	public void setType(String Type)
	{
		this.type = Type;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public Vector<Vector<String>> getTriples()
	{
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.SingleParPort, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.PortAttachedToPar, this.getSignal().getURI(), "URI", "literal"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.PortHasName,this.name, "URI", "literal"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.PortHasDatatype, this.getType(), "URI", "literal"));


		triples = TriplesUtilities.concatTriples(triples, this.getSignal().getTriples());
		return triples;
	}

}
