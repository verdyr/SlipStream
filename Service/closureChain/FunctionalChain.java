package closureChain;



import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

import closureLibrary.Add3IntClosure;
import closureLibrary.AddIntClosure;
import closureLibrary.AddSubIntClosure;
import closureLibrary.AddSubVecIntClosure;
import closureLibrary.AddVectorIntClosure;
import closureLibrary.AtomicClosure;
import closureLibrary.FunctionalParameter;
import closureLibrary.InputPort;
import closureLibrary.Logger;
import closureLibrary.MapClosure;
import closureLibrary.OntologyVocabulary;
import closureLibrary.Parameter;
import closureLibrary.SingleParPort;
import closureLibrary.SibConstants;
import closureLibrary.SingleParameter;
import closureLibrary.SubIntClosure;
import closureLibrary.VectorParPort;
import closureLibrary.VectorParameter;

import sofia_kp.KPICore;
import sofia_kp.SSAP_XMLTools;

public class FunctionalChain  implements sofia_kp.iKPIC_subscribeHandler, Runnable{

	public KPICore kp;
	public SSAP_XMLTools xmlTools;

	//private AtomicClosure[] ClosureList;
    private boolean isFunctionalParameterValue = false;
	private String URI;
	private Vector<Parameter> inputs = new Vector<Parameter>();//I can suppose that if an input variable value is not present then the input is for the chain this statement has to be verified (comment for me)
	private Vector<Parameter> outputs = new Vector<Parameter>();
	private SingleParameter[] intermediate;//When I add a function to the chain I don�t know if its output is intermediate ot outputparameter
	//The parameters  should be updated at each addfunction or when we are sure that no more function will be added tothe list.
	private Vector<AtomicClosure> AtomicFunctions = new Vector<AtomicClosure>();
	private String[] functionCallList;
	private Hashtable<String, Vector<String>> VectorParameterMapping = new Hashtable<String, Vector<String>>();

	Vector<String[]> requiredResults = new Vector<String[]>();
	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/FunctionalChain.txt";

	public Hashtable<String, String> connectionTable;

	public void addInput(Parameter p)
	{
		inputs.add(p);
	}
	
	public void addOutput(Parameter p)
	{
		outputs.add(p);
	}
	
	public Vector<Parameter> findChainInputs()
	{
		Vector<Parameter> temp =  new Vector<Parameter>();
		Vector<Parameter> inputs_copy;
		inputs_copy = new Vector<Parameter>();
		inputs_copy =inputs;
		Vector<Parameter> outputsCopy =new Vector<Parameter>();
	    outputsCopy = outputs;
		for(int i = 0; i < outputsCopy.size();i++)
		{
			inputs_copy.removeElement(outputsCopy.elementAt(i));
		}
		
		for(int i = 0; i < inputs_copy.size();i++)
		{
			if(!inputs_copy.elementAt(i).getValid())
			{
				temp.add(inputs_copy.elementAt(i));
			}
		}
		
		return temp;
		
	}
	
	public Vector<Parameter> findChainOutputs()
	{
		
		Vector<Parameter> inputs_copy;
		inputs_copy = new Vector<Parameter>();
		inputs_copy =inputs;
		Vector<Parameter> outputsCopy =new Vector<Parameter>();
	    outputsCopy = outputs;
	    for(int i = 0; i < inputs_copy.size();i++)
		{
	    	outputsCopy.removeElement(inputs_copy.elementAt(i));
		
		}
	    return outputsCopy;
	}
	
	
	public void set_IsFunctionalParameterValue(boolean isParameterValue)
	{
		this.isFunctionalParameterValue = isParameterValue;
	}
	
	public void setRandomURI()
	{
		this.URI = UUID.randomUUID().toString();
	}
	public void setURI(String uri)
	{
		this.URI = uri;
	}
	public String getURI()
	{
		return this.URI;
	}

	public FunctionalChain()
	{
		Initialize();
	}


	/*Perhaps the critical method, here I don't do any check if not necessary I suppose that inappropriate calls of this method can be partially
	 *treated at compilation time, but i don�t care now of the remaining
	 *Who calls this method wants to create a closure in a computational chain, the modalities are:
	 *1)if I write in the inputs and not in inputsRef I mean that a random input parameter will be created with the assigned value
	 *2)if I write both inputs and inputsRefs I assign A value to a parameter and this parameter will be used as input for other 
	 *  functions in the chain
	 *3)if I write null in inputs and !null in inputsRefs so this empty parameter will be input of this and possibly other closures
	 *   and possibly will be the output of an other function
	 *4)if I leave inputs and inputsRef = null I have an input of the whole chain 
	 */

	//public void InitializeAddVectorIntClosure(Vector<String[]> VectorValues, VectorParameter[] inputVectorParameter, VectorParameter[] outputVectorParameter, String[] Singlevalues, SingleParameter[] inputSingleParameter, SingleParameter[] outputSingleParameter)

	
	public void addFunctionalFunctionCall(String functionName, Vector<String[]> VectorValues, VectorParameter[] inputVectorParameter, VectorParameter[] outputVectorParameter, String[] Singlevalues, SingleParameter[] inputSingleParameter, SingleParameter[] outputSingleParameter, FunctionalParameter[] inputFunctionalParameters, FunctionalParameter[] outputFunctionalParameter)
	{
		if (functionName.equals(OntologyVocabulary.MapClosure))
		{
			MapClosure f = new MapClosure();
			f.InitializeMapClosure(VectorValues, null, outputVectorParameter, null, null, null, inputFunctionalParameters, null);
			VectorParameter outputPar = f.getVectorialOutputPorts()[0].getSignal();
			Vector<SingleParameter> vectorout = outputPar.getContent();
			for(int i = 0; i < vectorout.size();i++)
			{
				connectionTable.put("map"  + i , vectorout.elementAt(i).getURI());
			}
			AtomicFunctions.add(f);
		}
	}
	
