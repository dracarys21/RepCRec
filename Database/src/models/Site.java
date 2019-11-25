/**
 * 
 */
package models;

import java.util.List;
import java.util.Map;
/**
 * @author varada
 *
 */
public class Site {
	List<Data> variables;
	private Map<Data, Boolean> readLockTable; // need to keep track of transaction acquiring locks for integrity
	private Map<Data, Boolean> writeLockTable;
	int upTimeStamp;	//Time of becoming active
	char status; //active, failed , recovered
	
	public boolean isReadLockAvailable(Data x)
	{
		return !writeLockTable.get(x);		
	}
	
	public boolean isWriteLockAvailable(Data x)
	{
		return !(writeLockTable.get(x)|| readLockTable.get(x));
	}
	
	public void setReadLock(Data x)
	{
		readLockTable.put(x, true);
	}
	
	public void setWriteLock(Data x)
	{
		writeLockTable.put(x, true);
	}
	
	public boolean isSiteActive()
	{
		return status=='A';
	}
}
