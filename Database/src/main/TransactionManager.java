/**
 * 
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import fuctional.Pair;
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
	Map<Integer, Queue<Pair<Transaction,Character>>> waitingQueue; //easier to manage --- need some system to recognize the type of lock required by transaction. 
	List<Transaction> activeList;	//For deadlock detection -- should use Pair<Transaction,data/dataID>??
	List<Transaction> activeListRO;
	HashSet<Data> variableAccessed; //for the commit time to check whether the sites for accessed variable are up since the start.
	Map<Integer, List<Site>> routes;
	
	public TransactionManager()
	{
		time = 0;
		allAliveTransaction = new ArrayList<>();
		routes = DataManager.routes;
		waitingQueue = new HashMap<>();//initialize waiting queue
		activeList = new ArrayList<>();
		activeListRO = new ArrayList<>();
		variableAccessed = new HashSet<>();
		initializeWaitingQueue();
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
		Transaction t = getActiveTransaction(tName);  // get transaction from Name
		t.status = 'D';
		allAliveTransaction.remove(t);
		if(t.getType().equals("RW"))
			activeList.remove(t);
		else
			activeListRO.remove(t);	
	} 
	
	
	public boolean isWaitingQueueEmptyData(int iData)
	{
		return waitingQueue.get(iData).isEmpty();
	}
	
	public void availableCopies(String tname, int iData)
	{
		//for read transaction
		Transaction t = null;
		if(!isWaitingQueueEmptyData(iData))
		{
			Pair<Transaction, Character> p = peekQueueData(iData);
			if(p.getR()=='W')
			{
			  availableCopies(tname, iData, null );
				return;
			}
		}
		else
			t = getActiveTransaction(tname);
		
		Site s = null;
		List<Site> sitesfordata = routes.get(iData); //Assumption that dIDEX start from 1
		 
		     
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
			Queue<Pair<Transaction,Character>> qt = waitingQueue.get(iData);
			qt.add(new Pair<Transaction, Character>(t,'R'));
			waitingQueue.put(iData, qt);
			return;
		}
		
		s.setReadLock(iData);
	}
	
	public void availableCopies(String tname, int iData, int value)
	{

	}
	
	
	
	
	private void initializeWaitingQueue()
	{
		for(int i = 1;  i<=20; i++ )
		{
			waitingQueue.put(i, new LinkedList<Pair<Transaction, Character>>());
		}
	}
	
	private Pair<Transaction, Character> peekQueueData(int iData)
	{
		return waitingQueue.get(iData).peek();
	}
	
	/*Get transaction by name*/
	private Transaction getActiveTransaction(String tname)
	{
		Optional<Transaction> tr =  activeList.stream().filter(t -> t.name.equals(tname)).findFirst();
		if(tr.isPresent())
			return tr.get();
		Optional<Transaction> tr1 =  activeListRO.stream().filter(t -> t.name.equals(tname)).findFirst();
		if(tr1.isPresent())
			return tr1.get();
		return null;		
	}
	
}
