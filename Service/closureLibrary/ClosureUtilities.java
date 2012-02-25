package closureLibrary;

public class ClosureUtilities {

	public static int mapInputPortNameToPortIndex(String FunctionName, String PortName)
	{
		int temp = 42;//dummy value
		if (FunctionName.equals(OntologyVocabulary.AddIntClosure))
		{
			if(PortName.equals("In1"))
			{
				temp =  0;
			} 
			else if(PortName.equals("In2"))
			{
				temp = 1;
			}
		}
		else if(FunctionName.equals(OntologyVocabulary.SubIntClosure))
		{
			if(PortName.equals("In1"))
			{
				temp = 0;
			} 
			else if(PortName.equals("In2"))
			{
				temp = 1;
			}
		}
		
		return temp;
	}
	
	public static int mapOutputPortNameToPortIndex(String FunctionName, String PortName)
	{
		
		int temp = 42;//dummy value
		if (FunctionName.equals(OntologyVocabulary.AddIntClosure))
		{
			if(PortName.equals("Out"))
			{
				temp = 0;
			} 

		}
		else if (FunctionName.equals(OntologyVocabulary.SubIntClosure))
		{
			if(PortName.equals("Out"))
			{
				temp = 0;
			} 

		}
		
		return temp;
	}



}
