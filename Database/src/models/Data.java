/**
 * 
 */
package models;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author varada
 *
 */
public class Data implements Comparable<Data>{
	
	public final int index; //for identification of data variable
	int currentVal;
	int lastCommittedVal;
	boolean isValid;
	
	public Data( int i, int initialVal){
		currentVal = initialVal;
		lastCommittedVal = initialVal;
		index = i;
		isValid=true;
	}
	
	public Data(int i)
	{
		index = i;
	}
	
	public Data(Data d)
	{
		this.index = d.index;
		this.currentVal = d.currentVal;
		this.lastCommittedVal = d.lastCommittedVal;
		isValid = d.isValid;
	}
	
	public int getCurrentVal() {
		return currentVal;
	}
	public void setCurrentVal(int currentVal) {
		this.currentVal = currentVal;
	}
	public int getLastCommittedVal() {
		return lastCommittedVal;
	}
	public void setLastCommittedVal(int lastCommittedVal) {
		this.lastCommittedVal = lastCommittedVal;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o==this)
			return true;
		if(o==null || o.getClass()!=this.getClass())
			return false;
		Data d = (Data)o;
		return d.index==this.index;
				
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1; 
		result = prime * result +  index;
		return result;
	}
	
	
	@Override
	public int compareTo(Data d) {
		return Integer.compare(index, d.index);
	}
	
}
