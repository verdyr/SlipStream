package executor;

import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.print.attribute.standard.MediaSize.Other;

import sofia_kp.KPICore;
import sofia_kp.SSAP_XMLTools;

import closureLibrary.Add3IntClosure;
import closureLibrary.AddIntClosure;
import closureLibrary.AddSubIntClosure;
import closureLibrary.AddSubVecIntClosure;
import closureLibrary.AddVectorIntClosure;
import closureLibrary.AtomicClosure;
import closureLibrary.Logger;
import closureLibrary.MapClosure;
import closureLibrary.OntologyVocabulary;
import closureLibrary.OutputPort;
import closureLibrary.SingleParPort;
import closureLibrary.SibConstants;
import closureLibrary.SubIntClosure;
import closureLibrary.TriplesUtilities;
import closureLibrary.VectorParPort;

public class FunctionExecutor implements sofia_kp.iKPIC_subscribeHandler, Runnable{

	private KPICore kp;
	private SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);


	private String  ChainURI;
	private Vector<String[]> FunctionalWaitingList = new Vector<String[]>();//[InputPortName, ParameterURI]
	private Vector<String[]> VectorialWaitingList = new Vector<String[]>();//[InputPortName, ParameterURI]
	private Vector<String[]> WaitingList = new Vector<String[]>();//[InputPortName, ParameterURI]

	private String FunctionName;
	private Vector<String[]> InputPortValues= new Vector<String[]>();//[PortName, Value]
	private Vector<String[]> OutputPortReferences = new Vector<String[]>();//[Portname, ParameterURI]
	private Vector<String[]> ReadyVectorialInputPortReferences = new Vector<String[]>();//[Portname, VecParameterURI]]
	private Vector<String[]> ReadyVectorialInputPortDescriptors = new Vector<String[]>();//[Portname, PortType, ParDimension, ParValues]
	private Vector<String[]> VectorialOutputPortReferences = new Vector<String[]>();//[PortName, VecParURI]
	private Vector<String[]> FunctionalInputPorts = new Vector<String[]>();

	boolean finished = false;
	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/FunctionExecutor.txt";
	public FunctionExecutor()
	{
		String SIB_Host = SibConstants.SIB_Host;
		int SIB_Port = SibConstants.SIB_Port;
		String SIB_Name = SibConstants.SIB_Name;
		boolean ack = false;
		this.kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		this.xmlTools = new SSAP_XMLTools(null,null,null);
		kp.setEventHandler(this);
		String xml="";
		Logger.printFile("FunctionExecutorJoining*************************************************", DebugFileName);
		kp.setEventHandler(this);
		xml=kp.join();
		ack=xmlTools.isJoinConfirmed(xml);
		//		Sleep(500);//FIXME consider to remove it
		Logger.printFile("Join confirmed:"+(ack?"YES":"NO")+"\n", DebugFileName);
		if(!ack)
		{
			Logger.printFile("Can not JOIN the SIB", DebugFileName);
			return ;
		}
	}

	public void setFunctionName(String fName)
	{
		this.FunctionName = fName;
	}

	public String getFunctionName()
	{
		return this.FunctionName;
	}

	public void addToWaitingList (String[] waitingElement)
	{
		this.WaitingList.add(waitingElement);
	}

	public void addToVectorialWaitingList(String[] waitingElement)
	{
		this.VectorialWaitingList.add(waitingElement);
	}

	public void addValidVectorialInputPort(String[] VecPortDescriptor)
	{
		this.ReadyVectorialInputPortReferences.add(VecPortDescriptor);
	}

	public Vector<String[]> getWaitingList()
	{
		return this.WaitingList;
	}

	public void addInputValue(String[] InputValueDescriptor)
	{
		this.InputPortValues.add(InputValueDescriptor);
	}

	public Vector<String[]> getInputPortValues()
	{
		return this.InputPortValues;
	}

	public void setOutputReference(String[] outputRef)//Method valid for single parameters port
	{
		String[] temp = new String[2];
		temp[0] = outputRef[0];
		temp[1] = outputRef[1];
		this.OutputPortReferences.add(temp);
	}

	public void setVectorialOutputReference(String[] outputRef)//Method valid for Vectorial parameters port
	{
		/*
		 * I pass to the function executor only the name of the port and the URI of the parameter, possible situations are
		 * 1) The function provides a final output of the chain
		 * In this case the caller is subscribed to the URI of the Vectorial parameter and the URI of the single paramenter contained are not relevant 
		 * (also because thei number is perhaps unknown)
		 * 2) The function provides a a intermediate value input of another block of the chain)
		 *  This case is very similar to the previous one
		 * 3) The function provides a Vectorial Value whose elements are used by other functions a single input parameters.
		 *       - In this case we want the preserve the freedom to consider the number of elements in the vector not a priory knew
		 *       - The only (currently thought)way is managing this situation with an appropriate de-vectorizer 
		 *       -By supposing such a module existing currently is only needed to pass to the function executo the port name and the URI of the Vectorial parameter 
		 * 
		 */


		String[] temp = new String[2];
		temp[0] = outputRef[0];
		temp[1] = outputRef[1];
		this.VectorialOutputPortReferences.add(temp);
	}

	public Vector<String[]> getOutputReferences()
	{
		return this.OutputPortReferences;
	}

	public void run()
	{

		//debugging part
		for(int i = 0; i < InputPortValues.size();i++)
		{
			Logger.printFile(this.FunctionName + "-> Input port " + InputPortValues.elementAt(i)[0] + " ; " + InputPortValues.elementAt(i)[1], DebugFileName);
		}
		for(int i = 0; i < WaitingList.size();i++)
		{
			Logger.printFile(this.FunctionName + "-> Waiting List " + WaitingList.elementAt(i)[0] + " ; " + WaitingList.elementAt(i)[1], DebugFileName);

		}
		// end debugging part

		UpdateVectorialInputParameters();//This method allow to start from a vectorial parameter URI and to arrive to a VectorialParameterDescriptor.
		UpdateVectorialOutputParameters();

		if(WaitingList.isEmpty()&& VectorialWaitingList.isEmpty())
		{
			if(FunctionName.equals(OntologyVocabulary.AddIntClosure))
			{
				ExecuteAddIntClosure();
			}
			else if (FunctionName.equals(OntologyVocabulary.SubIntClosure))
			{
				ExecuteSubIntClosure();
			}
			else if (FunctionName.equals(OntologyVocabulary.AddSubVecIntClosure))
			{
				ExecuteAddSubVecIntClosure();
			}
			else if (FunctionName.equals(OntologyVocabulary.Add3IntClosure))
			{
				ExecuteAdd3IntClosure();
			}
			else if (FunctionName.equals(OntologyVocabulary.AddSubIntClosure))
			{
				ExecuteAddSubIntClosure();
			}
			if(FunctionName.equals(OntologyVocabulary.AddVectorIntClosure))
			{
				ExecuteAddVectorIntClosure();
			}
			else if (FunctionName.equals(OntologyVocabulary.MapClosure))
			{
				ExecuteMapClosure();
			}

		}
		else// Here I subscribe for missing inputs
		{
			for(int i = 0 ; i < WaitingList.size(); i++)
			{

				Logger.printFile("FunctionExecutor->" + this.FunctionName + "--> WAITING LIST  : Element at " + i + " : " + WaitingList.elementAt(i)[0 ] + ";"  + WaitingList.elementAt(i)[1], DebugFileName);
				String xml = kp.subscribeRDF( WaitingList.elementAt(i)[1], OntologyVocabulary.ParHasValue, null , "literal");

				Logger.printFile("Subscription for last input s= " + WaitingList.elementAt(i)[1] + "p = " +  OntologyVocabulary.ParHasValue + "o = *\n",DebugFileName );
				if(xml==null || xml.length()==0)
				{
					Logger.printFile("Subscription for waiting parameter index =" + i + "message NOT valid!\n",DebugFileName);
					//		break;
				}
				Logger.printFile("Subscription for waiting parameter index =" + i +" "+ (this.xmlTools.isSubscriptionConfirmed(xml)?"YES":"NO")+"\n", DebugFileName);
				if(!this.xmlTools.isSubscriptionConfirmed(xml)){break;}
				String  subID_1=this.xmlTools.getSubscriptionID(xml);
				Logger.printFile("RDF Subscribe initial result:"+xml.replace("\n", "")+"\nSubscription ID = " + subID_1, DebugFileName);

			}

			for(int i = 0 ; i < VectorialWaitingList.size(); i++)
			{

				Logger.printFile("FunctionExecutor->" + this.FunctionName + "--> Vectorial WAITING LIST  : Element at " + i + " : " + VectorialWaitingList.elementAt(i)[0 ] + ";"  + VectorialWaitingList.elementAt(i)[1], DebugFileName);
				String xml = kp.subscribeRDF( VectorialWaitingList.elementAt(i)[1], OntologyVocabulary.ParIsValid, null , "literal");

				Logger.printFile("Subscription for last input s= " + VectorialWaitingList.elementAt(i)[1] + "p = " +  OntologyVocabulary.ParIsValid + "o = *\n",DebugFileName );
				if(xml==null || xml.length()==0)
				{
					Logger.printFile("Subscription for waiting parameter index =" + i + "message NOT valid!\n",DebugFileName);
					//		break;
				}
				Logger.printFile("Subscription for waiting parameter index =" + i +" "+ (this.xmlTools.isSubscriptionConfirmed(xml)?"YES":"NO")+"\n", DebugFileName);
				if(!this.xmlTools.isSubscriptionConfirmed(xml)){break;}
				String  subID_1=this.xmlTools.getSubscriptionID(xml);
				Logger.printFile("RDF Subscribe initial result:"+xml.replace("\n", "")+"\nSubscription ID = " + subID_1, DebugFileName);

			}

			while(!finished)// This part will be removed when subscription works
			{

				//SingleParameters

				Sleep(500);//FIXME Wait to not have too much cpu waiting for values, unnecessary when subscription will work
				String changedParameter ="";
				String newValue = "";
				String changedVectorialParameter ="";
				String[] newInputDescriptor = new String[2];
				for(int i = 0; i < WaitingList.size();i++)
				{
					changedParameter ="";
					newValue = "";
					String xml_query_response = kp.queryRDF(WaitingList.elementAt(i)[1], OntologyVocabulary.ParHasValue, null, "URI", "literal");//Structurally there is only one result to this query ack=xmlTools.isQueryConfirmed(xml_query_response);
					Vector<Vector<String>> rdf_result = xmlTools.getQueryTriple(xml_query_response);
					if (rdf_result!=null)
					{
						for(int j = 0; j < rdf_result.size();j++)
						{

							Logger.printFile("FunctionExecutor->" + this.FunctionName + "--> Query for New Input, this query should be removed when subscription works, Results:\n"+
									"  S:["+rdf_result.elementAt(j).get(0)
									+"] P:["+rdf_result.elementAt(j).get(1)
									+"] O:["+rdf_result.elementAt(j).get(2)
									+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
							changedParameter = rdf_result.elementAt(j).get(0);
							newValue = rdf_result.elementAt(j).get(2);
							newInputDescriptor[1] = newValue;
							for(int z = 0; z < WaitingList.size();z++)
							{
								if(WaitingList.elementAt(z)[1].equals(changedParameter))
								{
									newInputDescriptor[0] = WaitingList.elementAt(z)[0];
									WaitingList.remove(z);
									Logger.printFile("Query succeded!! RECEIVED AND COMPUTED", DebugFileName);
								}
							}
							Logger.printFile(this.FunctionName + "-> FunctionExecutor: Added an input " + newInputDescriptor[0] +";" + newInputDescriptor[1] + "\n", DebugFileName);
							Logger.printFile("Waiting List:\n", DebugFileName);
							for(int l = 0; l < WaitingList.size();l++)
							{
								Logger.printFile(this.FunctionName + "-> Waiting List n0" + l + ": " + WaitingList.elementAt(l)[0] + "; " + WaitingList.elementAt(l)[1] + "\n", DebugFileName);
							}
							//Write the new value in the input port: 
							this.addInputValue(newInputDescriptor);
						}
					}

				}
				for(int i = 0; i < VectorialWaitingList.size();i++)
				{
					String xml_query_response = kp.queryRDF(VectorialWaitingList.elementAt(i)[1], OntologyVocabulary.ParIsValid, null, "URI", "literal");//Structurally there is only one result to this query ack=xmlTools.isQueryConfirmed(xml_query_response);
					Vector<Vector<String>> rdf_result = xmlTools.getQueryTriple(xml_query_response);
					if ((rdf_result!=null)&&rdf_result.elementAt(0).get(2).equals(OntologyVocabulary.ValidParameter))
					{
						for(int j = 0; j < rdf_result.size();j++)
						{
							Logger.printFile("FunctionExecutor->" + this.FunctionName + "--> Query for New Vectorial Input, this query should be removed when subscription works, Results:\n"+
									"  S:["+rdf_result.elementAt(j).get(0)
									+"] P:["+rdf_result.elementAt(j).get(1)
									+"] O:["+rdf_result.elementAt(j).get(2)
									+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
						}

						ReadyVectorialInputPortReferences.add(VectorialWaitingList.elementAt(i));
						VectorialWaitingList.remove(i);
						UpdateVectorialInputParameters();		
					}
				}

				if(WaitingList.isEmpty() && VectorialWaitingList.isEmpty() && FunctionalWaitingList.isEmpty())
				{
					if(FunctionName.equals(OntologyVocabulary.AddIntClosure))
					{
						ExecuteAddIntClosure();
						finished=true;
					}
					else if (FunctionName.equals(OntologyVocabulary.SubIntClosure))
					{
						ExecuteSubIntClosure();
						finished=true;
					}
					else if (FunctionName.equals(OntologyVocabulary.Add3IntClosure))
					{
						ExecuteAdd3IntClosure();
						finished=true;
					}
					else if (FunctionName.equals(OntologyVocabulary.AddSubIntClosure))
					{
						ExecuteAddSubIntClosure();
						finished=true;
					}
					else if (FunctionName.equals(OntologyVocabulary.AddVectorIntClosure))
					{
						ExecuteAddVectorIntClosure();
						finished=true;
					}
					else if (FunctionName.equals(OntologyVocabulary.AddSubVecIntClosure))
					{
						ExecuteAddSubVecIntClosure();
						finished=true;
					}
					else if (FunctionName.equals(OntologyVocabulary.MapClosure))
					{
						ExecuteMapClosure();
						finished=true;
					}
				}
			}
		}
	}




	public void writeBack(AtomicClosure c)
	{
		//Debuggiung part
		Logger.printFile("EXECUTINGG " + c.getURI(),DebugFileName);
		for(int i = 0; i < this.OutputPortReferences.size();i++)
		{
			for(int j = 0; j < this.OutputPortReferences.elementAt(i).length; j++)
			{
				Logger.printFile(this.FunctionName + "-> OutputPortReference num " + i + "; content " + this.OutputPortReferences.elementAt(i)[j], DebugFileName);
			}
		}

		//end debug
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		Vector<Vector<String>> triples_remove = new Vector<Vector<String>>();
		SingleParPort[] OutputPorts = c.getOutputPorts();
		if(OutputPorts!= null)
		{
			for(int i = 0; i < OutputPorts.length;i++)
			{
				String portName = OutputPorts[i].getName();
				for (int j =0 ; j < OutputPortReferences.size();j++)
				{
					if(OutputPortReferences.elementAt(j)[0].equals(portName))
					{
						Logger.printFile(this.FunctionName + "I'm inseting the obtained result", DebugFileName);
						triples.add(xmlTools.newTriple(OutputPortReferences.elementAt(i)[1],   OntologyVocabulary.ParHasValue, OutputPorts[i].getSignal().getValueForTriple(), "URI", "literal"));
						triples.add(xmlTools.newTriple(OutputPortReferences.elementAt(i)[1],   OntologyVocabulary.ParHasDatatype, OutputPorts[i].getType(), "URI", "literal"));
						triples.add(xmlTools.newTriple(OutputPortReferences.elementAt(i)[1],   OntologyVocabulary.ParIsValid, OntologyVocabulary.ValidParameter, "URI", "literal"));
						triples_remove.add(xmlTools.newTriple(OutputPortReferences.elementAt(i)[1], OntologyVocabulary.ParIsValid, OntologyVocabulary.InvalidParameter,"URI", "literal"));
					}
				}
			}
		}
		VectorParPort[] VectorialOutput = c.getVectorialOutputPorts();
		if(VectorialOutput!= null)
		{
			for (int j =0 ; j < VectorialOutput.length;j++)
			{

				triples = TriplesUtilities.concatTriples(triples, VectorialOutput[j].getTriples());
				triples_remove.add(xmlTools.newTriple(VectorialOutput[j].getSignal().getURI(), OntologyVocabulary.ParIsValid, OntologyVocabulary.InvalidParameter,"URI", "literal"));
				for(int ParCounter = 0; ParCounter < VectorialOutput[j].getSignal().getContent().size();ParCounter++)
				{
					triples_remove.add(xmlTools.newTriple(VectorialOutput[j].getSignal().getContent().elementAt(ParCounter).getURI(), OntologyVocabulary.ParIsValid, OntologyVocabulary.InvalidParameter,"URI", "literal"));
				}
			}
		}



		String xml="";
		boolean ack;
		//		xml=kp.join();
		//		boolean ack=xmlTools.isJoinConfirmed(xml);
		//		System.out.println("Join confirmed:"+(ack?"YES":"NO")+"\n");
		//		if(!ack)
		//		{
		//			System.out.println("Can not JOIN the SIB");
		//			return ;
		//		}
		xml= kp.update(triples, triples_remove);
		for(int l1= 0; l1< triples.size();l1++)
		{
			for(int l2 = 0; l2 < triples.elementAt(l1).size();l2++)
			{
				Logger.printFile(this.FunctionName + "--> Triple add num = "+ l1 + " element num + "+ l2 + " val = " + triples.elementAt(l1).elementAt(l2) + "\n", DebugFileName);
			}
		}
		xml= kp.update(triples, triples_remove);
		for(int l1= 0; l1< triples_remove.size();l1++)
		{
			for(int l2 = 0; l2 < triples_remove.elementAt(l1).size();l2++)
			{
				Logger.printFile(this.FunctionName + "--> Triple remove num = "+ l1 + " element num + "+ l2 + " val = " + triples_remove.elementAt(l1).elementAt(l2) + "\n", DebugFileName);
			}
		}
		ack=xmlTools.isInsertConfirmed(xml);
		Logger.printFile(this.FunctionName + "--> New Triples inserted? Acnwowledge = "+ack + "\n", DebugFileName);
	}
	@Override
	public void kpic_SIBEventHandler(String xml) {

		Logger.printFile("EventReceived From function Executor", DebugFileName);
		String event_output="\nEVENT_______________________________________________-\n";

		event_output=event_output+"The message:"+xml.replace("\n", "")+"\n";
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = xmlTools.getNewResultEventTriple(xml);
		//Here I suppose , as it will be that only one result is received for each subscription
		String changedParameter ="";
		String newValue = "";
		changedParameter = xmlTools.triple_getSubject(triples.get(0));
		newValue = xmlTools.triple_getObject(triples.get(2));
		if(triples!=null)
		{ 
			event_output=event_output+"New Triples List:\n";
			for(int i=0; i<triples.size() ; i++ )
			{
				Vector<String> t=triples.get(i);

				event_output=event_output+"                ->"
				+"  S:["+xmlTools.triple_getSubject(t)
				+"] P:["+xmlTools.triple_getPredicate(t)
				+"] O:["+xmlTools.triple_getObject(t)
				+"] Otype:["+xmlTools.triple_getObjectType(t)+"]\n";
			}
		}
		else
		{
			event_output = event_output + "no triples in envent";
		}
		Logger.printFile("EventOutput = " + event_output +"\n", DebugFileName);
		boolean changed = false;
		String[] newInputDescriptor = new String[2];
		newInputDescriptor[1] = newValue;

		for(int i = 0; i < WaitingList.size();i++)
		{
			if(WaitingList.elementAt(i)[1].equals(changedParameter))
			{
				newInputDescriptor[0] = WaitingList.elementAt(i)[0];
				WaitingList.remove(i);
				changed = true ;
				Logger.printFile("Event RECEIVED AND COMPUTED", DebugFileName);
			}
		}
		if(WaitingList.isEmpty())
		{
			ExecuteAddIntClosure();
			finished = true;
		}

	}

	private void Sleep(int timeMs)
	{
		try
		{
			Thread.sleep(timeMs);
		}
		catch(Throwable e)
		{
			Logger.printFile("Exception " + e.toString(), DebugFileName);
			//.printFile(e.printStackTrace()., DebugFileName);

		}
	}

	public void ExecuteAddIntClosure()     //FIXME Little problem with where to put the if(functionName.equqls("AddintClosure").... simple optimization and polishing probably possible
	{
		//	Sleep(500);   //FIXME try to remove this perhaps is safe... to Check
		AddIntClosure function = new AddIntClosure();
		for(int i = 0; i  < this.InputPortValues.size();i++)
		{
			function.setInputPort(InputPortValues.elementAt(i));
		}
		//Here I know that there is only one result so simply
		function.run(); //Otherways I have to run the function and take the results from the ports
		Logger.printFile(function.printClosure(), DebugFileName);
		//The writeback can be made also by the same atomic closure, here I want to decouple (perhaps more difficult way)
		writeBack(function);
	}

	public void ExecuteSubIntClosure()     //Little problem with where to put the if(functionName.equqls("AddintClosure")....
	{
		//	Sleep(500);  //FIXME try to remove this perhaps is safe... to Check
		SubIntClosure function = new SubIntClosure();
		for(int i = 0; i  < this.InputPortValues.size();i++)
		{
			function.setInputPort(InputPortValues.elementAt(i));
		}
		//Here I know that there is only one result so simply
		function.run(); //Otherways I have to run the function and take the results from the ports
		Logger.printFile(function.printClosure(), DebugFileName);
		//The writeback can be made also by the same atomic closure, here I want to decouple (perhaps more difficult way)
		writeBack(function);
	}

	public void ExecuteAdd3IntClosure()
	{
		//	Sleep(500);  //FIXME try to remove this perhaps is safe... to Check
		Add3IntClosure function = new Add3IntClosure();

		for(int i = 0; i  < Add3IntClosure.InputPortNames.length;i++)
		{
			function.setInputPort(InputPortValues.elementAt(i));
		}
		//Here I know that there is only one result so simply

		function.run(); //Otherways I have to run the function and take the results from the ports
		Logger.printFile(function.printClosure(), DebugFileName);
		//The writeback can be made also by the same atomic closure, here I want to decouple (perhaps more difficult way)
		writeBack(function);
	}

	public void ExecuteAddSubIntClosure()
	{
		//	Sleep(500);  //FIXME try to remove this perhaps is safe... to Check
		AddSubIntClosure function = new AddSubIntClosure();

		for(int i = 0; i  < AddSubIntClosure.InputPortNames.length;i++)
		{
			function.setInputPort(InputPortValues.elementAt(i));
		}

		Logger.printFile(this.FunctionName + "-> ready to run",DebugFileName);
		function.run(); // I have to run the function and take the results from the ports, this will be done in the writeback
		Logger.printFile(function.printClosure(), DebugFileName);
		//The writeback can be made also by the same atomic closure, here I want to decouple (perhaps more difficult way)
		writeBack(function);
	}

	public void ExecuteAddVectorIntClosure()
	{
		AddVectorIntClosure function = new AddVectorIntClosure();
		for(int i = 0; i < AddVectorIntClosure.getInputVectorPortNames().length;i++)
		{
			function.setInputVectorPort(ReadyVectorialInputPortDescriptors.elementAt(i));//The number must be the same it is also a check of validity
		}
		Logger.printFile(this.FunctionName + "-> ready to run",DebugFileName);
		function.run(); // I have to run the function and take the results from the ports, this will be done in the writeback
		Logger.printFile(function.printClosure(), DebugFileName);
		//The writeback can be made also by the same atomic closure, here I want to decouple (perhaps more difficult way)
		writeBack(function);
	}
	public void ExecuteAddSubVecIntClosure()
	{
		AddSubVecIntClosure function = new AddSubVecIntClosure();
		for(int i = 0; i  < AddSubIntClosure.InputPortNames.length;i++)
		{
			function.setInputPort(InputPortValues.elementAt(i));
		}
		function.setVectorOutputPortReference(VectorialOutputPortReferences);
		Logger.printFile(this.FunctionName + "-> ready to run",DebugFileName);
		Logger.printFile(this.FunctionName + "-> OutputPortDescriptor:\n",DebugFileName);
		for(int i = 0;  i < VectorialOutputPortReferences.size();i++)
		{
			for(int j = 0; j< VectorialOutputPortReferences.elementAt(i).length;j++)
			{
				Logger.printFile(this.FunctionName + "-> reference num : " + i + " Element: " + j +" Value: " + VectorialOutputPortReferences.elementAt(i)[j] +"\n",DebugFileName);

			}
		}

		function.run(); // I have to run the function and take the results from the ports, this will be done in the writeback
		//Logger.printFile(function.printClosure(), DebugFileName);
		//The writeback can be made also by the same atomic closure, here I want to decouple (perhaps more difficult way)
		writeBack(function);
	}

	public void ExecuteMapClosure()
	{
		MapClosure function = new MapClosure();
		for(int i = 0; i < MapClosure.getInputVectorPortNames().length;i++)
		{
			function.setInputVectorPort(ReadyVectorialInputPortDescriptors.elementAt(i));//The number must be the same it is also a check of validity
			//	Logger.printFile(this.FunctionName + "-> InputPort",DebugFileName);	
		}
		for(int i = 0; i < MapClosure.getInputFunctionalPortNames().length;i++)
		{
			function.setInputFunctionalPort(FunctionalInputPorts.elementAt(i));//The number must be the same it is also a check of validity
		}
		for(int  i = 0; i < VectorialOutputPortReferences.size(); i ++)
		{
			for(int j = 0;  j < VectorialOutputPortReferences.elementAt(i).length;j++)
			{
				Logger.printFile("VecOutputReference: " + i + " par num: " + j + " = " +  VectorialOutputPortReferences.elementAt(i)[j]+  "\n", DebugFileName);
			}
		}
		UpdateVectorialOutputParameters();
		for(int  i = 0; i < VectorialOutputPortReferences.size(); i ++)
		{
			for(int j = 0;  j < VectorialOutputPortReferences.elementAt(i).length;j++)
			{
				Logger.printFile("VecOutputReference: " + i + " par num: " + j + " = " +  VectorialOutputPortReferences.elementAt(i)[j]+  "\n", DebugFileName);
			}
		}
		
		function.setVectorOutputPortReference(VectorialOutputPortReferences);
		Logger.printFile(this.FunctionName + " before starting*****************************************\n ",DebugFileName);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		for (int i = 0; i < function.getVectorialInputPorts().length; i++)
		{
			TriplesUtilities.concatTriples(triples, function.getVectorialInputPorts()[i].getTriples());
		}
		for (int i = 0; i < function.getVectorialOutputPorts().length; i++)
		{
			TriplesUtilities.concatTriples(triples, function.getVectorialOutputPorts()[i].getTriples());
		}
		for(int i = 0; i < triples.size(); i++)
		{
			Vector<String > Triple  = triples.elementAt(i);
			Logger.printFile("sub = " + Triple.elementAt(0) + "; pred = " + Triple.elementAt(1) + "; obj = " + Triple.elementAt(2) + ";\n",DebugFileName);
		}

		Logger.printFile(this.FunctionName + "End before starting*****************************************\n ",DebugFileName);




		Logger.printFile(this.FunctionName + "-> ready to run",DebugFileName);
		function.run(); // I have to run the function and take the results from the ports, this will be done in the writeback
		Logger.printFile("\n \n \n" + function.printClosure() + "\n \n \n", DebugFileName);
		//The writeback can be made also by the same atomic closure, here I want to decouple (perhaps more difficult way)
		//	writeBack(function);
	}

	public void UpdateVectorialInputParameters()
	{
		String[] PortDescriptor = null;
		String xml_query_response = "";
		Vector<Vector<String>> rdf_result = null;
		Vector<Vector<String>> rdf_result_1 = null;
		boolean ack = false;
		//		if(this.ReadyVectorialInputPortReferences.size()>0)
		//		{

		for(int i = 0; i < ReadyVectorialInputPortReferences.size();i++)
		{
			Logger.printFile("\n Function Executor->" + this.FunctionName + "-->  number of vectorial references to update: " + ReadyVectorialInputPortReferences.size() + "I am at " + i, DebugFileName);
			//VectorPar Type
			Logger.printFile("\n Function Executor->" + this.FunctionName + "--> vectorial references 0: " + ReadyVectorialInputPortReferences.elementAt(i)[0] +" 1: " + ReadyVectorialInputPortReferences.elementAt(i)[1], DebugFileName);

			String xml_query_response_type = kp.queryRDF(ReadyVectorialInputPortReferences.elementAt(i)[1], OntologyVocabulary.ParHasDatatype, null, "URI", "literal");
			for (int l1 = 0; l1 <ReadyVectorialInputPortReferences.size();l1++)
			{
				for (int l2 = 0 ; l2 < ReadyVectorialInputPortReferences.elementAt(l1).length;l2++)
				{
					Logger.printFile("\n Function Executor->" + this.FunctionName + "Input descrptor num " + l1 + " elements num " + l2 + " -->"+ReadyVectorialInputPortReferences.elementAt(l1)[l2] +"\n", this.DebugFileName);
				}
			}
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response_type);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile("\n Function Executor->" + this.FunctionName + "-->  query for the type of the vectorial Port:" + ReadyVectorialInputPortReferences.elementAt(i)[0] + ", Results:\n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
			}
			String ParType = rdf_result.elementAt(0).get(2);

			//Parameters URI

			xml_query_response = kp.queryRDF(ReadyVectorialInputPortReferences.elementAt(i)[1], OntologyVocabulary.VectorParhasParameter, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile("Function Executor->" + this.FunctionName + "--> query for parameters in the vector relative to port:" + ReadyVectorialInputPortReferences.elementAt(i)[0] + ", Results:\n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
			}
			PortDescriptor = new String[rdf_result.size() + 3];
			PortDescriptor[0] = ReadyVectorialInputPortReferences.elementAt(i)[0];
			PortDescriptor[1] = ParType;
			PortDescriptor[2] = "" + rdf_result.size();

			//Parameters Positions
			int[] ParameterPositions = new int[rdf_result.size()];
			for (int parCounter = 0; parCounter < rdf_result.size(); parCounter++)
			{
				xml_query_response = kp.queryRDF(rdf_result.elementAt(parCounter).get(2), OntologyVocabulary.ParhasPositionInVector, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result_1 =  xmlTools.getQueryTriple(xml_query_response);
				ParameterPositions[parCounter] = Integer.parseInt(rdf_result_1.elementAt(0).get(2));
				Logger.printFile("\n Function Executor->" + this.FunctionName + "-->  query for the position in vector for the parameters:" +", Results:\n" +
						"  S:["+rdf_result_1.elementAt(0).get(0)
						+"] P:["+rdf_result_1.elementAt(0).get(1)
						+"] O:["+rdf_result_1.elementAt(0).get(2)
						+"] Otype:["+rdf_result_1.elementAt(0).get(3)+"]\n", DebugFileName);
			}

			//Parameters Values

			for (int parCounter = 0; parCounter < rdf_result.size(); parCounter++)
			{
				xml_query_response = kp.queryRDF(rdf_result.elementAt(parCounter).get(2), OntologyVocabulary.ParHasValue, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result_1 =  xmlTools.getQueryTriple(xml_query_response);
				PortDescriptor[3 + ParameterPositions[parCounter]] = rdf_result_1.elementAt(0).get(2);
				Logger.printFile("\n Function Executor->" + this.FunctionName + "-->  query for the Value of the single parameters:" +" Results:\n" +
						"  S:["+rdf_result_1.elementAt(0).get(0)
						+"] P:["+rdf_result_1.elementAt(0).get(1)
						+"] O:["+rdf_result_1.elementAt(0).get(2)
						+"] Otype:["+rdf_result_1.elementAt(0).get(3)+"]\n", DebugFileName);
			}

			ReadyVectorialInputPortDescriptors.add(PortDescriptor);
			Logger.printFile("\n Function Executor->" + this.FunctionName + "-->  I arrived to end of loop number of vectorial references to updated: " + (i+1) + " I have to arrive to: " + ReadyVectorialInputPortReferences.size(), DebugFileName);


			//Now I have the values of the parameters and the type of the vector
			//VectorParameterDescriptor: 0 type, 1 dimension, 2 to end values
		}
		ReadyVectorialInputPortReferences.removeAllElements();

		//		}

	}
	public void UpdateVectorialOutputParameters ()
	{
		//Final vector outpudescriptor portname, parameteruri type, dimension, parametersuri
		for(int  s = 0; s < VectorialOutputPortReferences.size(); s ++)
		{
			for(int d = 0;  d < VectorialOutputPortReferences.elementAt(s).length;d++)
			{
				Logger.printFile("VecOutputReferenceIn method BEGIN: " + s + " par num: " + d + " = " +  VectorialOutputPortReferences.elementAt(s)[d]+  "\n", DebugFileName);
			}
		}
		String[] PortDescriptor = null;
		String type = "";
		String dimension = "";

		String xml_query_response = "";
		Vector<Vector<String>> rdf_result = null;
		Vector<Vector<String>> rdf_result_1 = null;
		boolean ack = false;
		for(int i = 0;  i < this.VectorialOutputPortReferences.size(); i++)
		{
			String xml_query_response_type = kp.queryRDF(VectorialOutputPortReferences.elementAt(i)[1], OntologyVocabulary.ParHasDatatype, null, "URI", "literal");
			for (int l1 = 0; l1 <VectorialOutputPortReferences.size();l1++)
			{
				for (int l2 = 0 ; l2 < VectorialOutputPortReferences.elementAt(l1).length;l2++)
				{
					Logger.printFile("\n Function Executor->" + this.FunctionName + "Outout descrptor num " + l1 + " elements num " + l2 + " -->"+VectorialOutputPortReferences.elementAt(l1)[l2] +"\n", this.DebugFileName);
				}
			}
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response_type);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile("\n Function Executor->" + this.FunctionName + "-->  query for the type of the vectorial Output Port:" + VectorialOutputPortReferences.elementAt(i)[0] + ", Results:\n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
			}
			type = rdf_result.elementAt(0).get(2);
			//Parameter Dimension
			xml_query_response_type = kp.queryRDF(VectorialOutputPortReferences.elementAt(i)[1], OntologyVocabulary.VectorParhasDimension, null, "URI", "literal");

			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response_type);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile("\n Function Executor->" + this.FunctionName + "-->  query for the dimension of the vectorial Output Port:" + VectorialOutputPortReferences.elementAt(i)[0] + ", Results:\n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
			}
			dimension = rdf_result.elementAt(0).get(2);
			//Parameters URI

			xml_query_response = kp.queryRDF(VectorialOutputPortReferences.elementAt(i)[1], OntologyVocabulary.VectorParhasParameter, null, "URI", "URI");
			ack=xmlTools.isQueryConfirmed(xml_query_response);
			rdf_result = xmlTools.getQueryTriple(xml_query_response);
			for(int j = 0; j < rdf_result.size();j++)
			{

				Logger.printFile("Function Executor->" + this.FunctionName + "--> query for parameters in the vector relative to output port:" + VectorialOutputPortReferences.elementAt(i)[0] + ", Results:\n" +
						"  S:["+rdf_result.elementAt(j).get(0)
						+"] P:["+rdf_result.elementAt(j).get(1)
						+"] O:["+rdf_result.elementAt(j).get(2)
						+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n", DebugFileName);
			}
			PortDescriptor = new String[rdf_result.size() + 4];
			PortDescriptor[0] = VectorialOutputPortReferences.elementAt(i)[0];
			PortDescriptor[1] = VectorialOutputPortReferences.elementAt(i)[1];
			PortDescriptor[2] = type;
			PortDescriptor[3] = dimension;
			//Parameters Positions
			int[] ParameterPositions = new int[rdf_result.size()];
			for (int parCounter = 0; parCounter < rdf_result.size(); parCounter++)
			{
				xml_query_response = kp.queryRDF(rdf_result.elementAt(parCounter).get(2), OntologyVocabulary.ParhasPositionInVector, null, "URI", "literal");
				ack=xmlTools.isQueryConfirmed(xml_query_response);
				rdf_result_1 =  xmlTools.getQueryTriple(xml_query_response);
				ParameterPositions[parCounter] = Integer.parseInt(rdf_result_1.elementAt(0).get(2));
				Logger.printFile("\n Function Executor->" + this.FunctionName + "-->  query for the position in vector for the parameters in the outputVector :" +", Results:\n" +
						"  S:["+rdf_result_1.elementAt(0).get(0)
						+"] P:["+rdf_result_1.elementAt(0).get(1)
						+"] O:["+rdf_result_1.elementAt(0).get(2)
						+"] Otype:["+rdf_result_1.elementAt(0).get(3)+"]\n", DebugFileName);
				PortDescriptor[4 + ParameterPositions[parCounter]] = rdf_result.elementAt(parCounter).get(2);
			}
			for(int  s = 0; s < VectorialOutputPortReferences.size(); s ++)
			{
				for(int d = 0;  d < VectorialOutputPortReferences.elementAt(s).length;d++)
				{
					Logger.printFile("VecOutputReference: " + s + " par num: " + d + " = " +  VectorialOutputPortReferences.elementAt(s)[d]+  "\n", DebugFileName);
				}
			}
			VectorialOutputPortReferences.removeElementAt(i);
			VectorialOutputPortReferences.add(i, PortDescriptor);
			for(int  s = 0; s < VectorialOutputPortReferences.size(); s ++)
			{
				for(int d = 0;  d < VectorialOutputPortReferences.elementAt(s).length;d++)
				{
					Logger.printFile("VecOutputReferenceIn method: " + s + " par num: " + d + " = " +  VectorialOutputPortReferences.elementAt(s)[d]+  "\n", DebugFileName);
				}
			}
			
		}


	}

	public void addValidfunctionalInputPort(String[] portDescriptor)
	{
		FunctionalInputPorts.add(portDescriptor);
	}

	public void addTofunctionalWaitingList(String[] portDescriåptor)
	{
		FunctionalWaitingList.add(portDescriåptor);
	}

}
