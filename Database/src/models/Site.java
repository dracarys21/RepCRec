/**
 * 
 */
package models;

import java.util.*;
import java.util.Map.Entry;
/**
 * @author varada
 *
 */
public class Site implements Comparable<Site>{
	public final int index; //for identifying each site:
	
	public List<Data> variables;

	private Map<Data, List<Transaction>> readLockTable; // need to keep track of transaction acquiring locks for integrity
	private Map<Data, Transaction> writeLockTable;
	public int upTimeStamp;	//Time of becoming active
	char status; //active, failed , recovered
	
	public Site(int i, ArrayList<Data> var)
	{
		variables = new ArrayList<>(var);
		readLockTable = new HashMap<>();
		writeLockTable = new HashMap<>();
		upTimeStamp = 0;
		status = 'A';
		index = i;
		initializeLockTable();
	}
	
	public Site(int i)
	{
		index = i;
	}
	
	public boolean checkSiteStatus(char c)
	{
		return status==c;
	}
	
	public boolean isReadLockAvailable(Data d)
	{
		int index = variables.indexOf(d);
		Data data = variables.get(index);
		return (writeLockTable.get(d)==null)&&(data.isValid||data.index%2==1);		
	}
	
	public boolean isWriteLockAvailable(Data d)
	{
		return (writeLockTable.get(d)==null && readLockTable.get(d).isEmpty());
	}
	
	public void setReadLock(Data d, Transaction t)
	{
		List<Transaction> l = readLockTable.get(d);
		l.add(t);
		readLockTable.put(d, l);
	}
	
	public void releaseReadLock(Data d, Transaction t)
	{
		List<Transaction> l = readLockTable.get(d);
		l.remove(t);
		readLockTable.put(d,l);
	}
	
	public void releaseWriteLock(Data d)
	{
		writeLockTable.put(d,null);
	}
	public void setWriteLock(Data d, Transaction t)
	{
		writeLockTable.put(d, t);
	}
	
	public boolean checkWriteLock(Data d, Transaction t)
	{
		return writeLockTable.get(d)==t;
	}
	
	private void initializeLockTable()
	{
		for(Data d: variables)
		{
			readLockTable.put(d,new ArrayList<>());
			writeLockTable.put(d,null);
		}
	}
	public int getCurrentData(Data d)
	{
		int index = variables.indexOf(d);
		return variables.get(index).currentVal;
	}
	public void setData(Data d, int v)
	{
		int index = variables.indexOf(d);
		Data data = variables.get(index);
		data.currentVal = v;
		data.isValid = true;
	}
	public void commitData(Data d)
	{
		int index = variables.indexOf(d);
		Data data = variables.get(index);
		data.lastCommittedVal = data.currentVal;
	}
	public HashSet<Transaction> failSite()
	{
		status = 'F';
		upTimeStamp = -1;
		for(Data v: variables)
		{
			v.isValid = false;
		}
		
		HashSet<Transaction> transToBeAborted = new HashSet<>();
		
		Iterator<Entry<Data, List<Transaction>>> itr = readLockTable.entrySet().iterator();
		
		while(itr.hasNext())
		{
			Map.Entry<Data, List<Transaction>> e = itr.next();
			if(!e.getValue().isEmpty())
			{
				for(Transaction lt: e.getValue())
					transToBeAborted.add(lt);
			}
			readLockTable.put(e.getKey(),new ArrayList<>());
		}
			
		Iterator<Entry<Data, Transaction>> i = writeLockTable.entrySet().iterator();
		while(i.hasNext())
		{
			Map.Entry<Data, Transaction> e = i.next();
			if(e.getValue()!=null)
				transToBeAborted.add(e.getValue());
			writeLockTable.put(e.getKey(),null);
		}
		return transToBeAborted;
	}
	
	public void recoverSite(int timeStamp)
	{
		status = 'R';
		upTimeStamp = timeStamp;
	}
	
	public void activeSite(int timeStamp)
	{
		upTimeStamp = timeStamp;
		status = 'A';
	}
	
	private void addToBeAbortedTransaction(Map<Data, Transaction> m, HashSet<Transaction> ans)
	{
		
	}
	
	@Override
	public boolean equals(Object o) {
		if(o==this)
			return true;
		if(o==null || o.getClass()!=this.getClass())
			return false;
		Site d = (Site)o;
		return d.index==this.index;
				
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1; 
		result = prime * result +  index;
		return result;
	}
	
	
	@Override
	public int compareTo(Site s) {
		return Integer.compare(index, s.index);
	}
}
