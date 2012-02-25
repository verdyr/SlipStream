package closureLibrary;

import java.util.UUID;
import java.util.Vector;

import sofia_kp.SSAP_XMLTools;

public class AddSubVecIntClosure implements AtomicClosure{

	private String URI;//This has sense only when the computation is sent to the SIB, the assumption made in this file is that if I send two times a computation I have two different URIs 
	public  SingleParPort[] InputPorts ;
	public VectorParPort[] InputPortsVec;
	public  SingleParPort[] OutputPorts;
	public VectorParPort[] OutputPortsVec;
	public  static String Name = OntologyVocabulary.AddSubVecIntClosure;

	private static String[]  InputSinglePortNames = {"In1", "In2"} ;
	//private static String[] OutputSinglePortNames = {"Out"} ;

	private static String[] InputSinglePortTypes = {"int","int"} ;
	//public static String[] OutputSinglePortTypes = {"int"} ;

	//private static String[]  InputVectorPortNames = {"In1"} ;
	public static String[] OutputVectorPortNames =  {"OutVec"} ;

	//public static String[] InputVectorPortTypes = {"int"} ;
	private static String[] OutputVectorPortTypes = {"int"} ;

	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/AddSubVectorIntClosure";


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

	public  AddSubVecIntClosure() 
	{
		setRandomURI();
		InputPorts = new SingleParPort[2];
		SingleParPort In1 = new SingleParPort();
		In1.setName(InputSinglePortNames[0]);
		In1.setType(InputSinglePortTypes[0]);
		In1.setSignal(null);
		InputPorts[0] = In1;

		SingleParPort In2 = new SingleParPort();
		In2.setName(InputSinglePortNames[1]);
		In2.setType(InputSinglePortTypes[1]);
		In2.setSignal(null);
		InputPorts[1] = In2;

		OutputPortsVec= new VectorParPort[1];
		VectorParPort Out = new VectorParPort();
		Out.setName(OutputVectorPortNames[0]);
		Out.setType(OutputVectorPortTypes[0]);
		Out.setSignal(null);
		OutputPortsVec[0] = Out;
	}

	//This methods allows to initialize the input parameters and to attach them to the input ports, the id of the parameter is not
	//relevant in the context of a simple function, but we have to be careful with chain, 
	//After initialization the closure have all the parameter of the ports different from null;

	//For a vecttorial port a Vector<String[]> is required at least.

	//A complete Initialize could be 

