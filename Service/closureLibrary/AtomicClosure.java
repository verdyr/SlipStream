package closureLibrary;

import java.util.Vector;


public interface AtomicClosure {
	
	public Vector<Vector<String>> SerializeTriples();
	public String getURI();
	public SingleParPort[] getInputPorts();
	public SingleParPort[] getOutputPorts();
	public Parameter[] getInputParameter();
	public Parameter[] getOutputParameter();
	public String getName();
	public VectorParPort[] getVectorialInputPorts();
	public VectorParPort[] getVectorialOutputPorts();
	public void setVectorOutputPortReference(Vector<String[]> VectorOutputPortDescriptor);
	public void WriteTriplesOnFile();
	
	//public void setInputPort(String[] inputPortDescriptor)
	
	public Capabilities[] getCapabilities(); // get capabilities, from specified vector
	public Capabilities[] setCapabilities(); // set capabilities, to specified vector 
	
	

}
