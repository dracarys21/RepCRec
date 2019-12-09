package main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import models.Data;
import models.Transaction;

public class DeadlockDetector {
	int noOfActiveTransactions;	//number of entries in dependencies map
	List<Integer>[] waitsForGraph;
	List<Integer>[] cycles; 
	Map<Transaction, Data> dependencies;
	public Map<Data, Queue<Transaction>> waitingQueue;
	int firstU;
	int N = 8;
	int cyclenumber;
	
	@SuppressWarnings("unchecked")
	public void checkForDeadlock() {
		dependencies = new LinkedHashMap<>();
		getReverseMapping(waitingQueue);
		int size = dependencies.size();
		noOfActiveTransactions = size;
		cycles = new ArrayList[N];
		waitsForGraph = new ArrayList[N];
		for (int i = 0; i < N; i++) {
			waitsForGraph[i] = new ArrayList<>();
			cycles[i] = new ArrayList<>();
		}
		firstU = -1;
		constructWFGraph();
		detectCycle();
	}
	
	void getReverseMapping(Map<Data, Queue<Transaction>> waitingQueue) {
		for(Map.Entry<Data, Queue<Transaction>> element: waitingQueue.entrySet()) {
			Data dataItem = element.getKey();
			Queue<Transaction> wq = element.getValue();
			for(Transaction t: wq) {
				dependencies.put(t, dataItem);
			}
		}
//		printWaitingQueue();
//		printDependenciesMap();
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: VARADA HANUMANTE
	 * 
	 * ************************************************************************
	 */
	void constructWFGraph() {
		for(Map.Entry<Transaction, Data> element: dependencies.entrySet()) {
			Transaction source = element.getKey();
			int i = getTransactionIndex(source);
			Character opType = source.status.operation;
			Data dataItem = dependencies.get(source);
			
			Transaction alreadyWaiting = waitingQueue.get(dataItem).peek();
			if(alreadyWaiting != source && alreadyWaiting != null) {
				int j = getTransactionIndex(alreadyWaiting);
//				System.out.println("Source transaction = " + i);
//				System.out.println("Found transaction " + j + " waiting for item " + dataItem.index);
				waitsForGraph[i].add(j);
				if(firstU == -1) {
					firstU = i;
				}
			}
			
			else {
				for (Transaction dest : dependencies.keySet()) {
					int j = getTransactionIndex(dest);
					if (opType.equals('R')) {
						if (dest.writeLockPossesed.containsKey(dataItem)) {
							waitsForGraph[i].add(j);
							if (firstU == -1) {
								firstU = i;
							}
						}
					} else {
						if (dest.writeLockPossesed.containsKey(dataItem)
								|| dest.readLocksPossesed.containsKey(dataItem)) {
							waitsForGraph[i].add(j);
							if (firstU == -1) {
								firstU = i;
							}
						}
					}
				} 
			}
		}
		//printWFGraph();
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: VARADA HANUMANTE
	 * 
	 * ************************************************************************
	 */
	void dfs_cycle(int u, int p, int color[],  int mark[], int par[]) {
		// already (completely) visited vertex. 
	    if (color[u] == 2) { 
	        return; 
	    } 
	  
	    // seen vertex, but was not completely visited -> cycle detected. 
	    // backtrack based on parents to find the complete cycle. 
	    if (color[u] == 1) { 
	        cyclenumber++; 
	        int cur = p; 
	        mark[cur] = cyclenumber; 
	        //System.out.println("Cyclenumber = " + cyclenumber);
	        // backtrack the vertex which are 
	        // in the current cycle thats found 
	        while (cur != u) { 
	            cur = par[cur]; 
	            mark[cur] = cyclenumber; 
	        } 
	        return; 
	    } 
	    par[u] = p; 
	  
	    // partially visited. 
	    color[u] = 1; 
	  
	    // simple dfs on graph 
	    for (int v : waitsForGraph[u]) { 
	  
	        // if it has not been visited previously 
//	        if (v == par[u]) { 
//	            continue; 
//	        } 
	        dfs_cycle(v, u, color, mark, par); 
	    } 
	  
	    // completely visited. 
	    color[u] = 2;
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: VARADA HANUMANTE
	 * 
	 * ************************************************************************
	 */
	void breakCycles(int edges, int mark[], int cyclenumber) 
	{ 
	  
	    // push the edges that into the 
	    // cycle adjacency list 
	    for (int i = 1; i <= edges; i++) { 
	        if (mark[i] != 0) 
	            cycles[mark[i]].add(i); 
	    } 
	  
	    // print all the vertex with same cycle 
	    for (int i = 1; i <= cyclenumber; i++) {
	    	int youngestAge = Integer.MIN_VALUE;
	    	Data d = null;
	    	Transaction youngest = null;
	        for (int x : cycles[i]) {
	        	Transaction thisTransaction = getTransactionAtIndex(x);
	        	if(thisTransaction.startTime > youngestAge) {
	        		youngestAge = thisTransaction.startTime;
		        	youngest = thisTransaction;
		        	d = dependencies.get(youngest);
	        	}
	        }
	        //System.out.println("Aborting " + youngest.name);
	        TransactionManager.abortBlockedTransaction(youngest, dependencies.get(youngest));
	        TransactionManager.postDeadlock(d);
	    }
	}
	
	void detectCycle() {
		// arrays required to color the 
	    // graph, store the parent of node
		cyclenumber = 0;
	    int color[] = new int [N]; 
	    int par[] = new int[N];
	    int mark[] = new int[N];
	    
	    // call DFS to mark the cycles
	    if(firstU != -1) {
	    	//System.out.println("FirstU = " + firstU + " & firstP = " + firstP);
	    	dfs_cycle(firstU, 0, color, mark, par);
		    breakCycles(N-1, mark, cyclenumber);
	    }
	}
	
	//Utility functions
	
	void printDependenciesMap() {
		System.out.println("Printing dependencies map");
		for(Map.Entry<Transaction, Data> element: dependencies.entrySet()) {
			System.out.println("Transaction " + element.getKey().name
					+ " & Data = " + element.getValue().index);
		}
	}
	
	void printWaitingQueue() {
		System.out.println("Printing waiting queue");
		for(Map.Entry<Data, Queue<Transaction>> element: waitingQueue.entrySet()) {
			System.out.print("Data = " + element.getKey().index);
			System.out.println(" & Transaction list = ");
			for(Transaction t: element.getValue()) {
				System.out.println(t.name);
			}
		}
	}
	
	void printWFGraph() {
		System.out.println("Printing WF graph");
		for(int i = 0; i < waitsForGraph.length; i++) {
			System.out.print("For transaction # " + i + ": ");
			List<Integer> list = waitsForGraph[i];
			for(int j: list) {
				System.out.print("Transaction # " + j + " ");
			}
			System.out.println("");
		}
	}
	
	int getTransactionIndex(Transaction t) {
		int index = 0;
		String name = t.name.substring(1, t.name.length());
		index = Integer.parseInt(name);
		return index;
	}
	
	Transaction getTransactionAtIndex(int index) {
		String name = "T" + index;
		for(Transaction t: dependencies.keySet()) {
			if(t.name.equals(name))
				return t;
		}
		return null;
	}
}
