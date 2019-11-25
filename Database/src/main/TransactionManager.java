/**
 * 
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import models.Site;
import models.Transaction;
import models.Data;

/**
 * @author varada
 *
 */
public class TransactionManager {
	public  int time;
	List<Transaction> allAliveTransaction;
	List<Site> allSites;
	Map<Data, List<Site>> routes;
	Map<Data, List<Transaction>> waitingQueue;
	List<Transaction> activeList;	//For deadlock detection
	List<Transaction> activeListRO;
	HashSet<Data> variableAccessed; //for the commit time to check whether the sites for accessed variable are up since the start.
	
	public TransactionManager()
	{
		time = 0;
		allAliveTransaction = new ArrayList<>();
		allSites = new ArrayList<>();
		routes = new HashMap<Data, List<Site>>();
		waitingQueue = new HashMap<>();
		activeList = new ArrayList<>();
		activeListRO = new ArrayList<>();
		variableAccessed = new HashSet<>();
	}
	
	public void addToSystem(Transaction t)
	{
		allAliveTransaction.add(t);
		if(t.getType().equals("RW"))
			activeList.add(t);
		else
			activeListRO.add(t);
	}
	
	public void removeFromSystem(String tName)
	{
		Transaction t = getTransaction(tName);  // get transaction from Name
		t.status = 'D';
		allAliveTransaction.remove(t);
		if(t.getType().equals("RW"))
			activeList.remove(t);
		else
			activeListRO.remove(t);
		
	}
	
	public void availableCopies(String tname, Data d)
	{
		//get transaction
		Transaction t = getTransaction(tname);
		
		List<Site> sites = routes.get(d);
		Site s;
		
		for(Site st: sites)
		{
			if(st.isSiteActive() && st.isReadLockAvailable(d))
			{
				s = st;
				break;
			}
		}
	}
	
	public void availableCopies(Transaction t, Data d, int value)
	{
		
	}
	
	private Transaction getTransaction(String tname)
	{
		Optional<Transaction> tr =  allAliveTransaction.stream().filter(t -> t.name.equals(tname)).findFirst();
		if(tr.isPresent())
			return tr.get();
		return null;		
	}
	
}
