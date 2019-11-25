/**
 * 
 */
package models;

import java.util.HashSet;
import java.util.List;

/**
 * @author varada
 *
 */
public class Transaction {
	public final String name;
	int startTime;
	List<Site> sitesAccessed;
	public char status;	//active/blocked/dead
	
	 private final String type;
	
	public Transaction(String name, int time, String t)
	{
		this.name = name;
		startTime = time;
		type = t;
		status = 'A';
	}
	
	public String getType()
	{
		return type;
	}
	
}
