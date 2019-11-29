/**
 * 
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	Map<Data, Queue<Transaction>> waitingQueue;  
	List<Transaction> activeList;	
	List<Transaction> activeListRO;
	Map<Data, List<Site>> routes;
	
	public TransactionManager()
	{
		time = 0;
		allAliveTransaction = new ArrayList<>();
		routes = DataManager.routes;
		waitingQueue = new HashMap<>();//initialize waiting queue
		activeList = new ArrayList<>();
		activeListRO = new ArrayList<>();
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
		//get transaction type -- from RW/RO list
		//remove transaction from the list RW/RO
		Transaction t = null;
		if(getActiveTransaction(tName,activeList,"RW")!=null)
		{
			t = getActiveTransaction(tName,activeList,"RW");
			activeList.remove(t);
		}
		else
		{
			t = getActiveTransaction(tName,activeListRO,"RO");
			activeListRO.remove(t);
		}
		
		List<Site> siteAccessed = t.sitesAccessed;
		//if a site upTime < t.startTime then abort the transaction (site has failed)
		for(Site st: siteAccessed)
		{
			if(st.upTimeStamp<t.startTime)
			{
				//release all its locks
				releaseLocks(t);
				//abort transaction 
				return;
			}
				
		}
		
		if(!t.writeLockPossesed.isEmpty()) {
			
		//commit data on sites -- for write
			HashSet<Data> writeLockRelease = t.writeLockPossesed;
			Iterator<Data> i = writeLockRelease.iterator(); 
	        while (i.hasNext()) 
	        {
	        	Data d  = i.next();
	        	List<Site> siteAcc = routes.get(d);
	        	for(Site s: siteAcc)
	        		s.commitData(d);;
	        }
			
		}
		//update value on DM -- for write
		HashSet<Data> updateDataList = t.writeLockPossesed;
		Iterator<Data> i = updateDataList.iterator(); 
        while (i.hasNext()) 
        {
        	Data d  = i.next();
        	List<Site> siteAcc = routes.get(d);
        	for(Site s: siteAcc)
        		DataManager.updateDataValues(d, d.getLastCommittedVal());
        }
		//release all its lock.	
		releaseLocks(t);
	} 
	
	public void releaseLocks(Transaction t)
	{
		HashMap<Data,Site> readLockRelease = t.readLocksPossesed;
		for (Map.Entry<Data,Site> entry : readLockRelease.entrySet())
		{
			Data d  = entry.getKey();
			entry.getValue().releaseReadLock(d);
		}
		
		HashSet<Data> writeLockRelease = t.writeLockPossesed;
		Iterator<Data> i = writeLockRelease.iterator(); 
        while (i.hasNext()) 
        {
        	Data d  = i.next();
        	List<Site> siteAcc = routes.get(d);
        	for(Site s: siteAcc)
        		s.releaseWriteLock(d);
        }
	}
	
	public void readAction(String tname, int d)
	{
		if(getActiveTransaction(tname,activeList,"RW")!=null)
			availableCopies(tname, new Data(d));
		else
			multiversionRead(tname, new Data(d));
	}
	
	public void availableCopies(String tname, Data d)
	{
		Transaction t  = null;
		boolean isBlockedTrans = false;
		if(!waitingQueue.get(d).isEmpty())
		{
			t = waitingQueue.get(d).peek(); //first check whether the transaction can get locks as it requires only then remove.
			//change current transaction status from active to blocked for the data and insert to blocked queue
			Transaction currTrans = getActiveTransaction(tname,activeList,"RW"); 
			activeList.remove(currTrans);
			currTrans.changeStatusToBlocked(d, 'R'); 
			waitingQueue.get(d).add(currTrans);
			isBlockedTrans = true;
			
			if(t.checkAction('W'))
			{
				//implement AC for write for transaction t
				availableCopies(t.name,t.getActionData(),);
				return;
			}
		}
		else
		{
			t = getActiveTransaction(tname,activeList,"RW");
			//if transaction already has readLock/writeLock on the variable then new lock is not necessary
			if(t.readLocksPossesed.containsKey(d) || t.writeLockPossesed.contains(d))
			{
				//read the value
				System.out.println(t.name+" reads data"+d.index);
				return;
			}
		}
		
		//get sites for the data.
		Site s = null;
		List<Site> sitesfordata = routes.get(d);
		for(Site st:sitesfordata )
		{
			if(st.checkSiteStatus('A') && st.isReadLockAvailable(d))
			{
				s = st;
				break;
			}
		}
		//if site found --- get readLock
		if(s!=null)
		{
			//if t is from blocked queue, remove the transaction from blocked queue and insert into activeList 
			if(isBlockedTrans)
			{
				waitingQueue.get(d).remove();
				activeList.add(t);
			}
			
			s.setReadLock(d);
			t.readLocksPossesed.put(d,s);
			t.sitesAccessed.add(s);	
			//read the value
			System.out.println(t.name+" site:"+s.index+" "+s.getData(d));
		}
		//site not found
		else
		{
			//if t is from blocked queue, return
			//if t is in activeList, remove t from activeList and insert in blocked queue for data
			if(activeList.contains(t))
			{
				activeList.remove(t);
				t.changeStatusToBlocked(d, 'R'); 
				waitingQueue.get(d).add(t);
			}
		}
	}
	
	public void availableCopies(String tname, Data d, int value)
	{
		Transaction t  = null;
		boolean isBlockedTrans = false;
		if(!waitingQueue.get(d).isEmpty())
		{
			t = waitingQueue.get(d).peek();//first check whether the transaction can get locks as it requires---only then remove.
			
			//change current transaction status from active to blocked for the data and insert to blocked queue
			Transaction currTrans = getActiveTransaction(tname,activeList,"RW"); 
			activeList.remove(currTrans);
			currTrans.changeStatusToBlocked(d, 'W'); 
			waitingQueue.get(d).add(currTrans);
			isBlockedTrans = true;
			
			if(t.checkAction('R'))
			{
				//implement AC for read for transaction t
			///	availableCopies(t.name,t.getActionData(),);
				return;
			}
		}
		else
		{
			t = getActiveTransaction(tname,activeList,"RW"); 
			//if transaction already has writeLock on the variable then new lock is not necessary
			if(t.writeLockPossesed.contains(d))
			{
				List<Site> sitesfordata = routes.get(d);
				for(Site st:sitesfordata )
					st.setData(d, value);	
				return;
			}
			  
		}
		
		//get sites for the data.
		List<Site> sitesfordata = routes.get(d);
		//check status of each site for variable d
		for(Site st:sitesfordata )
		{
			//if any site is failed or lock not available--- block the transaction
			if(!st.isWriteLockAvailable(d) || st.checkSiteStatus('F'))
			{
				if(!isBlockedTrans)
				{
					activeList.remove(t);
					t.changeStatusToBlocked(d, 'W'); 
					waitingQueue.get(d).add(t);
					return;
				}			
			}
		}
		
		System.out.println(t.name+" writing "+d.index);
		//get write locks on all sites
		for(Site st:sitesfordata )
				st.setWriteLock(d);
					
		//perform write.
		for(Site st:sitesfordata )
			st.setData(d, value);				
	}
	
	public void multiversionRead(String tname, Data d)
	{
		
	}
	
	private void initializeWaitingQueue()
	{
		for(int i = 1;  i<=20; i++ )
		{
			waitingQueue.put(new Data(i), new LinkedList<Transaction>());
		}
	}
	
	/*Get active transaction by name*/
	private Transaction getActiveTransaction(String tname, List<Transaction> list, String type)
	{
		int index = list.indexOf(new Transaction(tname,type));
		if(index!=-1)
			return list.get(index);
		return null;		
	}
	
}
