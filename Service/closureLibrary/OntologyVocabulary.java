package closureLibrary;

public class OntologyVocabulary {
	
	public static String namespace = "http://www.semanticweb.org/ontologies/2010/11/TestCloud.owl#";
	
	//Classes
	public static String ComputationalChain    = namespace + "ComputationalCahain";
	public static String Parameter             = namespace + "Parameter";
	public static String SingleParameter       = namespace + "SingleParameter";//InputandoutputParameter distinction is not essential
	public static String Port                  = namespace + "Port";
	public static String SingleParPort         = namespace + "SingleParPort";
	public static String functionalParPort     = namespace + "functionalParPort";
	
//
//	public static String InputPort             = namespace + "InputPort";
//	public static String OutputPort            = namespace + "OutputPort";
	public static String Computation           = namespace + "Computation";//this is the class whose instances are the computations without name
	public static String HighOrderFunction     = namespace + "HighOrderFunction";
    public static String VectorialParameter    = namespace + "VectorialParameter";
	public static String AtomicClosure         = namespace + "AtomicClosure";//this is the class whose instances are the named closures belonging to the library
	public static String FunctionalParameter   = namespace + "FunctionalParameter";
	public static String FunctionalBehaviour   = namespace + "FunctionalBehaviour";//this is the class is used to associate behaviour to unnamed computations
	public static String VectorParPort         = namespace + "VectorParPort";//this is the class is used to associate behaviour to unnamed computations
	
	
	//DatatypeProperties 
	public static String ParHasDatatype          = namespace + "ParHasDatatype";
	public static String ParHasValue             = namespace + "ParHasValue";
	public static String ParIsValid              = namespace + "ParIsValid";
	public static String PortHasName             = namespace + "PortHasName";
	public static String PortHasDatatype         = namespace + "PortHasDatatype";
	public static String ClosureHasName          = namespace + "ClosureHasName";
	public static String VectorParhasDimension   = namespace + "VectorParhasDimension"; 
	public static String ParhasPositionInVector  = namespace + "ParhasPositionInVector"; 


	//ObjectProperties 
	public static String PortAttachedToPar                        =  namespace + "PortAttachedToPar";
	public static String ClosureHasInputPort                      =  namespace + "ClosureHasInputPort";
	public static String ClosureHasOutputPort                     =  namespace + "ClosureHasOutputPort";
	public static String ChainHasFunction                         =  namespace + "ChainHasFunction";
	public static String ParHasFunctionValue                      =  namespace + "ParHasFunctionValue";
	public static String ComputationHasFunctionalBehaviour        =  namespace + "ComputationHasFunctionalBehaviour";
	public static String VectorParhasParameter                    =  namespace + "VectorParhasParameter"; 
	//public static String ComposedFunctionHasFunction              =  namespace + "ComposedFunctionHasFunction"; 
	public static String FunctionalParameterHasFunctionalChain    =  namespace + "FunctionalParameterHasFunctionalChain";

	public static String FunctionalParameterChainHasFunction    =  namespace + "FunctionalParameterChainHasFunction"; 
	
	

	public static String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	
	//Instances
	
	public static String AddIntClosure               = namespace + "AddIntClosure";  //Instance of closure type
	public static String SubIntClosure               = namespace + "SubIntClosure";  //Instance of closure type
	public static String Add3IntClosure              = namespace + "Add3IntClosure";  //Instance of closure type
	public static String AddSubIntClosure            = namespace + "AddSubIntClosure";  //Instance of closure type
	public static String AddVectorIntClosure         = namespace + "AddVectorIntClosure";  //Instance of closure with vectorial input and single output
	//public static String VectorialToSingleParameters = namespace + "VectorialToSingleParameters";  //Instance of closure with vectorial input and single output
	public static String AddSubVecIntClosure         = namespace + "AddSubVecIntClosure";  //Instance of closure with vectorial input and single output
	public static String MapClosure                  = namespace + "MapClosure";  //An Higher order closure with a functional input a vectorial input and a vectorial poutput
	
	
	
	
    
	//Literal values
	public static String ValidParameter = "ValidParameter";
	public static String InvalidParameter = "InvalidParameter";

}
