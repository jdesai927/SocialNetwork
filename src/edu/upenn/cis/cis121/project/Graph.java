package edu.upenn.cis.cis121.project;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author jdesai
 * Class that represents an undirected, weighted graph of Nodes representing users (in which edges represent friendships. In distance
 * calculation, edge weights are not considered. Edge weights are computed as needed and therefore not calculated in the constructor.
 */
public class Graph {

	/**
	 * Adjacency data structure mapping each node to the edges incident to it.
	 */
	private HashMap<Node, ArrayList<Edge>> _edges;
	
	/**
	 * Database wrapper class to quickly get information from the database.
	 */
	public DBWrapper _dbw;
	
	/**
	 * Constructor that creates a new Graph object.
	 * @param user
	 * @param pass
	 * @param SID
	 * @param host
	 * @param port
	 * @throws SQLException if any parameter is invalid/incorrect.
	 */
	public Graph(String user, String pass, String SID, String host, int port) {
		_edges = new HashMap<Node, ArrayList<Edge>>();
		String queryAll = "select user_id from Users";
		_dbw = new DBWrapper(user, pass, SID, host, port);
		int[] allIDs = null;
		try {
			allIDs = _dbw.getOneColumn(queryAll);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ArrayList<Edge> al = null;
		//Populate the graph.
		for (int i = 0; i < allIDs.length; i++) {
			Node n1 = new Node(allIDs[i]);
			int[] nFriends = _dbw.getFriends(allIDs[i]);
			if (nFriends.length == 0) {
				_edges.put(n1, new ArrayList<Edge>());
			}
			for (int k = 0; k < nFriends.length; k++) {
				Node n2 = new Node(nFriends[k]);
				int found = 0;
				for (Node ch : _edges.keySet()) {
					//Makes sure not to insert repeated nodes.
					if (ch.getID() == allIDs[i]) {
						n1 = ch;
						found++;
						if (found == 2) {
							break;
						}
						continue;
					}
					if (ch.getID() == nFriends[k]) {
						n2 = ch;
						found++;
						if (found == 2) {
							break;
						}
					}
				}
				//Updates adjacency lists
				Edge e = new Edge(n1, n2);
				if (_edges.get(n1) == null) {
					al = new ArrayList<Edge>();
				} else {
					al = _edges.get(n1);	
				}
				if (!al.contains(e)) {
					al.add(e);
				}
				_edges.put(n1, al);
				if (_edges.get(n2) == null) {
					al = new ArrayList<Edge>();
				} else {
					al = _edges.get(n2);
				}
				if (!al.contains(e)) {
					al.add(e);
				}
				_edges.put(n2, al);
			}
		}
		System.out.println("Graph constructed!");
	}
	
	/**
	 * Method that gets a list of edges incident to a particular node.
	 * @param n the node whose adjacent edges are to be found.
	 * @return an ArrayList of edges adjacent to the input node.
	 */
	public ArrayList<Edge> incidentEdges(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("Null node entered");
		}
		return _edges.get(n);
	}
	
	/**
	 * Gets the node in the graph representing the user with the given ID.
	 * @param userID the ID of the user whose node is to be found.
	 * @return the node representing the input user.
	 */
	public Node getNode(int userID) {
		Set<Node> nodes = _edges.keySet();
		for (Node n : nodes) {
			if (n.getID() == userID) {
				return n;
			}
		}
		throw new IllegalArgumentException("Invalid user_id");
	}
	
	/**
	 * Sets the weight of an edge. This is based on the formula in the homework documentation.
	 * @param e the edge whose weight is to be set.
	 */
	public void setEdgeWeight(Edge e) {
		//Query to get the places liked by both users on either side of this edge
		String q1 = "select place_id from Likes where user_id = " + e.getNode1().getID() + 
				" intersect select place_id from Likes where user_id = " + e.getNode2().getID();
		
		//Queries that get the list of place types likes by each user.
		String q2 = "select type_id from Likes, Places where Likes.user_id = " + e.getNode1().getID() + 
				" and Places.place_id = Likes.place_id";
		String q3 = "select type_id from Likes, Places where Likes.user_id = " + e.getNode2().getID() + 
				" and Places.place_id = Likes.place_id";
		
		double commonPlaces = 0;
		ArrayList<Integer> types1 = new ArrayList<Integer>();
		ArrayList<Integer> types2 = new ArrayList<Integer>();
		try {
			ResultSet rs = _dbw._statement.executeQuery(q1);
			while(rs.next())  {
				//Counts the number of common places, which is just the number of rows returned by the query.
				commonPlaces++;
			}
			rs = _dbw._statement.executeQuery(q2);
			while(rs.next()) {
				types1.add(rs.getInt(1));
			}
			rs = _dbw._statement.executeQuery(q3);
			while(rs.next()) {
				types2.add(rs.getInt(1));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		//finds the number of common types between the lists of types
		double commonTypes = 0;
		Integer in = null;
		int counter = 0;
		while (counter < types1.size()) {
			in = types1.get(counter);
			if (types2.contains(in)) {
				commonTypes++;
				types2.remove(in);
			}
			counter++;
		}
		
		//Sets the edge weight according to the formula.
		e.setWeight((1.0/(commonPlaces + (0.1 * commonTypes) + 0.01)));
	}
	
	/**
	 * Checks if two users are friends.
	 * @param user_id1 the first user.
	 * @param user_id2 the second user.
	 * @return true if the users are friends, false otherwise.
	 */
	public boolean areFriends(int user_id1, int user_id2) {
		Node n1 = getNode(user_id1);
		Node n2 = getNode(user_id2);
		for (Edge e : incidentEdges(n1)) {
			if (e.getOpposite(n1).equals(n2)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Helper method to determine the Cartesian distance between two points, represented by arrays.
	 * @param arr1 The first point. Has length 2.
	 * @param arr2 The second point. Has length 2.
	 * @return
	 */
	public double getDist(double[] arr1, double[] arr2) {
		if (arr1 == null || arr2 == null || arr1.length != 2 || arr2.length != 2) {
			throw new IllegalArgumentException("One of the input arrays is invalid");
		}
		double x1 = arr1[0];
		double y1 = arr1[1];
		double x2 = arr2[0];
		double y2 = arr2[1];
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}
	
	/**
	 * Computes the geographical distance between two users.
	 * @param user_id1 the first user.
	 * @param user_id2 the second user.
	 * @return the distance between the two users.
	 */
	public double computeDistance(int user_id1, int user_id2) {
		return getDist(_dbw.getLocUser(user_id1),_dbw.getLocUser(user_id2));
	}
	
	/**
	 * Computes the geographical proximity of a point from a place. Used to determine distance of place from the average point between friends.
	 * @param av the point to be compared.
	 * @param place_id the place whose distance from the point is to be calculated.
	 * @return the distance from the point to the place.
	 */
	public double computeProximityPlace(double[] arr1, int place_id) {
		if (arr1 == null || arr1.length != 2) {
			throw new IllegalArgumentException("Input point is invalid");
		}
		return getDist(arr1, _dbw.getLocation(place_id));
	}
	
	/** Gets a set of all nodes in the graph.
	 * @return Set of all nodes in the graph.
	 */
	public Set<Node> getKeySet() {
		return _edges.keySet();
	}
	
}