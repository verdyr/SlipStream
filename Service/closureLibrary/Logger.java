package closureLibrary;

import java.io.FileWriter;

public class Logger {
	
	private static String DefaultLogFile = "/home/verdyr/workspace/TestCloud1/src/DefaultDebug.txt";
	private static String DefaultOutput = Logger.FILE; 
	
	public static String FILE = "file";
	public static String STDOUT = "stdout";
	
	
	public static void setDefaultOtput(String defoutput)
	{
		DefaultOutput = defoutput;
	}
	
	public static String getDefaultOtput()
	{
		return DefaultOutput;
	}
	
	public static void print(String s)
	{
		System.out.println(s);
	}
	
	public static void printFile(String s, String FileName)
	{
		try
		{
			FileWriter fw = new FileWriter(FileName, true);
			fw.write(s);
			fw.flush();
		}
		catch(Throwable e)
		{
			System.out.println("Exception:" + e.toString());
			e.printStackTrace();
		}
	}
	
	public static void printDefaultFile(String s)
	{
		printFile (s,Logger.DefaultLogFile);
	}

	public static void printDefaultOut(String s)
	{
		if(Logger.DefaultOutput.equals(Logger.FILE))
		{
			printDefaultFile(s);
		}
		else if (Logger.DefaultOutput.equals(Logger.STDOUT))
		{
			print(s);
		}
	}
	
}