	public void InitializeAddSubVecIntClosure(Vector<String[]> VectorValues, VectorParameter[] inputVectorParameter, VectorParameter[] outputVectorParameter, String[] Singlevalues, SingleParameter[] inputSingleParameter, SingleParameter[] outputSingleParameter)
	{
		//for this closure only outputVectorParameter,Singlevalues,inputSingleParameter will be used

		SingleParameter p;

		for(int i = 0; i < InputSinglePortNames.length;i++)
		{   
			p = new SingleParameter();
			if(inputSingleParameter[i]==null)//If I have  not a parameter in this constructor I create one random
			{


				p.setURI(UUID.randomUUID().toString());
				p.setType(InputSinglePortTypes[i]);//TypeCheck needed?
				if (!(Singlevalues[i]==null)) //If I know the value of the parameter I  set the value and I put Valid == true
				{
					p.setValue(Singlevalues[i]);
					p.setValid(true);
				}
				else  //The parameter has not a constant value to be assigned
				{
					p.setValid(false);
				}

			}
			else//I have a parameter given to the initializator and I use it
			{
				/*
				 * Here I consider that who calls this initialization gives me the values of the inputs and the references in the
				 * the parameters, the initialization assigns the value to the parameters
				 * OR
				 * 
				 * */
				p = inputSingleParameter[i];
				if (!(Singlevalues[i] == null)) //If I know the value of the parameter I  set the value and I put Valid == true
				{
					p.setValue(Singlevalues[i]);
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

			this.InputPorts[i].setSignal(p);
		}
		VectorParameter pout = new VectorParameter();
		for(int i = 0; i < OutputVectorPortNames.length;i++)
		{
			if ((outputVectorParameter == null)||(outputVectorParameter[i] == null))//If I have not an output parameter from the constructor (so no reference) I create one Randomly
			{
				pout.setURI(UUID.randomUUID().toString());
				pout.setType(OutputVectorPortTypes[i]); 
				pout.setValid(false);
			}
			else
			{
				pout = outputVectorParameter[i];
				pout.setType(OutputVectorPortTypes[i]);//Unuseful if  we suppose to pass the right reference
			}
			this.OutputPortsVec[i].setSignal(pout);
			pout.setWritingAtomicClosure(this);
		}
	}






	public VectorParPort[] getVectorialInputPorts()
	{
		return this.InputPortsVec;
	}

	public VectorParPort[] getVectorialOutputPorts()
	{
		return this.OutputPortsVec;
	}

	@Override
	public Vector<Vector<String>> SerializeTriples() {
		//Logger.printFile(this.printClosure(), DebugFileName);

		//Triples
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.AtomicClosure, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ClosureHasName,OntologyVocabulary.AddSubVecIntClosure, "URI", "URI"));
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ComputationHasFunctionalBehaviour,OntologyVocabulary.AddSubVecIntClosure, "URI", "URI"));

		for(int i = 0; i < this.InputPorts.length;i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasInputPort, this.InputPorts[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.InputPorts[i].getTriples());
		}
		for(int i = 0; i < this.OutputPortsVec.length;i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasOutputPort, this.OutputPortsVec[i].getURI(), "URI", "URI"));
			triples = TriplesUtilities.concatTriples(triples, this.OutputPortsVec[i].getTriples());
		}

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

		//End Debug
		return triples;
	}
	public void run()//FIXME here generalization "MAYBE"is needed to manage routing of the vector parameters 
	{
		VectorParPort temp= new VectorParPort();
		for(int i = 0; i < this.OutputVectorPortNames.length;i++)
		{
			if (this.OutputPortsVec[i] == null)
			{
				temp = new VectorParPort();


				this.OutputPortsVec[i] = temp;


				//				if(temp.getSignal()==null)
				//				{
				//				temp.setSignal(new VectorParameter());
				//				}

			}
			else if(this.OutputPortsVec[i].getSignal() == null)
			{
				temp = new VectorParPort();
				this.OutputPortsVec[i] = temp;
			}
			else
			{
				temp = this.OutputPortsVec[i];
			}
		}
		SingleParameter sumParameter; 
		SingleParameter diffParameter;
		if(this.OutputPortsVec[0].getSignal().getContent().size()==0)//I don't know the URI of the output
		{
			sumParameter  = new SingleParameter();
			diffParameter = new SingleParameter();
		}
		else
		{
			sumParameter  = this.OutputPortsVec[0].getSignal().getContent().elementAt(0);
			diffParameter = this.OutputPortsVec[0].getSignal().getContent().elementAt(1);
		}
		Integer resultSum  = (Integer.parseInt(InputPorts[0].getSignal().getValueForTriple()) + (Integer.parseInt(InputPorts[1].getSignal().getValueForTriple())));
		Integer resultDiff = (Integer.parseInt(InputPorts[0].getSignal().getValueForTriple()) - (Integer.parseInt(InputPorts[1].getSignal().getValueForTriple())));

		sumParameter.setPositionInVector(0);
		sumParameter.setType(this.OutputVectorPortTypes[0]);
		sumParameter.setValue(resultSum.toString());
		sumParameter.setValid(true);

		diffParameter.setPositionInVector(1);
		diffParameter.setType(this.OutputVectorPortTypes[0]);
		diffParameter.setValue(resultDiff.toString());
		diffParameter.setValid(true);
		VectorParameter outpar;
		if(temp.getSignal()==null)
		{
			outpar = new VectorParameter();
		}
		else
		{
			outpar = temp.getSignal();
		}
		outpar.setType(this.OutputVectorPortTypes[0]);
		outpar.Content = new Vector<SingleParameter>() ;
		outpar.setdimension(0);
		outpar.addSingleParameter(sumParameter);
		outpar.addSingleParameter(diffParameter);
		this.OutputPortsVec[0].setSignal(outpar);
		this.OutputPortsVec[0].getSignal().setValid(true);

	}

	public void setVectorOutputPortReference(Vector<String[]> VectorOutputPortDescriptor) //OutputPortDescriptor = [PortName, VetorParameterURI, type dimension, singleparameterURI...]
	{
		for(int i = 0; i < VectorOutputPortDescriptor.size();i++)
		{
			int TargetPortIndex = getVectorOutputPortIndexFromName(VectorOutputPortDescriptor.elementAt(i)[0]);
			Logger.printFile("Updating outputportReference target portIndex = " + TargetPortIndex + "\n", DebugFileName);
			if(this.OutputPortsVec[TargetPortIndex].getSignal()!=null)
			{
				this.OutputPortsVec[TargetPortIndex].getSignal().setURI(VectorOutputPortDescriptor.elementAt(i)[1]);
				Logger.printFile("Inserted the URI " + VectorOutputPortDescriptor.elementAt(i)[1] + " for the vectorial parameter\n", DebugFileName);

			}
			else
			{
				VectorParameter vp = new VectorParameter();
				vp.setURI(VectorOutputPortDescriptor.elementAt(i)[1]);
				vp.setType(VectorOutputPortDescriptor.elementAt(i)[2]);
				Logger.printFile("Inserted the URI " + VectorOutputPortDescriptor.elementAt(i)[1] + " for the vectorial parameter\n", DebugFileName);
				this.OutputPortsVec[TargetPortIndex].setSignal(vp);
			}
			for(int j = 0; j < VectorOutputPortDescriptor.elementAt(i).length - 4 ; j++)
			{
				if(VectorOutputPortDescriptor.elementAt(i)[j+4]!= null)
				{
					SingleParameter s = new SingleParameter();
					s.setURI(VectorOutputPortDescriptor.elementAt(i)[j+4]);
					s.setType(VectorOutputPortDescriptor.elementAt(i)[2]);
					this.OutputPortsVec[TargetPortIndex].getSignal().addSingleParameter(s);
				}
			}
		}
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
	@Override
	public SingleParPort[] getInputPorts() {

		return this.InputPorts;
	}

	@Override
	public SingleParPort[] getOutputPorts() {

		return this.OutputPorts;
	}

	@Override
	public Parameter[] getInputParameter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parameter[] getOutputParameter() {

		return null;
	}

	public Integer getVectorOutputPortIndexFromName(String portName)
	{
		for(Integer i = 0; i < this.OutputVectorPortNames.length;i++)
		{
			if (this.OutputVectorPortNames[i].equals(portName))
			{
				return i;
			}
		}
		return null;
	}

	public Integer getInputPortIndexFromName(String portName)
	{
		for(Integer i = 0; i < this.InputPorts.length;i++)
		{
			if (AddSubVecIntClosure.InputSinglePortNames[i].equals(portName))
			{
				return i;
			}
		}
		return null;
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
