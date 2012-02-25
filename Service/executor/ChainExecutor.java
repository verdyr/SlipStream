package executor;

import java.util.Vector;

import closureLibrary.Logger;
import closureLibrary.OntologyVocabulary;
import closureLibrary.SibConstants;

import sofia_kp.KPICore;
import sofia_kp.SSAP_XMLTools;

public class ChainExecutor implements Runnable{

	private String  ChainURI;
	private KPICore kp;
	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/ChainExecutor.txt";
	Vector<String> functionsURI = new Vector<String>();
	private SSAP_XMLTools xmlTools;
	Vector<String> functionNames = new Vector<String>();
	Vector<String> functionBehaviours = new Vector<String>();

	Vector<Vector<String>> functionsInputPorts = new Vector<Vector<String>>();//many ports for each function
	Vector<Vector<String>> functionsOutputPorts = new Vector<Vector<String>>();
	Vector<Vector<String[]>> InputParameters = new Vector<Vector<String[]>>();//Four fields for each parameter portDescriptor : portName, ParameterURI, ParameterValue, ParameterValid
	Vector<Vector<String[]>> OutputParameters = new Vector<Vector<String[]>>();//two fields for each parameter //portDescriptor : portName, ParameterURI
	@Override
	public void run() {
		//Initialize KP

		String SIB_Host = SibConstants.SIB_Host;
		int SIB_Port = SibConstants.SIB_Port;
		String SIB_Name = SibConstants.SIB_Name;
		this.kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		this.xmlTools = new SSAP_XMLTools(null,null,null);
		//kp.setEventHandler(this); This line should be decommented and the upcoming errors solved in the case of central management
		//Retrieve functions
		String xml_query_response = kp.queryRDF(this.ChainURI, OntologyVocabulary.ChainHasFunction, null, "URI", "URI");
		boolean ack=xmlTools.isQueryConfirmed(xml_query_response);
		Logger.printFile("ChainExecutor->Query For AtomicFunctions in Chain confirmed:"+(ack?"YES":"NO"), DebugFileName);
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

				Logger.printFile("ChainExecutor->Query For Function Names, Results: \n" +
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

				Logger.printFile("ChainExecutor->Query For Function Behaviours, Results: \n" +
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

				Logger.printFile("ChainExecutor->Query For InputsPorts"+
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

				Logger.printFile("ChainExecutor-> Query for outputPorts\n" +
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

					Logger.printFile("ChainExecutor->Query for Input port Names Result: \n" +
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

					Logger.printFile("ChainExecutor-> Query for parameters attached to inputs ports, Results: \n"+
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

					Logger.printFile("ChainExecutor->Query for Value of parameters at input ports, Results:\n"+
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

					Logger.printFile("ChainExecutor: Query for validity of parameters at input ports, Results:\n"+
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

					Logger.printFile("ChainExecutor: Query for Vector/singleParameters parameters at input ports, Results:\n"+
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

					Logger.printFile("Chain Executor-> query for names of output ports, Results:\n" +
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

					Logger.printFile("Chain Executor-> query for parameters of output ports, Results:\n" +
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

					Logger.printFile("Chain Executor-> query for type of parameters of output ports, Results:\n" +
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

		//Query finished, now analysis of the functions to understand 
		for (int func_Counter = 0; func_Counter < functionsURI.size();func_Counter++)
		{
			FunctionExecutor f = new FunctionExecutor();
			//f.setFunctionName(this.functionNames.elementAt(func_Counter));  //some optimization possible, at moment name and behaviours are the same, perhaps one will be eliminate later in the project
			f.setFunctionName(this.functionBehaviours.elementAt(func_Counter));

			//Inputs
			Vector<String[]> FunctionInputParameterDescriptors = InputParameters.elementAt(func_Counter);


			Logger.printFile("Functions->URI: " + functionsURI.elementAt(func_Counter) + " name " + functionBehaviours.elementAt(func_Counter) + "\n", DebugFileName);
			for(int  portCounter  = 0; portCounter < FunctionInputParameterDescriptors.size(); portCounter++)
			{
				String[] portDescriptor = FunctionInputParameterDescriptors.elementAt(portCounter);

				Logger.printFile("Chain Executor -> INPUT PORT DESCRIPTOR: " + portDescriptor[0] + " ; " +portDescriptor[1] + " ; " +portDescriptor[2] + " ; " +portDescriptor[3] + ";" + portDescriptor[4] + "\n",DebugFileName);
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
				else if(portDescriptor[4].equals(OntologyVocabulary.functionalParPort))//I'm considering a functional port
				{
					if(portDescriptor[3].equals(OntologyVocabulary.ValidParameter))//I have a valid functional parameter so only the URI of the functional parameter seems to be relevant for the executor 
					{
						String[] inputFunctionalPortDescriptorForExecutor = new String[2];
						inputFunctionalPortDescriptorForExecutor[0] = portDescriptor[0];
						inputFunctionalPortDescriptorForExecutor[1] = portDescriptor[1];
						f.addValidfunctionalInputPort(inputFunctionalPortDescriptorForExecutor);
					}
					else//I add an Item to the waitinglist of the vectorial parameters: are needed PortName and Parameter URI
					{
						String[] WaitingInputDescriptortForExecutor = new String[2];
						WaitingInputDescriptortForExecutor[0] = portDescriptor[0];
						WaitingInputDescriptortForExecutor[1] = portDescriptor[1];
						f.addTofunctionalWaitingList(WaitingInputDescriptortForExecutor);//Not implemented yet
					}
				}
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
			Vector<String[]> FunctionOutputParameterDescriptors = OutputParameters.elementAt(func_Counter);
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

			//Vectorial Outputs
			

		}
	}

	public void setChainURI(String URI)
	{
		this.ChainURI = URI;
	}

}
