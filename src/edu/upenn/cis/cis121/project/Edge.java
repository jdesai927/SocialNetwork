package edu.upenn.cis.cis121.project;

/**
 * @author jdesai
 * Class representing an edge connecting two nodes in an undirected graph.
 */
public class Edge {
	
	/**
	 * @author jdesai
	 * Enum used to enumerate possible edge states during breadth-first search.
	 */
	public enum Label {
		UNEXPLORED, DISCOVERY, CROSS
	}
	
	/**
	 * The first node in this edge.
	 */
	private Node _node1;
	
	/**
	 * The second node in this edge.
	 */
	private Node _node2;
	
	/**
	 * The breadth-first search exploration state of this edge.
	 */
	private Label _label;
	
	/**
	 * The weight of this edge.
	 */
	private double _weight;
	
	/**
	 * Constructs an unexplored edge between two nodes with 0 weight.
	 * @param node1 First node.
	 * @param node2 Second node.
	 */
	public Edge(Node node1, Node node2) {
		_node1 = node1;
		_node2 = node2;
		_label = Label.UNEXPLORED;
		_weight = 0;
	}
	
	/** _label value getter
	 * @return the exploration label of this edge
	 */
	public Label getLabel() {
		return _label;
	}
	
	/**_label value setter
	 * @param l new exploration label value
	 */
	public void setLabel(Label l) {
		_label = l;
	}
	
	/** edge weight getter
	 * @return edge weight
	 */
	public double getWeight() {
		return _weight;
	}
	
	/** edge weight setter
	 * @param w new edge weight
	 */
	public void setWeight(double w) {
		_weight = w;
	}
	
	/** Gets the first node of this edge.
	 * @return the first node of this edge.
	 */
	public Node getNode1() {
		return _node1;
	}
	
	/** Gets the second node of this edge.
	 * @return the second node of this edge.
	 */
	public Node getNode2() {
		return _node2;
	}
	
	/**
	 * Gets the node at the other side of an edge.
	 * @param n one node on the edge
	 * @return the node at the side of the edge opposite the input node.
	 */
	public Node getOpposite(Node n) {
		if (_node1 == n) {
			return _node2;
		} 
		if (_node2 != n) {
			throw new IllegalArgumentException("Node not present in the input edge");
		}
		return _node1;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		if (arg0 instanceof Edge) {
			Edge e = (Edge) arg0;
			if ((_node1.getID() == e._node1.getID() && e._node2.getID() == _node2.getID()) ||
					_node1.getID() == e._node2.getID() && e._node1.getID() == _node2.getID()) {
				return true;
			}
		}
		return false;
	}
	
}