	public void addVecFunctionCall(String FunctionName,Vector<String[]> vecInputs, Vector<String> VecReferences, String[] inputs, String[] references,String[] VecOutputRef, String[] OutputsRef)
	{
		Vector<String[]> vec_Inputs = null;
		VectorParameter[] vec_Input_References = new VectorParameter[1];
		Vector<String> Single_Input_Values = new Vector<String>();
		Vector<SingleParameter> Single_Input_References = new Vector<SingleParameter>();
		SingleParameter[] Single_Output_References = null;
		Vector<VectorParameter> Vec_Output_References = null;


		if(FunctionName.equals(OntologyVocabulary.AddVectorIntClosure))//very specific implementation to do fast, only in a second phase a more general method
		{


			AddVectorIntClosure f = new AddVectorIntClosure();
			if ((vecInputs == null)|| (vecInputs.elementAt(0) == null))//I have a reference to the input or input of the whole chain
			{
				if (VecReferences == null)//I have an input of the whole chain
				{
					vec_Inputs = null;
					vec_Input_References = null;
					Single_Input_Values = null;
					Logger.printFile("Vectorial references for addintClosure ==null\n", DebugFileName);
					

				}
				else // I have the reference to a Vectorial parameter
				{
					VectorParameter vp = new VectorParameter();
					if(connectionTable.containsKey(VecReferences.elementAt(0)))
					{
						//I use the same URI for the parameter
						vp.setURI(connectionTable.get(VecReferences.elementAt(0)));
						Logger.printFile("URI of the Vectorial Parameter given to AddVectorIntClosure = " + VecReferences.elementAt(0) + "\n", DebugFileName);
					}
					else //A new Reference
					{
						vp.setURI(UUID.randomUUID().toString());
						connectionTable.put(VecReferences.elementAt(0), vp.getURI());
						Logger.printFile("URI of the Vectorial Parameter given to AddVectorIntClosure Random and equals to= " + vp.getURI() + "\n", DebugFileName);
						
					}
					vec_Input_References[0] = vp;
				}
			}
			else// I have a constant input (in this case vectorial)
			{
				vec_Inputs = vecInputs;
			}
			//Output
			Single_Output_References = new SingleParameter[1];;
			for(int i = 0; i < OutputsRef.length;i++)
			{

				SingleParameter p = new SingleParameter();
				/*
				 * The outputRef can be present or not on the table so
				 */
				if(connectionTable.containsKey(OutputsRef[i]))
				{
					p.setURI(connectionTable.get(OutputsRef[i]));
				}
				else
				{
					p.setURI(UUID.randomUUID().toString());
					connectionTable.put(OutputsRef[i], p.getURI());
				}
				Single_Output_References[i] = p;
			}
			f.InitializeAddVectorIntClosure(vec_Inputs, vec_Input_References,null, null, null, Single_Output_References);

			AtomicFunctions.add(f);
		}

		else if(FunctionName.equals(OntologyVocabulary.AddSubVecIntClosure))
		{
			AddSubVecIntClosure f = new AddSubVecIntClosure();

			String[] inStrings = new String[2];
			SingleParameter[] inParameters = new SingleParameter[2]; 
			VectorParameter[] outParametersConstr = new VectorParameter[1];

			for(int i = 0; i < inputs.length; i++)
			{
				if(inputs[i]==null)//I have a reference or an input of the Whole chain
				{
					if (references[i].equals(null))//I have an input of the whole chain so the chain cannot be executed
					{
						//The closure will be created with an empty(invalid) parameter at the beginning
						Single_Input_Values.add(null);
						Single_Input_References.add(null);
					}
					else//I have a reference to the output or other inputs in the chain
					{
						SingleParameter p = new SingleParameter();
						if (connectionTable.containsKey(references[i]))//The reference is to an existing parameter 
						{
							//I use the same parameter ID
							p.setURI(connectionTable.get(references[i]));
						}
						else
						{
							//a new parameter and I update the table
							p.setURI(UUID.randomUUID().toString());
							connectionTable.put(references[i], p.getURI());
						}
						//I update the parameter vector which will be input of the Closure constructor
						Single_Input_Values.add(null);
						Single_Input_References.add(p);
					}

				}
				else//I have a constant input for only this closure or for more
				{
					if (references[i]==null)//I have a constant input
					{
						Single_Input_Values.add(inputs[i]);
						Single_Input_References.add(null);
					}
				}
			}//end of for on the inputs
			Vec_Output_References = new Vector<VectorParameter>();
			for(int i = 0; i < VecOutputRef.length;i++)
			{

				VectorParameter vp = new VectorParameter();
				/*
				 * The outputRef can be present or not on the table so
				 */
				if(connectionTable.containsKey(VecOutputRef[i]))
				{
					vp.setURI(connectionTable.get(VecOutputRef[i]));

					Vector<String> singleParametersReferences = VectorParameterMapping.get(VecOutputRef[i]);
					for(int j =0; j < singleParametersReferences.size();j++)
					{
						SingleParameter p = new SingleParameter();
						p.setURI(connectionTable.get(singleParametersReferences.elementAt(j)));
						vp.addSingleParameter(p);
					}

				}
				else
				{
					Logger.printFile("Addiing " +FunctionName + " Vectorial Parameter not present in the connection table remeber to map it before doing the addfunctioncall", DebugFileName);
				}
				Vec_Output_References.add(vp);

			}
			VectorParameter[] Vec_Output_References_ForInitialize = new VectorParameter[Vec_Output_References.size()];
			for(int l = 0; l < Vec_Output_References.size();l++)
			{
				Vec_Output_References_ForInitialize[l] = Vec_Output_References.elementAt(l);
			}
			String[] Single_Input_Values_ForInitialize = new String[Single_Input_Values.size()];
			for(int l = 0; l < Single_Input_Values.size();l++)
			{
				Single_Input_Values_ForInitialize[l] = Single_Input_Values.elementAt(l);
			}
			SingleParameter[] Single_Input_References_ForInitialize = new SingleParameter[Single_Input_References.size()];
			for(int l = 0; l < Single_Input_References.size();l++)
			{
				Single_Input_References_ForInitialize[l] = Single_Input_References.elementAt(l);
			}

			f.InitializeAddSubVecIntClosure(null, null,Vec_Output_References_ForInitialize,Single_Input_Values_ForInitialize, Single_Input_References_ForInitialize, null);
			AtomicFunctions.add(f);


		}
	}

