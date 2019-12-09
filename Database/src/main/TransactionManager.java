/**
 * 
 */
package main;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;

import models.Site;
import models.Transaction;
import models.Data;
/**
 * @author varada
 *
 */
public class TransactionManager {
	public static int time;
	static Map<Data, Queue<Transaction>> waitingQueue;  
	static List<Transaction> activeList;	
	static List<Transaction> activeListRO;
	static List<Transaction> deadTransactions;
	static Map<Data, List<Site>> routes;
	static DeadlockDetector detector;
	
	public TransactionManager()
	{
		detector = new DeadlockDetector();
		time = 0;
		routes = DataManager.routes;
		waitingQueue = new HashMap<>();//initialize waiting queue
		activeList = new ArrayList<>();
		activeListRO = new ArrayList<>();
		deadTransactions = new ArrayList<>();
		initializeWaitingQueue();
	}
	
	public static void addToSystem(Transaction t)
	{
		if(t.getType().equals("RW"))
			activeList.add(t);
		else
			activeListRO.add(t);
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: PRANALI AWASEKAR
	 * 
	 * ************************************************************************
	 */
	public static void removeFromSystem(String tName)
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
		
		if(!t.writeLockPossesed.isEmpty()) {
			
		//commit data on sites -- for write
			HashMap<Data,List<Site>> writeLockRelease = t.writeLockPossesed;
			Iterator<Entry<Data, List<Site>>> i = writeLockRelease.entrySet().iterator(); 
	        while (i.hasNext()) 
	        {
	        	Map.Entry<Data, List<Site>> es = (Map.Entry<Data, List<Site>>)i.next();
	        	Data d  = es.getKey();
	        	List<Site> siteAcc = es.getValue();
	        	for(Site s: siteAcc)
	        		s.commitData(d);
	        }
					
		//update value on DM -- for write
	         i = writeLockRelease.entrySet().iterator(); 
	        while (i.hasNext()) 
	        {
	        	Map.Entry<Data, List<Site>> es = i.next();
	        	Data d  = es.getKey();
	        	DataManager.updateDataValues(d, es.getValue().get(0).getCurrentData(d));
	        }
		}
		
		Sim.outputLines.add(t.name+" commits");
	//	System.out.println(t.name+" commits");
		//release all its lock.	
		releaseLocks(t);
		t.changeStatusToDead();
		deadTransactions.add(t);
	} 
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: PRANALI AWASEKAR
	 * 
	 * ************************************************************************
	 */
	public static void releaseLocks(Transaction t)
	{
		//release read locks
		HashMap<Data,Site> readLockRelease = t.readLocksPossesed;
		HashSet<Data> dataLocksReleased= new HashSet<>();
		for (Map.Entry<Data,Site> entry : readLockRelease.entrySet())
		{
			Data d  = entry.getKey();
			entry.getValue().releaseReadLock(d,t);
			dataLocksReleased.add(d);
		}

		//release write locks
		HashMap<Data,List<Site>> writeLockRelease = t.writeLockPossesed;
		Iterator<Entry<Data, List<Site>>> i = writeLockRelease.entrySet().iterator(); 
		
        while (i.hasNext()) 
        {
        	Map.Entry<Data, List<Site>> es = i.next();
        	Data d  = es.getKey();
        	dataLocksReleased.add(d);
        	List<Site> siteAcc = es.getValue();
        	for(Site s: siteAcc)
        		s.releaseWriteLock(d);
        }
        
        Iterator<Data> itr = dataLocksReleased.iterator();
        while(itr.hasNext())
        {
        	Data key = itr.next();
        	if(!waitingQueue.get(key).isEmpty()) {
        		Transaction bt = waitingQueue.get(key).peek();
        		if(bt.getType().equals("RW"))
        			if(bt.status.operation=='R')
        				availableCopiesRead(bt,bt.getActionData(),true);
        			else
        				availableCopiesWrite(bt,bt.getActionData(),bt.status.writingVal,true);
        	}
        }
        
	}
	
	public static void readAction(String tname, int d)
	{
		if(getActiveTransaction(tname,activeList,"RW")!=null)
			availableCopies(tname, new Data(d));
		else
			multiversionRead(tname, new Data(d));
	}
	
