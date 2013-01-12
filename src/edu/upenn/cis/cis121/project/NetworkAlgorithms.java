package edu.upenn.cis.cis121.project;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;



/**
 * @author jdesai
 * Class that allows the user to implement the standard network algorithms on a database of users, friends, places, and likes.
 */
public class NetworkAlgorithms {
	
	/**
	 * The graph used to implement graph algorithms.
	 */
	private Graph _graph;
	
	/**
	 * Constructor for the NetworkAlgorithms class.
	 * @param dbUser
	 * @param dbPass
	 * @param dbSID
	 * @param dbHost
	 * @param port
	 */
	public NetworkAlgorithms(String dbUser, String dbPass, String dbSID,
			String dbHost, int port) {
		_graph = new Graph(dbUser, dbPass, dbSID, dbHost, port);
	}
	
	/**
	 * Gets the minimum Bacon distance between two users.
	 * @param user_id1 The first user.
	 * @param user_id2 The second user.
	 * @return The minimum Bacon distance between the two users.
	 * @throws IllegalArgumentException If either user ID does not exist in the database.
	 */
	public int distance(int user_id1, int user_id2) throws IllegalArgumentException {
		//Throws IllegalArgumentException if invalid/not found.
		Node n1 = _graph.getNode(user_id1);
		//Checks that the second user exists in the graph. If not found, IllegalArgumentException is thrown.
		_graph.getNode(user_id2);
		//Degenerate case, no need to waste time.
		if (user_id1 == user_id2) {
			return 0;
		}
		//Set all adjacent edges to unexplored
		for (Node nod : _graph.getKeySet()) {
			nod.setVisited(false);
			for (Edge ed : _graph.incidentEdges(nod)) {
				ed.setLabel(Edge.Label.UNEXPLORED);
			}
		}
		Queue<Node> l1 = new LinkedList<Node>();
		l1.add(n1);
		n1.setVisited(true);
		int i = 0;
		Queue<Node> l2 = null;
		//Main BFS loop
		while (!l1.isEmpty()) {
			l2 = new LinkedList<Node>();
			for (Node v : l1) {
				for (Edge e : _graph.incidentEdges(v)) {
					if (e.getLabel() == Edge.Label.UNEXPLORED) {
						Node w = e.getOpposite(v);
						if (!w.isVisited()) {
							if (w.getID() == user_id2) {
								return i + 1;
							}
							e.setLabel(Edge.Label.DISCOVERY);
							w.setVisited(true);
							l2.add(w);
						} else {
							e.setLabel(Edge.Label.CROSS);
						}
					}
				}
			}
			i++;
			l1 = l2;
		}
		//If no connection is found
		return -1;
	}
	
	/**
	 * Method that determines recommended friends for a user.
	 * @param user_id User ID of the user to find recommendations for.
	 * @param numRec Number of friend recommendations desired.
	 * @return A list of friend recommendations.
	 * @throws IllegalArgumentException
	 */
	public List<Integer> recommendFriends(int user_id, int numRec)
			throws IllegalArgumentException {
		if (numRec < 0) {
			throw new IllegalArgumentException("Negative number of recommendations is invalid.");
		}
		//Degenerate case, no need to waste time
		if (numRec == 0) {
			return new ArrayList<Integer>();
		}
		//Throws exception if user ID is invalid or not found.
		_graph.getNode(user_id);
		PriorityQueue<Node> recfriends = new PriorityQueue<Node>();
		//Set all node distances to maximum possible value.
		for (Node n : _graph.getKeySet()) {
			if (n.getID() == user_id) {
				n.setDistance(0);
			} else {
				n.setDistance(999999999);
			}
			recfriends.add(n);
		}
		PriorityQueue<Node> finalrecs = new PriorityQueue<Node>();
		Node u = null;
		while (!recfriends.isEmpty()) {
			u = recfriends.remove();
			//Adds this value to final recommendations only if it isn't a friend (shouldn't recommend existing friends).
			if (!_graph.areFriends(user_id, u.getID()) && user_id != u.getID()) {
				finalrecs.add(u);
			}
			//Enforces loop invariant.
			if (finalrecs.size() == numRec) {
				break; 
			}
			for (Edge e : _graph.incidentEdges(u)) {
				//Relax edge e
				Node z = e.getOpposite(u);
				_graph.setEdgeWeight(e);
				double r = u.getDistance() + e.getWeight(); 	
				if (z.getDistance() > r) {
					z.setDistance(r);
					//Re-order the queue
					recfriends.remove(z);
					recfriends.add(z);
				}
			}
		}
		List<Integer> ret = new LinkedList<Integer>();
		for (int i = 0; i < numRec; i++) {
			Node n = finalrecs.remove();
			ret.add(n.getID());
		}
		return ret;
	} 
	