	public void addFunctionCall(String FunctionName, String[] inputs, String[] inputsRef, String[] OutputsRef)
	{
		if(FunctionName.equals(OntologyVocabulary.AddIntClosure))//no better way at moment, this method depend only from the structure and perhaps the signature of the closure
		{
			AddIntClosure f = new AddIntClosure();
			String[] inValues = new String[2];
			SingleParameter[] inParameters = new SingleParameter[2]; 
			SingleParameter[] outParametersConstr = new SingleParameter[1];
			for(int i = 0; i < inputs.length; i++)
			{
				if(inputs[i]==null)//I have a reference or an input of the Whole chain
				{
					if (inputsRef[i].equals(null))//I have an input of the whole chain so the chain cannot be executed
					{
						//The closure will be created with an empty(invalid) parameter at the beginning
						inValues[i] = null;
						inParameters[i] = null;
					}
					else//I have a reference to the output or other inputs in the chain
					{
						SingleParameter p = new SingleParameter();
						if (connectionTable.containsKey(inputsRef[i]))//The reference is to an existing parameter 
						{
							//I use the same parameter ID
							p.setURI(connectionTable.get(inputsRef[i]));
						}
						else
						{
							//a new parameter and I update the table
							p.setURI(UUID.randomUUID().toString());
							connectionTable.put(inputsRef[i], p.getURI());
						}
						//I update the parameter vector which will be input of the Closure constructor
						inValues[i] = null;
						inParameters[i]= p;
					}

				}
				else//I have a constant input for only this closure or for more
				{
					if (inputsRef[i]==null)//I have a constant input
					{
						inValues[i] = inputs[i];
						inParameters[i] = null;
					}
					else
					{
						SingleParameter p = new SingleParameter();
						if (connectionTable.containsKey(inputsRef[i]))//The reference is to an existing parameter 
						{
							p.setURI(connectionTable.get(inputsRef[i]));
						}
						else
						{
							//a new parameter and I update the table
							p.setURI(UUID.randomUUID().toString());
							connectionTable.put(inputsRef[i], p.getURI());
						}
						
						p.setValue(inputs[i]);
						p.setValid(true);
						inValues[i] = null;
						inParameters[i] = p;
					}
				}
			}//end of for on the inputs
			/*
			 * the for is needed only for functions with  multiple output(not now). The vectors are multiple outputs in this context, but we will see them later
			 */
			for(int i = 0; i < OutputsRef.length;i++)
			{

				SingleParameter p = new SingleParameter();
				/*
				 * The outputRef can be present or not on the table so
				 */
				if(connectionTable.containsKey(OutputsRef[i]))
				{
					p.setURI(connectionTable.get(OutputsRef[i]));
				}
				else
				{
					p.setURI(UUID.randomUUID().toString());
					connectionTable.put(OutputsRef[i], p.getURI());
				}
				outParametersConstr[i] = p;
			}//end of for on the outputs

			/*
			 * Initialization of the closure
			 */


			f.InitializeAddIntClosure( inValues, inParameters, outParametersConstr);
			AtomicFunctions.add(f);

		}//end if(FunctionName.equals("AddIntClosure"))
		else if (FunctionName.equals(OntologyVocabulary.SubIntClosure))
		{
			SubIntClosure f = new SubIntClosure();
			String[] inValues = new String[2];
			SingleParameter[] inParameters = new SingleParameter[2]; 
			SingleParameter[] outParametersConstr = new SingleParameter[1];
			for(int i = 0; i < inputs.length; i++)
			{
				if(inputs[i]==null)//I have a reference or an input of the Whole chain
				{
					if (inputsRef[i].equals(null))//I have an input of the whole chain so the chain cannot be executed
					{
						//The closure will be created with an empty(invalid) parameter at the beginning
						inValues[i] = null;
						inParameters[i] = null;
					}
					else//I have a reference to the output or other inputs in the chain
					{
						SingleParameter p = new SingleParameter();
						if (connectionTable.containsKey(inputsRef[i]))//The reference is to an existing parameter 
						{
							//I use the same parameter ID
							p.setURI(connectionTable.get(inputsRef[i]));
						}
						else
						{
							//a new parameter and I update the table
							p.setURI(UUID.randomUUID().toString());
							connectionTable.put(inputsRef[i], p.getURI());
						}
						//I update the parameter vector which will be input of the Closure constructor
						inValues[i] = null;
						inParameters[i]= p;
					}

				}
				else//I have a constant input for only this closure or for more
				{
					if (inputsRef[i]==null)//I have a constant input
					{
						inValues[i] = inputs[i];
						inParameters[i] = null;
					}
					else
					{
						SingleParameter p = new SingleParameter();
						if (connectionTable.containsKey(inputsRef[i]))//The reference is to an existing parameter 
						{
							p.setURI(connectionTable.get(inputsRef[i]));
						}
						else
						{
							//a new parameter and I update the table
							p.setURI(UUID.randomUUID().toString());
							connectionTable.put(inputsRef[i], p.getURI());
						}
						
						p.setValue(inputs[i]);
						p.setValid(true);
						inValues[i] = null;
						inParameters[i] = p;
					}
				}
			}//end of for on the inputs
			/*
			 * the for is needed only for functions with  multiple output(not now). The vectors are multiple outputs in this context, but we will see them later
			 */
			for(int i = 0; i < OutputsRef.length;i++)
			{

				SingleParameter p = new SingleParameter();
				/*
				 * The outputRef can be present or not on the table so
				 */
				if(connectionTable.containsKey(OutputsRef[i]))
				{
					p.setURI(connectionTable.get(OutputsRef[i]));
				}
				else
				{
					p.setURI(UUID.randomUUID().toString());
					connectionTable.put(OutputsRef[i], p.getURI());
				}
				outParametersConstr[i] = p;
			}//end of for on the outputs

			/*
			 * Initialization of the closure
			 */


			f.InitializeSubIntClosure( inValues, inParameters, outParametersConstr);
			AtomicFunctions.add(f);
		}
		if(FunctionName.equals(OntologyVocabulary.Add3IntClosure))//no better way at moment, this method depend only from the structure and perhaps the signature of the closure
		{

			/*
			 * In this part I initialize the 
			 */
			Add3IntClosure f = new Add3IntClosure();
			String[] inValues = new String[3];
			SingleParameter[] inParameters = new SingleParameter[3]; 
			SingleParameter[] outParametersConstr = new SingleParameter[1];
			for(int i = 0; i < inputs.length; i++)
			{
				if(inputs[i]==null)//I have a reference or an input of the Whole chain
				{
					if (inputsRef[i].equals(null))//I have an input of the whole chain so the chain cannot be executed
					{
						//The closure will be created with an empty(invalid) parameter at the beginning
						inValues[i] = null;
						inParameters[i] = null;
					}
					else//I have a reference to the output or other inputs in the chain
					{
						SingleParameter p = new SingleParameter();
						if (connectionTable.containsKey(inputsRef[i]))//The reference is to an existing parameter 
						{
							//I use the same parameter ID
							p.setURI(connectionTable.get(inputsRef[i]));
						}
						else
						{
							//a new parameter and I update the table
							p.setURI(UUID.randomUUID().toString());
							connectionTable.put(inputsRef[i], p.getURI());
						}
						//I update the parameter vector which will be input of the Closure constructor
						inValues[i] = null;
						inParameters[i]= p;
					}

				}
				else//I have a constant input for only this closure or for more
				{
					if (inputsRef[i]==null)//I have a constant input
					{
						inValues[i] = inputs[i];
						inParameters[i] = null;
					}
				}
			}//end of for on the inputs
			/*
			 * the for is needed only for functions with  multiple output(not now). The vectors are multiple outputs in this context, but we will see them later
			 */
			for(int i = 0; i < OutputsRef.length;i++)
			{

				SingleParameter p = new SingleParameter();
				/*
				 * The outputRef can be present or not on the table so
				 */
				if(connectionTable.containsKey(OutputsRef[i]))
				{
					p.setURI(connectionTable.get(OutputsRef[i]));
				}
				else
				{
					p.setURI(UUID.randomUUID().toString());
					connectionTable.put(OutputsRef[i], p.getURI());
				}
				outParametersConstr[i] = p;
			}//end of for on the outputs

			/*
			 * Initialization of the closure
			 */


			f.InitializeAdd3IntClosure( inValues, inParameters, outParametersConstr);
			AtomicFunctions.add(f);
		}
		else if(FunctionName.equals(OntologyVocabulary.AddSubIntClosure))//no better way at moment, this method depend only from the structure and perhaps the signature of the closure
		{
			AddSubIntClosure f = new AddSubIntClosure();
			String[] inValues = new String[2];
			SingleParameter[] inParameters = new SingleParameter[2]; 
			SingleParameter[] outParametersConstr = new SingleParameter[2];
			for(int i = 0; i < inputs.length; i++)
			{
				if(inputs[i]==null)//I have a reference or an input of the Whole chain
				{
					if (inputsRef[i].equals(null))//I have an input of the whole chain so the chain cannot be executed
					{
						//The closure will be created with an empty(invalid) parameter at the beginning
						inValues[i] = null;
						inParameters[i] = null;
					}
					else//I have a reference to the output or other inputs in the chain
					{
						SingleParameter p = new SingleParameter();
						if (connectionTable.containsKey(inputsRef[i]))//The reference is to an existing parameter 
						{
							//I use the same parameter ID
							p.setURI(connectionTable.get(inputsRef[i]));
						}
						else
						{
							//a new parameter and I update the table
							p.setURI(UUID.randomUUID().toString());
							connectionTable.put(inputsRef[i], p.getURI());
						}
						//I update the parameter vector which will be input of the Closure constructor
						inValues[i] = null;
						inParameters[i]= p;
					}

				}
				else//I have a constant input for only this closure or for more
				{
					if (inputsRef[i]==null)//I have a constant input
					{
						inValues[i] = inputs[i];
						inParameters[i] = null;
					}
				}
			}//end of for on the inputs
			/*
			 * the for is needed only for functions with  multiple output(not now). The vectors are multiple outputs in this context, but we will see them later
			 */
			for(int i = 0; i < OutputsRef.length;i++)
			{

				SingleParameter p = new SingleParameter();
				/*
				 * The outputRef can be present or not on the table so
				 */
				if(connectionTable.containsKey(OutputsRef[i]))
				{
					p.setURI(connectionTable.get(OutputsRef[i]));
				}
				else
				{
					p.setURI(UUID.randomUUID().toString());
					connectionTable.put(OutputsRef[i], p.getURI());
				}
				outParametersConstr[i] = p;
			}//end of for on the outputs

			/*
			 * Initialization of the closure
			 */


			f.InitializeAddSubIntClosure( inValues, inParameters, outParametersConstr);
			AtomicFunctions.add(f);

		}
	}

