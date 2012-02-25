package closureLibrary;

import java.util.UUID;
import java.util.Vector;

import sofia_kp.SSAP_XMLTools;


public class SingleParameter implements Parameter{

	private String type;
	private Integer PositionInVector = null;
	private String URI;
	private boolean valid = false;
	private String value; //FIXME I use String as common serialiazation format for single parameter.When putting a value in triple it is transformed into a String and it must be reconstructed from that String and the type

	public SingleParameter()
	{
	setRandomURI();
	}
	
	public void setType(String Type)
	{
		this.type = Type;
	}

	public void setRandomURI()
	{
		this.URI = UUID.randomUUID().toString();
	}

	/*
	 * If each parameter has a reference to the closure which writes it and to the closures depending from its value, it is simpler
	 * to manage the graph, this information is explicit in the triples, but not so much in the object representation,
	 *  so these private fields maye are important. These fields have sense only in a Chain and in particular during reconstruction
	 *  to manage execution order and parallelism
	 */

	private Vector<AtomicClosure> InputOfClosures = new Vector<AtomicClosure>();
	private AtomicClosure OutputOfClosure;

	public void addInputToAtomicClosure(AtomicClosure a)
	{
		InputOfClosures.add(a);
	}
	public void setWritingAtomicClosure(AtomicClosure a)
	{
		OutputOfClosure = a;
	}

	public Vector<AtomicClosure> getDependingClosures()
	{
		return InputOfClosures;
	}
	public AtomicClosure getWritingClosure()
	{
		return OutputOfClosure;
	}


	public String getType()
	{
		return this.type;
	}

	public void setURI(String uri)
	{
		this.URI = uri;
	}

	public String getURI()
	{
		return this.URI;
	}
	public void setValid(Boolean Valid)
	{
		this.valid = Valid;
	}

	public Boolean getValid()
	{
		return this.valid;
	}

	public void setValue(String Value)
	{
		this.value = Value;
	}

	public String getValueForTriple()
	{
		return this.value;
	}
	public Object getValue()
	{
		if(this.type.equals("int"))
		{
			return Integer.parseInt(this.value);
		}
		else if (this.type.equals("String"))
		{
			return (this.value);
		}
		else
		{
			return null;
		}
	}

	public Vector<Vector<String>> getTriples()
	{
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.SingleParameter, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ParHasDatatype, this.getType(), "URI", "literal"));
		if(this.getValid())
		{
			triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ParHasValue, this.getValueForTriple(), "URI", "literal"));	
			triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ParIsValid, OntologyVocabulary.ValidParameter, "URI", "literal"));	
		}
		else
		{
			triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ParIsValid, OntologyVocabulary.InvalidParameter, "URI", "literal"));	
		}
	   if(this.PositionInVector!= null)
	   {
		   triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ParhasPositionInVector, this.PositionInVector.toString(), "URI", "literal"));
	   }
		
		return triples;
	}
	
	public void setPositionInVector(int positionInVector)
	{
		this.PositionInVector = positionInVector;
	}
}