	/**
	 * Method that determines recommended activities for a user.
	 * @param user_id User ID of the user to be searched for.
	 * @param maxFriends Maximum number of friends of target user to be used.
	 * @param maxPlaces Maximum number of places liked by target user to be used.
	 * @return A list of activity recommendations.
	 * @throws IllegalArgumentException if the user ID is nonexistent/invalid, or if maxFriends or maxPlaces are negative.
	 */
	public String recommendActivities(int user_id, int maxFriends, int maxPlaces)
			throws IllegalArgumentException {
		if (maxFriends < 1) {
			throw new IllegalArgumentException("Zero or negative maxFriends input!");
		}
		if (maxPlaces < 1) {
			throw new IllegalArgumentException("Zero or negative maxPlaces input!");
		}
		String json = "";
		try {
			json = createUserString(user_id);
		} catch (SQLException e1) {
			throw new IllegalArgumentException("Invalid/nonexistent user_id input.");
		}
		//Sets distances to maximum possible
		int[] friends =_graph._dbw.getFriends(user_id);
		for (Node nod : _graph.getKeySet()) {
			nod.setDistance(999999999);
		}
		//Orders friends by geographical proximity
		PriorityQueue<Node> closest = new PriorityQueue<Node>();
		for (int i = 0; i < friends.length; i++) {
			Node n = _graph.getNode(friends[i]);
			n.setDistance(_graph.computeDistance(user_id ,friends[i]));
			closest.add(n);
		}
		ArrayList<Integer> al = new ArrayList<Integer>();
		
		//Calculates center of friends and formats JSON strings for each friend
		double[] avg = _graph._dbw.getLocUser(user_id);
		json += "\"friends\": {";
		int fin = Math.min(maxFriends, closest.size());
		for (int j = 0; j < fin; j++) {
			Node no = closest.remove();
			al.add(no.getID());
			double[] loc = _graph._dbw.getLocUser(no.getID());
			//Adds values to sum of locations
			avg[0] += loc[0];
			avg[1] += loc[1];
			try {
				//Adds formatted string for each friend
				json += createFriendString(j, no.getID());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (j != fin - 1) {
				json += ",";
			} else {
				json += "}, \"places\": {";
			}
		}
		//Finalizes average calculation
		avg[0] /= (al.size() + 1);
		avg[1] /= (al.size() + 1);
		int[] allPlaces = null;
		try {
			allPlaces = _graph._dbw.getOneColumn("select place_id from Places");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//Orders places by suitability
		PriorityQueue<Place> places = new PriorityQueue<Place>();
		for (int k = 0; k < allPlaces.length; k++) {
			//Counts number of people who like this place
			double count = 0;
			int[] likers = null;
			try {
				likers = _graph._dbw.getLikers(allPlaces[k]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			for (int l = 0; l < likers.length; l++) {
				if (al.contains(likers[l])) {
					count++;
				}
				if (likers[l] == user_id) {
					count++;
				}
			}
			//Calculates suitability based on homework documentation
			double suitability = count /(_graph.computeProximityPlace(avg, allPlaces[k]) + 0.01);
			places.add(new Place(allPlaces[k], suitability));
		}
		fin = Math.min(places.size(), maxPlaces);
		for (int x = 0; x < fin; x++) {
			Place p = places.remove();
			try {
				//Adds formatted strings for each place
				json += placeString(x, p.getID());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (x != fin - 1) {
				json += ",";
			} else {
				json += "}}";
			}
		}
		return json;
	}
	
	/**
	 * Helper method for recommendActivites to generate a JSON string for the user.
	 * @param user_id the user ID from whom a JSON string is to be made.
	 * @return the JSON string for the input user.
	 * @throws SQLException if the user does not exist in the database.
	 */
	public String createString(int user_id) throws SQLException {
		ResultSet rs = _graph._dbw._statement.executeQuery("select * from Users where user_id = " + Integer.toString(user_id));
		rs.next();
		return "{\"user_id\":" + Integer.toString(user_id) + ",\"first_name\":\"" 
									 + rs.getString(2) + "\",\"last_name\":\"" + rs.getString(3) 
									 + "\",\"latitude\":" + Double.toString(rs.getDouble(4)) 
									 + ",\"longitude\":" + Double.toString(rs.getDouble(5)) + "}";
	}
	
	/**
	 * Helper method for recommendActivities. Helps construct a JSON string for a particular friend.
	 * @param index The friend's index number (which also represents 0-indexed geographic closeness ranking).
	 * @param user_id The friend's user ID.
	 * @return Formatted JSON string from this friend.
	 * @throws SQLException  thrown by createString if the user ID is invalid/nonexistent
	 */
	public String createFriendString(int index, int user_id) throws SQLException {
		return "\"" + Integer.toString(index) + "\" : " + createString(user_id);
	}
	
	/**
	 * Helper method for recommendActivities. Helps construct a JSON string for the main user (which forms the beginning of
	 * the complete JSON string.
	 * @param user_id the ID of the user for whom a JSON string is to be constructed.
	 * @return the formatted JSON string for the user.
	 * @throws SQLException thrown by createString if the user ID is invalid/nonexistent
	 */
	public String createUserString(int user_id) throws SQLException {
		return "{\"user\": " + createString(user_id) + ",";
	}
	
	/**
	 * Helper method for recommendActivities.
	 * @param index 0-based index of the place, also represents the 0-based ranking of the place in terms of suitability.
	 * @param place_id the ID of the place for which a string is to be made.
	 * @return a JSON-formatted string with this place's information
	 * @throws SQLException if the place ID is invalid/nonexistent
	 */
	public String placeString(int index, int place_id) throws SQLException {
		ResultSet rs = _graph._dbw._statement.executeQuery("select * from Places where place_id = " + Integer.toString(place_id));
		rs.next();
		String ret1 = "\"" + Integer.toString(index) + "\" : {\"place_id\":" + Integer.toString(place_id) + ",\"place_name\":\"" 
									 + rs.getString(2) + "\",\"description\":\"";
		String ret2 = "\",\"latitude\":" + Double.toString(rs.getDouble(4)) 
									 + ",\"longitude\":" + Double.toString(rs.getDouble(5)) + "}";
		String type = Integer.toString(rs.getInt(3));
		rs.close();
		rs = _graph._dbw._statement.executeQuery("select description from Place_Types where type_id = " + type);
		rs.next();
		return ret1 + rs.getString(1) + ret2;
	}
	
	/**
	 * Method that determines recommended places for a user. See the attached "extracredit.pdf" for information on the algorithm I use.
	 * @param user_id User ID of user for whom recommendations are desired.
	 * @param numRec Number of place recommendations desired.
	 * @return A list of place recommendations.
	 * @throws IllegalArgumentException if the user ID provided is invalid/nonexistent, or if numRec is negative.
	 */
	public List<Integer> recommendPlaces(int user_id, int numRec)
			throws IllegalArgumentException {
		if (numRec < 1) {
			throw new IllegalArgumentException("Invalid number of recommendation");
		}
		//Location of user (used later
		double[] loc = _graph._dbw.getLocUser(user_id);
		//Throws IllegalArgumentException if invalid/nonexistent users ID.
		Node user = _graph.getNode(user_id);
		//Orders friends by geographical proximity
		PriorityQueue<Node> bestfriends = new PriorityQueue<Node>();
		for (Edge e : _graph.incidentEdges(user)) {
			Node f = e.getOpposite(user);
			f.setDistance(_graph.computeDistance(user_id, f.getID()));
			bestfriends.add(f);
		}
		//Gets types liked by user
		ArrayList<Integer> typesILike = new ArrayList<Integer>();
		String q1 = "select distinct type_id from Likes, Places where Likes.user_id = ";
		String q2 = " and Places.place_id = Likes.place_id";
		ResultSet rs = null;
		try {
			rs = _graph._dbw._statement.executeQuery(q1 + user_id + q2);
			while (rs.next()) {
				typesILike.add(rs.getInt(1));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		//For each type liked by the user, calculate how many of the 5 geographically closest friends like each type.
		PriorityQueue<Place> finalTypes = new PriorityQueue<Place>();
		for (Integer in : typesILike) {
			double count = 0;
			for (int k = 0; k < Math.min(bestfriends.size(), 5); k++) {
				Node bff = bestfriends.remove();
				ArrayList<Integer> thisBffTypes = new ArrayList<Integer>();
				try {
					rs = _graph._dbw._statement.executeQuery(q1 + bff.getID() + q2);
					while (rs.next()) {
						thisBffTypes.add(rs.getInt(1));
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				for (Integer ty : thisBffTypes) {
					if (in == ty) {
						count++;
					}
				}
			}
			Place p = new Place(in, count);
			finalTypes.add(p);
		}
		//For each type, get a list of places of that type. Set the suitability of the place according to the formula in
		//extracredit.pdf, which weights the suitability of the place type and the distance. Order every place by suitability.
		PriorityQueue<Place> retPlaces = new PriorityQueue<Place>();
		for (Place pl : finalTypes) {
			try {
				rs = _graph._dbw._statement.executeQuery("select place_id, latitude, longitude from Places where type_id = " + pl.getID());
				while (rs.next()) {
					double[] arr = new double[2];
					arr[0] = rs.getDouble(2);
					arr[1] = rs.getDouble(3);
					Place truePlace = new Place(rs.getInt(1), ((pl.getSuitability() * 0.7)/(_graph.getDist(arr, loc) * 0.05)));
					retPlaces.add(truePlace);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		//Return the numRec number of places with the greatest suitability that the user does not already like.
		List<Integer> ret = new LinkedList<Integer>();
		int[] myLikes = _graph._dbw.getLikes(user_id);
		while (ret.size() < numRec) {
			Integer num = retPlaces.remove().getID();
			boolean contains = false;
			for (int n = 0; n < myLikes.length; n++) {
				if (myLikes[n] == num) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				ret.add(num);
			}
		}
		return ret;
	}
	
	/**
	 * Closes the connection to the database.
	 */
	public void closeDBConnection() {
		_graph._dbw.closeDBConnection();
	}
	
	/**
	 * For testing.
	 * @param args Command-line arguments
	 */
	public static void main(String args[]) {
		
	}
	
}
