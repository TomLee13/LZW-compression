package edu.cmu.andrew.mingyan2;

public class HashMap {

	private final static int HASH_SIZE = 127;
	private final static int ASCII_SIZE = 256;
	private Node head[];
	private final static int MAP_INIT_SIZE = 4096; // 2^12
	private int count;
	// map[] is used to store the String value of a certain key,
	// making it faster to search
	String map[];

	private class Node {
		int key;
		String value;
		Node next;

		public Node(int k, String s) {
			key = k;
			value = s;
		}
	}

	public HashMap() {
		reset();
	}

	public String getValue(int key) {
		return map[key];
	}

	// put the String to the HashMap
	public int put(String s) {
		if (count == MAP_INIT_SIZE) {
			reset();
		}

		int index = s.charAt(0) % HASH_SIZE;
		if (head[index] == null) {
			head[index] = new Node(count++, s);
			map[count - 1] = s;
			return head[index].key;
		}
		if (head[index].value.equals(s))
			return head[index].key;
		return put(s, head[index]);
	}

	private int put(String s, Node node) {
		if (node.next == null) {
			node.next = new Node(count++, s);
			map[count - 1] = s;
			return node.next.key;
		}
		if (node.next.value.equals(s))
			return node.next.key;
		return put(s, node.next);
	}

	// find the String in the hashmap,
	// return the key of the String to be found
	public int find(String s) {
		int index = s.charAt(0) % HASH_SIZE;
		if (head[index] == null) {
			return -1;
		}
		if (head[index].value.equals(s))
			return head[index].key;
		return find(s, head[index]);
	}

	private int find(String s, Node node) {
		if (node.next == null) {
			return -1;
		}
		if (node.next.value.equals(s))
			return node.next.key;
		return find(s, node.next);
	}

	// reset the hashmap
	private void reset() {
		head = new Node[HASH_SIZE];
		map = new String[MAP_INIT_SIZE];
		count = 0;
		String s;
		for (int i = 0; i < ASCII_SIZE; i++) {
			s = "" + (char) i;
			put(s);
		}
	}
}
