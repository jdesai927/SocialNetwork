package edu.upenn.cis.cis121.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * @author jdesai
 * Class that has methods allowing the population of the Likes and Places tables.
 */
public class PopulateDB {
	
	
	/**
	 * Constructor for the PopulateDB object. Opens a database connection using DBUtils.
	 * @param dbUser
	 * @param dbPass
	 * @param dbSID
	 * @param dbHost
	 * @param port
	 */
	public PopulateDB(String dbUser, String dbPass, String dbSID,
			String dbHost, int port) {
		try {
			DBUtils.openDBConnection(dbUser, dbPass, dbSID, dbHost, port);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method that parses a .csv file and gets an ArrayList containing arrays that represent tuples/records.
	 * @param filename The .csv to be parsed.
	 * @return An ArrayList containing arrays that represent tuples/records.
	 * @throws FileNotFoundException if the file could not be found.
	 */
	public ArrayList<String[]> parseCSV(String filename) throws FileNotFoundException {
		if (filename == null) {
			throw new IllegalArgumentException("null filename for parseCSV");
		}
		File f = new File(filename);
		Scanner s = new Scanner(f);
		ArrayList<String[]> ret = new ArrayList<String[]>();
		String n = null;
		while (s.hasNextLine()) {
			n = s.nextLine();
			ret.add(n.split(","));
		}
		return ret;
	}
	
	/**
	 * Populates the Likes table based on input from a file.
	 * @param filename The file to read from (must be in .csv format)
	 */
	public void populateLikes(String filename) {
		if (filename == null) {
			return;
		}
		ArrayList<String[]> likeArr = null;
		try {
			likeArr = parseCSV(filename);
		} catch (FileNotFoundException e1) {
			System.out.println("File " + filename + " not found. Population failed.");
			return;
		}
		String query;
		for (String[] arr : likeArr) {
			query = "insert into Likes (user_id, place_id) values (" + arr[0] + ", " + arr[1] + ")";
			try {
				DBUtils.executeUpdate(query);
			} catch (SQLException e) {
				System.out.println("Like insert command + <" + query + "> failed");
			}
		}
	}
	
	/**
	 * Populates the Places table based on input from a file.
	 * @param filename The file to read from (must be in .csv format)
	 */
	public void populatePlaces(String filename) {
		if (filename == null) {
			return;
		}
		ArrayList<String[]> placeArr = null;
		try {
			placeArr = parseCSV(filename);
		} catch (FileNotFoundException e2) {
			System.out.println("File " + filename + " not found. Population failed.");
			return;
		}
		String query;
		for (String[] arr : placeArr) {
			arr[1] = arr[1].replaceAll("'", "''");
			query = "insert into Places (place_id, place_name, type_id, latitude, longitude) values (" + arr[0] + ", \'" + arr[1] + "\', " + arr[2] + ", " + arr[3] + ", " + arr[4] + ")";
			try {
				DBUtils.executeUpdate(query);
			} catch (SQLException e) {
				System.out.println(query);
				System.out.println("Place insert command failed for place_id " + arr[0]);
			}
		}
		try {
			placeArr = parseCSV(filename);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Closes the connection to the database.
	 */
	public void closeDBConnection() {
		try {
			DBUtils.closeDBConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Populates the Places and Likes tables in the database..
	 * @param args Command line arguments
	 */
	public static void main(String args[]) {
		PopulateDB pdb = new PopulateDB(args[0], args[1], "CIS", "fling.seas.upenn.edu", 1521);
		pdb.populatePlaces("places_data.csv");
		pdb.populateLikes("likes_data.csv");
		pdb.closeDBConnection();
	}
	
}
