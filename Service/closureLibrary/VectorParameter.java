package closureLibrary;

import java.util.UUID;
import java.util.Vector;

import sofia_kp.SSAP_XMLTools;

public class VectorParameter implements Parameter{

	private String type = null;
	private String URI;
	private boolean valid = false;
	public Vector<SingleParameter> Content = new Vector<SingleParameter>(); //A VectorParameter "contains" single parameters of the same type of th vector type(i still don't know if this second assumpion is mandatory). 
	private int dimension = 0;
	private boolean dimensionDefined = false;

	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/VectorParameter.txt";


	public VectorParameter()
	{
		setRandomURI();
	}

	public void setURI(String URI)
	{
		this.URI = URI;
	}

	public void setRandomURI()
	{
		this.URI = UUID.randomUUID().toString();
	}

	public Integer getdimension()
	{
		//	if(dimensionDefined)
		//	{
		return dimension;
		//	}
		//	else
		//	{
		//		return null;
		//	}
	}

	public void setdimension( int dim)
	{
		//		if(!dimensionDefined)
		//		{
		dimension = dim;
		//			dimensionDefined = true;
		//		}
		//		else
		//		{
		//			Logger.printFile("VectorParameter: Error: Trying to set a dimension for a defined VectorParameter-> No Action Done", DebugFileName);
		//		}
	}

	public void setType(String Type)
	{
		this.type = Type;
		for(int i =0;  i < Content.size(); i++)
		{
			Content.elementAt(i).setType(Type);
		}
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

	public void URI(String uri)
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

	public void setContent(String[] Values, String type)
	{
		for (int i = 0; i < Values.length; i++)
		{
			SingleParameter p = new SingleParameter();
			p.setType(type);
			p.setValue(Values[i]);
			p.setValid(true);
			p.setPositionInVector(i);
			Content.add(p);
		}
		this.setdimension(Values.length);
		this.setValid(true);
	}

	public void setContent(String[] Values)
	{
		for (int i = 0; i < Values.length; i++)
		{
			SingleParameter p = new SingleParameter();
			p.setValue(Values[i]);
			p.setValid(true);
			p.setPositionInVector(i);
			Content.add(p);
		}
		this.setdimension(Values.length);
		this.setValid(true);
	}

	public void setEmptyContent(int dim, String type)
	{
		for (int i = 0; i < dim; i++)
		{
			SingleParameter p = new SingleParameter();

			p.setType(type);
			Content.add(p);
		}
		this.setdimension(dim);
	}


	public void setContentFromDescriptor(String[] VectorParameterDescriptor)
	/*
	 *VectorParameterDescriptor: 0 type, 1 dimension, 2 to end values
	 */
	{
		this.Content.removeAllElements();
		this.setType(VectorParameterDescriptor[0]);
		this.setdimension(Integer.parseInt(VectorParameterDescriptor[1]));
		for(int i = 2; (i < (VectorParameterDescriptor.length)); i++)
		{
			SingleParameter p = new SingleParameter();
			p.setType(this.type);
			p.setValid(true);
			p.setValue(VectorParameterDescriptor[i]);
			p.setPositionInVector(i-2);
			this.Content.add(p);
		}
		this.setValid(true);
	}

	public void setContentFromParameters(Vector<SingleParameter> elements)
	{
		if(this.type==null)
		{
			this.setType(elements.get(0).getType());
		}
		for (int i = 0; i < elements.size(); i++)
		{
			SingleParameter p = elements.elementAt(i);
			if(p.getType().equals(this.type))
			{
				Logger.printFile("VectorParameter Error: Tryed to put parameter of different type i.e. " + p.getType() + " in the same Vector of type " +
						this.type  + " ", DebugFileName);
				System.exit(1);
			}
			this.Content.add(elements.elementAt(i));

			p.setType(type);
			Content.add(p);
		}
		this.setdimension(elements.size());
	}

	public Vector<SingleParameter> getContent()
	{
		return this.Content;	
	}

	public Vector<Vector<String>> getTriples()
	{
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.VectorialParameter, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ParHasDatatype, this.getType(), "URI", "literal"));
		if(this.getdimension()!= null)
		{
			triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.VectorParhasDimension, this.getdimension().toString(), "URI", "literal"));	

			for(int i=0; i < this.dimension;i++)
			{
				if((this.Content != null) && (this.Content.size()==this.dimension))////FIXME Check are needed for managing dimensions
				{
					SingleParameter p = this.Content.elementAt(i);
					triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.VectorParhasParameter, p.getURI(), "URI", "URI"));
					triples = TriplesUtilities.concatTriples(triples, p.getTriples());
				}
			}
		}
		if(this.valid)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ParIsValid, OntologyVocabulary.ValidParameter, "URI", "literal"));
		}
		else
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ParIsValid, OntologyVocabulary.InvalidParameter, "URI", "literal"));

		}

		return triples;

	}

	public void addSingleParameter(SingleParameter p)
	{
		/*
		 *This method is used to add parameters to the vector of parameter, the dimension of the ve3ctor will increase due to this operation
		 */
		Content.add(p);
		p.setPositionInVector(Content.size()-1);
		dimension = dimension +1;
		if(this.type!=null)
		{
			p.setType(this.type);
		}
	}

	//	public Object getValue()
	//	{
	//		if(this.type.equals("int"))
	//		{
	//			return Integer.parseInt(this.value);
	//		}
	//		else if (this.type.equals("String"))
	//		{
	//			return (this.value);
	//		}
	//		else
	//		{
	//			return null;
	//		}
	//	}
	//	

}
