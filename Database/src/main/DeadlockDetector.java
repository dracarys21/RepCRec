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
	int[][] waitsForGraph;	//(WRT order in dependencies map) 0th index -> T1, 1st index -> T2, 2nd index -> T3 and so on
	Map<Transaction, List<Data>> dependencies;
	
	public DeadlockDetector(int size) {
		noOfActiveTransactions = size;
		waitsForGraph = new int[size][size];
		dependencies = new LinkedHashMap<>();
	}
	
	void getReverseMapping(Map<Data, Queue<Transaction>> waitingQueue) {
		for(Map.Entry<Data, Queue<Transaction>> element: waitingQueue.entrySet()) {
			Data dataItem = element.getKey();
			Queue<Transaction> wq = element.getValue();
			for(Transaction t: wq) {
				if(!dependencies.containsKey(t)) {
					List<Data> dataList = new ArrayList<Data>();
					dataList.add(dataItem);
					dependencies.put(t, dataList);
				}
				else {
					List<Data> dataList = dependencies.get(t);
					dataList.add(dataItem);
					dependencies.put(t, dataList);
				}
			}
		}
	}
	
	void constructWFGraph(Map<Data, Queue<Transaction>> waitingQueue) {
		getReverseMapping(waitingQueue);
		int i = 0;
		for(Map.Entry<Transaction, List<Data>> element: dependencies.entrySet()) {
			Transaction source = element.getKey();
			Character opType = source.status.operation;
			List<Data> dataList = dependencies.get(source);
			int j = 0;
			for(Data dataItem: dataList) {
				//find out which transaction possesses this data item
				for(Transaction dest: dependencies.keySet()) {
					if(opType.equals('R')) {
						if(dest.readLocksPossesed.containsKey(dataItem))
							waitsForGraph[i][j] = 1;
					}
					else if(opType.equals('W')) {
						if(dest.writeLockPossesed.contains(dataItem))
							waitsForGraph[i][j] = 1;
					}
				}
				j++;
			}
			i++;
		}
		detectCycle();
	}
	
	void detectCycle() {
		
	}
}
