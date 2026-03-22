package memory;

import java.util.LinkedList;

import util.Const;
/**
 * Cache — 16-line fully associative unified cache.
 * Uses FIFO replacement policy via a LinkedList.
 * addFirst() inserts new entries at front.
 * removeLast() evicts the oldest entry when full.
 */
public class Test {
	/**
     * One cache line stores a memory address (tag) and its value (data).
     */
	public class CacheLine {

		int tag;
		int data;

		public CacheLine(int tag, int data) {
			this.tag = tag;
			this.data = data;
		}

		public int getTag() {
			return this.tag;
		}

		public void setTag(int tag) {
			this.tag = tag;
		}

		public int getData() {
			return data;
		}

		public void setData(int data) {
			this.data = data;
		}
	}

	LinkedList<CacheLine> cacheLines;

	public Test() {
		this.cacheLines = new LinkedList<CacheLine>();
	}

	public LinkedList<CacheLine> getCacheLines() {
		return cacheLines;
	}
	/**
     * Add a new entry to the cache.
     * If cache is full (16 lines), evict the oldest line (removeLast).
     * New entry always goes to front (addFirst = most recently used).
     */

	public void add(int address, int value) {
		if (this.cacheLines.size() >= Const.CACHE_LINES) {
			this.cacheLines.removeLast();
		}
		this.cacheLines.addFirst(new CacheLine(address, value));
	}

}
