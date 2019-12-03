/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author varada
 *
 */
class TransactionStatus{
	char status; //A: alive, D: dead, B: blocked
	Character operation;	//read/write -> type of operation that the transaction is waiting for
	Data variable;
	int writingVal;
	TransactionStatus(char s, Character o, Data d)
	{
		status = s;
		operation = o;
		variable = d;
		writingVal = Integer.MAX_VALUE;
	}
	
	TransactionStatus(char s, Character o, Data d, int wVal)
	{
		status = s;
		operation = o;
		variable = d;
		writingVal = wVal;
	}
	
}
public class Transaction implements Comparable<Transaction>{
	public final String name;
	public final int startTime;
	public HashSet<Site> sitesAccessed = new HashSet<Site>();
	private TransactionStatus status;	//active/blocked/dead
	//list of locks held by it...
	public HashMap<Data,Site> readLocksPossesed = new HashMap<Data,Site>();
	public HashSet<Data> writeLockPossesed = new HashSet<Data>();
	private final String type;
	
	public Transaction(String name, int time, String t)
	{
		this.name = name;
		startTime = time;
		type = t;
		status = new TransactionStatus('A',null,null);
	}
	
	public Transaction(String tname, String t)
	{
		name  = tname;
		type = t;
		startTime = -1;
	}
	public String getType()
	{
		return type;
	}
	
	public Data getActionData()
	{
		return status.variable; 
	}
	
	public void changeStatusToActive()//do I need Data d and o for keeping Active Transaction track??
	{
		status = new TransactionStatus('A', null, null);
	}
	
	public void changeStatusToBlocked(Data d, char o)
	{
		status = new TransactionStatus('B', new Character(o), d);
	}
	
	public void changeStatusToBlocked(Data d, char o, int wVal)
	{
		status = new TransactionStatus('B', new Character(o), d, wVal);
	}
	
	public int getWriteValue()
	{
		return status.writingVal;
	}
	
	public void changeStatusToDead()
	{
		status = new TransactionStatus('D', null, null);
		readLocksPossesed.clear();
		writeLockPossesed.clear();
	}
	
	public boolean checkAction(char a)
	{
		return status.operation.equals(new Character(a));		
	}
	
	@Override
	public boolean equals(Object o) {
		if(o==this)
			return true;
		if(o==null || o.getClass()!=this.getClass())
			return false;
		Transaction t = (Transaction)o;
		return name.equals(t.name);
				
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 17; 
		result = prime * result +  name.hashCode();
		return result;
	}
	
	@Override
	public int compareTo(Transaction t) {
		return name.compareTo(t.name);
	}
	
}
