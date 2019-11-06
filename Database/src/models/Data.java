/**
 * 
 */
package models;

/**
 * @author varada
 *
 */
public class Data {
	int currentVal;
	int lastCommittedVal;
	//Read and write lockes required here???
//	boolean readLock;
//	boolean writeLock;
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
