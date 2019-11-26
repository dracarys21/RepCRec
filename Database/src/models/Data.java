/**
 * 
 */
package models;

/**
 * @author varada
 *
 */
public class Data {
	
	public final int index; //for identification of data variable
	int currentVal;
	int lastCommittedVal;
	
	public Data( int i, int initialVal){
		currentVal = initialVal;
		lastCommittedVal = initialVal;
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
}
