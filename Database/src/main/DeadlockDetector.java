package main;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import models.Data;
import models.Site;
import models.Transaction;

public class DeadlockDetector {
	int noOfActiveTransactions;	//size of activeList
	int[][] waitsForGraph;	//0th index -> T1, 1st index -> T2, 2nd index -> T3 and so on
	
	public DeadlockDetector(int size) {
		noOfActiveTransactions = size;
		waitsForGraph = new int[size][size];
	}
	
	void constructWFGraph(List<Transaction> activeList, Map<Data, Queue<Transaction>> waitingQueue) {
		for(Map.Entry<Data, Queue<Transaction>> element: waitingQueue.entrySet()) {
			Data dataItem = element.getKey();
			Queue<Transaction> wq = element.getValue();
			for(Transaction waitingTransaction: wq) {
				Character opType = waitingTransaction.status.operation;
				for(Transaction t: activeList) {
					if(opType.equals('R')) {
						for(Map.Entry<Data, Site> readLock: t.readLocksPossesed.entrySet()) {
							
						}
					}
					else if(opType.equals('W')) {
						
					}
				}
			}
		}
	}
}
