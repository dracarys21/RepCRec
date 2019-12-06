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
	List<Integer>[] waitsForGraph;	//(WRT order in dependencies map) 0th index -> T1, 1st index -> T2, 2nd index -> T3 and so on
	List<Integer>[] cycles; 
	Map<Transaction, Data> dependencies;
	int firstU, firstP;
	
	@SuppressWarnings("unchecked")
	public DeadlockDetector(Map<Data, Queue<Transaction>> waitingQueue) {
		firstU = -1;
		firstP = -1;
		getReverseMapping(waitingQueue);
		int size = dependencies.size();
		noOfActiveTransactions = size;
		waitsForGraph = new ArrayList[size];
		cycles = new ArrayList[size];
		for (int i = 0; i < size; i++) 
			waitsForGraph[i] = new ArrayList<>();
		constructWFGraph();
		dependencies = new LinkedHashMap<>();
	}
	
	void getReverseMapping(Map<Data, Queue<Transaction>> waitingQueue) {
		for(Map.Entry<Data, Queue<Transaction>> element: waitingQueue.entrySet()) {
			Data dataItem = element.getKey();
			Queue<Transaction> wq = element.getValue();
			for(Transaction t: wq) {
				dependencies.put(t, dataItem);
			}
		}
	}
	
	void constructWFGraph() {
		int i = 0;
		for(Map.Entry<Transaction, Data> element: dependencies.entrySet()) {
			Transaction source = element.getKey();
			Character opType = source.status.operation;
			Data dataItem = dependencies.get(source);
			int j = 0;
			for(Transaction dest: dependencies.keySet()) {
				if(opType.equals('R')) {
					if(dest.readLocksPossesed.containsKey(dataItem)) {
						waitsForGraph[i].add(j);
						if(firstU == -1) {
							firstU = j;
							firstP = i;
						}
					}
				}
				else if(opType.equals('W')) {
					if(dest.writeLockPossesed.contains(dataItem)) {
						waitsForGraph[i].add(j);
						if(firstU == -1) {
							firstU = j;
							firstP = i;
						}
					}
				}
				j++;
			}
			i++;
		}
	}
	
	void dfs_cycle(int u, int p, int color[],  int mark[], int par[], Integer cyclenumber) {
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
	        if (v == par[u]) { 
	            continue; 
	        } 
	        dfs_cycle(v, u, color, mark, par, cyclenumber); 
	    } 
	  
	    // completely visited. 
	    color[u] = 2;
	}
	
	void breakCycles(int edges, int mark[], Integer cyclenumber) 
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
	    	Transaction youngest = null;
	        for (int x : cycles[i]) {
	        	Transaction thisTransaction = getTransactionAtIndex(x);
	        	if(thisTransaction.startTime > youngestAge)
	        		youngestAge = thisTransaction.startTime;
	        	youngest = thisTransaction;
	        }
	        //abortTransaction(youngest);
	    }
	}
	
	void detectCycle() {
		// arrays required to color the 
	    // graph, store the parent of node 
	    int color[] = new int [noOfActiveTransactions]; 
	    int par[] = new int[noOfActiveTransactions];
	    int mark[] = new int[noOfActiveTransactions];
	    int cyclenumber = 0;
	    
	    // call DFS to mark the cycles 
	    dfs_cycle(firstU, firstP, color, mark, par, cyclenumber);
	    breakCycles(noOfActiveTransactions, mark, cyclenumber);
	}
	
	//Utility function
	Transaction getTransactionAtIndex(int index) {
		int i = 0;
		for(Transaction t: dependencies.keySet()) {
			if(i == index)
				return t;
			i++;
		}
		return null;
	}
}