	public void Initialize()//This function is introduced only to make sure that this class has an updated view of the supported closures
	{
		//	ClosureList = UpdateList(ReferenceServeer);
		//	ClosureList = new AtomicClosure[1];
		//	ClosureList[0] = "AddIntClosure";
		connectionTable = new Hashtable<String, String>();
		setRandomURI();
	}

	public void analyzeChain(String serializedChain)//I still don't decided the type of the output
	{

	}

	public String serializeGraph()
	{
		return "";
	}

	public void resolveHOF()
	{

	}

	public void optimizeGraph()
	{

	}

	public static void main (String[] argv)
	{
		FunctionalChain c = new FunctionalChain();
		c.Initialize();
		c.run2();
	}

	public void run()
	{
		//We currently MUST begin with the mapping of the outputVectorparameters IMPORTANT!!!
		String[] inputs;
		String[] inputsRef;
		Vector<String[]> VecInputs;
		Vector<String> InReferences; 

		MapVectorParameter("a", 2);
		MapVectorParameter("e", 2);

		String[] outputsRef = new String[1];
		inputs = new String[2];
		inputsRef = new String[2];
		VecInputs = new Vector<String[]>();
		InReferences = new Vector<String>();
		
		//String[] inValues = {"1", "2", "3"};
		VecInputs.add(null);
		InReferences.add("a");
		outputsRef = new String[1];
		outputsRef[0] = "d";
		addVecFunctionCall(OntologyVocabulary.AddVectorIntClosure, VecInputs,InReferences, null, null, null, outputsRef);

		inputs[0] = null;
		inputs[1] = "2";
		inputsRef[0] = "a_0";
		inputsRef[1] = null;
		outputsRef = new String[1];
		outputsRef[0] = "b";
		addFunctionCall(OntologyVocabulary.AddIntClosure, inputs,inputsRef, outputsRef );

		inputs[0] = null;
		inputs[1] = "6";
		inputsRef[0] = "a_1";
		inputsRef[1] = null;
		outputsRef = new String[1];
		outputsRef[0] = "c";
		addFunctionCall(OntologyVocabulary.SubIntClosure, inputs,inputsRef, outputsRef );

		inputs[0] = "30";
		inputs[1] = "5";
		inputsRef[0] = null;
		inputsRef[1] = null;
		outputsRef = new String[1];
		outputsRef[0] = "a";
		addVecFunctionCall(OntologyVocabulary.AddSubVecIntClosure, null,null, inputs, inputsRef, outputsRef, null);


		inputs[0] = null;
		inputs[1] = "70";
		inputsRef[0] = "d";
		inputsRef[1] = null;
		outputsRef = new String[1];
		outputsRef[0] = "e";
		addVecFunctionCall(OntologyVocabulary.AddSubVecIntClosure, null,null, inputs, inputsRef, outputsRef, null);

		for(int i = 0; i < connectionTable.size();i++)
		{
			Logger.printFile("ConnectionTable: " + connectionTable.keySet().toArray()[i] + " --> " + connectionTable.values().toArray()[i] + "\n", DebugFileName);
			
		}
		
		Vector<Vector<String>>triples= serializeTriples();
		String SIB_Host = SibConstants.SIB_Host;
		int SIB_Port = SibConstants.SIB_Port;
		String SIB_Name = SibConstants.SIB_Name;
		boolean ack = false;
		this.kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		this.xmlTools = new SSAP_XMLTools(null,null,null);
		kp.setEventHandler(this);
		String xml="";
		xml=kp.join();
		ack=xmlTools.isJoinConfirmed(xml);
		System.out.println("Join confirmed:"+(ack?"YES":"NO")+"\n");
		if(!ack)
		{
			System.out.println("Can not JOIN the SIB");
			return ;
		}
		createSubscriptionsForFinalResults();

		//	Vector<Vector<String>> triples= new Vector<Vector<String>>(); 



		//Here code for subscription to results

		//End subscription to results, now insert the chain on the SIB

		triples= serializeTriplesAsExecutable();
		xml= kp.insert(triples);
		ack=xmlTools.isInsertConfirmed(xml);
		/*
		 *From here there is polling of the final relults to print them, when subscription works the following part of this method can be removed
		 */

		boolean  finished = false;
		while(!finished)// This part will be removed when subscription works
		{
			Sleep(1000);
			for(int i = 0; i < requiredResults.size();i++)
			{
				xml = kp.queryRDF( requiredResults.elementAt(i)[0], OntologyVocabulary.ParHasValue, null ,"URI" , "literal");
				Vector<Vector<String>> rdf_result = xmlTools.getQueryTriple(xml);
				if (rdf_result!=null && rdf_result.size()>0)

					/*
					 * Elementary port record
					 * <String ParameterID, String Port Name, String ClosureURI, String ClosureName>
					 * "The solution from " + closureName + " in output" + "outportName" = value
					 */
				{
					Logger.print("The solution from closure: " + requiredResults.elementAt(i)[3] + 
							" With URI " + requiredResults.elementAt(i)[2] + " on port named " + requiredResults.elementAt(i)[1] +
							" is " + rdf_result.elementAt(0).get(2));
					requiredResults.remove(i);
					if(requiredResults.isEmpty())
					{
						finished = true;
					}

					//					for(int j = 0; j < rdf_result.size();j++)
					//					{
					//						Logger.print("  S:["+rdf_result.elementAt(j).get(0)
					//									+"] P:["+rdf_result.elementAt(j).get(1)
					//									+"] O:["+rdf_result.elementAt(j).get(2)
					//									+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n");
					//					}
				}
			}
		}

		Logger.print("Comuputation terminated!");

	}


	
	public void run2()
	{
		FunctionalChain fc = new FunctionalChain();
		String[] inputs;
		String[] inputsRef;
		Vector<String[]> VecInputs = new Vector<String[]>();
		Vector<String> InReferences;
		String[] outputsRef;
		
		inputs = new String[2];
		inputsRef = new String[2];
		outputsRef = new String[1];
		inputs[0] = "3";
		inputs[1] = null;
		inputsRef[0] = null;
		inputsRef[1] = "in";
		outputsRef[0] = "f_x";
		fc.addFunctionCall(OntologyVocabulary.AddIntClosure, inputs,inputsRef, outputsRef );
		
		inputs = new String[2];
		inputsRef = new String[2];
		outputsRef = new String[1];
		inputs[0] = null;
		inputs[1] = "4";
		inputsRef[0] = "f_x";
		inputsRef[1] = null;
		outputsRef[0] = "f_y";
		fc.addFunctionCall(OntologyVocabulary.SubIntClosure, inputs,inputsRef, outputsRef );
//		It is possible to de-comment this and run a closure chain of three blocks as a functional parameter of the map closure
//		inputs = new String[2];
//		inputsRef = new String[2];
//		outputsRef = new String[1];
//		inputs[0] = "1";
//		inputs[1] = null;
//		inputsRef[0] = null;
//		inputsRef[1] = "f_y";
//		outputsRef[0] = "f_z";
//		fc.addFunctionCall(OntologyVocabulary.AddIntClosure, inputs,inputsRef, outputsRef );
//		
		FunctionalParameter fp = new FunctionalParameter();
		fp.setContent(fc);
		FunctionalParameter[] forInitialize = new FunctionalParameter[1];
		forInitialize[0] = fp;
		                                                             
		
		
		String[] inValues = {"1", "2", "3", "8"};
		VecInputs.add(inValues);
		
		VectorParameter outParameter = new VectorParameter();
		outParameter.setRandomURI();
		//connectionTable.add
		outParameter.setValid(false);
		VectorParameter[] mapoutputs = new VectorParameter[1];
		mapoutputs[0] = outParameter;
		
		addFunctionalFunctionCall(OntologyVocabulary.MapClosure, VecInputs, null, mapoutputs,null, null ,null,forInitialize , null);
		
		Vector<SingleParameter> outs = AtomicFunctions.lastElement().getVectorialOutputPorts()[0].getSignal().getContent();
		
		
		
		for (int i = 0;  i < outs.size();i++)
		{
			connectionTable.put(outs.elementAt(i).getURI(), ("mapout_" + i) );
		}
		
		String SIB_Host = SibConstants.SIB_Host;
		int SIB_Port = SibConstants.SIB_Port;
		String SIB_Name = SibConstants.SIB_Name;
		boolean ack = false;
		this.kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		this.xmlTools = new SSAP_XMLTools(null,null,null);
		kp.setEventHandler(this);
		String xml="";
		xml=kp.join();
		ack=xmlTools.isJoinConfirmed(xml);
		System.out.println("Join confirmed:"+(ack?"YES":"NO")+"\n");
		if(!ack)
		{
			System.out.println("Can not JOIN the SIB");
			return ;
		}
		createSubscriptionsForFinalResults();

		//	Vector<Vector<String>> triples= new Vector<Vector<String>>(); 



		//Here code for subscription to results

		//End subscription to results, now insert the chain on the SIB
        Vector<Vector<String>> triples;
		triples= serializeTriplesAsExecutable();
		xml= kp.insert(triples);
		ack=xmlTools.isInsertConfirmed(xml);
		/*
		 *From here there is polling of the final relults to print them, when subscription works the following part of this method can be removed
		 */

		boolean  finished = false;
		while(!finished)// This part will be removed when subscription works
		{
			Sleep(1000);
			for(int i = 0; i < requiredResults.size();i++)
			{
				xml = kp.queryRDF( requiredResults.elementAt(i)[0], OntologyVocabulary.ParHasValue, null ,"URI" , "literal");
				Vector<Vector<String>> rdf_result = xmlTools.getQueryTriple(xml);
				if (rdf_result!=null && rdf_result.size()>0)

					/*
					 * Elementary port record
					 * <String ParameterID, String Port Name, String ClosureURI, String ClosureName>
					 * "The solution from " + closureName + " in output" + "outportName" = value
					 */
				{
					Logger.print("The solution from closure: " + requiredResults.elementAt(i)[3] + 
							" With URI " + requiredResults.elementAt(i)[2] + " on port named " + requiredResults.elementAt(i)[1] +
							" is " + rdf_result.elementAt(0).get(2));
					requiredResults.remove(i);
					if(requiredResults.isEmpty())
					{
						finished = true;
					}

					//					for(int j = 0; j < rdf_result.size();j++)
					//					{
					//						Logger.print("  S:["+rdf_result.elementAt(j).get(0)
					//									+"] P:["+rdf_result.elementAt(j).get(1)
					//									+"] O:["+rdf_result.elementAt(j).get(2)
					//									+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n");
					//					}
				}
			}
		}

		Logger.print("Comuputation terminated!");
		
		for(int i = 0; i < connectionTable.size();i++)
		{
			Logger.printFile("ConnectionTable: " + connectionTable.keySet().toArray()[i] + " --> " + connectionTable.values().toArray()[i] + "\n", DebugFileName);
			
		}
		
		
		for (int i = 0; i < fc.AtomicFunctions.size();i++)
		{
			fc.AtomicFunctions.elementAt(i).WriteTriplesOnFile();
		}
		
		
		
		
		
		
		
	}

