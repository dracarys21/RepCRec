/**
 * 
 */
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.StringTokenizer;

import models.Transaction;

/**
 * @author varada
 *
 */

public class Sim {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		DataManager dm = new DataManager();
		dm.createData();
		dm.createSites();
		//dm.printRoutes();
		

		Path inputFile = Paths.get("Input\\Test1.txt");
		File file = inputFile.toFile(); 
		Scanner sc = new Scanner(file); 
			  
		TransactionManager tm = new TransactionManager();
		
		while (sc.hasNextLine()) 
		{ 
			String line = sc.nextLine();
			StringTokenizer st = new StringTokenizer(line, "(,)");
			String instr = st.nextToken();
			
			if(instr.equals("begin"))
			{
				String tname = st.nextToken();
				Transaction t =  new Transaction(tname,tm.time,"RW");
				//assign transaction
				tm.addToSystem(t);			
			}
			else if(instr.equals("beginRO"))
			{
				String tname = st.nextToken();
				Transaction t =  new Transaction(tname,tm.time,"RO");
				tm.addToSystem(t);
			}
			else if(instr.equals("end"))
			{
				String tname = st.nextToken();
			//	tm.removeFromSystem(tname);
			}
			else if(instr.equals("commits"))
			{
				//commit the final values to database --- maybe have to make a class for DM
			}
			else if(instr.equals("fail"))
			{
				String siteName = st.nextToken();
			}
			else if(instr.equals("recover"))
			{
				String siteName = st.nextToken();
			}
			else if(instr.equals("dump"))
			{
				//output results.
			}
			else if(instr.equals("R"))
			{
				String tname = st.nextToken();
				String varName = st.nextToken();
				//available copies read
				tm.readAction(tname, Integer.parseInt(varName.substring(1)));
			}
			else if(instr.equals("W"))
			{
				//System.out.println(instr);
				String tname = st.nextToken();
				String varName = st.nextToken();
				int value =  Integer.parseInt(st.nextToken());
				//available copies write
			}
			
			tm.time++;				
		}
		
		 
	}

}
