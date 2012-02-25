package closureLibrary;

import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

import closureChain.FunctionalChain;

import executor.FunctionExecutor;

import sofia_kp.KPICore;
import sofia_kp.SSAP_XMLTools;

public class MapClosure implements AtomicClosure{


	private String URI;//This has sense only when the computation is sent to the SIB, the assumption made in this file is that if I send two times a computation I have two different URIs 
	public  SingleParPort[] InputPorts ;

	public  SingleParPort[] OutputPorts;
	public VectorParPort[] OutputPortsVec;
	public VectorParPort[] InputPortsVec;
	public FunctionalParPort[] FuncInputPorts;
	public FunctionalParPort[] FuncOutputPorts;

	public  static String Name = OntologyVocabulary.MapClosure;

	//private static String[]  InputSinglePortNames = {"In1"} ;
	//private static String[] OutputSinglePortNames = {"Out"} ;

	//private static String[] InputSinglePortTypes = {"int"} ;
	//public static String[] OutputSinglePortTypes = {"int"} ;

	private static String[]  InputVectorPortNames = {"In1"} ;
	public static String[] OutputVectorPortNames = {"Map_Out"} ;

	private static String[] InputFunctionalPortNames = {"In2"} ;
	//public static String[] OutputSinglePortTypes = {"int"} ;

	public static String[] InputVectorPortTypes = {"int"} ;//If the content of the vector is "int" is only temporary
	//	private static String[] OutputVectorPortTypes = {"int"} ;

	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/MapClosure.txt";