	public void run1()
	{
		String[] inputs = new String[2];
		String[] inputsRef = new String[2];
		//First Element of the chain 2+3, the result is stored in "a"
		String[] outputsRef = new String[1];
		inputs = new String[2];
		inputsRef = new String[2];
		Vector<String[]> VecInputs = new Vector<String[]>();
		Vector<String> InReferences = new Vector<String>();
		String[] inValues = {"1", "2", "3"};
		VecInputs.add(inValues);
		outputsRef = new String[1];
		outputsRef[0] = "a";
		addVecFunctionCall(OntologyVocabulary.AddVectorIntClosure, VecInputs,InReferences, null, null, null, outputsRef);
		inputs[0] = null;
		inputs[1] = "2";
		inputsRef[0] = "a";
		inputsRef[1] = null;
		outputsRef = new String[1];
		outputsRef[0] = "b";
		addFunctionCall(OntologyVocabulary.AddIntClosure, inputs,inputsRef, outputsRef );
		Vector<Vector<String>>triples= serializeTriplesAsExecutable();
		String SIB_Host = SibConstants.SIB_Host;
		int SIB_Port = SibConstants.SIB_Port;
		String SIB_Name = SibConstants.SIB_Name;
		boolean ack = false;
		this.kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		this.xmlTools = new SSAP_XMLTools(null,null,null);
		kp.setEventHandler(this);
		String xml="";
		xml=kp.join();
		ack=xmlTools.isJoinConfirmed(xml);
		System.out.println("Join confirmed:"+(ack?"YES":"NO")+"\n");
		if(!ack)
		{
			System.out.println("Can not JOIN the SIB");
			return ;
		}
		createSubscriptionsForFinalResults();

		//	Vector<Vector<String>> triples= new Vector<Vector<String>>(); 



		//Here code for subscription to results

		//End subscription to results, now insert the chain on the SIB

		triples= serializeTriplesAsExecutable();
		xml= kp.insert(triples);
		ack=xmlTools.isInsertConfirmed(xml);
		/*
		 *From here there is polling of the final relults to print them, when subscription works the following part of this method can be removed
		 */

		boolean  finished = false;
		while(!finished)// This part will be removed when subscription works
		{
			Sleep(1000);
			for(int i = 0; i < requiredResults.size();i++)
			{
				xml = kp.queryRDF( requiredResults.elementAt(i)[0], OntologyVocabulary.ParHasValue, null ,"URI" , "literal");
				Vector<Vector<String>> rdf_result = xmlTools.getQueryTriple(xml);
				if (rdf_result!=null && rdf_result.size()>0)

					/*
					 * Elementary port record
					 * <String ParameterID, String Port Name, String ClosureURI, String ClosureName>
					 * "The solution from " + closureName + " in output" + "outportName" = value
					 */
				{
					Logger.print("The solution from closure: " + requiredResults.elementAt(i)[3] + 
							" With URI " + requiredResults.elementAt(i)[2] + " on port named " + requiredResults.elementAt(i)[1] +
							" is " + rdf_result.elementAt(0).get(2));
					requiredResults.remove(i);
					if(requiredResults.isEmpty())
					{
						finished = true;
					}

					//					for(int j = 0; j < rdf_result.size();j++)
					//					{
					//						Logger.print("  S:["+rdf_result.elementAt(j).get(0)
					//									+"] P:["+rdf_result.elementAt(j).get(1)
					//									+"] O:["+rdf_result.elementAt(j).get(2)
					//									+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n");
					//					}
				}
			}
		}

		Logger.print("Comuputation terminated!");

	}
	public void run0()
	{
		String[] inputs = new String[2];
		String[] inputsRef = new String[2];
		//First Element of the chain 2+3, the result is stored in "a"
		inputs[0] = "2";
		inputs[1] = "3";
		inputsRef[0] = null;
		inputsRef[1] = null;
		String[] outputsRef = new String[1];
		outputsRef[0] = "a";
		addFunctionCall(OntologyVocabulary.AddIntClosure, inputs,inputsRef, outputsRef );
		//Second Element of the chain 4 + a, the result is stored in b; b is the final result to which I have to be subscribed
		inputs = new String[2];
		inputsRef = new String[2];
		inputs[0] = "4";
		inputs[1] = null;
		inputsRef[0] = null;
		inputsRef[1] = "a";
		outputsRef = new String[1];
		outputsRef[0] = "b";
		addFunctionCall(OntologyVocabulary.AddIntClosure, inputs,inputsRef, outputsRef );
		inputs = new String[2];
		inputsRef = new String[2];
		inputs[0] = "10";
		inputs[1] = "4";
		inputsRef[0] = null;
		inputsRef[1] = null;
		outputsRef = new String[1];
		outputsRef[0] = "c";
		addFunctionCall(OntologyVocabulary.SubIntClosure, inputs,inputsRef, outputsRef );
		inputs = new String[3];
		inputsRef = new String[3];
		inputs[0] = null;
		inputs[1] = "1";
		inputs[2] = null;
		inputsRef[0] = "a";
		inputsRef[1] = null;
		inputsRef[2] = "c";
		outputsRef = new String[1];
		outputsRef[0] = "d";
		addFunctionCall(OntologyVocabulary.Add3IntClosure, inputs,inputsRef, outputsRef );
		inputs = new String[2];
		inputsRef = new String[2];
		inputs[0] = null;
		inputs[1] = null;
		inputsRef[0] = "b";
		inputsRef[1] = "d";
		outputsRef = new String[2];
		outputsRef[0] = "e";
		outputsRef[1] = "f";
		addFunctionCall(OntologyVocabulary.AddSubIntClosure, inputs,inputsRef, outputsRef );

		/*
		 * This function is supposed to be called when all the functions are inserted in the chain and the chain is ready to be sent
		 * somewhere. In this case we want to understand from the content of the chain which are the subscriptions to be done
		 * 
		 */

		String SIB_Host = SibConstants.SIB_Host;
		int SIB_Port = SibConstants.SIB_Port;
		String SIB_Name = SibConstants.SIB_Name;
		boolean ack = false;
		this.kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		this.xmlTools = new SSAP_XMLTools(null,null,null);
		kp.setEventHandler(this);
		String xml="";
		xml=kp.join();
		ack=xmlTools.isJoinConfirmed(xml);
		System.out.println("Join confirmed:"+(ack?"YES":"NO")+"\n");
		if(!ack)
		{
			System.out.println("Can not JOIN the SIB");
			return ;
		}
		createSubscriptionsForFinalResults();

		Vector<Vector<String>> triples= new Vector<Vector<String>>(); 



		//Here code for subscription to results

		//End subscription to results, now insert the chain on the SIB

		triples= serializeTriplesAsExecutable();
		xml= kp.insert(triples);
		ack=xmlTools.isInsertConfirmed(xml);
		/*
		 *From here there is polling of the final relults to print them, when subscription works the following part of this method can be removed
		 */

		boolean  finished = false;
		while(!finished)// This part will be removed when subscription works
		{
			Sleep(1000);
			for(int i = 0; i < requiredResults.size();i++)
			{
				xml = kp.queryRDF( requiredResults.elementAt(i)[0], OntologyVocabulary.ParHasValue, null ,"URI" , "literal");
				Vector<Vector<String>> rdf_result = xmlTools.getQueryTriple(xml);
				if (rdf_result!=null && rdf_result.size()>0)

					/*
					 * Elementary port record
					 * <String ParameterID, String Port Name, String ClosureURI, String ClosureName>
					 * "The solution from " + closureName + " in output" + "outportName" = value
					 */
				{
					Logger.print("The solution from closure: " + requiredResults.elementAt(i)[3] + 
							" With URI " + requiredResults.elementAt(i)[2] + " on port named " + requiredResults.elementAt(i)[1] +
							" is " + rdf_result.elementAt(0).get(2));
					requiredResults.remove(i);
					if(requiredResults.isEmpty())
					{
						finished = true;
					}

					//					for(int j = 0; j < rdf_result.size();j++)
					//					{
					//						Logger.print("  S:["+rdf_result.elementAt(j).get(0)
					//									+"] P:["+rdf_result.elementAt(j).get(1)
					//									+"] O:["+rdf_result.elementAt(j).get(2)
					//									+"] Otype:["+rdf_result.elementAt(j).get(3)+"]\n");
					//					}
				}
			}
		}

		Logger.print("Comuputation terminated!");
	}
	public void createSubscriptionsForFinalResults()
	{
		//Vector containing a superset of the necessary subscriptions, it will be filtered to obtain required subscriptions 
		Vector<String[]> PartialrequiredResults = new Vector<String[]>();
		String[] ElementarParameterRecord = new String[4];
		Vector<String> InputParametrsID = new Vector<String>();
		for (int i = 0; i < this.AtomicFunctions.size();i++)
		{
			SingleParPort[] OutputPorts = AtomicFunctions.elementAt(i).getOutputPorts();
			VectorParPort[] vport = AtomicFunctions.elementAt(i).getVectorialOutputPorts();
			if(OutputPorts!= null)
			{
			for(int j = 0; j < OutputPorts.length; j++)
			{

				/*
				 * Elementary port record
				 * <String ParameterID, String Port Name, String ClosureURI, String Closure Name>
				 * too much information? It is all that I need to know (I believe) about a parameter, also the only parameterID 
				 * is sufficient for subscription and getting the result
				 */
				SingleParPort port = OutputPorts[j];

				ElementarParameterRecord = new String[4];
				ElementarParameterRecord[0] = port.getSignal().getURI();
				ElementarParameterRecord[1] = port.getName();
				ElementarParameterRecord[2] = AtomicFunctions.elementAt(i).getURI();
				ElementarParameterRecord[3] = AtomicFunctions.elementAt(i).getName();
				PartialrequiredResults.add(ElementarParameterRecord);
			}
			}
			SingleParPort[] InputPorts = AtomicFunctions.elementAt(i).getInputPorts();
			if (InputPorts!=null)
			{
				for(int j = 0; j < InputPorts.length; j++)
				{
					InputParametrsID.add(InputPorts[j].getSignal().getURI());	
				}
			}
			if(vport!= null)
			{
				for(int j = 0; j< vport.length;j++)
				{

					Vector<SingleParameter> outArray = vport[j].getSignal().getContent();
					if(outArray != null)
					{
						for(int parCounter = 0; parCounter < outArray.size();parCounter++)
						{
							ElementarParameterRecord = new String[4];
							SingleParameter singlePar = outArray.elementAt(parCounter);
							ElementarParameterRecord[0] = singlePar.getURI();
							ElementarParameterRecord[1] = vport[j].getName() + parCounter;
							ElementarParameterRecord[2] = AtomicFunctions.elementAt(i).getURI();
							ElementarParameterRecord[3] = AtomicFunctions.elementAt(i).getName();
							PartialrequiredResults.add(ElementarParameterRecord);
						}
					}
				}
			}
		}

		for(int i = 0; i < PartialrequiredResults.size();i++)
		{
			if(!InputParametrsID.contains(PartialrequiredResults.elementAt(i)[0]))
			{
				requiredResults.add(PartialrequiredResults.elementAt(i));
			}
		}

		/*
		 * What does the subscription handler:
		 * The subscriptions are made to each (requiredResults.elementAT[i][0], any any) or also (,,hasValue,any)
		 * When the solution is printed we will be able to write: "The solution from " + closureName + " in output" + "outportName" = value
		 */


		for(int i = 0; i < requiredResults.size();i++)
		{
			String xml = kp.subscribeRDF( requiredResults.elementAt(i)[0], OntologyVocabulary.ParHasValue, null , "literal");
			System.out.println("s= " + requiredResults.elementAt(i)[0] + "p = " +  OntologyVocabulary.ParHasValue + "o = " );
			if(xml==null || xml.length()==0)
			{
				System.out.println("Subscription message NOT valid!\n");
				//		break;
			}
			System.out.println("Subscribe confirmed:"+(this.xmlTools.isSubscriptionConfirmed(xml)?"YES":"NO")+"\n");
			if(!this.xmlTools.isSubscriptionConfirmed(xml)){break;}
			String  subID_1=this.xmlTools.getSubscriptionID(xml);
			System.out.println ("RDF Subscribe initial result:"+xml.replace("\n", "")+"\nSubscription ID = " + subID_1);
		}




	}

