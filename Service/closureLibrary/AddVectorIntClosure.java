package closureLibrary;

import java.util.UUID;
import java.util.Vector;

import sofia_kp.SSAP_XMLTools;

public class AddVectorIntClosure implements AtomicClosure{
	
	
	private String URI;//This has sense only when the computation is sent to the SIB, the assumption made in this file is that if I send two times a computation I have two different URIs 
	public  SingleParPort[] InputPorts ;
	
	public  SingleParPort[] OutputPorts;
	public VectorParPort[] OutputPortsVec;
	public VectorParPort[] InputPortsVec;
    public  static String Name = OntologyVocabulary.AddVectorIntClosure;
    
	//private static String[]  InputSinglePortNames = {"In1"} ;
	private static String[] OutputSinglePortNames = {"Out"} ;

	//private static String[] InputSinglePortTypes = {"int"} ;
	public static String[] OutputSinglePortTypes = {"int"} ;
	
	private static String[]  InputVectorPortNames = {"In1"} ;
    public static String[] OutputVectorPortNames = null ;

	public static String[] InputVectorPortTypes = {"int"} ;
//	private static String[] OutputVectorPortTypes = {"int"} ;
	
	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/AddVectorIntClosure";

	public String getName()
	{
		return AddVectorIntClosure.Name;
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

	public AddVectorIntClosure()//only the ports and URI, no parameters
	{
		setRandomURI();
		InputPortsVec = new VectorParPort[1];
		VectorParPort In1 = new VectorParPort();
		In1.setName(getInputVectorPortNames()[0]);
		In1.setType(InputVectorPortTypes[0]);
		In1.setSignal(null);
		InputPortsVec[0] = In1;
		
		SingleParPort Out = new SingleParPort();
		Out.setName(getOutputSinglePortNames()[0]);
		Out.setType(OutputSinglePortTypes[0]);
		Out.setSignal(null);
		OutputPorts = new SingleParPort[1];
		OutputPorts[0] = Out;
	}

	//This methods allows to initialize the input parameters and to attach them to the input ports, the id of the parameter is not
	//relevant in the context of a simple function, but we have to be careful with chain, 
	//After initialization the closure have all the parameter of the ports different from null;
	
	//For a vecttorial port a Vector<String[]> is required at least.
	
	//A complete Initialize could be 
	
	public void InitializeAddVectorIntClosure(Vector<String[]> VectorValues, VectorParameter[] inputVectorParameter, VectorParameter[] outputVectorParameter, String[] Singlevalues, SingleParameter[] inputSingleParameter, SingleParameter[] outputSingleParameter)
	{
		//two input parameters can be removed,  are here to be copied in some reference complete file about a general closure
		
//		//Single input :
//		SingleParameter p, pout;
//		for(int i = 0; i < AddVectorIntClosure.InputSinglePortNames.length;i++)
//		{   
//			p = new SingleParameter();
//			if(inputSingleParameter[i]==null)//If I have  not a parameter in this constructor I create one random
//			{
//
//				
//				p.setURI(UUID.randomUUID().toString());
//				p.setType(InputSinglePortTypes[i]);//TypeCheck needed?
//				if (!Singlevalues[i].equals(null)) //If I know the value of the parameter I  set the value and I put Valid == true
//				{
//					p.setValue(Singlevalues[i]);
//					p.setValid(true);
//				}
//				else  //The parameter has not a constant value to be assigned
//				{
//					p.setValid(false);
//				}
//
//			}
//			else//I have a parameter in the constructor and I use it
//			{
//				/*
//				 * Here I consider that who calls this initialization gives me the values of the inputs and the references in the
//				 * the parameters, the initialization assigns the value to the parameters
//				 * */
//				p = inputSingleParameter[i];
//				if (!(Singlevalues[i] == null)) //If I know the value of the parameter I  set the value and I put Valid == true
//				{
//					p.setValue(Singlevalues[i]);
//					p.setValid(true);
//				}
//				else  //The parameter has not a constant value to be assigned
//				{
//					p.setValid(false);
//				}
//			}
//			
//		
//			
//			
//			/*
//			 * Now p1 can be:
//			 * 1) random with assigned value and valid
//			 * 2) random without value and invalid
//			 * 3) not random with assigned value and true
//			 * 4) not random witout value and invalid
//			 * At moment these are all the situations managed
//			 */
//			InputPorts[i].setSignal(p);
//			p.addInputToAtomicClosure(this);
//		}
//		
		//Single out
		SingleParameter pout;
			for(int i = 0; i < AddVectorIntClosure.getOutputSinglePortNames().length;i++)
		{
			if (outputSingleParameter.equals(null))//If I have not an output parameter from the constructor (so no reference) I create one Randomly
			{
				pout = new SingleParameter();
				pout.setURI(UUID.randomUUID().toString());
				pout.setType(OutputSinglePortTypes[i]); 
			}
			else
			{
				pout = outputSingleParameter[i];
				pout.setType(OutputSinglePortTypes[i]);//Unuseful if  we suppose to pass the right reference
			}
			this.OutputPorts[i].setSignal(pout);
			pout.setWritingAtomicClosure(this);
		}
		
		//Vector input
			
			VectorParameter p_v;
			for(int i = 0; i < AddVectorIntClosure.getInputVectorPortNames().length;i++)
			{   
				p_v = new VectorParameter();
				if((inputVectorParameter== null)||(inputVectorParameter[i]==null))//If I have  not a parameter in this constructor I create one random
				{

					
					p_v.setRandomURI();
					p_v.setType(InputVectorPortTypes[i]);//TypeCheck needed?
					if (!(VectorValues.elementAt(i) == null)) //If I know the value of the parameter I  set the value and I put Valid == true
					{
						p_v.setContent(VectorValues.elementAt(i), AddVectorIntClosure.InputVectorPortTypes[i]);
						p_v.setValid(true);
					}
					else  //The parameter has not a constant value to be assigned pr it is just ready
					{
						p_v.setValid(false);
					}

				}
				else//I have a parameter in the constructor and I use it
				{
					/*
					 * Here I consider that who calls this initialization gives me the values of the inputs and the references in the
					 * the parameters, the initialization assigns the value to the parameters
					 * */
					p_v = (inputVectorParameter[i]);
					Logger.printFile("I have given the the following URI to the input: " + p_v.getURI() +"\n", DebugFileName);
					if ((VectorValues!=null)&& (VectorValues.elementAt(i) != null)) //If I know the value of the parameter I  set the value and I put Valid == true
					{
						p_v.setContent(VectorValues.elementAt(i), AddVectorIntClosure.InputVectorPortTypes[i]);
						p_v.setValid(true);
					}
					else  //The parameter has not a constant value to be assigned ot it is ready
					{
						if(!p_v.getValid())
						{
						p_v.setValid(false);
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
				InputPortsVec[i].setSignal(p_v);
				Logger.printFile("Final URI of the input of the closure before serialization: " + InputPortsVec[0].getSignal().getURI() +"\n", DebugFileName);

				p_v.addInputToAtomicClosure(this);
			}
			
//			//output Vector
//			VectorParameter pout_v;
//			for(int i = 0; i < AddVectorIntClosure.OutputPortNamesVec;i++)
//		{
//			if (outputVectorParameter.equals(null))//If I have not an output parameter from the constructor (so no reference) I create one Randomly
//			{
//				pout_v = new VectorParameter();
//				pout_v.setRandomURI();
//				pout.setType(OutputPortTypesVec[i]); 
//			}
//			else
//			{
//				pout = outputVectorParameter[i];
//			}
//			this.OutputPorts[i].setSignal(pout);
//			pout.setWritingAtomicClosure(this);
//		}
//		

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
		Integer Acc = 0;
		VectorParameter pin_v = InputPortsVec[0].getSignal();
		Vector<SingleParameter> parameters = pin_v.getContent();
		for(int i = 0; i < parameters.size(); i++)
		{
			Acc = Acc+ Integer.parseInt(parameters.elementAt(i).getValueForTriple());
		}
		this.OutputPorts[0].getSignal().setValue(Acc.toString());
		this.OutputPorts[0].getSignal().setValid(true);
		return Acc;
	}
	//Some  rdf:type declarations are not so useful, but I leave them at moment 
	//Some of the triples seems to be redundant, but this is not an optimized version.	
	public Vector<Vector<String>> SerializeTriples()
	{
		Logger.printFile(this.printClosure(), DebugFileName);
		


		
//		String[] InPortParURI = new String[AddVectorIntClosure.InputPortNames.length];
//		for(int i = 0; i < AddIntClosure.InputPortNames.length; i++)
//		{
//			InPortParURI[i] = this.InputPorts[i].getSignal().getURI();;
//		}
	//	String[] InPortVectorParURI = new String[AddVectorIntClosure.getInputVectorPortNames().length];
	//	for(int i = 0; i < AddVectorIntClosure.getInputVectorPortNames().length; i++)
	//	{
	//		InPortVectorParURI[i] = this.InputPortsVec[i].getSignal().getURI();;
	//	}
			
		
		
		//Triples
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.AtomicClosure, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ClosureHasName,OntologyVocabulary.AddVectorIntClosure, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ComputationHasFunctionalBehaviour,OntologyVocabulary.AddVectorIntClosure, "URI", "URI"));
		
		for(int i = 0; i < AddVectorIntClosure.getInputVectorPortNames().length;i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasInputPort, this.InputPortsVec[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.InputPortsVec[i].getTriples());
		}
		for(int i = 0; i < AddVectorIntClosure.getOutputSinglePortNames().length;i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasOutputPort, this.OutputPorts[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.OutputPorts[i].getTriples());
		}
		
//		for(int i = 0; i < AddIntClosure.InputPortNames.length; i++)
//		{
//			triples.add(xmlTools.newTriple(inputPortsURI[i], OntologyVocabulary.type,OntologyVocabulary.InputPort, "URI", "URI"));
//			triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ClosureHasInputPort, inputPortsURI[i], "URI", "URI"));
//			triples.add(xmlTools.newTriple(inputPortsURI[i], OntologyVocabulary.PortHasDatatype,AddIntClosure.InputPortTypes[i], "URI", "literal"));
//			triples.add(xmlTools.newTriple(inputPortsURI[i], OntologyVocabulary.PortHasName,AddIntClosure.InputPortNames[i], "URI", "literal"));
//			triples.add(xmlTools.newTriple(InPortParURI[i], OntologyVocabulary.type, OntologyVocabulary.Parameter, "URI", "URI"));
//			triples.add(xmlTools.newTriple(inputPortsURI[i], OntologyVocabulary.PortAttachedToPar,InPortParURI[i] , "URI", "URI"));
//			triples.add(xmlTools.newTriple(InPortParURI[i], OntologyVocabulary.ParHasDatatype, AddIntClosure.InputPortTypes[i], "URI", "literal"));
//			if(this.InputPorts[i].getSignal().getValid())
//			{
//				triples.add(xmlTools.newTriple(InPortParURI[i], OntologyVocabulary.ParHasValue, this.InputPorts[i].getSignal().getValueForTriple(), "URI", "literal"));
//				triples.add(xmlTools.newTriple(InPortParURI[i], OntologyVocabulary.ParIsValid, OntologyVocabulary.ValidParameter, "URI", "literal"));
//			}
//			else
//			{
//				triples.add(xmlTools.newTriple(InPortParURI[i], OntologyVocabulary.ParIsValid, OntologyVocabulary.InvalidParameter, "URI", "literal"));
//			}
//		}
//		//		triples.add(xmlTools.newTriple(InPort2Uri, OntoloogyVocabulary.type,OntoloogyVocabulary.InputPort, "URI", "URI"));
//		//		triples.add(xmlTools.newTriple(this.URI,   OntoloogyVocabulary.ClosureHasInputPort, InPort2Uri, "URI", "URI"));
//		//		triples.add(xmlTools.newTriple(InPort2Uri, OntoloogyVocabulary.PortHasType,AddIntClosure.InputPort2Type, "URI", "literal"));
//		//		triples.add(xmlTools.newTriple(InPort2Uri, OntoloogyVocabulary.PortHasName,AddIntClosure.InputPort2Name, "URI", "literal"));
//		for(int i = 0; i < AddIntClosure.OutputPortNames.length; i++)
//		{
//			triples.add(xmlTools.newTriple(outputPortsURI[i], OntologyVocabulary.type,OntologyVocabulary.OutputPort, "URI", "URI"));
//			triples.add(xmlTools.newTriple(this.URI,          OntologyVocabulary.ClosureHasOutputPort, outputPortsURI[i], "URI", "URI"));
//			triples.add(xmlTools.newTriple(outputPortsURI[i], OntologyVocabulary.PortHasDatatype,AddIntClosure.OutputPortTypes[i], "URI", "literal"));
//			triples.add(xmlTools.newTriple(outputPortsURI[i], OntologyVocabulary.PortHasName,AddIntClosure.OutputPortNames[i], "URI", "literal"));
//			
//			triples.add(xmlTools.newTriple(OutPortParURI[i], OntologyVocabulary.type, OntologyVocabulary.Parameter, "URI", "URI"));
//			triples.add(xmlTools.newTriple(outputPortsURI[i], OntologyVocabulary.PortAttachedToPar, OutPortParURI[i], "URI", "URI"));
//			triples.add(xmlTools.newTriple(OutPortParURI[i], OntologyVocabulary.ParHasDatatype, AddIntClosure.OutputPortTypes[i], "URI", "literal"));
//			if(this.OutputPorts[0].getSignal().getValid())
//			{
//				triples.add(xmlTools.newTriple(OutPortParURI[i], OntologyVocabulary.ParHasValue, this.OutputPorts[i].getSignal().getValueForTriple(), "URI", "literal"));
//				triples.add(xmlTools.newTriple(OutPortParURI[i], OntologyVocabulary.ParIsValid, OntologyVocabulary.ValidParameter, "URI", "literal"));
//			}
//			else
//			{
//				triples.add(xmlTools.newTriple(OutPortParURI[i], OntologyVocabulary.ParIsValid, OntologyVocabulary.InvalidParameter, "URI", "literal"));
//			}
//		}
		
		
		
		
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
	public Parameter[] getInputParameter() {
		
		Parameter[] temp = new Parameter[AddVectorIntClosure.getInputVectorPortNames().length];// in general + the single port + the functional port
		for (int i = 0; i < AddVectorIntClosure.getInputVectorPortNames().length;i++)
		{
			Parameter p = this.InputPortsVec[i].getSignal();
			if(p != null)
			{
				temp[i] = p;
			}
			else
			{
				System.out.println("Closure not correctly initialized: the parameter on port " + getInputVectorPortNames()[i] + " = null!");
			}
		}
		return temp;
	}

	@Override
	public Parameter[] getOutputParameter() {
		Parameter[] temp = new SingleParameter[AddVectorIntClosure.getOutputSinglePortNames().length];
		for (int i = 0; i < AddVectorIntClosure.getOutputSinglePortNames().length;i++)
		{
			SingleParameter p = this.OutputPorts[i].getSignal();
			if(p != null)
			{
				temp[i] = p;
			}
			else
			{
				System.out.println("Closure not correctly initialized: the parameter on port " + this.getOutputSinglePortNames()[i] + " = null!");
			}
		}
		return temp;
	}
	
//	public void setInputPort(String[] InputPortDescriptor)
//	{
//		SingleParPort temp = new SingleParPort();
//		temp.setSignal(new SingleParameter());
//		temp.getSignal().setValue(InputPortDescriptor[1]);
//		temp.getSignal().setValid(true);
//		String portName = InputPortDescriptor[0];
//		int TargetPortIndex = getInputPortIndexFromName(portName);
//		this.InputPorts[TargetPortIndex] = temp;
//		
//	}
	
	
	public void setInputVectorPort(String[] VectorInputPortDescriptor)
	{
		VectorParPort temp = new VectorParPort();
		temp.setSignal(new VectorParameter());
		String[] VectorParameterDescriptor = new String[VectorInputPortDescriptor.length -1];
		for (int i = 0; (i < (VectorInputPortDescriptor.length-1));i++)
		{
			VectorParameterDescriptor[i] = VectorInputPortDescriptor[i+1];
		}
		
		temp.getSignal().setContentFromDescriptor(VectorParameterDescriptor);
		temp.getSignal().setValid(true);
		String portName = VectorInputPortDescriptor[0];
		int TargetPortIndex = getInputVectorPortIndexFromName(portName);
		this.InputPortsVec[TargetPortIndex] = temp;
		
	}
	
	public Integer getInputVectorPortIndexFromName(String portName)
	{
		for(Integer i = 0; i < AddVectorIntClosure.InputVectorPortNames.length;i++)
		{
			if (AddVectorIntClosure.InputVectorPortNames[i].equals(portName))
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
			if (AddVectorIntClosure.OutputSinglePortNames[i].equals(portName))
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
	public void setVectorOutputPortReference(Vector<String[]> VectorOutputPortDescriptor)
	{
		//Nothing to be done because there are not Vectorial OutPut Ports
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
		for(int i=0; i < AddVectorIntClosure.getInputVectorPortNames().length;i++ )
		{
			temp = temp + ("port index = " + i + " + name = " + InputPortsVec[i] + "URI= " + this.InputPortsVec[i].getURI() + "\n");
			temp = temp + ("parameter URI: " + this.InputPortsVec[i].getSignal().getURI() + " ParameterValid: " + this.InputPortsVec[i].getSignal().getValid() +
					        "\n"); 
		}
		temp = temp + "output ports:\n";
		for(int i=0; i < AddVectorIntClosure.getOutputSinglePortNames().length;i++ )
		{
			temp = temp + ("port index = " + i + " + name = " + this.OutputPorts[i] + "URI= " + this.OutputPorts[i].getURI() + "\n");
			temp = temp + ("parameter URI: " + this.OutputPorts[i].getSignal().getURI() + " ParameterValue: " + this.OutputPorts[i].getSignal().getValueForTriple() +
					       "ParameterValid " + this.OutputPorts[i].getSignal().getValid()+ "\n"); 
		}
		return temp;
	}

	public static void setInputVectorPortNames(String[] inputVectorPortNames) {
		InputVectorPortNames = inputVectorPortNames;
	}

	public static String[] getInputVectorPortNames() {
		return InputVectorPortNames;
	}

	public static void setOutputSinglePortNames(String[] outputSinglePortNames) {
		OutputSinglePortNames = outputSinglePortNames;
	}

	public static String[] getOutputSinglePortNames() {
		return OutputSinglePortNames;
	}
	
	public VectorParPort[] getVectorialInputPorts()
	{
		return this.InputPortsVec;
	}
	
	public VectorParPort[] getVectorialOutputPorts()
	{
		return this.OutputPortsVec;
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
