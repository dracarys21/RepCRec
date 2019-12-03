/**
 * 
 */
package main;

import java.util.ArrayList;
import java.util.Collections;
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
	Map<Data, Queue<Transaction>> waitingQueue;  
	List<Transaction> activeList;	
	List<Transaction> activeListRO;
	Map<Data, List<Site>> routes;
	
	public TransactionManager()
	{
		time = 0;
		routes = DataManager.routes;
		waitingQueue = new HashMap<>();//initialize waiting queue
		activeList = new ArrayList<>();
		activeListRO = new ArrayList<>();
		initializeWaitingQueue();
	}
	
	public void addToSystem(Transaction t)
	{
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
		
		HashSet<Site> siteAccessed = t.sitesAccessed;
		//if a site upTime < t.startTime then abort the transaction (site has failed)
		for(Site st: siteAccessed)
		{
			if(st.upTimeStamp>t.startTime || st.upTimeStamp==-1)
			{
				//release all its locks
				releaseLocks(t);
				//abort transaction 
				System.out.println(t.name+" aborted");
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
	
	//to Decide which transaction to execute
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
			{//implement AC for write for transaction t
				availableCopiesWrite(t,t.getActionData(),t.getWriteValue(),true);
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
		availableCopiesRead(t,d,isBlockedTrans);
	}
	
	//to Peform Read transaction
	private void availableCopiesRead(Transaction t, Data d, boolean isBlockedTrans)
	{
		//get sites for the data.
				Site s = null;
				List<Site> sitesfordata = routes.get(d);
				for(Site st:sitesfordata )
				{
					if(st.isReadLockAvailable(d))
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
						t.changeStatusToActive();
						activeList.add(t);
					}
					
					s.setReadLock(d,t);
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
	
	//to Decide which transaction to execute
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
			currTrans.changeStatusToBlocked(d, 'W',value); 
			waitingQueue.get(d).add(currTrans);
			isBlockedTrans = true;
			
			if(t.checkAction('R'))
			{
				//implement AC for read for transaction t
				availableCopiesRead(t,t.getActionData(),true);
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
		availableCopiesWrite(t,d,value,isBlockedTrans);
	}
	
	//to Peform write transaction
	private void availableCopiesWrite(Transaction t, Data d, int value, boolean isBlockedTrans)
	{

		//get sites for the data.
		List<Site> sitesfordata = routes.get(d);
		//check status of each site for variable d
		for(Site st:sitesfordata )
		{
			//if any site is failed leave that site
			if(st.checkSiteStatus('F'))
				continue;
			
			// lock not available on up site--- block the transaction
			if(!st.isWriteLockAvailable(d))
			{
				if(!isBlockedTrans)
				{
					activeList.remove(t);
					t.changeStatusToBlocked(d, 'W', value); 
					waitingQueue.get(d).add(t);
					return;
				}			
			}
		}
		
		System.out.println(t.name+" writing "+d.index);
		//get write locks on all sites
		for(Site st:sitesfordata )
		{
			if(!st.checkSiteStatus('F')) {
				st.setWriteLock(d,t);
				t.sitesAccessed.add(st);
			}
		}
		
		t.writeLockPossesed.add(d);	
		
		//perform write.
		for(Site st:sitesfordata )
			if(!st.checkSiteStatus('F')) 
				st.setData(d, value);
		
		//if blocked transaction--make it active
		if(isBlockedTrans)
		{
			waitingQueue.get(d).remove();
			t.changeStatusToActive();
			activeList.add(t);
		}
	}
	
	public void multiversionRead(String tname, Data d)
	{
		
	}
	
	public void dump()
	{
		List<Site> s = new ArrayList<>(DataManager.sites);
		Collections.sort(s);
		for(Site stemp: s)
		{
			List<Data> data= new ArrayList<Data>(stemp.variables);
			System.out.println(stemp.index+":");
			for(Data dtemp: data)
				System.out.print(dtemp.index+":"+dtemp.getLastCommittedVal()+" ");
			System.out.println();
		}
		
		System.out.println("Data Mangaer Values");
		
		for(Data dtemp: DataManager.variables)
		{
			System.out.print(dtemp.index+":"+dtemp.getLastCommittedVal()+" ");
		}
	}
	
	
	//change Site Status to failed
	public void failSite(int sindex)
	{
		int index = DataManager.sites.indexOf(new Site(sindex));
		Site s = DataManager.sites.get(index);
		s.failSite();	
	}
	
	//change Site Status to recover
	public void recoverSite(int sindex)
	{
		int index = DataManager.sites.indexOf(new Site(sindex));
		Site s = DataManager.sites.get(index);
		s.recoverSite(time);
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
