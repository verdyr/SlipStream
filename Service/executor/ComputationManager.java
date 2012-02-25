package executor;

import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import closureChain.FunctionalChain;
import closureLibrary.Logger;
import closureLibrary.OntologyVocabulary;
import closureLibrary.SibConstants;
import sofia_kp.KPICore;
import sofia_kp.SSAP_XMLTools;

public class ComputationManager implements sofia_kp.iKPIC_subscribeHandler{

	private KPICore kp;
	private SSAP_XMLTools xmlTools;
	private String debugFileName= "/home/verdyr/workspace/TestCloud1/src/mainProgram_deb.txt";

	public static void main (String[] argv)
	{
		ComputationManager Program = new ComputationManager();
		Program.test();

	}




	public void test()
	{
		Logger.printFile("Test",this.debugFileName);
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



		xml = kp.subscribeRDF( null, OntologyVocabulary.ChainHasFunction, null , "URI");
		System.out.println("s= null p = " + OntologyVocabulary.ChainHasFunction + "o = null" );
		if(xml==null || xml.length()==0)
		{
			System.out.println("Subscription message NOT valid!\n");
			//		break;
		}
		System.out.println("Subscribe confirmed:"+(this.xmlTools.isSubscriptionConfirmed(xml)?"YES":"NO")+"\n");
		
		String  subID_1=this.xmlTools.getSubscriptionID(xml);
		System.out.println ("RDF Subscribe initial result:"+xml.replace("\n", "")+"\nSubscription ID = " + subID_1);
		//		//vmware ip = 192.168.0.102
		//		String SIB_Host = "192.168.0.102";
		//		int SIB_Port = 10010;
		//		String SIB_Name = "X";
		//		KPICore kp = null;
		//		kp = new KPICore(SIB_Host, SIB_Port, SIB_Name);
		//		SSAP_XMLTools xmlTools = new SSAP_XMLTools(null,null,null);
		//		System.out.println(kp.join());
		//		boolean ack = false;
		//		
	}

	public void testadvanced()
	{
		//1		JoinAndSubScribe()//The executor join the SIB and subscribes to the creation of computationType


		//2     When an event arrives the graph has to be queried to undestand what's going on, a model of the computation is created and according to datflow 
		//      rules the execution is scheduled and performed.
		//2.1 When subroutines ends other events arrive to the SIB so the executor is subscribed to different kinds of events perhaps parameter subscription is after 
		//the first event of computation is created
		//If the event is a parameter the the executor probably know which execution is fired or where to set a that this task has been done

		//3 Who generates the graphs? How to test feasability?

		// given a closure in th language it is a series of instructions with name known. The input parameter are inside the function call,
		//The output parameters are somewhere in assignements (assignements = intermediate value or output) we can assume there is only one output for closure.
		//Each instruction can be Closurelibrary.call_closurename_(parameter_1, parameter2, ecc)
		//The connection between these function is etter to be made in a known context: something similar to the monad concept.
		//This context is what will be serialized

		//not interested in closure recognition (less priority)
	}




	@Override
	public void kpic_SIBEventHandler(String xml) {
		
           
		/*
		 * This is the core part of he server side, how it works?
		 * It is subscribed to functional chains, when a new one arrives it has to perform multiple queries in order to reconstruct
		 *  the computation; a possible algorithm follows:
		 *  1) I find all the  elementary functions attached to the node of the chain (1 query)
		 *  2) From the name of this functions I can understand which functionality to execute (1 query for each function found)
		 *  3) Then I have to ask for the formal inputs and outputs because I'm not putting in order the arguments. (2 queries for each function)
		 *  4) At this point I need the reference for the parameters attached to each input and output(lot of queries Fi*NIi + Fi*NOi = ZI +ZO = Z )
		 *  5) Each parameter has a name which allows to understand the meaning of that input in the context of the single function (Z queries)
		 *  6) Each parameter has a value  and a validity bit that need to be analyzed (ZI queries)
		 *  7) Queries finished, all the functions with the  input valid values can run asap while the other ones will start only when the missing parameters are filled by some other function in the chain.
		 *  8) If I give to each function its inputs and the URI to which relate the output, each execution is autonomous without centralization, but is this the desired behavior?
		 *  9) In scenarios like "vectorial output" and "function of function" it is possible that a central management of the computation is required and not optional
		 *    
		 */
		
		String event_output="\nEVENT_______________________________________________-\n";

		event_output=event_output+"The message:"+xml.replace("\n", "")+"\n";
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
	    Logger.printFile(event_output,this.debugFileName);
		ChainExecutor chainExecutor= new ChainExecutor();
        chainExecutor.setChainURI(xmlTools.triple_getSubject(triples.elementAt(0)));
		new Thread(chainExecutor).run();


	}
	
	public void printDebug (String debug)
	{
		
		try
		{
			FileWriter fw = new FileWriter(this.debugFileName);
			fw.write(debug);
			fw.flush();
		}
		catch(Throwable e)
		{
			System.out.println("Exception:" + e.toString());
			e.printStackTrace();
		}
		
		
	}






}
