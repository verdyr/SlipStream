verdyrpackage closureLibrary;

import java.util.UUID;
import java.util.Vector;

import sofia_kp.SSAP_XMLTools;

public class Add3IntClosure implements AtomicClosure{
	
	private String URI;//This has sense only when the computation is sent to the SIB, the assumption made in this file is that if I send two times a computation I have two different URIs 
	public  SingleParPort[] InputPorts ;
	public  SingleParPort[] OutputPorts;
	public VectorParPort[] OutputPortsVec;
	public VectorParPort[] InputPortsVec;
    public  static String Name = OntologyVocabulary.Add3IntClosure;
	public static String[]  InputPortNames = {"In1", "In2", "In3"} ;
	public static String[] OutputPortNames = {"Out"} ;

	private static String[] InputPortTypes = {"int", "int", "int"} ;
	private static String[] OutputPortTypes = {"int"} ;
	
	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/Add3intClosure.txt";

	public String getName()
	{
		return Add3IntClosure.Name;
	}
	
	public void setURI(String Uri)
	{
		this.URI = Uri;
	}

	public void setRandomURI()
	{
		this.URI = UUID.randomUUID().toString();
	}

	public String getURI()
	{
		return this.URI;
	}

	public Add3IntClosure()//only the ports and URI, no parameters
	{
		setRandomURI();
		InputPorts = new SingleParPort[3];
		SingleParPort In1 = new SingleParPort();
		In1.setRandomURI();
		In1.setName(InputPortNames[0]);
		In1.setType(InputPortTypes[0]);
		In1.setSignal(null);
		InputPorts[0] = In1;
		SingleParPort In2 = new SingleParPort();
		In2.setRandomURI();
		In2.setName(InputPortNames[1]);
		In2.setType(InputPortTypes[1]);
		In2.setSignal(null);
		InputPorts[1] = In2;
		SingleParPort In3 = new SingleParPort();
		In3.setRandomURI();
		In3.setName(InputPortNames[2]);
		In3.setType(InputPortTypes[2]);
		In3.setSignal(null);
		InputPorts[2] = In3;
		SingleParPort Out = new SingleParPort();
		Out.setRandomURI();
		Out.setName(OutputPortNames[0]);
		Out.setType(OutputPortTypes[0]);
		Out.setSignal(null);
		OutputPorts = new SingleParPort[1];
		OutputPorts[0] = Out;
	}

	//This methods allows to initialize the input parameters and to attach them to the input ports, the id of the parameter is not
	//relevant in the context of a simple function, but we have to be careful with chain, 
	//After initialization the closure have all the parameter of the ports different from null;
	public void InitializeAdd3IntClosure(String[] values, SingleParameter[] inputParameter, SingleParameter[] outputParameter)                                                        //
	{
		SingleParameter p,pout;
		for(int i = 0; i < Add3IntClosure.InputPortNames.length;i++)
		{   
			p = new SingleParameter();
			if(inputParameter[i]==null)//If I have  not a parameter in this constructor I create one random
			{

				
				p.setURI(UUID.randomUUID().toString());
				p.setType(InputPortTypes[i]);//TypeCheck needed?
				if (!values[i].equals(null)) //If I know the value of the parameter I  set the value and I put Valid == true
				{
					p.setValue(values[i]);
					p.setValid(true);
				}
				else  //The parameter has not a constant value to be assigned
				{
					p.setValid(false);
				}

			}
			else//I have a parameter in the constructor and I use it
			{
				/*
				 * Here I consider that who calls this initialization gives me the values of the inputs and the references in the
				 * the parameters, the initialization assigns the value to the parameters
				 * */
				p = inputParameter[i];
				if (!(values[i] == null)) //If I know the value of the parameter I  set the value and I put Valid == true
				{
					p.setValue(values[i]);
					p.setValid(true);
				}
				else  //The parameter has not a constant value to be assigned
				{
					if(!p.getValid())
					{
					p.setValid(false);
					}
				}
			}
			/*
			 * Now p1 can be:
			 * 1) random with assigned value and valid
			 * 2) random without value and invalid
			 * 3) not random with assigned value and true
			 * 4) not random witout value and invalid
			 * At moment these are all the situations managed
			 */
			InputPorts[i].setSignal(p);
			p.addInputToAtomicClosure(this);
		}
		//		if(inputParameter[1].equals(null))
		//		{
		//			p2 = new Parameter();
		//			p2.setID(UUID.randomUUID().toString());
		//			p2.setType(InputPort2Type);//TypeCheck needed?
		//			if (!values[1].equals(null))
		//			{
		//				p2.setValue(values[1]);
		//				p2.setValid(true);
		//			}
		//			else
		//			{
		//				p2.setValid(false);
		//			}
		//
		//		}
		//		else
		//		{
		//			p2 = inputParameter[1];
		//			if (!values[1].equals(null))
		//			{
		//				p2.setValue(values[1]);
		//				p2.setValid(true);
		//			}
		//			else
		//			{
		//				p2.setValid(false);
		//			}
		//		}
		//
		//		InputPorts[1].setSignal(p2);
		for(int i = 0; i < Add3IntClosure.OutputPortNames.length;i++)
		{
			if (outputParameter.equals(null))//If I have not an output parameter from the constructor (so no reference) I create one Randomly
			{
				pout = new SingleParameter();
				pout.setURI(UUID.randomUUID().toString());
				pout.setType(OutputPortTypes[i]); 
			}
			else
			{
				pout = outputParameter[i];
			}
			this.OutputPorts[i].setSignal(pout);
			pout.setWritingAtomicClosure(this);
		}

	}


