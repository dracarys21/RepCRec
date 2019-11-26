package main;

import java.util.*;

import models.*;

public class DataManager {
	
	static List<Data> variables  = new ArrayList<>();
	final static List<Site> sites = new ArrayList<>();
	final static Map<Integer, List<Site>> routes = new HashMap<>(); //Directly mapping data-index to sites
	
	 void createData()
	{
		for(int i = 0; i<20; i++)
			variables.add(new Data((i+1),10*(i+1)));
	}
	
	 void createSites()
	{
		List<Data> evenList = new ArrayList<>();
		for(int i = 1; i< 20; i = i+2)
			evenList.add(new Data(variables.get(i)));
		
		for(int i = 0; i<10; i++)
		{
			List<Data> newList = new ArrayList<>(evenList);
			newList.add(new Data(variables.get(i)));
			newList.add(new Data(variables.get(10+i)));
			sites.add(new Site(i+1, newList));
		}
		
		initializeRoute();
	}
	
	private void initializeRoute()
	{
		for(int i = 1; i<20; i = i+2)
			routes.put(i+1,new ArrayList<Site>(sites));
		
		for(int i = 0; i<20; i = i+2)
		{
			List<Site> s1 = new ArrayList<Site>();
			s1.add(sites.get(i%10));
			routes.put(i+1,s1 );
		}
	}
	
	 Map<Integer, List<Site>> getRoutes()
	{
		return routes;
	}
	
	boolean getReadLock(int idata, int isite)
	{
		
	}
	 
	 
	 
	 /*
	 void printDataOnSite()
	{
		for(Site s: sites)
		{
			System.out.println("site"+s.index+":");
			for (Data d: s.variables)
				System.out.print(d.index+" ");
			System.out.println();
		}
		
	}
	
	 void printRoutes()
	{
		for (Map.Entry<Integer,List<Site>> entry : routes.entrySet())
		{
			System.out.println("d"+entry.getKey()+":");
			for(Site s: entry.getValue())
			{
				System.out.print("s:"+s.index+", ");
			}
			System.out.println();
		}
	}*/
}
