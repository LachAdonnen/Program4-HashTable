///////////////////////////////////////////////////////////////////////////////
// Title:            Program4-HashTable
// Files:            HashTable.java
// Semester:         Fall 2016
//
// Author:           Alex McClain, gamcclain@wisc.edu
// CS Login:         gamcclain@wisc.edu
// Lecturer's Name:  Charles Fischer
///////////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class implements a hashtable that uses chaining for collision handling.
 * Any non-<tt>null</tt> item may be added to a hashtable.  Chains are 
 * implemented using <tt>LinkedList</tt>s.  When a hashtable is created, its 
 * initial size, maximum load factor, and (optionally) maximum chain length are 
 * specified.  The hashtable can hold arbitrarily many items and resizes itself 
 * whenever it reaches its maximum load factor or whenever it reaches its 
 * maximum chain length (if a maximum chain length has been specified).
 * 
 * Note that the hashtable allows duplicate entries.
 */
public class HashTable<T> {
    
	// Hash table array storing LinkedLists for chaining elements
	private LinkedList<T>[] hashTable;
	// Maximum load factor before resizing the hash table
	private double maxLoadFactor;
	// Maximum chain length before resizing the hash table
	private int maxChainLength;
	// Current number of items in the hash table
	private int numItems;
	// Number of times the hash table has been resized
	private int numResizes;
	// Maximum number of times to resize before disabling chain-length check
	private int maxResizes = 10000;
	
    /**
     * Constructs an empty hashtable with the given initial size, maximum load
     * factor, and no maximum chain length.  The load factor should be a real 
     * number greater than 0.0 (not a percentage).  For example, to create a 
     * hash table with an initial size of 10 and a load factor of 0.85, one 
     * would use:
     * 
     * <dir><tt>HashTable ht = new HashTable(10, 0.85);</tt></dir>
     *
     * @param initSize the initial size of the hashtable.
     * @param loadFactor the load factor expressed as a real number.
     * @throws IllegalArgumentException if <tt>initSize</tt> is less than or 
     *         equal to 0 or if <tt>loadFactor</tt> is less than or equal to 0.0
     **/
	public HashTable(int initSize, double loadFactor) {
        this(initSize, loadFactor, Integer.MAX_VALUE);
    }
    
    
    /**
     * Constructs an empty hashtable with the given initial size, maximum load
     * factor, and maximum chain length.  The load factor should be a real 
     * number greater than 0.0 (and not a percentage).  For example, to create 
     * a hash table with an initial size of 10, a load factor of 0.85, and a 
     * maximum chain length of 20, one would use:
     * 
     * <dir><tt>HashTable ht = new HashTable(10, 0.85, 20);</tt></dir>
     *
     * @param initSize the initial size of the hashtable.
     * @param loadFactor the load factor expressed as a real number.
     * @param maxChainLength the maximum chain length.
     * @throws IllegalArgumentException if <tt>initSize</tt> is less than or 
     *         equal to 0 or if <tt>loadFactor</tt> is less than or equal to 0.0 
     *         or if <tt>maxChainLength</tt> is less than or equal to 0.
     **/
	@SuppressWarnings("unchecked")
	public HashTable(int initSize, double loadFactor, int maxChainLength) {
        hashTable = (LinkedList<T>[])(new LinkedList[initSize]);
        initializeHashTable();
        this.maxLoadFactor = loadFactor;
        this.maxChainLength = maxChainLength;
    }
    
	private void initializeHashTable() {
		for (int i = 0; i < hashTable.length; i++) {
			hashTable[i] = new LinkedList<T>();  
		}
		numItems = 0;
	}
    
    /**
     * Determines if the given item is in the hashtable and returns it if 
     * present.  If more than one copy of the item is in the hashtable, the 
     * first copy encountered is returned.
     *
     * @param item the item to search for in the hashtable.
     * @return the item if it is found and <tt>null</tt> if not found.
     **/
    public T lookup(T item) {
        int hashCode = generateObjectHash(item);
        int index = hashTable[hashCode].indexOf(item);
        return hashTable[hashCode].get(index);
    }
    
    private int generateObjectHash(T item) {
    	int hashCode = item.hashCode();
    	hashCode = hashCode % hashTable.length;
    	if (hashCode < 0) { hashCode += hashTable.length; }
    	return hashCode;
    }
    
    /**
     * Inserts the given item into the hashtable.  The item cannot be 
     * <tt>null</tt>.  If there is a collision, the item is added to the end of
     * the chain.
     * <p>
     * If the load factor of the hashtable after the insert would exceed 
     * (not equal) the maximum load factor (given in the constructor), then the 
     * hashtable is resized.  
     * 
     * If the maximum chain length of the hashtable after insert would exceed
     * (not equal) the maximum chain length (given in the constructor), then the
     * hashtable is resized.
     * 
     * When resizing, to make sure the size of the table is reasonable, the new 
     * size is always 2 x <i>old size</i> + 1.  For example, size 101 would 
     * become 203.  (This guarantees that it will be an odd size.)
     * </p>
     * <p>Note that duplicates <b>are</b> allowed.</p>
     *
     * @param item the item to add to the hashtable.
     * @throws NullPointerException if <tt>item</tt> is <tt>null</tt>.
     **/
    public void insert(T item) {
        if (item == null) { throw new NullPointerException(); }
        int hashCode = generateObjectHash(item);
        hashTable[hashCode].add(item);
        numItems++;
        if (checkLoadFactor() || checkChainLength(hashCode)) {
        	do {
        		resizeHashTable();
        	} while (checkLoadFactor() || checkChainLength());
        }
    }
    