	public AtomicClosure getClosureFromListByURI(String uri)
	{
		AtomicClosure a;
		for(int i = 0; i < AtomicFunctions.size();i++)
		{
			a = AtomicFunctions.elementAt(i);
			if(a.getURI().equals(uri))
			{
				return a;
			}
		}
		return null;
	}

	public Vector<Vector<String>> serializeTriplesAsExecutable()
	{
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.AtomicClosure, "URI", "URI"));
		for(int i = 0; i < AtomicFunctions.size();i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ChainHasFunction, AtomicFunctions.elementAt(i).getURI(), "URI", "URI"));
			triples = concatTriples(triples , AtomicFunctions.elementAt(i).SerializeTriples());
		}

		return triples;
	}
	
	public Vector<Vector<String>> serializeTriplesAsFunctionalParameter()
	{
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.ComputationalChain, "URI", "URI"));
		for(int i = 0; i < AtomicFunctions.size();i++)
		{
			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.FunctionalParameterChainHasFunction, AtomicFunctions.elementAt(i).getURI(), "URI", "URI"));
			triples = concatTriples(triples , AtomicFunctions.elementAt(i).SerializeTriples());
		}

		return triples;
	}
	
	public Vector<Vector<String>> serializeTriples()
	{
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = new Vector<Vector<String>>();
		if(isFunctionalParameterValue)
		{
			return this.serializeTriplesAsFunctionalParameter();
		}
		else
		{
			return this.serializeTriplesAsExecutable();
		}
		
	}

	public Vector<Vector<String>> concatTriples(Vector<Vector<String>> acc,Vector<Vector<String>> newtriples)
	{
		Vector<Vector<String>> out = new Vector<Vector<String>>();
		out = acc;
		for (int i = 0; i < newtriples.size();i++)
		{
			out.add(newtriples.elementAt(i));
		}
		return out;
	}
	@Override
	public void kpic_SIBEventHandler(String xml) {
		//At moement only prints the events
		String event_output="\nEVENT_______________________________________________-\n";

		event_output=event_output+"The message:"+xml.replace("\n", "")+"\n";
		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
		Vector<Vector<String>> triples = xmlTools.getNewResultEventTriple(xml);


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
		Logger.print("EventOutput = " + event_output +"\n");


	}

	private void Sleep(int timeMs)
	{
		try
		{
			Thread.sleep(timeMs);
		}
		catch(Throwable e)
		{
			Logger.print("Exception " + e.toString());
			e.printStackTrace();
			//.printFile(e.printStackTrace()., DebugFileName);

		}
	}

	public Vector<String> MapVectorParameter(String VectorParameterIdentifier, int numberOfSingleParameters)
	{
		Vector<String> temp = new Vector<String>();
		String randomID = "";
		
		connectionTable.put(VectorParameterIdentifier, UUID.randomUUID().toString());
		
		for(int i = 0;  i < numberOfSingleParameters;i++)
		{
			temp.add(VectorParameterIdentifier + "_" + i);
			randomID = UUID.randomUUID().toString();
			connectionTable.put(VectorParameterIdentifier + "_" + i, randomID);
		}
		VectorParameterMapping.put(VectorParameterIdentifier, temp);
		return temp;
	}

	public String getParameterinsdideVector(String VectorReferenceIdentifier, int positionInVector)
	{
		String searchField = VectorReferenceIdentifier + positionInVector;
		return connectionTable.get((String) searchField);
	}


}
