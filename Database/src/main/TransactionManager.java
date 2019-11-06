/**
 * 
 */
package main;

import java.util.List;
import java.util.Map;

import models.Site;
import models.Transaction;
import models.Data;

/**
 * @author varada
 *
 */
public class TransactionManager {
	List<Site> allSites;
	Map<Data, List<Site>> routes;
	Map<Data, List<Transaction>> waitingQueue;
	List<Transaction> activeList;	//For deadlock detection
}