	public String getName()
	{
		return MapClosure.Name;
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

	public MapClosure()//only the ports and URI, no parameters
	{
		setRandomURI();
		InputPortsVec = new VectorParPort[1];
		VectorParPort In1 = new VectorParPort();
		In1.setName(getInputVectorPortNames()[0]);
		if((InputVectorPortTypes!= null)&&(InputVectorPortTypes.length>0))
		{
			In1.setType(InputVectorPortTypes[0]);
		}
		In1.setSignal(null);
		InputPortsVec[0] = In1;

		FunctionalParPort In2 = new FunctionalParPort();
		In2.setSignal(null);
		In2.setName(InputFunctionalPortNames[0]);
		FuncInputPorts = new FunctionalParPort[1];
		FuncInputPorts[0] = In2;

		OutputPortsVec= new VectorParPort[1];
		VectorParPort Out = new VectorParPort();
		Out.setName(OutputVectorPortNames[0]);
		if((InputVectorPortTypes!= null)&&(InputVectorPortTypes.length>0))
		{
			Out.setType(InputVectorPortTypes[0]);
		}
		Out.setSignal(null);
		OutputPortsVec[0] = Out;


	}

	//This methods allows to initialize the input parameters and to attach them to the input ports, the id of the parameter is not
	//relevant in the context of a simple function, but we have to be careful with chain, 
	//After initialization the closure have all the parameter of the ports different from null;

	//For a vecttorial port a Vector<String[]> is required at least.

	//A complete Initialize could be 

	public void InitializeMapClosure(Vector<String[]> VectorValues, VectorParameter[] inputVectorParameter, VectorParameter[] outputVectorParameter, String[] Singlevalues, SingleParameter[] inputSingleParameter, SingleParameter[] outputSingleParameter, FunctionalParameter[] inputFunctionalParameters, FunctionalParameter[] outputFunctionalParameter)
	{

		//Vector input WARNING THE REFERENCE INPUTVECTORPARAMETER GIVES ONLY THE URI, NOT THE VALUES FIXME if needed

		VectorParameter p_v;
		for(int i = 0; i < AddVectorIntClosure.getInputVectorPortNames().length;i++)
		{   
			p_v = new VectorParameter();
			if((inputVectorParameter== null)||(inputVectorParameter[i]==null))//If I have  not a parameter in this constructor I create one random
			{


				p_v.setRandomURI();
				if((InputVectorPortTypes != null)&&(InputVectorPortTypes.length>0))
				{
					p_v.setType(InputVectorPortTypes[i]);//TypeCheck needed?
				}
				if (!(VectorValues.elementAt(i) == null)) //If I know the value of the parameter I  set the value and I put Valid == true
				{
					p_v.setContent(VectorValues.elementAt(i));
					p_v.setValid(true);
				}
				else  //The parameter has not a constant value to be assigned
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
				p_v.setURI(inputVectorParameter[i].getURI());
				Logger.printFile("I have given the the following URI to the input: " + p_v.getURI() +"\n", DebugFileName);
				if ((VectorValues!=null)&& (VectorValues.elementAt(i) != null)) //If I know the value of the parameter I  set the value and I put Valid == true
				{
					p_v.setContent(VectorValues.elementAt(i), AddVectorIntClosure.InputVectorPortTypes[i]);
					p_v.setValid(true);
				}
				else  //The parameter has not a constant value to be assigned
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

		/*
		 * FunctionalINPUT in current implementation only ready parameters can be passed, so not by reference
		 */

		this.FuncInputPorts[0].setSignal(inputFunctionalParameters[0]);
		this.FuncInputPorts[0].getSignal().setValid(true); //We consider in this phase that a functional parameter has alway associated a functional chain.

		//VectorOut

		VectorParameter pout = new VectorParameter();
		for(int i = 0; i < OutputVectorPortNames.length;i++)
		{
			if ((outputVectorParameter == null)||(outputVectorParameter[i] == null))//If I have not an output parameter from the constructor (so no reference) I create one Randomly
			{
				pout.setURI(UUID.randomUUID().toString());
				pout.setType(InputVectorPortTypes[i]);
				if(InputPortsVec[0].getSignal()!= null)
				{

					if(InputPortsVec[0].getSignal().getdimension()!= null)
					{
						pout.setdimension(InputPortsVec[0].getSignal().getdimension());
					}
					if(InputPortsVec[0].getSignal().getType()!= null)
					{
						pout.setType(InputVectorPortTypes[0]);
					}
				}

				pout.setValid(false);
			}
			else
			{
				pout = outputVectorParameter[i];
				if(InputPortsVec[0].getSignal()!= null)
				{

					if(InputPortsVec[0].getSignal().getdimension()!= null)
					{
						pout.setdimension(InputPortsVec[0].getSignal().getdimension());
					}
					if(InputPortsVec[0].getSignal().getType()!= null)
					{
						pout.setType(InputVectorPortTypes[0]);//It shoud be the functional input not the vectorial one FIXME
					}
				}

			}
			if((pout.getdimension()!= null) &&((  (pout.getContent() == null)  )||(  pout.getContent().size()!= pout.getdimension()) ) )
			{
				int dimension = pout.getdimension();
				for(int j = 0; j < dimension; j++)
				{
					SingleParameter p = new SingleParameter();
					pout.addSingleParameter(p);
				}
				pout.setdimension(dimension);
			}
			this.OutputPortsVec[i].setSignal(pout);
			pout.setWritingAtomicClosure(this);
		}

	}




	public void run()//The types are complicated in a general discussion. For this execution the type is not necessary so void is ok
	{
		/*
		 * The map runs in a way similar to a functional Chain, so it is a multithreaded process.
		 * 
		 * Various strategies possible, but it has been decided to apply one for the first implementation: writing the new functions to execute 
		 * in the SIB and then running the executing threads, this to simulate an environment with multiple execution units. 
		 */

		//Analysis of the input function to find the input

		//creation of the output(not necessary I suppose the output are present)

		//creation of the functional chains objects and writing them to the SIB

		//end, the rest should be done by the rest of the system in a recursive way.

		//First of all we define the output if they still are not

		//||(this.OutputPortsVec[0].getSignal().getdimension()!= this.InputPortsVec[0].getSignal().getdimension())

		if (this.OutputPortsVec[0] == null)
		{
			VectorParPort temp = new VectorParPort();
			temp.setSignal(new VectorParameter());
			this.OutputPortsVec[0] = temp;
		}

		if((this.OutputPortsVec[0].getSignal()== null) || (this.OutputPortsVec[0].getSignal().getdimension()!= this.InputPortsVec[0].getSignal().getdimension()))
		{
			VectorParameter vout = new VectorParameter();
			int dimension = this.getVectorialInputPorts()[0].getSignal().getdimension();
			for (int  i = 0; i < dimension; i++)
			{
				vout.addSingleParameter(new SingleParameter());
			}
			vout.setdimension(dimension);
		}

		//Chain reconstruction
		//Be careful if the real input of the map is detectable in the RDF through some additional property there is more flexibility
		//Be careful te only output type supported at moment is single parameter output


		/*
		 * Debug
		 */
		Logger.printFile("Initialization of the run method" +
				"Closure:" + this.getName()
				+ "\nURI:" + this.getURI()
				+ "\nInputPortURI" + this.InputPortsVec[0].getURI()
				+ "\nInputPortPArURI" + this.InputPortsVec[0].getSignal().getURI()
				+ "\nInputPortDimension" + this.InputPortsVec[0].getSignal().getdimension()
				+ "\nFunctionalInputPortPArURI" + this.FuncInputPorts[0].getSignal().getURI()
					+ "\nOutputPortURI" + this.OutputPortsVec[0].getURI()
					+ "\nOutputPortParameter" + this.OutputPortsVec[0].getSignal().getURI()
					+ "\nOutputInsideParameter" + this.OutputPortsVec[0].getSignal().getContent().elementAt(0).getURI()
					+ "\nOutputInsiderameter" + this.OutputPortsVec[0].getSignal().getContent().elementAt(1).getURI()
					+ "\nOutputInsiderameter" + this.OutputPortsVec[0].getSignal().getContent().elementAt(2).getURI()
				+ "\n", DebugFileName);
		//end debug
		Vector<String> functionsURI = new Vector<String>();
		Vector<String> functionNames = new Vector<String>();
		Vector<String> functionBehaviours = new Vector<String>();

		Vector<Vector<String>> functionsInputPorts = new Vector<Vector<String>>();//many ports for each function
		Vector<Vector<String>> functionsOutputPorts = new Vector<Vector<String>>();
		Vector<Vector<String[]>> InputParameters = new Vector<Vector<String[]>>();//Four fields for each parameter portDescriptor : portName, ParameterURI, ParameterValue, ParameterValid
		Vector<Vector<String[]>> OutputParameters = new Vector<Vector<String[]>>();//two fields for each parameter //portDescriptor : portName, ParameterURI
		String SIB_Host = SibConstants.SIB_Host;
		int SIB_Port = SibConstants.SIB_Port;
		String SIB_Name = SibConstants.SIB_Name;
		KPICore kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null,null,null);
		//kp.setEventHandler(this); This line should be decommented and the upcoming errors solved in the case of central management
		//Retrieve functions
		String xml=kp.join();
		boolean ack=xmlTools.isJoinConfirmed(xml);
		System.out.println("Join confirmed:"+(ack?"YES":"NO")+"\n");
		if(!ack)
		{
			System.out.println("Can not JOIN the SIB");
			//return ;
		}

		String xml_query_response = kp.queryRDF(this.FuncInputPorts[0].getSignal().getURI(), OntologyVocabulary.FunctionalParameterHasFunctionalChain, null, "URI", "URI");
		ack=xmlTools.isQueryConfirmed(xml_query_response);
		Logger.printFile(this.getName() + "->Query For functionalChain atached to parameter:"+(ack?"YES":"NO"), DebugFileName);
		Vector<Vector<String>> rdf_result = xmlTools.getQueryTriple(xml_query_response);
		for(int i = 0; i < rdf_result.size();i++)
		{

			Logger.printFile(
					"  S:["+rdf_result.elementAt(i).get(0)
					+"] P:["+rdf_result.elementAt(i).get(1)
					+"] O:["+rdf_result.elementAt(i).get(2)
					+"] Otype:["+rdf_result.elementAt(i).get(3)+"]\n", DebugFileName);
		}


		xml_query_response = kp.queryRDF(rdf_result.elementAt(0).get(2), OntologyVocabulary.FunctionalParameterChainHasFunction, null, "URI", "URI");
		ack=xmlTools.isQueryConfirmed(xml_query_response);
		Logger.printFile(this.getName() + "->Query For AtomicFunctions in Chain confirmed:"+(ack?"YES":"NO"), DebugFileName);
		rdf_result = xmlTools.getQueryTriple(xml_query_response);
		for(int i = 0; i < rdf_result.size();i++)
		{

			Logger.printFile(
					"  S:["+rdf_result.elementAt(i).get(0)
					+"] P:["+rdf_result.elementAt(i).get(1)
					+"] O:["+rdf_result.elementAt(i).get(2)
					+"] Otype:["+rdf_result.elementAt(i).get(3)+"]\n", DebugFileName);

			functionsURI.add(rdf_result.elementAt(i).get(2));
		}

		//retrieve function names

		//Function Names
		for(int i = 0; i < functionsURI.size();i++)//FIXME this for is redundant with the next one and can be eliminated. It is here because a choice has to be made (15-12-2010)
		{
			Vector<String> functionInputPorts = new Vector<String>();
			xml_query_response = kp.queryRDF(functionsURI.elementAt(i), OntologyVocabulary.ClosureHasName, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile(this.getName() + "->Query For Function Names, Results: \n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);


			}
			functionNames.add(rdf_result.elementAt(0).get(2));
		}

		//Function Behaviours
		for(int i = 0; i < functionsURI.size();i++)
		{
			Vector<String> functionInputPorts = new Vector<String>();
			xml_query_response = kp.queryRDF(functionsURI.elementAt(i), OntologyVocabulary.ComputationHasFunctionalBehaviour, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile(this.getName() + "->Query For Function Behaviours, Results: \n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);


			}
			functionBehaviours.add(rdf_result.elementAt(0).get(2));
		}

		//retrieve InputsPorts
		for(int i = 0; i < functionsURI.size();i++)
		{
			Vector<String> functionInputPorts = new Vector<String>();
			xml_query_response = kp.queryRDF(functionsURI.elementAt(i), OntologyVocabulary.ClosureHasInputPort, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile(this.getName() + "->Query For InputsPorts"+
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);

				functionInputPorts.add(rdf_result.elementAt(j).get(2));
			}
			functionsInputPorts.add(functionInputPorts);
		}

		//retrieve OutputPorts
		for(int i = 0; i < functionsURI.size();i++)
		{
			Vector<String> functionOutputPorts = new Vector<String>();
			xml_query_response = kp.queryRDF(functionsURI.elementAt(i), OntologyVocabulary.ClosureHasOutputPort, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile(this.getName() + "->Query for outputPorts\n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);

				functionOutputPorts.add(rdf_result.elementAt(j).get(2));
			}
			functionsOutputPorts.add(functionOutputPorts);
		}

		//Parameters at input ports
		//In the current implementation each I/O port has a parameter if the parameter has no value is also invalid and I call it an empty parameter
		for(int functionCounter = 0 ; functionCounter  < functionsURI.size();functionCounter++)
		{

			//portDescriptor : portName, ParameterURI, ParameterValue, ParameterValid, portType, portURI

			Vector<String[]> InputParametersForFunction = new Vector<String[]>();
			Vector<String> CurrentPorts = functionsInputPorts.elementAt(functionCounter);
			for (int portCounter = 0; portCounter < CurrentPorts.size(); portCounter++ )
			{
				String[] PortDescriptor = new String[6];
				xml_query_response = kp.queryRDF(functionsInputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.PortHasName, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for Input port Names Result: \n" +
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[0] = rdf_result.elementAt(0).get(2);

				xml_query_response = kp.queryRDF(functionsInputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.PortAttachedToPar, null, "URI", "URI");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for parameters attached to inputs ports, Results: \n"+
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[1] = rdf_result.elementAt(0).get(2);

				xml_query_response = kp.queryRDF(PortDescriptor[1], OntologyVocabulary.ParHasValue, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for Value of parameters at input ports, Results:\n"+
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				if(rdf_result.size() > 0)
				{
					PortDescriptor[2] = rdf_result.elementAt(0).get(2);
				}

				xml_query_response = kp.queryRDF(PortDescriptor[1], OntologyVocabulary.ParIsValid, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for validity of parameters at input ports, Results:\n"+
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[3] = rdf_result.elementAt(0).get(2);
				//PortType
				xml_query_response = kp.queryRDF(functionsInputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.type, null, "URI", "URI");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for Vector/singleParameters parameters at input ports, Results:\n"+
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[4] = rdf_result.elementAt(0).get(2);

				PortDescriptor[5] = functionsInputPorts.elementAt(functionCounter).elementAt(portCounter);


				InputParametersForFunction.add(PortDescriptor);
			}

			InputParameters.add(InputParametersForFunction);
		}
		for(int i = 0; i < InputParameters.size();i++)
		{
			Vector<String[]> Inpforfunct = InputParameters.elementAt(i);
			for(int j = 0; j < Inpforfunct.size();j++)
			{
				for (int z  =0 ;  z < 5; z++)
				{
					Logger.printFile(Inpforfunct.elementAt(j)[z] + "***InputPorts\n", DebugFileName);
				}
			}
		}
		//ParametersAtOutputPorts
		//I suppose here that the parameters in output are always without value when queried by a chainConstructor
		for(int functionCounter = 0 ; functionCounter  < functionsOutputPorts.size();functionCounter++)
		{

			//portDescriptor : portName, ParameterURI, portType, PortURI

			Vector<String[]> OutputParametersForFunction = new Vector<String[]>();
			Vector<String> CurrentPorts = functionsOutputPorts.elementAt(functionCounter);
			for (int portCounter = 0; portCounter < CurrentPorts.size(); portCounter++ )
			{
				String[] PortDescriptor = new String[4];
				xml_query_response = kp.queryRDF(functionsOutputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.PortHasName, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->query for names of output ports, Results:\n" +
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[0] = rdf_result.elementAt(0).get(2);

				xml_query_response = kp.queryRDF(functionsOutputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.PortAttachedToPar, null, "URI", "URI");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->query for parameters of output ports, Results:\n" +
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);
				}
				PortDescriptor[1] = rdf_result.elementAt(0).get(2);

				xml_query_response = kp.queryRDF(functionsOutputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.type, null, "URI", "URI");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->query for type of parameters of output ports, Results:\n" +
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);
				}
				PortDescriptor[2] = rdf_result.elementAt(0).get(2);

				PortDescriptor[3] = functionsOutputPorts.elementAt(functionCounter).elementAt(portCounter);

				System.out.println("port descriptor" + PortDescriptor[0]+ " ; " +  PortDescriptor[1]+ " ; " +  PortDescriptor[2]+ " ; " +  PortDescriptor[3]);
				OutputParametersForFunction.add(PortDescriptor);




			}

			OutputParameters.add(OutputParametersForFunction);
		}

		//following part is for debug
		for(int i = 0; i < OutputParameters.size();i++)
		{
			Vector<String[]> Outpforfunct = OutputParameters.elementAt(i);
			for(int j = 0; j < Outpforfunct.size();j++)
			{
				for (int z  =0 ;  z < 2; z++)
				{
					Logger.printFile(Outpforfunct.elementAt(j)[z] + "***OutputPorts\n", DebugFileName);
				}
			}
		}
		//end debug


		/*
		 * Chain transformation and replication
		 * Alternative implementation, threads are started directly
		 * Input:
		 * In the current implementation a map doesn't allow to specify its inputs in the RDF so the rela input is the missing value
		 * Output:
		 *  they should be knew form the calling chain executor so replication etc can be done properly:
		 * 
		 */



		int[] funcPortCombinationInput = new int[2];
		int[] funcPortCombinationOutput = new int[2];
		Vector<String> InputsUris = new Vector<String>();
		Vector<String> OutputsUris = new Vector<String>();

		for(int funcCounter = 0; funcCounter< InputParameters.size();funcCounter++)
		{
			for (int portCounter = 0;  portCounter < InputParameters.elementAt(funcCounter).size();  portCounter++ )
			{
				InputsUris.add(InputParameters.elementAt(funcCounter).elementAt(portCounter)[1]);
			}
		}

		for(int funcCounter = 0; funcCounter< OutputParameters.size();funcCounter++)
		{
			for (int portCounter = 0;  portCounter < OutputParameters.elementAt(funcCounter).size();  portCounter++ )
			{
				OutputsUris.add(OutputParameters.elementAt(funcCounter).elementAt(portCounter)[1]);
				if(!InputsUris.contains(OutputParameters.elementAt(funcCounter).elementAt(portCounter)[1])){
					funcPortCombinationOutput[0] = funcCounter;
					funcPortCombinationOutput[1] = portCounter;
					Logger.printFile("\nAddred a new func port combination function output so this is the outputof the chain: " +functionBehaviours.elementAt(funcCounter) + "for output:" + funcCounter + "_" + portCounter , DebugFileName);
				}
			}
		}
		for(int i = 0; i < OutputsUris.size();i++)
		{
			Logger.printFile("OutputURI: " + OutputsUris.elementAt(i) + "\n", DebugFileName);	
		}
		for(int funcCounter = 0; funcCounter< InputParameters.size();funcCounter++)
		{
			for (int portCounter = 0;  portCounter < InputParameters.elementAt(funcCounter).size();  portCounter++ )
			{
				Logger.printFile("To compare invalid :" +
						InputParameters.elementAt(funcCounter).elementAt(portCounter)[3]
						                                                              + "\nOutputURI with:" +InputParameters.elementAt(funcCounter).elementAt(portCounter)[1] + "\n", DebugFileName);
				if((InputParameters.elementAt(funcCounter).elementAt(portCounter)[3].equals(OntologyVocabulary.InvalidParameter))&&(!OutputsUris.contains(InputParameters.elementAt(funcCounter).elementAt(portCounter)[1])))
				{
					funcPortCombinationInput[0] = funcCounter;
					funcPortCombinationInput[1] = portCounter;
					Logger.printFile("\nAddred a new func port combination for input:" + funcCounter + "_" + portCounter +"function = " + functionBehaviours.elementAt(funcCounter), DebugFileName);
				}
			}
		}

		//		String portName = InputParameters.elementAt(funcPortCombinationInput[0]).elementAt(funcPortCombinationInput[1])[0];
		//		Vector<String[]> missingInputsDescriptors = constructMissingInputsDescriptors(portName);
		
		Logger.printFile("Printing map in execution phase before queries\n", DebugFileName);
	for(int i = 0; i < InputParameters.size();i++)
	{
		for (int j = 0; j < InputParameters.elementAt(i).size();j++)
		{
			for(int z = 0; z < InputParameters.elementAt(i).elementAt(j).length;z++)
			Logger.printFile("InputDescriptor: function num " + i + "\n" +
					"portn num  " + j + "\n parameter " + z +" = " + InputParameters.elementAt(i).elementAt(j)[z] +"\n", DebugFileName);
		}
	}
	
	for(int i = 0; i < OutputParameters.size();i++)
	{
		for (int j = 0; j < OutputParameters.elementAt(i).size();j++)
		{
			for(int z = 0; z < OutputParameters.elementAt(i).elementAt(j).length;z++)
			Logger.printFile("OutputDescriptor: function num " + i + "\n" +
					"portn num  " + j + "\n parameter " + z +" = " + OutputParameters.elementAt(i).elementAt(j)[z] +"\n", DebugFileName);
		}
	}
	
		String[] inputs;
		String[] inputsRef;
		String[] outputsRef;
		/*
		 * Now I initialize the inputs and the outputs descriptors with the data I have from the query and  I construct a functional chain, then I replicate i appropriately and, in end I send all of them to the SIB
		 * the inputs and the outputs rom the descriptors may be no in the correct order and so I need to use the names to order them 
		 * I need a static function that from the name of the closure and the name of the port gives me the index of the port so I can assigna
		 * if(inputparameter.elementat(func_counter).elementAt(portCounter)[2].equals )// valid
		 * then 
		 * inputs[map(functionname, inputparameter.elementat(func_counter).elementAt(portCounter)[0])] = value {inputparameter.elementat(func_counter).elementAt(portCounter)[3]}
		 * else
		 * connectionTable.put(newURI, someStringIrecognize)
		 * I consider only single parametershere FIXME
		 * 
		 */

		FunctionalChain[] fcs = new FunctionalChain[InputPortsVec[0].getSignal().getdimension()];


		/*
		 * Here I have to reconstruct closure connections through the analysis of the URI of the parameters
		 * No vectorial functions currently allowed as map functional input
		 */
		//		Vector<String> URIs = new Vector<String>();
		//		Vector<String> cicleindexes = new Vector<String>();
		//		Hashtable<String, String> originalConnections = new Hashtable<String, String>();
		//
		//		String parID ="";
		//		for(int functioncounter = 0; functioncounter < OutputParameters.size();functioncounter++)
		//		{
		//			for(int  outPortCounter = 0;  outPortCounter < OutputParameters.elementAt(functioncounter).size(); outPortCounter++)
		//			{
		//				String  outURI = OutputParameters.elementAt(functioncounter).elementAt(outPortCounter)[1];
		//				if(!URIs.contains(outURI))
		//				{
		//					cicleindexes.add("o_" + functioncounter + "_" + outPortCounter);
		//					URIs.add(outURI);
		//				}
		//				else
		//				{
		//					originalConnections.put("o_" + functioncounter + "_" + outPortCounter, cicleindexes.elementAt(URIs.indexOf(outURI)));
		//				}
		//			}
		//
		//
		//			for(int  inPortCounter = 0;  inPortCounter < InputParameters.elementAt(functioncounter).size(); inPortCounter++)
		//			{
		//				String  inURI = InputParameters.elementAt(functioncounter).elementAt(inPortCounter)[1];
		//				if(!URIs.contains(inURI))
		//				{
		//					cicleindexes.add("i_" + functioncounter + "_" + inPortCounter);
		//					URIs.add(inURI);
		//				}
		//				else
		//				{
		//					originalConnections.put("i_" + functioncounter + "_" + inPortCounter, cicleindexes.elementAt(URIs.indexOf(inURI)));
		//				}
		//			}
		//		}
		String parUUID="";
		
		//Construction of the static connection table
		Hashtable<String, String> staticConnectionTable = new Hashtable<String, String>();
		Hashtable<String, String> reverseConnectionTable = new Hashtable<String, String>();
		Vector<String> Connections = new Vector<String>();
		
			for( int func_counter = 0; func_counter< functionsURI.size();func_counter++)
			{
				for(int outPortCounter=0; outPortCounter< OutputParameters.elementAt(func_counter).size(); outPortCounter++)
				{
					parUUID = OutputParameters.elementAt(func_counter).elementAt(outPortCounter)[1];
					if(!staticConnectionTable.containsValue(parUUID))
					{
						staticConnectionTable.put("p" + func_counter + "_o" + outPortCounter, parUUID);
						reverseConnectionTable.put(parUUID, "p" + func_counter + "_o" + outPortCounter);
							}
					else
					{
						Connections.add(parUUID);
					}
				}
				for(int inPortCounter=0; inPortCounter< InputParameters.elementAt(func_counter).size(); inPortCounter++)
				{
					parUUID =InputParameters.elementAt(func_counter).elementAt(inPortCounter)[1];
				
					if(!staticConnectionTable.containsValue(parUUID))
					{
						staticConnectionTable.put("p" + func_counter +  "_i" + inPortCounter,parUUID);
						reverseConnectionTable.put(parUUID, "p" + func_counter + "_i" + inPortCounter);
						
					}
					else
					{
						Connections.add(parUUID);
					}

				}
			}
		
		
		for(int inputCounter  = 0; inputCounter < this.InputPortsVec[0].getSignal().getdimension(); inputCounter++)
		{
			FunctionalChain fc_i = new FunctionalChain();

			
			for( int func_counter = 0; func_counter< functionsURI.size();func_counter++)
			{

				inputs= new String[InputParameters.elementAt(func_counter).size()];
				inputsRef= new String[InputParameters.elementAt(func_counter).size()];

				//		SingleParameter par = new SingleParameter();
				outputsRef = new String[OutputParameters.elementAt(func_counter).size()];
				/*
				 * for the output I have only to set a new or a kenw URI and take in consideration the special index for output
				 */
				for(int outPortCounter=0; outPortCounter< OutputParameters.elementAt(func_counter).size(); outPortCounter++)
				{
					if((func_counter == funcPortCombinationOutput[0] )&&(outPortCounter == funcPortCombinationOutput[1]))
					{
						parUUID = OutputPortsVec[0].getSignal().getContent().elementAt(inputCounter).getURI();
						Logger.printFile("\nFound chain output, index = " + func_counter + "_"+ outPortCounter + "; URI = " + parUUID, DebugFileName);
						fc_i.connectionTable.put("p" + func_counter + "_o" + outPortCounter, parUUID);
						reverseConnectionTable.put(parUUID, "p" + func_counter + "_o" + outPortCounter);
						outputsRef[ClosureUtilities.mapOutputPortNameToPortIndex(functionNames.elementAt(func_counter), OutputParameters.elementAt(func_counter).elementAt(outPortCounter)[0])] = reverseConnectionTable.get(parUUID);
					}
					else
					{
						Logger.printFile("\nNot chain outputfor index = " + func_counter + "_"+ outPortCounter , DebugFileName);
						
						if(!Connections.contains(OutputParameters.elementAt(func_counter).elementAt(outPortCounter)[1]))
						{
							parUUID = UUID.randomUUID().toString();
Logger.printFile("\new Output , index = " + func_counter + "_"+ outPortCounter + "; URI = " + parUUID, DebugFileName);
							fc_i.connectionTable.put("p" + func_counter + "_o" + outPortCounter, parUUID);
							outputsRef[ClosureUtilities.mapOutputPortNameToPortIndex(functionNames.elementAt(func_counter), OutputParameters.elementAt(func_counter).elementAt(outPortCounter)[0])] = "p" + func_counter + "_o" + outPortCounter;
						}
						else//This should never happen
						{
							outputsRef[ClosureUtilities.mapOutputPortNameToPortIndex(functionNames.elementAt(func_counter), OutputParameters.elementAt(func_counter).elementAt(outPortCounter)[0])] = reverseConnectionTable.get(OutputParameters.elementAt(func_counter).elementAt(outPortCounter)[1]);					
							Logger.printFile("\nFound output just in table, index = " + func_counter + "_"+ outPortCounter + "; URI = " + reverseConnectionTable.get(OutputParameters.elementAt(func_counter).elementAt(outPortCounter)[1]), DebugFileName);					
						}
					}
				}//End for (outputPortConter)

				/*
				 * For the input I have to
				 *  set URI, 
				 *  value
				 *   and take in consideration the special value for the input 
				 */
				for(int inPortCounter=0; inPortCounter< InputParameters.elementAt(func_counter).size(); inPortCounter++)
				{
					if((func_counter == funcPortCombinationInput[0] )&&(inPortCounter == funcPortCombinationInput[1]))
					{
						parUUID = InputPortsVec[0].getSignal().getContent().elementAt(inputCounter).getURI();
						fc_i.connectionTable.put("p" + func_counter +  "_i" + inPortCounter,parUUID);
						reverseConnectionTable.put(parUUID, "p" + func_counter +  "_i" + inPortCounter);
						inputs[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] = InputPortsVec[0].getSignal().getContent().elementAt(inputCounter).getValueForTriple();
						inputsRef[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] = reverseConnectionTable.get(parUUID);
						Logger.printFile("Assigning new Input to the whole chain  index = " + func_counter + "_"+ inPortCounter
								 + "URI= " + parUUID
								+ "Value=" + InputPortsVec[0].getSignal().getContent().elementAt(inputCounter).getValueForTriple() , DebugFileName);


					}
					else
					{
						if(!Connections.contains(InputParameters.elementAt(func_counter).elementAt(inPortCounter)[1]))
						{

							parUUID = UUID.randomUUID().toString();
							Logger.printFile("New Random input for port index = " + func_counter + "_"+ inPortCounter
									// + "URI= " + parUUID
									+ "Value=" + InputParameters.elementAt(func_counter).elementAt(inPortCounter)[2] , DebugFileName);

							fc_i.connectionTable.put("p" + func_counter +  "_i" + inPortCounter,parUUID);
							if(InputParameters.elementAt(func_counter).elementAt(inPortCounter)[3].equals(OntologyVocabulary.ValidParameter) )// valid
							{
								inputs[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] = InputParameters.elementAt(func_counter).elementAt(inPortCounter)[2];
								inputsRef[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] = "p" + func_counter +  "_i" + inPortCounter;
								Logger.printFile("Value present!",DebugFileName);
								
							}
							else
							{
								inputs[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] =null;
								inputsRef[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] = "p" + func_counter +  "_i" + inPortCounter;
								Logger.printFile("Value not present!",DebugFileName);
							
							}
						}
						else
						{
							parUUID = fc_i.connectionTable.get(reverseConnectionTable.get(InputParameters.elementAt(func_counter).elementAt(inPortCounter)[1]));
							Logger.printFile("Assigning just used input for  index = " + func_counter + "_"+ inPortCounter
									 + "URI= " + parUUID,DebugFileName);
								//	+ "Value=" + InputPortsVec[0].getSignal().getContent().elementAt(inputCounter).getValueForTriple() , DebugFileName);

							if(InputParameters.elementAt(func_counter).elementAt(inPortCounter)[3].equals(OntologyVocabulary.ValidParameter) )// valid
							{
								inputs[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] = InputParameters.elementAt(func_counter).elementAt(inPortCounter)[2];
								inputsRef[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] = reverseConnectionTable.get(InputParameters.elementAt(func_counter).elementAt(inPortCounter)[1]);
							}
							else
							{
								inputs[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] =null;
								inputsRef[ClosureUtilities.mapInputPortNameToPortIndex(functionNames.elementAt(func_counter), InputParameters.elementAt(func_counter).elementAt(inPortCounter)[0])] = reverseConnectionTable.get(InputParameters.elementAt(func_counter).elementAt(inPortCounter)[1]);
							}
						}
					}




				}//End for (inputPortConter)
				for(int i = 0; i < fc_i.connectionTable.size();i++)
				{
					Logger.printFile("fc_" + inputCounter+ " ConnectionTable: " + fc_i.connectionTable.keySet().toArray()[i] + " --> " + fc_i.connectionTable.values().toArray()[i] + "\n", DebugFileName);
					
				}
				fc_i.addFunctionCall(functionNames.elementAt(func_counter), inputs, inputsRef, outputsRef);
			}//enf for(functioncounter)

			fcs[inputCounter] = fc_i;
		}//end for inputCounter 

		for(int i = 0; i < fcs.length;i++)
		{

			FunctionalChain chain = fcs[i];
			//chain.kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
			//xmlTools = new SSAP_XMLTools(null,null,null);
			//chain.kp.setEventHandler(chain);

			xml=kp.leave();
			xml=kp.join();
			ack=xmlTools.isJoinConfirmed(xml);
			System.out.println("Join confirmed:"+(ack?"YES":"NO")+"\n");
			if(!ack)
			{
				System.out.println("Can not JOIN the SIB");
				//return ;
			}


			Vector<Vector<String>> triples= new Vector<Vector<String>>(); 




			//Here code for subscription to results

			//End subscription to results, now insert the chain on the SIB

			triples= chain.serializeTriplesAsExecutable();
			xml= kp.insert(triples);

			ack=xmlTools.isInsertConfirmed(xml);
		}



	}


	public void run1()//The types are complicated in a general discussion. For this execution the type is not necessary so void is ok
	{
		/*
		 * The map runs in a way similar to a functional Chain, so it is a multithreaded process.
		 * 
		 * Various strategies possible, but it has been decided to apply one for the first implementation: writing the new functions to execute 
		 * in the SIB and then running the executing threads, this to simulate an environment with multiple execution units. 
		 */

		//Analysis of the input function to find the input

		//creation of the output(not necessary I suppose the output are present)

		//creation of the functional chains objects and writing them to the SIB

		//end, the rest should be done by the rest of the system in a recursive way.

		//First of all we define the output if they still are not

		//||(this.OutputPortsVec[0].getSignal().getdimension()!= this.InputPortsVec[0].getSignal().getdimension())
		if (this.OutputPortsVec[0] == null)
		{
			VectorParPort temp = new VectorParPort();
			temp.setSignal(new VectorParameter());
			this.OutputPortsVec[0] = temp;
		}

		if((this.OutputPortsVec[0].getSignal()== null) || (this.OutputPortsVec[0].getSignal().getdimension()!= this.InputPortsVec[0].getSignal().getdimension()))
		{
			VectorParameter vout = new VectorParameter();
			int dimension = this.getVectorialInputPorts()[0].getSignal().getdimension();
			for (int  i = 0; i < dimension; i++)
			{
				vout.addSingleParameter(new SingleParameter());
			}
			vout.setdimension(dimension);
		}

		//Chain reconstruction
		//Be careful if the real input of the map is detectable in the RDF through some additional property there is more flexibility
		//Be careful te only output type supported at moment is single parameter output

		Vector<String> functionsURI = new Vector<String>();
		Vector<String> functionNames = new Vector<String>();
		Vector<String> functionBehaviours = new Vector<String>();

		Vector<Vector<String>> functionsInputPorts = new Vector<Vector<String>>();//many ports for each function
		Vector<Vector<String>> functionsOutputPorts = new Vector<Vector<String>>();
		Vector<Vector<String[]>> InputParameters = new Vector<Vector<String[]>>();//Four fields for each parameter portDescriptor : portName, ParameterURI, ParameterValue, ParameterValid
		Vector<Vector<String[]>> OutputParameters = new Vector<Vector<String[]>>();//two fields for each parameter //portDescriptor : portName, ParameterURI
		String SIB_Host = SibConstants.SIB_Host;
		int SIB_Port = SibConstants.SIB_Port;
		String SIB_Name = SibConstants.SIB_Name;
		KPICore kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null,null,null);
		//kp.setEventHandler(this); This line should be decommented and the upcoming errors solved in the case of central management
		//Retrieve functions
		String xml_query_response = kp.queryRDF(this.FuncInputPorts[0].getSignal().getURI(), OntologyVocabulary.ChainHasFunction, null, "URI", "URI");
		boolean ack=xmlTools.isQueryConfirmed(xml_query_response);
		Logger.printFile(this.getName() + "->Query For AtomicFunctions in Chain confirmed:"+(ack?"YES":"NO"), DebugFileName);
		Vector<Vector<String>> rdf_result = xmlTools.getQueryTriple(xml_query_response);
		for(int i = 0; i < rdf_result.size();i++)
		{

			Logger.printFile(
					"  S:["+rdf_result.elementAt(i).get(0)
					+"] P:["+rdf_result.elementAt(i).get(1)
					+"] O:["+rdf_result.elementAt(i).get(2)
					+"] Otype:["+rdf_result.elementAt(i).get(3)+"]\n", DebugFileName);

			functionsURI.add(rdf_result.elementAt(i).get(2));
		}

		//retrieve function names

		//Function Names
		for(int i = 0; i < functionsURI.size();i++)//FIXME this for is redundant with the next one and can be eliminated. It is here because a choice has to be made (15-12-2010)
		{
			Vector<String> functionInputPorts = new Vector<String>();
			xml_query_response = kp.queryRDF(functionsURI.elementAt(i), OntologyVocabulary.ClosureHasName, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile(this.getName() + "->Query For Function Names, Results: \n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);


			}
			functionNames.add(rdf_result.elementAt(0).get(2));
		}

		//Function Behaviours
		for(int i = 0; i < functionsURI.size();i++)
		{
			Vector<String> functionInputPorts = new Vector<String>();
			xml_query_response = kp.queryRDF(functionsURI.elementAt(i), OntologyVocabulary.ComputationHasFunctionalBehaviour, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile(this.getName() + "->Query For Function Behaviours, Results: \n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);


			}
			functionBehaviours.add(rdf_result.elementAt(0).get(2));
		}

		//retrieve InputsPorts
		for(int i = 0; i < functionsURI.size();i++)
		{
			Vector<String> functionInputPorts = new Vector<String>();
			xml_query_response = kp.queryRDF(functionsURI.elementAt(i), OntologyVocabulary.ClosureHasInputPort, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile(this.getName() + "->Query For InputsPorts"+
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);

				functionInputPorts.add(rdf_result.elementAt(j).get(2));
			}
			functionsInputPorts.add(functionInputPorts);
		}

		//retrieve OutputPorts
		for(int i = 0; i < functionsURI.size();i++)
		{
			Vector<String> functionOutputPorts = new Vector<String>();
			xml_query_response = kp.queryRDF(functionsURI.elementAt(i), OntologyVocabulary.ClosureHasOutputPort, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile(this.getName() + "->Query for outputPorts\n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);

				functionOutputPorts.add(rdf_result.elementAt(j).get(2));
			}
			functionsOutputPorts.add(functionOutputPorts);
		}

		//Parameters at input ports
		//In the current implementation each I/O port has a parameter if the parameter has no value is also invalid and I call it an empty parameter
		for(int functionCounter = 0 ; functionCounter  < functionsURI.size();functionCounter++)
		{

			//portDescriptor : portName, ParameterURI, ParameterValue, ParameterValid, portType, portURI

			Vector<String[]> InputParametersForFunction = new Vector<String[]>();
			Vector<String> CurrentPorts = functionsInputPorts.elementAt(functionCounter);
			for (int portCounter = 0; portCounter < CurrentPorts.size(); portCounter++ )
			{
				String[] PortDescriptor = new String[6];
				xml_query_response = kp.queryRDF(functionsInputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.PortHasName, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for Input port Names Result: \n" +
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[0] = rdf_result.elementAt(0).get(2);

				xml_query_response = kp.queryRDF(functionsInputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.PortAttachedToPar, null, "URI", "URI");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for parameters attached to inputs ports, Results: \n"+
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[1] = rdf_result.elementAt(0).get(2);

				xml_query_response = kp.queryRDF(PortDescriptor[1], OntologyVocabulary.ParHasValue, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for Value of parameters at input ports, Results:\n"+
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				if(rdf_result.size() > 0)
				{
					PortDescriptor[2] = rdf_result.elementAt(0).get(2);
				}

				xml_query_response = kp.queryRDF(PortDescriptor[1], OntologyVocabulary.ParIsValid, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for validity of parameters at input ports, Results:\n"+
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[3] = rdf_result.elementAt(0).get(2);
				//PortType
				xml_query_response = kp.queryRDF(functionsInputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.type, null, "URI", "URI");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->Query for Vector/singleParameters parameters at input ports, Results:\n"+
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[4] = rdf_result.elementAt(0).get(2);

				PortDescriptor[5] = functionsInputPorts.elementAt(functionCounter).elementAt(portCounter);


				InputParametersForFunction.add(PortDescriptor);
			}

			InputParameters.add(InputParametersForFunction);
		}
		for(int i = 0; i < InputParameters.size();i++)
		{
			Vector<String[]> Inpforfunct = InputParameters.elementAt(i);
			for(int j = 0; j < Inpforfunct.size();j++)
			{
				for (int z  =0 ;  z < 5; z++)
				{
					Logger.printFile(Inpforfunct.elementAt(j)[z] + "***InputPorts\n", DebugFileName);
				}
			}
		}
		//ParametersAtOutputPorts
		//I suppose here that the parameters in output are always without value when queried by a chainConstructor
		for(int functionCounter = 0 ; functionCounter  < functionsOutputPorts.size();functionCounter++)
		{

			//portDescriptor : portName, ParameterURI, portType, PortURI

			Vector<String[]> OutputParametersForFunction = new Vector<String[]>();
			Vector<String> CurrentPorts = functionsOutputPorts.elementAt(functionCounter);
			for (int portCounter = 0; portCounter < CurrentPorts.size(); portCounter++ )
			{
				String[] PortDescriptor = new String[4];
				xml_query_response = kp.queryRDF(functionsOutputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.PortHasName, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->query for names of output ports, Results:\n" +
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
				}
				PortDescriptor[0] = rdf_result.elementAt(0).get(2);

				xml_query_response = kp.queryRDF(functionsOutputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.PortAttachedToPar, null, "URI", "URI");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->query for parameters of output ports, Results:\n" +
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);
				}
				PortDescriptor[1] = rdf_result.elementAt(0).get(2);

				xml_query_response = kp.queryRDF(functionsOutputPorts.elementAt(functionCounter).elementAt(portCounter), OntologyVocabulary.type, null, "URI", "URI");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result = xmlTools.getQueryTriple(xml_query_response);
				for(int j = 0; j < rdf_result.size();j++)
				{

					Logger.printFile(this.getName() + "->query for type of parameters of output ports, Results:\n" +
							"  S:["+rdf_result.elementAt(j).get(0)
							+"] P:["+rdf_result.elementAt(j).get(1)
							+"] O:["+rdf_result.elementAt(j).get(2)
							+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n",DebugFileName);
				}
				PortDescriptor[2] = rdf_result.elementAt(0).get(2);

				PortDescriptor[3] = functionsOutputPorts.elementAt(functionCounter).elementAt(portCounter);

				System.out.println("port descriptor" + PortDescriptor[0]+ " ; " +  PortDescriptor[1]+ " ; " +  PortDescriptor[2]+ " ; " +  PortDescriptor[3]);
				OutputParametersForFunction.add(PortDescriptor);




			}

			OutputParameters.add(OutputParametersForFunction);
		}

		//following part is for debug
		for(int i = 0; i < OutputParameters.size();i++)
		{
			Vector<String[]> Outpforfunct = OutputParameters.elementAt(i);
			for(int j = 0; j < Outpforfunct.size();j++)
			{
				for (int z  =0 ;  z < 2; z++)
				{
					Logger.printFile(Outpforfunct.elementAt(j)[z] + "***OutputPorts\n", DebugFileName);
				}
			}
		}
		//end debug


		/*
		 * Chain transformation and treplication
		 * Alternative implementation, threads are started directly
		 * Input:
		 * In the current implementation a map doesn't allow to specify its inputs in the RDF so the rela input is the missing value
		 * Output:
		 *  they should be knew form the calling chain executor so replication etc can be done properly:
		 * 
		 */
		int[] funcPortCombination = new int[2];


		for(int funcCounter = 0; funcCounter< InputParameters.size();funcCounter++)
		{
			for (int portCounter = 0;  portCounter < InputParameters.elementAt(funcCounter).size();  portCounter++ )
			{
				if(InputParameters.elementAt(funcCounter).elementAt(portCounter)[3].equals(OntologyVocabulary.InvalidParameter))
				{
					funcPortCombination[0] = funcCounter;
					funcPortCombination[1] = portCounter;
				}
			}
		}
		String portName = InputParameters.elementAt(funcPortCombination[0]).elementAt(funcPortCombination[1])[0];
		Vector<String[]> missingInputsDescriptors = constructMissingInputsDescriptors(portName);

		for(int inputCounter  = 0; inputCounter < this.InputPortsVec[0].getSignal().getdimension(); inputCounter++)
		{
			for( int func_counter = 0; func_counter< functionsURI.size();func_counter++)
			{
				FunctionExecutor f = new FunctionExecutor();

				//f.setFunctionName(this.functionNames.elementAt(func_Counter));  //some optimization possible, at moment name and behaviours are the same, perhaps one will be eliminate later in the project
				f.setFunctionName(functionBehaviours.elementAt(func_counter));

				//Inputs
				Vector<String[]> FunctionInputParameterDescriptors = InputParameters.elementAt(func_counter);
				if(func_counter== funcPortCombination[0])
				{
					FunctionInputParameterDescriptors.remove(funcPortCombination[1]);
					FunctionInputParameterDescriptors.add( funcPortCombination[1], missingInputsDescriptors.elementAt(inputCounter));			
				}



				Logger.printFile("Functions->URI: " + functionsURI.elementAt(func_counter) + " name " + functionBehaviours.elementAt(func_counter) + "\n", DebugFileName);
				for(int  portCounter  = 0; portCounter < FunctionInputParameterDescriptors.size(); portCounter++)
				{
					String[] portDescriptor = FunctionInputParameterDescriptors.elementAt(portCounter);

					Logger.printFile("Chain Executor -> INPUT PORT DESCRIPTOR: " + portDescriptor[0] + " ; " +portDescriptor[1] + " ; " +portDescriptor[2] + " ; " +portDescriptor[3] +"\n",DebugFileName);
					//				if(portDescriptor[2] != null && portDescriptor[3] == OntologyVocabulary.ValidParameter)//I have a valid parameter

					if(portDescriptor[4].equals(OntologyVocabulary.VectorParPort))//I'm considering a vectorial port
					{
						if(portDescriptor[3].equals(OntologyVocabulary.ValidParameter))//I have a valid vectorial parameter so only the URI of the vectorial parameter seems to be relevant for the executor 
						{
							String[] inputVectorPortDescriptorForExecutor = new String[2];
							inputVectorPortDescriptorForExecutor[0] = portDescriptor[0];
							inputVectorPortDescriptorForExecutor[1] = portDescriptor[1];
							f.addValidVectorialInputPort(inputVectorPortDescriptorForExecutor);
						}
						else//I add an Item to the waitinglist of the vectorial parameters: are needed PortName and Parameter URI
						{
							String[] WaitingInputDescriptortForExecutor = new String[2];
							WaitingInputDescriptortForExecutor[0] = portDescriptor[0];
							WaitingInputDescriptortForExecutor[1] = portDescriptor[1];
							f.addToVectorialWaitingList(WaitingInputDescriptortForExecutor);
						}
					}
					//				else if(portDescriptor[4].equals(OntologyVocabulary.functionalParPort))//I'm considering a functional port
					//				{
					//					if(portDescriptor[3].equals(OntologyVocabulary.ValidParameter))//I have a valid functional parameter so only the URI of the functional parameter seems to be relevant for the executor 
					//					{
					//						String[] inputFunctionalPortDescriptorForExecutor = new String[2];
					//						inputFunctionalPortDescriptorForExecutor[0] = portDescriptor[0];
					//						inputFunctionalPortDescriptorForExecutor[1] = portDescriptor[1];
					//						f.addValidfunctionalInputPort(inputFunctionalPortDescriptorForExecutor);
					//					}
					//					else//I add an Item to the waitinglist of the vectorial parameters: are needed PortName and Parameter URI
					//					{
					//						String[] WaitingInputDescriptortForExecutor = new String[2];
					//						WaitingInputDescriptortForExecutor[0] = portDescriptor[0];
					//						WaitingInputDescriptortForExecutor[1] = portDescriptor[1];
					//						f.addTofunctionalWaitingList(WaitingInputDescriptortForExecutor);//Not implemented yet
					//					}
					//				}
					else //I�m considering a port connected to a single parameter
					{
						if(portDescriptor[3].equals(OntologyVocabulary.ValidParameter))//I have a valid parameter
						{
							String[] inputPortDescriptortForExecutor = new String[2];
							inputPortDescriptortForExecutor[0] = portDescriptor[0];
							inputPortDescriptortForExecutor[1] = portDescriptor[2];
							f.addInputValue(inputPortDescriptortForExecutor);
						}
						else//I add an Item to the waiting list of the single parameters
						{
							String[] WaitingInputDescriptortForExecutor = new String[2];
							WaitingInputDescriptortForExecutor[0] = portDescriptor[0];
							WaitingInputDescriptortForExecutor[1] = portDescriptor[1];
							f.addToWaitingList(WaitingInputDescriptortForExecutor);
						}
					}
				}

				//Outputs
				//there is only one single output of chain (choice for current implementation)so it is the ponly output to which does not correspond an input
				Vector<String> inputsURI = new Vector<String>();
				int[] targetOutputFunctionPortCombination = new int[2];
				for(int  i = 0; i  < InputParameters.size();i++)
				{
					for(int j = 0;  j < InputParameters.elementAt(i).size();j++)
					{
						inputsURI.add(InputParameters.elementAt(i).elementAt(j)[1]);
					}
				}

				for (int  i = 0; i < OutputParameters.size();i++)
				{
					for(int j = 0;  j < OutputParameters.elementAt(i).size();j++)
					{
						if (inputsURI.contains(OutputParameters.elementAt(i).elementAt(j)[1]))
						{
							targetOutputFunctionPortCombination[0] = i;
							targetOutputFunctionPortCombination[1] = j;
						}
					}
				}

				Vector<String[]> FunctionOutputParameterDescriptors = OutputParameters.elementAt(func_counter);
				if (func_counter== targetOutputFunctionPortCombination[0])
				{
					FunctionOutputParameterDescriptors.elementAt(targetOutputFunctionPortCombination[1])[1] = OutputPortsVec[0].getSignal().getContent().elementAt(inputCounter).getURI();
				}
				for(int  portCounter  = 0; portCounter < FunctionOutputParameterDescriptors.size(); portCounter++)
				{
					String[] portDescriptor = FunctionOutputParameterDescriptors.elementAt(portCounter);
					if(portDescriptor[2].equals(OntologyVocabulary.VectorParPort))
					{
						f.setVectorialOutputReference(portDescriptor);
					}
					else if(portDescriptor[2].equals(OntologyVocabulary.SingleParPort))
					{
						f.setOutputReference(portDescriptor);
					}
				}


				new Thread(f).start();


			}

		}

		//Writeback


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
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ClosureHasName,OntologyVocabulary.MapClosure, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ComputationHasFunctionalBehaviour,OntologyVocabulary.MapClosure, "URI", "URI"));

		for(int i = 0; i < MapClosure.getInputVectorPortNames().length;i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasInputPort, this.InputPortsVec[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.InputPortsVec[i].getTriples());
		}
		for(int i = 0; i < InputFunctionalPortNames.length;i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasInputPort, this.FuncInputPorts[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.FuncInputPorts[i].getTriples());
		}
		for(int i = 0; i < OutputPortsVec.length;i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasOutputPort, this.OutputPortsVec[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.OutputPortsVec[i].getTriples());
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
				//				System.out.println("Closure not correctly initialized: the parameter on port " + this.getOutputSinglePortNames()[i] + " = null!");
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
		temp.setType(InputVectorPortTypes[TargetPortIndex]);
		this.InputPortsVec[TargetPortIndex] = temp;
		Logger.printFile("Updated a new input port portName =" + VectorInputPortDescriptor[0] +"index =" + TargetPortIndex, DebugFileName);
		for (int i = 0; i < VectorInputPortDescriptor.length;i++)
		{
			Logger.printFile("\ninputDescrptor par num " + i + " = " + VectorInputPortDescriptor[i], DebugFileName);
		}
			

	}

	public Integer getInputVectorPortIndexFromName(String portName)
	{
		for(Integer i = 0; i < MapClosure.InputVectorPortNames.length;i++)
		{
			if (MapClosure.InputVectorPortNames[i].equals(portName))
			{
				return i;
			}
		}
		return null;
	}

	public Vector<String[]> constructMissingInputsDescriptors(String portName)
	{
		Vector<String[]> temp = new Vector<String[]>();
		for (int  i = 0; i < this.InputPortsVec[0].getSignal().getdimension(); i++)
		{
			String[] desc = new String[4];
			desc[0] = portName;
			desc[1] = InputPortsVec[0].getSignal().getContent().elementAt(i).getURI();
			desc[2] = InputPortsVec[0].getSignal().getContent().elementAt(i).getValueForTriple();
			Boolean b = InputPortsVec[0].getSignal().getContent().elementAt(i).getValid();
			if(b)
			{
				desc[3] = OntologyVocabulary.ValidParameter;
			}
			else
			{
				desc[3] = OntologyVocabulary.InvalidParameter;
			}
			temp.add(desc);			                           
		}
		return temp;

	}


	//	public void setOutputPortReference(String[] OutputPortDescriptor) //OutputPortDescriptor = [PortName, ParameterURI]
	//	{
	//		int TargetPortIndex = getOutputPortIndexFromName(OutputPortDescriptor[0]);
	//		this.OutputPorts[TargetPortIndex].getSignal().setURI(OutputPortDescriptor[1]);
	//	}


	//	public Integer getOutputPortIndexFromName(String portName)
	//	{
	//		for(Integer i = 0; i < this.OutputPorts.length;i++)
	//		{
	//			if (MapClosure.OutputSinglePortNames[i].equals(portName))
	//			{
	//				return i;
	//			}
	//		}
	//		return null;
	//	}
	@Override
	public SingleParPort[] getInputPorts() {
		return this.InputPorts;
	}
	public void setVectorOutputPortReference(Vector<String[]> VectorOutputPortDescriptor)
	{
		//descriptor = //portname parURI type dimension uris

		for (int outportCounter  = 0; outportCounter < VectorOutputPortDescriptor.size();outportCounter++)
		{
			String[] descriptor = VectorOutputPortDescriptor.elementAt(outportCounter);
			Logger.printFile("\nSetting vectorial output port with descriptor: \n", DebugFileName);
			for(int i = 0; i < descriptor.length;i++)
			{
			Logger.printFile("parameter n: " + i +" = " + descriptor[i] +  "\n", DebugFileName);
			}
			Integer portIndex  = getVectorialOutputPortIndexFromName(descriptor[0]);
			Logger.printFile("vecoutportindex = " + portIndex,DebugFileName);
			VectorParameter vp_out = new VectorParameter();
			vp_out.setURI(descriptor[1]);
			vp_out.setType(descriptor[2]);
			for(int i = 4; i < descriptor.length;i ++)
			{
				SingleParameter p = new SingleParameter(); 
				p.setURI(descriptor[i]);
				p.setType(descriptor[2]);
				vp_out.addSingleParameter(p);
			}
			vp_out.setdimension(Integer.parseInt(descriptor[3]));
			OutputPortsVec[portIndex].setSignal(vp_out);
		}
	}



	@Override
	public SingleParPort[] getOutputPorts() {
		return this.OutputPorts;
	}

	public String printClosure()
	{
		String temp = "print method for this closure still not implemented";
		//		temp = temp + "Closure:\n";
		//		temp = temp + "URI: " + this.getURI();
		//		temp = temp + "Name: " + this.getName(); 
		//		temp = temp + "input ports:\n";
		//		for(int i=0; i < AddVectorIntClosure.getInputVectorPortNames().length;i++ )
		//		{
		//			temp = temp + ("port index = " + i + " + name = " + InputPortsVec[i] + "URI= " + this.InputPortsVec[i].getURI() + "\n");
		//			temp = temp + ("parameter URI: " + this.InputPortsVec[i].getSignal().getURI() + " ParameterValid: " + this.InputPortsVec[i].getSignal().getValid() +
		//			"\n"); 
		//		}
		//		temp = temp + "output ports:\n";
		//		for(int i=0; i < AddVectorIntClosure.getOutputSinglePortNames().length;i++ )
		//		{
		//			temp = temp + ("port index = " + i + " + name = " + this.OutputPorts[i] + "URI= " + this.OutputPorts[i].getURI() + "\n");
		//			temp = temp + ("parameter URI: " + this.OutputPorts[i].getSignal().getURI() + " ParameterValue: " + this.OutputPorts[i].getSignal().getValueForTriple() +
		//					"ParameterValid " + this.OutputPorts[i].getSignal().getValid()+ "\n"); 
		//		}
		return temp;
	}

	public static void setInputVectorPortNames(String[] inputVectorPortNames) {
		InputVectorPortNames = inputVectorPortNames;
	}

	public static String[] getInputVectorPortNames() {
		return InputVectorPortNames;
	}

	public static String[] getInputFunctionalPortNames() {
		return InputFunctionalPortNames;
	}


	//	public static void setOutputSinglePortNames(String[] outputSinglePortNames) {
	//		OutputSinglePortNames = outputSinglePortNames;
	//	}

	//	public static String[] getOutputSinglePortNames() {
	//		return OutputSinglePortNames;
	//	}

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

	public void setInputFunctionalPort(String[] PortDescriptor) //PortDescriptor is PortName PortURI currently. Method only to be used during execution
	{
		Logger.printFile(this.getName() + "-> setInputFunctionalPort -> portdescrptor: " + PortDescriptor[0] + ";" + PortDescriptor[1], DebugFileName);
		int TargetPortIndex = getInputFunctionalPortIndexFromName(PortDescriptor[0]);
		FunctionalParPort fport = this.FuncInputPorts[TargetPortIndex];
		FunctionalParameter fpar;
		if(fport.getSignal() == null)
		{
			fpar = new FunctionalParameter();
		}
		else
		{
			fpar = fport.getSignal();
		}
		fpar.setURI(PortDescriptor[1]);
		fport.setSignal(fpar);


	}
	public Integer getInputFunctionalPortIndexFromName(String portName)
	{
		for(Integer i = 0; i < MapClosure.InputFunctionalPortNames.length;i++)
		{
			if (MapClosure.InputFunctionalPortNames[i].equals(portName))
			{
				return i;
			}
		}
		return null;

	}

	public Integer getVectorialOutputPortIndexFromName(String portName)
	{
		Integer temp = null;
		for (int i = 0; i  < OutputPortsVec.length;i++)
		{
			if(portName.equals(MapClosure.OutputVectorPortNames[i]))
			{
				temp = i;
			}
		}

		return temp;
	}



}

