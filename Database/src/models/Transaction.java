/**
 * 
 */
package models;

import java.util.List;

/**
 * @author varada
 *
 */
public class Transaction {
	int startTime;
	List<Site> sitesAccessed;
	char status;	//active/blocked/dead
}
