RepCRec

Team: Pranali Awasekar(pa1373) and Varada Hanumante(vsh231)
Replicated Concurrency Control and Recovery


Code structure:

Our code is organized into the following modules:
	1. TransactionManager: It is responsible for keeping a track of all transactions - active and dead (Using Queue<Transaction> waitingQueue, List<Transaction> activeList, activeListRO and deadTransactions). Transactions are scheduled on a first-come-first-serve basis. Deadlock detection occurs every time a transaction goes into the waitingQueue.

	2. DataManager: It creates and initializes data members as well as available sites. Data are added to appropriate sites per the given policy. It also contains a HashMap - routes, which maps each of the data-indices to site(s) where the data are present.

	3. Sim: This is a simulator containing the main function. It parses the input file(s) line by line and calls corresponding functions of the transaction manager.

	4. Deadlock detector: It checks for occurrences of deadlocks every time a transaction gets blocked, i.e., enters the waitingQueue from the activeList. A waits-for graph is constructed for all the blocked transactions. Then a Depth First Search is done on this graph to break all the cycles. After blocking the youngest transaction in each cycle, it schedules the remaining transactions from the waitingQueue.


Workload distribution:

TransactionManager along with most of its member functions have been implemented by Pranali Awasekar. This includes implementation of the available copies algorithm. She has also written the main function in the simulator class which parses the input file and invokes functions of the transaction manager.
Multiversion Read along with deadlock detection have been implemented by Varada Hanumante. Models were initially designed by Varada Hanumante, but were revised by both the team members as needed, to accommodate changes and ensure adherence to object oriented programming principles.


Data
index: int
currentVal:  int
lastCommittedVal:  int
isValid:  boolean
hasCommitted:  boolean

Data()
@Override equals()
@Override hashCode()
@Override compareTo()Transaction
name: String
startTime: int
sitesAccessed: HashSet<Site>
status: TransactionStatus
readLocksPossesed: Map<Data, Site>
writeLockPossesed: Map<Data, List<Site>>
type: String
snapshot: Map<Integer, Integer>

Transaction()
@Override equals()
@Override hashCode()
@Override compareTo()
changeStatusToActive()
changeStatusToBlocked()
changeStatusToDead()
checkAction() Models:
Site
index: int
variables: List<Data>
readLockTable: Map<Data, List<Transaction>>
WriteLockTable: Map<Data, Transaction>
upTimeStamp: int
status: char

Site()
failSite()
recoverSite()
activeSite()
@Override equals()
@Override hashCode()
@Override compareTo()
















Description of our models:
1. Data class contains information about the data variables. The index is used to uniquely identify each variable. "isValid" is used to check if the data is valid upon site recovery (i.e., if at least one write operation has occurred on it). hasCommitted is used to check if the lastCommittedVal can be read by an RO transaction. When a site fails, this is changed to false and remains false until at least one transaction performs a write operation on that variable and subsequently commits. It is changed to true only after the transaction has committed.

2. The Site class represents the available sites. Index is used to identify each site. There is a list of variables present on the site. It also has its own read lock table and write lock table. "upTimeStamp" indicates when the site became active. It is changed to -1 when a site fails. Upon recovery, it is changed back to store the time of recovery. Status can be either of A(active), F(failed) or R(recovered).

3. Transaction class contains members and functions for the transactions occurring. Name is used to identify the transaction. "startTime" is the time at which the transaction is registered by the system and processed. SitesAccessed is a set of all the sites accessed by the transaction so far. As a result, when a site fails, we can make a decision as to whether the transaction needs to be aborted. Read and write locks possessed are acquired while execution and released together when the transaction commits or aborts. TransactionStatus is a class which stores information about the current status of the transaction (alive/blocked/dead). It also contains a member called operation which describes the operation that the transaction wants to perform (read/write).


Test cases:
1. begin(T1)
begin(T2)
begin(T3)
R(T1,x1)
W(T2,x2,40)
W(T1,x2,60)
begin(T4)
W(T3,x3,10)
R(T2,x3)
R(T4,x4)
W(T3,x1,77)
begin(T5)
W(T5,x5,55)
W(T5,x4,80)
R(T4,x5)
end(T2)
end(T1)
end(T3)
end(T4)
end(T5)
dump()

2. begin(T1)
begin(T2)
begin(T3)
R(T1,x1)
W(T2,x2,40)
W(T1,x2,60)
begin(T4)
W(T3,x3,10)
R(T2,x3)
R(T4,x4)
W(T3,x1,77)
begin(T5)
W(T5,x5,55)
W(T4,x1,44)
end(T2)
R(T1,x5)
W(T5,x4,80)
end(T1)
end(T3)
end(T4)
end(T5)

3. begin(T1)
fail(2)
beginRO(T2)
R(T2,x1)
R(T2,x2)
W(T1,x3,33)
end(T1)
R(T2,x3)
end(T2)

4. begin(T1)
beginRO(T2)
fail(2)
R(T2,x1)
R(T2,x2)
W(T1,x4,33)
end(T1)
R(T2,x3)
end(T2)

5. begin(T1)
beginRO(T2)
fail(2)
R(T2,x1)
R(T2,x2)
W(T1,x4,33)
end(T1)
R(T2,x3)
end(T2)
begin(T3)
beginRO(T4)
W(T3,x1,67)
R(T4,x2)
recover(2)
R(T3,x1)
R(T4,x1)
end(T3)
end(T4)

