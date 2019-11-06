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
	Map<Data, Boolean> readLockTable;
	Map<Data, Boolean> writeLockTable;
	int upTimeStamp;	//Time of becoming active
	boolean isActive;
}