	public int run()
	{
		//Here should be some type check, perhaps it has been made before in the computational chain. The check is for understanding 
		//if the parameter is ready or not
		//At moment the cast is made twice, perhaps it is useful only here
		if (this.OutputPorts[0] == null)
		{
			SingleParPort temp = new SingleParPort();
			temp.setSignal(new SingleParameter());
			this.OutputPorts[0] = temp;
		}
		else if(this.OutputPorts[0].getSignal() == null)
		{
			this.OutputPorts[0].setSignal(new SingleParameter());
		}
		Logger.printFile("\nAdd3IntClosure-> I am going to execute the sum input 0 " + InputPorts[0].getSignal() , DebugFileName);

		Logger.printFile("\nAdd3IntClosure-> I am going to execute the sum input 1 " + InputPorts[1].getSignal() , DebugFileName);

		Logger.printFile("\nAdd3IntClosure-> I am going to execute the sum input 2 " + InputPorts[2].getSignal() , DebugFileName);
		Integer result = (Integer.parseInt(InputPorts[0].getSignal().getValueForTriple()) + (Integer.parseInt(InputPorts[1].getSignal().getValueForTriple())) + (Integer.parseInt(InputPorts[2].getSignal().getValueForTriple())));
		this.OutputPorts[0].getSignal().setValue(result.toString());
		this.OutputPorts[0].getSignal().setValid(true);
		return result;
	}
	//Some  rdf:type declarations are not so useful, but I leave them at moment 
	//Some of the triples seems to be redundant, but this is not an optimized version.	
	public Vector<Vector<String>> SerializeTriples()
	{
		Logger.printFile(this.printClosure(), DebugFileName);
		

		
		
		
		//Triples
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.AtomicClosure, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ClosureHasName,OntologyVocabulary.Add3IntClosure, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ComputationHasFunctionalBehaviour,OntologyVocabulary.Add3IntClosure, "URI", "URI"));
		
		for(int i = 0; i < Add3IntClosure.InputPortNames.length; i++)
		{
	
			triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ClosureHasInputPort, this.InputPorts[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.InputPorts[i].getTriples());
		}
		for(int i = 0; i < Add3IntClosure.OutputPortNames.length; i++)
		{
			triples.add(xmlTools.newTriple(this.URI,          OntologyVocabulary.ClosureHasOutputPort, this.OutputPorts[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.OutputPorts[i].getTriples());
		}
		return triples;
//		triples.add(xmlTools.newTriple(InPort1ParURI, OntoloogyVocabulary.type, OntoloogyVocabulary.Parameter, "URI", "URI"));
//		triples.add(xmlTools.newTriple(InPort1ParURI, OntoloogyVocabulary.ParAttachedToPort, InPort1Uri, "URI", "URI"));
//		triples.add(xmlTools.newTriple(InPort1ParURI, OntoloogyVocabulary.ParHasType, AddIntClosure.InputPort1Type, "URI", "literal"));
//		if(this.InputPorts[0].getSignal().getValid())
//		{
//			triples.add(xmlTools.newTriple(InPort1ParURI, OntoloogyVocabulary.ParHasValue, this.InputPorts[0].getSignal().getValueForTriple(), "URI", "literal"));
//			triples.add(xmlTools.newTriple(InPort1ParURI, OntoloogyVocabulary.ParIsValid, "true", "URI", "Literal"));
//		}
//		else
//		{
//			triples.add(xmlTools.newTriple(InPort1ParURI, OntoloogyVocabulary.ParIsValid, "false", "URI", "Literal"));
//		}
//
//		triples.add(xmlTools.newTriple(InPort2ParURI, OntoloogyVocabulary.type, OntoloogyVocabulary.Parameter, "URI", "URI"));
//		triples.add(xmlTools.newTriple(InPort2ParURI, OntoloogyVocabulary.ParAttachedToPort, InPort2Uri, "URI", "URI"));
//		triples.add(xmlTools.newTriple(InPort2ParURI, OntoloogyVocabulary.ParHasType, AddIntClosure.InputPort2Type, "URI", "literal"));
//		if(this.InputPorts[1].getSignal().getValid())
//		{
//			triples.add(xmlTools.newTriple(InPort2ParURI, OntoloogyVocabulary.ParHasValue, this.InputPorts[1].getSignal().getValueForTriple(), "URI", "literal"));
//			triples.add(xmlTools.newTriple(InPort2ParURI, OntoloogyVocabulary.ParIsValid, "true", "URI", "Literal"));
//		}
//		else
//		{
//			triples.add(xmlTools.newTriple(InPort2ParURI, OntoloogyVocabulary.ParIsValid, "false", "URI", "Literal"));
//		}


//		triples.add(xmlTools.newTriple(OutPortParURI, OntoloogyVocabulary.type, OntoloogyVocabulary.Parameter, "URI", "URI"));
//		triples.add(xmlTools.newTriple(OutPortParURI, OntoloogyVocabulary.ParAttachedToPort, OutPortUri, "URI", "URI"));
//		triples.add(xmlTools.newTriple(OutPortParURI, OntoloogyVocabulary.ParHasType, AddIntClosure.OutputPortType, "URI", "literal"));
//		if(this.OutputPorts[0].getSignal().getValid())
//		{
//			triples.add(xmlTools.newTriple(OutPortParURI, OntoloogyVocabulary.ParHasValue, this.OutputPorts[0].getSignal().getValueForTriple(), "URI", "literal"));
//			triples.add(xmlTools.newTriple(OutPortParURI, OntoloogyVocabulary.ParIsValid, "true", "URI", "Literal"));
//		}
//		else
//		{
//			triples.add(xmlTools.newTriple(OutPortParURI, OntoloogyVocabulary.ParIsValid, "false", "URI", "Literal"));
//		}

		



	}

	@Override
	public SingleParameter[] getInputParameter() {
		
		SingleParameter[] temp = new SingleParameter[Add3IntClosure.InputPortNames.length];
		for (int i = 0; i < Add3IntClosure.InputPortNames.length;i++)
		{
			SingleParameter p = this.InputPorts[i].getSignal();
			if(p != null)
			{
				temp[i] = p;
			}
			else
			{
				System.out.println("Closure not correctly initialized: the parameter on port " + Add3IntClosure.InputPortNames[i] + " = null!");
			}
		}
		return temp;
	}

	@Override
	public SingleParameter[] getOutputParameter() {
		SingleParameter[] temp = new SingleParameter[Add3IntClosure.OutputPortNames.length];
		for (int i = 0; i < Add3IntClosure.OutputPortNames.length;i++)
		{
			SingleParameter p = this.OutputPorts[i].getSignal();
			if(p != null)
			{
				temp[i] = p;
			}
			else
			{
				System.out.println("Closure not correctly initialized: the parameter on port " + Add3IntClosure.OutputPortNames[i] + " = null!");
			}
		}
		return temp;
	}
	
	public void setInputPort(String[] InputPortDescriptor)
	{
		SingleParPort temp = new SingleParPort();
		temp.setSignal(new SingleParameter());
		temp.getSignal().setValue(InputPortDescriptor[1]);
		temp.getSignal().setValid(true);
		String portName = InputPortDescriptor[0];
		int TargetPortIndex = getInputPortIndexFromName(portName);
		this.InputPorts[TargetPortIndex] = temp;
		
	}
	
	public Integer getInputPortIndexFromName(String portName)
	{
		for(Integer i = 0; i < this.InputPorts.length;i++)
		{
			if (Add3IntClosure.InputPortNames[i].equals(portName))
			{
				return i;
			}
		}
		return null;
	}
	
	
	public void setOutputPortReference(String[] OutputPortDescriptor) //OutputPortDescriptor = [PortName, ParameterURI]
	{
		int TargetPortIndex = getOutputPortIndexFromName(OutputPortDescriptor[0]);
		this.OutputPorts[TargetPortIndex].getSignal().setURI(OutputPortDescriptor[1]);
	}
	
	public Integer getOutputPortIndexFromName(String portName)
	{
		for(Integer i = 0; i < this.OutputPorts.length;i++)
		{
			if (Add3IntClosure.OutputPortNames[i].equals(portName))
			{
				return i;
			}
		}
		return null;
	}

	@Override
	public SingleParPort[] getInputPorts() {
		return this.InputPorts;
	}

	@Override
	public SingleParPort[] getOutputPorts() {
		return this.OutputPorts;
	}
	
	public String printClosure()
	{
		String temp = "";
		temp = temp + "Closure:\n";
		temp = temp + "URI: " + this.getURI();
		temp = temp + "Name: " + this.getName(); 
		temp = temp + "input ports:\n";
		for(int i=0; i < Add3IntClosure.InputPortNames.length;i++ )
		{
			temp = temp + ("port index = " + i + " + name = " + Add3IntClosure.InputPortNames[i] + "URI= " + this.InputPorts[i].getURI() + "\n");
			temp = temp + ("parameter URI: " + this.InputPorts[i].getSignal().getURI() + " ParameterValue: " + this.InputPorts[i].getSignal().getValueForTriple() +
					       "ParameterValid " + this.InputPorts[i].getSignal().getValid()+ "\n"); 
		}
		temp = temp + "output ports:\n";
		for(int i=0; i < Add3IntClosure.OutputPortNames.length;i++ )
		{
			temp = temp + ("port index = " + i + " + name = " + Add3IntClosure.OutputPortNames[i] + "URI= " + this.OutputPorts[i].getURI() + "\n");
			temp = temp + ("parameter URI: " + this.OutputPorts[i].getSignal().getURI() + " ParameterValue: " + this.OutputPorts[i].getSignal().getValueForTriple() +
					       "ParameterValid " + this.OutputPorts[i].getSignal().getValid()+ "\n"); 
		}
		return temp;
	}
	
	
	public VectorParPort[] getVectorialInputPorts()
	{
		return this.InputPortsVec;
	}
	
	public VectorParPort[] getVectorialOutputPorts()
	{
		return this.OutputPortsVec;
	}
	public void setVectorOutputPortReference(Vector<String[]> VectorOutputPortDescriptor)
	{
		//Nothing to be done because there are not Vectorial OutPut Ports
	}
	
	public void WriteTriplesOnFile()
	{
		Vector<Vector<String>> triples = this.SerializeTriples();
		//This prints the triples in the debug file
		Logger.printFile("Writing Closure triples\n" +
		         "Closure: " + this.getURI() + "\n" +
		         "Triples:\n",DebugFileName);
		for (int i = 0; i < triples.size();i++)
		{
			
			Vector<String> Triple = triples.elementAt(i);
			{
				
					Logger.printFile("sub = " + Triple.elementAt(0) + "; pred = " + Triple.elementAt(1) + "; obj = " + Triple.elementAt(2) + ";\n",DebugFileName);
				
			}
		}
	}

}