	//to Decide which transaction to execute
	public static void availableCopies(String tname, Data d)
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
			detector.waitingQueue = waitingQueue;
			detector.checkForDeadlock();
			isBlockedTrans = true;
			if(waitingQueue.get(d).isEmpty())
				return;
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
			if(t.readLocksPossesed.containsKey(d) || t.writeLockPossesed.containsKey(d))
			{
				Site sacc = t.readLocksPossesed.get(d);
				if(sacc==null)
					sacc = t.writeLockPossesed.get(d).get(0);
				//read the value
				Sim.outputLines.add(t.name+" reads data"+d.index+" at site"+t.readLocksPossesed.get(d));
				return;
			}
		}
		availableCopiesRead(t,d,isBlockedTrans);
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: PRANALI AWASEKAR
	 * 
	 * ************************************************************************
	 */
	//to Perform Read transaction
	static boolean availableCopiesRead(Transaction t, Data d, boolean isBlockedTrans)
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
					
					if(!t.sitesAccessed.contains(s))
						t.sitesAccessed.add(s);	
					//read the value
					Sim.outputLines.add(t.name+" site:"+s.index+" data:"+d.index+" "+s.getCurrentData(d));
				//	System.out.println(t.name+" site:"+s.index+" data:"+d.index+" "+s.getCurrentData(d));
					return true;
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
						detector.waitingQueue = waitingQueue;
						detector.checkForDeadlock();
					}
					return false;
				}
		}
	
	//to Decide which transaction to execute
	public static void availableCopies(String tname, Data d, int value)
	{
		Transaction t  = null;
		if(!waitingQueue.get(d).isEmpty())
		{
			t = waitingQueue.get(d).peek();//first check whether the transaction can get locks as it requires---only then remove.
			
			//change current transaction status from active to blocked for the data and insert to blocked queue
			Transaction currTrans = getActiveTransaction(tname,activeList,"RW"); 
			activeList.remove(currTrans);
			currTrans.changeStatusToBlocked(d, 'W',value); 
			waitingQueue.get(d).add(currTrans);
			detector.waitingQueue = waitingQueue;
			detector.checkForDeadlock();
			if(waitingQueue.get(d).isEmpty())
				return;
			if(t.checkAction('R'))
			{
				//implement AC for read for transaction t
				boolean succ = availableCopiesRead(t,t.getActionData(),true);
				if(!succ)
				{
						Optional<Transaction>qt =  waitingQueue.get(d).stream().filter(te -> te.status.operation=='W').findFirst();
						if(qt.isPresent())
						{
							Transaction te = qt.get();
							waitingQueue.get(d).remove(te);
							activeList.add(te);
							availableCopiesWrite(te,te.getActionData(),te.status.writingVal,false);
						}
				}
				return;
			}
			availableCopiesWrite(t,d, t.status.writingVal,true);	
			return;
		}
		else
		{
			t = getActiveTransaction(tname,activeList,"RW"); 
			//if transaction already has writeLock on the variable then new lock is not necessary
			if(t.writeLockPossesed.containsKey(d))
			{
				List<Site> sitesfordata = t.writeLockPossesed.get(d);
				for(Site st:sitesfordata )
					 st.setData(d, value);	
				return;
			}  
		}
		availableCopiesWrite(t,d,value,false);
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: PRANALI AWASEKAR
	 * 
	 * ************************************************************************
	 */
	//to Peform write transaction
	private static void availableCopiesWrite(Transaction t, Data d, int value, boolean isBlockedTrans)
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
					detector.waitingQueue = waitingQueue;
					detector.checkForDeadlock();
					return;
				}			
			}
		}
		
		Sim.outputLines.add(t.name+" writing "+d.index);
	//	System.out.println(t.name+" writing "+d.index);
		List<Site> acquiredLocksOnSites = new ArrayList<>();
		//get write locks on all up sites
		for(Site st:sitesfordata )
		{
			if(!st.checkSiteStatus('F')) {
				st.setWriteLock(d,t);
				t.sitesAccessed.add(st);
				acquiredLocksOnSites.add(st);
			}
		}
		
		if(acquiredLocksOnSites.isEmpty())
		{
			if(!isBlockedTrans)
			{
				activeList.remove(t);
				t.changeStatusToBlocked(d, 'W', value); 
				waitingQueue.get(d).add(t);
				detector.waitingQueue = waitingQueue;
				return;
			}			
		}
		
		t.writeLockPossesed.put(d,acquiredLocksOnSites);	
		
		//perform write.
		for(Site st:t.writeLockPossesed.get(d) )
				st.setData(d, value);
		
		//if blocked transaction--make it active
		if(isBlockedTrans)
		{
			waitingQueue.get(d).remove();
			t.changeStatusToActive();
			activeList.add(t);
		}
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: VARADA HANUMANTE
	 * 
	 * ************************************************************************
	 */
	public static void multiversionRead(String tname, Data d) {
		Transaction t = getActiveTransaction(tname, activeListRO, "RO");
		if(!t.snapshot.containsKey(d.index)) {
			abortTransaction(t);
		}
		else {
			int value = t.snapshot.get(d.index);
			System.out.println(tname + " reads data " + d.index + " & value read = " + value);
		}
	}
	
	//change Site Status to failed
	public static void failSite(int sindex)
	{
		int index = DataManager.sites.indexOf(new Site(sindex));
		Site s = DataManager.sites.get(index);
		HashSet<Transaction> transToBeAborted= s.failSite();	
		Iterator<Transaction> i = transToBeAborted.iterator();
		while(i.hasNext())
		{
			abortTransaction(i.next());
		}	
	}
	
	public static boolean isAlive(String  tname)
	{
		return deadTransactions.stream().filter(t ->t.name.equals(tname)).count()==0;
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: PRANALI AWASEKAR
	 * 
	 * ************************************************************************
	 */
	private static void abortTransaction(Transaction t)
	{
		if(activeList.contains(t))
		{	
			activeList.remove(t);
			releaseLocks(t);
		}
		else
			activeListRO.remove(t);
		
		Sim.outputLines.add(t.name+" aborted");
	//	System.out.println(t.name+" aborted");
		t.changeStatusToDead();
		deadTransactions.add(t);
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: VARADA HANUMANTE
	 * 
	 * ************************************************************************
	 */
	public static void abortBlockedTransaction(Transaction t, Data d) {
		Queue<Transaction> wq = waitingQueue.get(d);
		wq.remove(t);
		releaseLocks(t);
		Sim.outputLines.add(t.name+" aborted");
//		System.out.println(t.name+" aborted");
		t.changeStatusToDead();
		deadTransactions.add(t);
	}
	
	public static void dump()
	{
		List<Site> s = new ArrayList<>(DataManager.sites);
		Collections.sort(s);
		for(Site stemp: s)
		{
			List<Data> data= new ArrayList<Data>(stemp.variables);
			Sim.outputLines.add(stemp.index+":");
			//System.out.println(stemp.index+":");
			String dataAdd = "";
			for(Data dtemp: data)
			{
				dataAdd = dataAdd+dtemp.index+":"+dtemp.getLastCommittedVal()+" ";
			//	System.out.print(dtemp.index+":"+dtemp.getLastCommittedVal()+" ");
			}
		 Sim.outputLines.add(dataAdd);
		//	System.out.println();
		}
		
		Sim.outputLines.add("Data Mangaer Values");
	//	System.out.println("Data Mangaer Values");
		
		for(Data dtemp: DataManager.variables)
		{
			Sim.outputLines.add(dtemp.index+":"+dtemp.getLastCommittedVal()+" ");
		//	System.out.print(dtemp.index+":"+dtemp.getLastCommittedVal()+" ");
		}
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: PRANALI AWASEKAR
	 * 
	 * ************************************************************************
	 */
	//change Site Status to recover
	public static void recoverSite(int sindex)
	{
		int index = DataManager.sites.indexOf(new Site(sindex));
		Site s = DataManager.sites.get(index);
		s.recoverSite(time);
		 List<Data> siteD = s.variables;
		 for(Data d: siteD)
		 {
			 Queue<Transaction> waitingTrans = waitingQueue.get(d);
			 Optional<Transaction> tw = waitingTrans.stream().filter(t->t.status.operation=='W').findFirst();
			 if(tw.isPresent())
			 {
				 waitingTrans.remove(tw.get());
				 activeList.add(tw.get());
				 availableCopies(tw.get().name,tw.get().getActionData(),tw.get().status.writingVal);
			 }
		 }
		
	}
	
	private static void initializeWaitingQueue()
	{
		for(int i = 1;  i<=20; i++ )
		{
			waitingQueue.put(new Data(i), new LinkedList<Transaction>());
		}
	}
	
	/*Get active transaction by name*/
	private static Transaction getActiveTransaction(String tname, List<Transaction> list, String type)
	{
		int index = list.indexOf(new Transaction(tname,type));
		if(index!=-1)
			return list.get(index);
		return null;		
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: VARADA HANUMANTE
	 * 
	 * ************************************************************************
	 */
	public static void postDeadlock(Data d) {
		Queue<Transaction> q = waitingQueue.get(d);
		while(!q.isEmpty()) {
			Transaction t = q.remove();
			if(t.status.operation.equals('R'))
				availableCopiesRead(t, d, true);
			else
				availableCopiesWrite(t, d, t.getWriteValue(), true);
		}
	}
	
	/*
	 * ************************************************************************
	 * 
	 * AUTHOR: VARADA HANUMANTE
	 * 
	 * ************************************************************************
	 */
	public static void takeSnapshot(Transaction t) {
		for(Map.Entry<Data, List<Site>> element: routes.entrySet()) {
			List<Site> sites = element.getValue();
			for(Site s: sites) {
				if(!s.checkSiteStatus('F')) {
					Data dataItem = s.getRODataItem(element.getKey());
					if(dataItem.hasCommitted) {
						t.snapshot.put(dataItem.index, dataItem.getLastCommittedVal());
						break;
					}
				}
			}
		}
	}
}
