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
	public int upTimeStamp;	//Time of becoming active
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
	
	public boolean isReadLockAvailable(Data d)
	{
		return !writeLockTable.get(d);		
	}
	
	public boolean isWriteLockAvailable(Data d)
	{
		return !(writeLockTable.get(d)|| readLockTable.get(d));
	}
	
	public void setReadLock(Data d)
	{
		readLockTable.put(d, true);
	}
	
	public void setWriteLock(Data d)
	{
		writeLockTable.put(d, true);
	}
	
	private void initializeLockTable()
	{
		for(Data d: variables)
		{
			readLockTable.put(d,false);
			writeLockTable.put(d,false);
		}
	}
	public int getData(Data d)
	{
		int index = variables.indexOf(d);
		return variables.get(index).currentVal;
	}
	public void setData(Data d, int v)
	{
		int index = variables.indexOf(d);
		Data data = variables.get(index);
		data.currentVal = v;	
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
}
