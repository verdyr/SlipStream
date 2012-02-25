package closureLibrary;

import java.util.Vector;

public class TriplesUtilities {
	
	public static Vector<Vector<String>> concatTriples(Vector<Vector<String>> acc,Vector<Vector<String>> newtriples)
	{
		Vector<Vector<String>> out = new Vector<Vector<String>>();
		out = acc;
		for (int i = 0; i < newtriples.size();i++)
		{
			out.add(newtriples.elementAt(i));
		}
		return out;
	}
	
	

}