    private boolean checkLoadFactor() {
    	return (getLoadFactor() > maxLoadFactor);
    }
    
    private double getLoadFactor() {
    	return ((double)numItems / hashTable.length);
    }
    
    private boolean checkChainLength() {
    	// Disable this check if we've resized too many times
    	if (numResizes > maxResizes) { return false; }
    	for (int i = 0; i < hashTable.length; i++) {
    		if (checkChainLength(i)) { return true; } 
    	}
    	return false;
    }
    
    private boolean checkChainLength(int index) {
    	// Disable this check if we've resized too many times
    	if (numResizes > maxResizes) { return false; }
    	return (hashTable[index].size() > maxChainLength);
    }
    
    private int getMaxChainLength() {
    	int maxLength = 0;
    	for (int i = 0; i < hashTable.length; i++) {
    		if (hashTable[i].size() > maxLength) {
    			maxLength = hashTable[i].size();
    		}
    	}
    	return maxLength;
    }
    
    @SuppressWarnings("unchecked")
	private void resizeHashTable() {
		LinkedList<T>[] currHashTable = hashTable.clone();
    	hashTable = (LinkedList<T>[])
    			(new LinkedList[2 * currHashTable.length + 1]);
    	initializeHashTable();
    	for (int i = 0; i < currHashTable.length; i++) {
    		if (!currHashTable[i].isEmpty()) {
	    		Iterator<T> listIterator = currHashTable[i].iterator();
	    		while (listIterator.hasNext()) { insert(listIterator.next()); }
    		}
    	}
    	numResizes++;
    }
    
    /**
     * Removes and returns the given item from the hashtable.  If the item is 
     * not in the hashtable, <tt>null</tt> is returned.  If more than one copy 
     * of the item is in the hashtable, only the first copy encountered is 
     * removed and returned.
     *
     * @param item the item to delete in the hashtable.
     * @return the removed item if it was found and <tt>null</tt> if not found.
     **/
    public T delete(T item) {
        T objToRemove = null;
    	int hashCode = generateObjectHash(item);
        int index = hashTable[hashCode].indexOf(item);
        if (index >= 0) {
        	objToRemove = hashTable[hashCode].remove(index);
        	numItems--;
        }
        return objToRemove;
    }
    
    
    /**
     * Prints all the items in the hashtable to the <tt>PrintStream</tt> 
     * supplied.  The items are printed in the order determined by the index of
     * the hashtable where they are stored (starting at 0 and going to 
     * (table size - 1)).  The values at each index are printed according 
     * to the order in the <tt>LinkedList</tt> starting from the beginning. 
     *
     * @param out the place to print all the output.
     **/
    public void dump(PrintStream out) {
    	out.println("Hashtable contents:");
    	for (int i = 0; i < hashTable.length; i++) {
    		if (!hashTable[i].isEmpty()) {
    			out.print(i + ": [");
	    		Iterator<T> listIterator = hashTable[i].iterator();
	    		while (listIterator.hasNext()) {
	    			out.print(listIterator.next());
	    			if (listIterator.hasNext()) { out.print(", "); }
	    		}
	    		out.println("]");
    		}
    	}
    }
    
  
    /**
     * Prints statistics about the hashtable to the <tt>PrintStream</tt> 
     * supplied.  The statistics displayed are: 
     * <ul>
     * <li>the current table size
     * <li>the number of items currently in the table 
     * <li>the current load factor
     * <li>the length of the largest chain
     * <li>the number of chains of length 0
     * <li>the average length of the chains of length > 0
     * </ul>
     *
     * @param out the place to print all the output.
     **/
    public void displayStats(PrintStream out) {
        out.println("Hashtable statistics:");
        
        out.printf("%-28s", "  current table size:");
        out.println(hashTable.length);
        
        out.printf("%-28s", "  # items in table:");
        out.println(numItems);
        
        out.printf("%-28s", "  current load factor:");
        out.println(getLoadFactor());
        
        out.printf("%-28s", "  longest chain length:");
        out.println(getMaxChainLength());
        
        int numNonEmptyChains = 0;
        for (int i = 0; i < hashTable.length; i++) {
        	if (!hashTable[i].isEmpty()) { numNonEmptyChains++; }
        }
        
        out.printf("%-28s", "  # 0-length chains:");
        int numEmptyChains = hashTable.length - numNonEmptyChains;
        out.println(numEmptyChains);
        
        out.printf("%-28s", "  avg (non-0) chain length:");
        double avgChainLength = (double)numItems / numNonEmptyChains;
        out.println(avgChainLength);
    }
}
