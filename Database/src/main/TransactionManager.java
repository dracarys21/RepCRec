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
	public static int time;
	List<Transaction> allAliveTransaction;
	Map<Integer, List<Transaction>> waitingQueue; //easier to manage
	List<Transaction> activeList;	//For deadlock detection
	List<Transaction> activeListRO;
	HashSet<Data> variableAccessed; //for the commit time to check whether the sites for accessed variable are up since the start.
	Map<Integer, List<Site>> routes;
	
	public TransactionManager()
	{
		time = 0;
		allAliveTransaction = new ArrayList<>();
		routes = DataManager.routes;
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
	
	public void availableCopies(String tname, int iData)
	{
		//for read transaction
		Transaction t = getTransaction(tname);
		List<Site> sites = routes.get(iData);
		Site s = null;
		List<Site> sitesfordata = routes.get(iData);
		
		//check for 
		     
		for(Site st:sitesfordata )
		{
			if(st.checkSiteStatus('A') && st.isReadLockAvailable(iData))
			{
				s = st;
				break;
			}
		}
		
		if(s==null)
		{
			List<Transaction> qt = waitingQueue.get(iData);
			qt.add(t);
			waitingQueue.put(iData, qt);
		}
		
		time++;
	}
	
	public void availableCopies(Transaction t, Data d, int value)
	{
		// for write transaction
	}
	
	/*Get transaction by name*/
	private Transaction getTransaction(String tname)
	{
		Optional<Transaction> tr =  allAliveTransaction.stream().filter(t -> t.name.equals(tname)).findFirst();
		if(tr.isPresent())
			return tr.get();
		return null;		
	}
	
}
