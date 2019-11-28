/**
 * 
 */
package models;

/**
 * @author varada
 *
 */
public class Data implements Comparable<Data>{
	
	public final int index; //for identification of data variable
	int currentVal;
	int lastCommittedVal;
	
	public Data( int i, int initialVal){
		currentVal = initialVal;
		lastCommittedVal = initialVal;
		index = i;
	}
	
	public Data(int i)
	{
		index = i;
	}
	
	public Data(Data d)
	{
		index = d.index;
		currentVal = d.currentVal;
		lastCommittedVal = d.lastCommittedVal;
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
	public int hashCode()
	{
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
