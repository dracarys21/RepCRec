/**
 * @author varada
 *
 */
package models;

public class TransactionStatus{
	char status; //A: alive, D: dead, B: blocked
	public Character operation;	//read/write -> type of operation that the transaction wants to do
	Data variable;
	int writingVal;
	public TransactionStatus(char s, Character o, Data d)
	{
		status = s;
		operation = o;
		variable = d;
		writingVal = Integer.MAX_VALUE;
	}
	
	public TransactionStatus(char s, Character o, Data d, int wVal)
	{
		status = s;
		operation = o;
		variable = d;
		writingVal = wVal;
	}
	
}
