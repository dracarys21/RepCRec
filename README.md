# RepCRec

A distributed database with multiversion concurrency control, deadlock detection, replication and failure recovery.

## Code structure:

### Our code is organized into the following modules:
	
* TransactionManager: It is responsible for keeping a track of all transactions - active and dead (Using Queue<Transaction> waitingQueue, List<Transaction> activeList, activeListRO and deadTransactions). Transactions are scheduled on a first-come-first-serve basis. Deadlock detection occurs every time a transaction goes into the waitingQueue.

* DataManager: It creates and initializes data members as well as available sites. Data are added to appropriate sites per the given policy. It also contains a HashMap - routes, which maps each of the data-indices to site(s) where the data are present.

* Sim: This is a simulator containing the main function. It parses the input file(s) line by line and calls corresponding functions of the transaction manager.

* Deadlock detector: It checks for occurrences of deadlocks every time a transaction gets blocked, i.e., enters the waitingQueue from the activeList. A waits-for graph is constructed for all the blocked transactions. Then a Depth First Search is done on this graph to break all the cycles. After blocking the youngest transaction in each cycle, it schedules the remaining transactions from the waitingQueue.

### Description of our models:

* Data class contains information about the data variables. The index is used to uniquely identify each variable. "isValid" is used to check if the data is valid upon site recovery (i.e., if at least one write operation has occurred on it). hasCommitted is used to check if the lastCommittedVal can be read by an RO transaction. When a site fails, this is changed to false and remains false until at least one transaction performs a write operation on that variable and subsequently commits. It is changed to true only after the transaction has committed.

* The Site class represents the available sites. Index is used to identify each site. There is a list of variables present on the site. It also has its own read lock table and write lock table. "upTimeStamp" indicates when the site became active. It is changed to -1 when a site fails. Upon recovery, it is changed back to store the time of recovery. Status can be either of A(active), F(failed) or R(recovered).

* Transaction class contains members and functions for the transactions occurring. Name is used to identify the transaction. "startTime" is the time at which the transaction is registered by the system and processed. SitesAccessed is a set of all the sites accessed by the transaction so far. As a result, when a site fails, we can make a decision as to whether the transaction needs to be aborted. Read and write locks possessed are acquired while execution and released together when the transaction commits or aborts. TransactionStatus is a class which stores information about the current status of the transaction (alive/blocked/dead). It also contains a member called operation which describes the operation that the transaction wants to perform (read/write).

## Assumptions
* The data consists of 20 distinct variables x1, ..., x20. There are 10 sites numbered 1 to 10. The odd indexed variables are at one site each (i.e. 1+ (index number mod 10)). For example, x3 and x13 are both at site 4. Even indexed variables are at all sites. Each variable xi is initialized to the value 10i (10 times i). Each site has an independent lock table. If that site fails, the lock table is erased.
* Input is parsed line-by-line. Algorithms cannot look ahead in the input. Each line has at most a single instruction from one transaction or a fail, recover, dump, end, etc.
* Some of these operations may be blocked due to conflicting locks. When a transaction is waiting, it will not receive another operation.
* Lock acquisition is first come first served but several transactions may hold read locks on the same item.

## Algorithms used
* Available copies for replication
* Strict two phase locking (using read and write locks) at each site and validation at commit time
* Deadlock detection using DFS with white/grey/black coloring
* Multiversion read consistency for read-only type of transactions

## Testing Assumptions

* The execution file has the following format:
begin(T1) says that T1 begins
beginRO(T3) says that T3 begins and is read-only
* R(T1, x4) says transaction 1 wishes to read x4 (provided it can get the locks or provided it doesn’t need the locks (if T1 is a read-only transaction)).
* W(T1, x6,v) says transaction 1 wishes to write all available copies of x6 (provided it can get the locks on available copies) with the value v. So, T1 can write to x6 only when T1 has locks on all sites that are up and that contain x6.
* end(T1) causes the system to report whether T1 can commit in the
format
T1 commits
or
T1 aborts
* fail(6) says site 6 fails.
* recover(7) says site 7 recovers.
* A newline in the input means time advances by one.
* Example (partial script with six steps in which transactions T1 commits,
and one of T3 and T4 may commit)
```
begin(T1)
begin(T2)
begin(T3)
W(T1, x1,5)
W(T3, x2,32)
W(T2, x1,17) // will cause T2 to wait, but the write will go ahead after T1
commits
end(T1)
begin(T4)
W(T4, x4,35)
W(T3, x5,21)
W(T4,x2,21)
W(T3,x4,23) // T4 will abort because it’s younger
```

### Some Valid Tests:
```
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
```

