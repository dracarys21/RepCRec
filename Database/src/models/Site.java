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
	
	private Map<Data, Transaction> readLockTable; // need to keep track of transaction acquiring locks for integrity
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
		return (writeLockTable.get(d)==null && readLockTable.get(d)==null);
	}
	
	public void setReadLock(Data d, Transaction t)
	{
		readLockTable.put(d, t);
	}
	
	public void releaseReadLock(Data d)
	{
		readLockTable.put(d,null);
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
			readLockTable.put(d,null);
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
		addToBeAbortedTransaction(readLockTable,transToBeAborted);
		addToBeAbortedTransaction(writeLockTable,transToBeAborted);
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
		Iterator<Entry<Data, Transaction>> i = m.entrySet().iterator();
		while(i.hasNext())
		{
			Map.Entry<Data, Transaction> e = i.next();
			if(e.getValue()!=null)
				ans.add(e.getValue());
			m.put(e.getKey(),null);
		}	
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
