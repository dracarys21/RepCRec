/**
 * 
 */
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import models.Data;
import models.Transaction;

/**
 * @author varada
 *
 */

public class Sim {

	static Path outputfile ;
	static List<String> outputLines = new ArrayList();
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		DataManager dm = new DataManager();
		dm.createData();
		dm.createSites();
	//	dm.printRoutes();
	//	dm.printDataOnSite();
		
		
		Path inputFile = Paths.get(args[0]);
		File file = inputFile.toFile(); 
		
		 outputfile = Paths.get(args[1]);
	
		@SuppressWarnings("resource")
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
				Transaction t =  new Transaction(tname,TransactionManager.time,"RW");
				//assign transaction
				tm.addToSystem(t);			
			}
			else if(instr.equals("beginRO"))
			{
				String tname = st.nextToken();
				Transaction t =  new Transaction(tname,TransactionManager.time,"RO");
				tm.addToSystem(t);
				tm.takeSnapshot(t);
			}
			else if(instr.equals("end"))
			{
				String tname = st.nextToken();
				if(tm.isAlive(tname))
					tm.removeFromSystem(tname);
			}
			else if(instr.equals("fail"))
			{
				String siteName = st.nextToken();
				tm.failSite(Integer.parseInt(siteName));
			}
			else if(instr.equals("recover"))
			{
				String siteName = st.nextToken();
				tm.recoverSite(Integer.parseInt(siteName));
			}
			else if(instr.equals("dump"))
			{
				tm.dump();
				//output results.
			}
			else if(instr.equals("R"))
			{
				String tname = st.nextToken();
				String varName = st.nextToken();
				//available copies read
				if(tm.isAlive(tname))
					tm.readAction(tname, Integer.parseInt(varName.substring(1)));
			}
			else if(instr.equals("W"))
			{
				//System.out.println(instr);
				String tname = st.nextToken();
				String varName = st.nextToken();
				int value =  Integer.parseInt(st.nextToken());
	//			System.out.println(varName.substring(1));
				//available copies write
				if(tm.isAlive(tname))
					tm.availableCopies(tname, new Data(Integer.parseInt(varName.substring(1))), value);
			}
			
			TransactionManager.time++;				
		}
		
		//output result
		try {
			Files.write(Sim.outputfile,outputLines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			 
	}

}
