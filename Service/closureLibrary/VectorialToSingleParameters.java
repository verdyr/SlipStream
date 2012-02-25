/*
 * This class is currently in stand by because I decided to realize its funcitonality at level of functional chain eand not at closure level!!! Perhaps will be important again 
 * as part of the mao , but it is not sure
 */


//package closureLibrary;
//
//import java.util.UUID;
//import java.util.Vector;
//
//import sofia_kp.SSAP_XMLTools;
//
//
///*
// * This closure is fundamental so a bit of notes are needed
// * It is atypical because the number of output ports is not a priori known, but this probably will not be a problem
// * When the Vector Parameter at the input port will have a dimension the outputPorts can be instantiated
// * The executor should be subscribed to each of the elements of the input in order to make valid the element at the output as soon as possible
// * obiously this means that the writeback of the functions with vectorial output should be performed by the function itself during execution
// * and not more by the function executor when the computation has been terminated
// */
//
//
//public class VectorialToSingleParameters {
//
//
//	private String URI;//This has sense only when the computation is sent to the SIB, the assumption made in this file is that if I send two times a computation I have two different URIs 
//	//public  SingleParPort[] InputPorts ;
//	public VectorParPort[] InputPortsVec;//only one for this Closure
//	public  SingleParPort[] OutputPorts;//Cardinality not a priory defined
//	//public VectorParPort[] OutputPortsVec;
//	public  static String Name = OntologyVocabulary.AddVectorIntClosure;
//
//	//private static String[]  InputSinglePortNames = {"In1"} ;
//	private static String[] OutputSinglePortNames; //Here no sense to initialize only declaration needed 
//
//	//private static String[] InputSinglePortTypes = {"int"} ;
//	public static String[] OutputSinglePortTypes; //Here no sense to initialize only declaration needed 
//
//	private static String[]  InputVectorPortNames = {"In1"} ;//Ok to initialize it
//	public static String[] OutputVectorPortNames = null ;//Not present in this closure
//
//	public static String[] InputVectorPortTypes;//Here no sense to initialize only declaration needed 
//
//	//	private static String[] OutputVectorPortTypes = {"int"} ;
//
//
//	String DebugFileName = "/home/verdyr/workspace/TestCloud1/src/VectorialToSingleParameters";
//
//	/*
//	 *  Less thing than usual are knew in this closure so the initialization method will be more complicated
//	 */
//	public String getName()
//	{
//		return AddVectorIntClosure.Name;
//	}
//
//	public void setURI(String Uri)
//	{
//		this.URI = Uri;
//	}
//
//	public void setRandomURI()
//	{
//		this.URI = UUID.randomUUID().toString();
//	}
//
//	public String getURI()
//	{
//		return this.URI;
//	}
//
//	/*
//	 *Constructor, almost nothing can be said a priori, only the input port name can be defined and some constant like the URI and the name of the closure
//	 */
//	public VectorialToSingleParameters()	
//	{
//		setRandomURI();
//		InputPortsVec = new VectorParPort[1];
//		VectorParPort In1 = new VectorParPort();
//		In1.setName(VectorialToSingleParameters.InputVectorPortNames[0]);
//		In1.setSignal(null);
//		InputPortsVec[0] = In1;
//	}
//
//	/*
//	 * In the initialization : Type of the input, dimension of the input, number and type and names of the output.
//	 *  Also the putputportvector must be created with the right dimension.
//	 *  The routing module which connects the ouputs of this this closure with hte inputs of the following MUST know
//	 *   the number of the outputs and so also the dimension of the input Vector
//	 */
//
//	public void initializeVectorialToSingleParameter(Vector<String[]> VectorValues, VectorParameter[] inputVectorParameter, SingleParameter[] outputSingleParameter, String Datatype)
//	{
//		int dimension  = VectorValues.size();
//
//		this.OutputPorts = new SingleParPort[dimension];
//		this.OutputSinglePortNames = new String[dimension];
//		this.OutputSinglePortTypes = new String[dimension];
//
//		for(int i = 0; i  < dimension; i++)
//		{
//			this.OutputSinglePortNames[i] = "Out_" + i;
//			this.OutputSinglePortTypes[1] = Datatype;
//		}
//
//		for(int i = 0; i < AddVectorIntClosure.getOutputSinglePortNames().length;i++)
//		{
//
//			SingleParameter pout;
//			if (outputSingleParameter.equals(null))//If I have not an output parameter from the constructor (so no reference) I create one Randomly
//			{
//				pout = new SingleParameter();
//				pout.setURI(UUID.randomUUID().toString());
//				pout.setType(OutputSinglePortTypes[i]); 
//			}
//			else //I have passed to the initialization method a Parameter of the output containing only the reference and so the URI
//			{
//				pout = outputSingleParameter[i];
//				pout.setType(OutputSinglePortTypes[i]);//Unuseful if  we suppose to pass also the datatype
//			}
//			this.OutputPorts[i].setSignal(pout);
//			//		pout.setWritingAtomicClosure(this);
//		}
//
//		//Vector input
//
//		VectorParameter p_v;
//		for(int i = 0; i < AddVectorIntClosure.getInputVectorPortNames().length;i++)
//		{   
//			p_v = new VectorParameter();
//			if((inputVectorParameter== null)||(inputVectorParameter[i]==null))//If I have  not an input parameter URI passed to this initialisation I create one random
//			{
//				p_v.setRandomURI();
//				p_v.setType(Datatype);
//
//				if (!VectorValues.elementAt(i).equals(null)) //If I know the values of the parameters I  set the value and I put Valid == true
//				{
//					p_v.setContent(VectorValues.elementAt(i), Datatype);
//					p_v.setValid(true);
//				}
//				else  //The parameter has not a constant value to be assigned
//				{
//					p_v.setValid(false);
//				}
//
//			}
//			else//I have an input parameter URI in the constructor and I use it
//			{
//				/*
//				 * Here I consider that who calls this initialization gives me the values of the inputs and the references in the
//				 * the parameters, the initialization assigns the value to the parameters
//				 * */
//				p_v = inputVectorParameter[i]; //So simpli a VectorialParameter provided with a URI
//				if (!(VectorValues.elementAt(i) == null)) //If I know the value of the parameter I  set the value and I put Valid == true
//				{
//					p_v.setContent(VectorValues.elementAt(i), Datatype);
//					p_v.setValid(true);
//				}
//				else  //The parameter has not a constant value to be assigned
//				{
//					p_v.setValid(false);
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
//			 * 3) not random with assigned value and valid
//			 * 4) not random without value and invalid
//			 * At moment these are all the situations managed
//			 */
//			InputPortsVec[i].setSignal(p_v);
//			//p_v.addInputToAtomicClosure(this);
//
//		}
//	}
//	
//	//Now I need a riun method that is done in general when the input vevctor is ready, but the most important this is
//	//to create a speculative run method in the function executor which assigns to the the correct output the value as soon as it is available.l
//	public void run()
//	{
//		//Here should be some type check, perhaps it has been made before in the computational chain. The check is for understanding 
//		//if the parameter is ready or not
//		//At moment the cast is made twice, perhaps it is useful only here
//		
//		if (this.OutputPorts[0] == null)//This controls coud be avoided in a stable release
//		{
//			SingleParPort temp = new SingleParPort();
//			temp.setSignal(new SingleParameter());
//			this.OutputPorts[0] = temp;
//		}
//		else if(this.OutputPorts[0].getSignal() == null)
//		{
//			this.OutputPorts[0].setSignal(new SingleParameter());
//		}
//		Vector<SingleParameter> input = this.InputPortsVec[0].getSignal().getContent();
//		for(int i = 0; i < input.size();i++)
//		{
//			this.OutputPorts[i].getSignal().setValue(input.elementAt(i).getValueForTriple());
//			this.OutputPorts[i].getSignal().setValid(true);
//		}
//	}
//	
//	public Vector<Vector<String>> SerializeTriples()
//	{
//		//Logger.printFile(this.printClosure(), DebugFileName);
//		
//		//Triples
//		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null, null, null);
//		Vector<Vector<String>> triples = new Vector<Vector<String>>();
//		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.type, OntologyVocabulary.AtomicClosure, "URI", "URI"));
//		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ClosureHasName,OntologyVocabulary.VectorialToSingleParameters, "URI", "URI"));
//		triples.add(xmlTools.newTriple(this.URI,   OntologyVocabulary.ComputationHasFunctionalBehaviour,OntologyVocabulary.VectorialToSingleParameters, "URI", "URI"));
//		
//		for(int i = 0; i < this.InputPortsVec.length;i++)
//		{
//			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasInputPort, this.InputPortsVec[i].getURI(), "URI", "URI"));
//			triples = TriplesUtilities.concatTriples(triples, this.InputPortsVec[i].getTriples());
//		}
//		for(int i = 0; i < this.OutputPorts.length;i++)
//		{
//			triples.add(xmlTools.newTriple(this.URI, OntologyVocabulary.ClosureHasOutputPort, this.OutputPorts[i].getURI(), "URI", "URI"));
//			triples = TriplesUtilities.concatTriples(triples, this.OutputPorts[i].getTriples());
//		}
//		
//		//This prints the triples in the debug file
//		Logger.printFile("Writing Closure triples\n" +
//		         "Closure: " + this.getURI() + "\n" +
//		         "Triples:\n",DebugFileName);
//		for (int i = 0; i < triples.size();i++)
//		{
//			
//			Vector<String> Triple = triples.elementAt(i);
//			{
//				
//					Logger.printFile("sub = " + Triple.elementAt(0) + "; pred = " + Triple.elementAt(1) + "; obj = " + Triple.elementAt(2) + ";\n",DebugFileName);
//				
//			}
//		}
//		
//		//End Debug
//		return triples;
//
//	}
//	
//	
//	
//}
