package koda.tanks;

import java.util.ArrayList;

public class Node implements Comparable<Node> {

	public ArrayList<Node> neighbors = new ArrayList<Node>();
	public Node cameFrom;
	public int type;
	public int priority;
	public int row;
	public int col;
	public int index;
	
	
	public Node(int type, int row, int col, int index) {
		this.type = type;
		this.row = row;
		this.col = col;
		this.index = index;
	}
	
	public void addNeighbor(Node n) {
		neighbors.add(n);
	}

	@Override
	public int compareTo(Node n) {
		return priority - n.priority;
	}
	
	@Override
	public String toString() {
		return "(" + col + ", " + row + ")";
	}
}
