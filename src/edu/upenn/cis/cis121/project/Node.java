package edu.upenn.cis.cis121.project;

/**
 * @author jdesai
 * Node class used to represent vertices in an undirected graph (used to model friendships). Each node represends a user.
 */
public class Node implements Comparable<Node> {
	
	/**
	 * The ID of the user represented by this node.
	 */
	private int _userID;
	
	/**
	 * This variable stores the distance of this Node from the target Node in Dijkstra's algorithm, as well as the geographical
	 * distance of this Node from the target Node in the recommendActivites method in the NetworkAlgorithms class.
	 * The reason I chose to store two types of values in the same variable is because this variable is the basis of which Nodes are
	 * compared, which means that algorithms that use PriorityQueues will not work for one or the other if I have two variables, and
	 * this seems a lot simpler than any other fix for that. I've taken care to ensure that there's no conflict, as the variable is
	 * set to the appropriate starting value at the beginning of each algorithm.
	 */
	private double _distance;
	
	/**
	 * Whether or not the node has been visited in a breadth-first search.
	 */
	private boolean _visited;
	
	/**
	 * Constructs a new Node representing the user with the input ID.
	 * @param ID the ID of the user to build a Node for.
	 */
	public Node(int ID) {
		_userID = ID;
		_visited = false;
		_distance = 0;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Node arg0) {
		if (_distance < arg0._distance) {
			return -1;
		} else if (_distance == arg0._distance) {
			if (_userID < arg0._userID) {
				return -1;
			} else if (_userID == arg0._userID) {
				return 0;
			}
			return 1;
		}
		return 1;
	}
	
	/** (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		if (arg0 instanceof Node) {
			return ((Node) arg0)._userID == _userID;
		}
		return false;
	}
	
	/** user ID getter
	 * @return the ID of the user represented by this node
	 */
	public int getID() {
		return _userID;
	}
	
	/** _visited getter
	 * @return true if this node has been visited in a breadth-first search, false otherwise.
	 */
	public boolean isVisited() {
		return _visited;
	}
	
	/** _visited setter.
	 * @param visit Whether or not this node has been visited in a breadth-first search.
	 */
	public void setVisited(boolean visit) {
		_visited = visit;
	}
	
	/** _distance getter.
	 * @return The distance of this node (see instance variable description for _distance).
	 */
	public double getDistance() {
		return _distance;
	}
	
	/** _distance setter.
	 * @param d The new distance.
	 */
	public void setDistance(double d) {
		_distance = d;
	}
	
}
