package memory;

import java.util.LinkedList;

import util.Const;

public class Test {

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

	public void add(int address, int value) {
		if (this.cacheLines.size() >= Const.CACHE_LINES) {
			this.cacheLines.removeLast();
		}
		this.cacheLines.addFirst(new CacheLine(address, value));
	}

}
