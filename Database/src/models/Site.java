/**
 * 
 */
package models;

import java.util.*;
/**
 * @author varada
 *
 */
public class Site {
	public final int index; //for identifying each site:
	
	public List<Data> variables;
	
	private Map<Data, Boolean> readLockTable; // need to keep track of transaction acquiring locks for integrity
	private Map<Data, Boolean> writeLockTable;
	int upTimeStamp;	//Time of becoming active
	char status; //active, failed , recovered
	
	public Site(int i, List<Data> var)
	{
		variables = var;
		readLockTable = new HashMap<>();
		writeLockTable = new HashMap<>();
		upTimeStamp = 0;
		status = 'A';
		index = i;
		initializeLockTable();
	}
	
	public boolean checkSiteStatus(char c)
	{
		return status==c;
	}
	
	public boolean isReadLockAvailable(int xid)
	{
		Data x = getVariable(xid);
		return !writeLockTable.get(x);		
	}
	
	public boolean isWriteLockAvailable(int xid)
	{
		Data x = getVariable(xid);
		return !(writeLockTable.get(x)|| readLockTable.get(x));
	}
	
	public void setReadLock(int xid)
	{
		Data x = getVariable(xid);
		readLockTable.put(x, true);
	}
	
	public void setWriteLock(int xid)
	{
		Data x = getVariable(xid);
		writeLockTable.put(x, true);
	}
	
	private void initializeLockTable()
	{
		for(Data d: variables)
		{
			readLockTable.put(d,false);
			writeLockTable.put(d,false);
		}
	}
	
	public void failSite()
	{
		
	}
	
	public void recoverSite()
	{
		
	}
	
	public void activeSite()
	{
		
	}
	
	private Data getVariable(int idata)
	{
		Optional<Data> tr =  variables.stream().filter(d -> d.index == idata).findFirst();
		if(tr.isPresent())
			return tr.get();
		return null;		
	}
}
