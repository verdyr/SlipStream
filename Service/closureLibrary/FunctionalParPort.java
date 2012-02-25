package closureLibrary;

import java.util.UUID;
import java.util.Vector;

import sofia_kp.SSAP_XMLTools;

public class FunctionalParPort {

	private FunctionalParameter Signal;
	private String name;
	private String type;
	private String URI;



	public FunctionalParPort()
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

	public void setSignal(FunctionalParameter fp)
	{
		this.Signal = fp;
	}

	public FunctionalParameter getSignal()
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

//	public void setType(String Type)FIXME: Here is it probably would be needed something related with haskell types declaration => modifications in functional Chain and atomic closure to support this  
//	{
//		this.type = Type;
//	}
//
//	public String getType()
//	{
//		return this.type;
//	}

	public Vector<Vector<String>> getTriples()
	{
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.functionalParPort, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.PortAttachedToPar, this.getSignal().getURI(), "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.PortHasName,this.name, "URI", "literal"));
		//triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.PortHasDatatype, this.getType(), "URI", "literal"));//only when datatype for functions will be supported


		triples = TriplesUtilities.concatTriples(triples, this.getSignal().getTriples());
		return triples;
	}
}
