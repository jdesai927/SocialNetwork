package edu.upenn.cis.cis121.project;

/**
 * @author jdesai
 * Wrapper class used to allow ordering of places by sustainability in the recommendActivities method of the NetworkAlgorithms class.
 * This class is also used to store the suitability of place types in the recommendPlaces method.
 * Note that the compareTo method actually returns the opposite of the expected value to allow maximum-based sorting in a min-based
 * PriorityQueue.
 */
public class Place implements Comparable<Place> {
	
	/**
	 * The ID of the place represented by this object.
	 */
	private int _placeID;
	
	/**
	 * The suitability of the place represented by this object given the user and friends in the invocation of 
	 * NetworkAlgorithms.recommendActivities in which this object was instantiated.
	 */
	private double _suitability;
	
	/**
	 * Constructs a new Place.
	 * @param place_id The ID of the place represented by this place object.
	 * @param suit the suitability of this place.
	 */
	public Place(int place_id, double suit) {
		_placeID = place_id;
		_suitability = suit;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		if (arg0 instanceof Place) {
			return ((Place) arg0)._placeID == _placeID;
		}
		return false;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Place o) {
		if (_suitability < o._suitability) {
			return 1;
		} else if (_suitability == o._suitability) {
			return 0;
		}
		return -1;
	}
	
	/** _placeID getter
	 * @return The ID of the place represented by this object.
	 */
	public int getID() {
		return _placeID;
	}
	
	/** _suitability getter.
	 * @return The suitability of the place represented by this object.
	 */
	public double getSuitability() {
		return _suitability;
	}
	
	/** _suitability setter.
	 * @param s The new value for the suitability of the place represented by this object.
	 */
	public void setSuitability(double s) {
		_suitability = s;
	}

}