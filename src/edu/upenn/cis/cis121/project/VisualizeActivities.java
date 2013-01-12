package edu.upenn.cis.cis121.project;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.awt.Desktop;
import java.io.*;


/**
 * Generates an html/javascript representation of friends and places
 * The canvas + javascript for the generated html are based on the KineticJS javascript library (http://www.kineticjs.com)
 * and one of its official tutorials (http://www.html5canvastutorials.com/labs/html5-canvas-tooltips-with-kineticjs)
 * <br />
 * This class requires the simple-json library (available here: http://code.google.com/p/json-simple/downloads/detail?name=json_simple-1.1.jar)
 * <br />
 * To use, set the <b>user</b> and <b>pass</b> at the start of the main method, and set the <b>userID</b>, <b>maxFriends</b>, and <b>maxPlaces</b> to
 * the desired input values.
 * <br />
 * This class expects to:
 * <br />
 * <ul>
 * <li>Be in the same package as a working NetworkAlgorithms implementation</li>
 * <li>Have access to json_simple.jar</li>
 * <li>Have access to template.html</li>
 * </ul>
 * 
 * 
 * @author Gary Menezes (gmenezes@seas.upenn.edu)
 */
public class VisualizeActivities {
	/**
	 * Since this is not intended to be anything other than a manually run visualization of recommendActivities,
	 * all parameters are hard coded - all significant variables are at the beginning of the main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// database connection parameters
		String user = "jdesai";//args[0];
		String pass = "Pritzthecrow219";//args[1];
		String SID = "CIS";
		String host = "fling.seas.upenn.edu";
		int port = 1521;
		NetworkAlgorithms toVisualize = new NetworkAlgorithms(user, pass, SID, host, port);
		
		// parameters for call to NetworkAlgorithms.recommendActivities()
		int userID = 0;
		int maxFriends = 3;
		int maxPlaces = 3;
		
		// characteristics of the javascript graph
		String userColor = "yellow";
		double userRadius = 20;
		String placeColor = "blue";
		double placeRadius = 20;
		String friendColor = "green";
		double friendRadius = 15;
		
		try{
			String jsonString = toVisualize.recommendActivities(userID, maxFriends, maxPlaces);

			// Use the simple-json library to parse the input json
			JSONObject jsonObj = (JSONObject)JSONValue.parse(jsonString);
			
	        File f = new File("VisualizeActivities.html");
	        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
	        // We're going to rewrite the template file to our output, with an inserted segment in the middle
	        BufferedReader br = new BufferedReader(new FileReader("template.html"));
	        String line;
	        while((line = br.readLine()) != null){
	        	if(line.contains("visualize activities insert here")){
		        	// Once we've found the insertion comment in the template file, stop copying
	        		break;
	        	}else{
	        		bw.write(line + "\n");
	        	}
	        }
	        
	        // Generate user circle
	        JSONObject callingUser = (JSONObject)JSONValue.parse(jsonObj.get("user").toString());
	        // The actual x,y location is scaled up and translated a bit from the 0-360 values in order to better fit the display
			writeHtmlLine(getCircleString("user",
										  Double.parseDouble(callingUser.get("latitude").toString()) * 2 + 180,
										  Double.parseDouble(callingUser.get("longitude").toString()) * 2 + 20,
										  userColor,
										  userRadius,
										  callingUser.get("first_name") + " " + callingUser.get("last_name") + " (" + callingUser.get("user_id") + ")",
										  callingUser.get("latitude").toString(),
										  callingUser.get("longitude").toString()
										  ),
						  bw,
						  1
						);
	        
	        // Generate place circles
	        JSONObject jsonPlaces = (JSONObject)JSONValue.parse(jsonObj.get("places").toString());
			for(int i = 0; i < jsonPlaces.size(); i++){
				JSONObject place = (JSONObject)JSONValue.parse(jsonPlaces.get(new Integer(i).toString()).toString());
				// individual place radii are scaled based on their ranking
				double thisPlaceRadius = placeRadius * (1 - 1.0 / (jsonPlaces.size() - i + 1));
				writeHtmlLine(getCircleString("place" + i,
											  Double.parseDouble(place.get("latitude").toString()) * 2 + 180,
											  Double.parseDouble(place.get("longitude").toString()) * 2 + 20,
											  placeColor,
											  thisPlaceRadius,
											  place.get("description") + ": " + place.get("place_name"),
											  place.get("latitude").toString(),
											  place.get("longitude").toString()
											  ),
							  bw,
							  1
							);
			}


	        // Generate friend circles
			JSONObject jsonFriends = (JSONObject)JSONValue.parse(jsonObj.get("friends").toString());
			for(int i = 0; i < jsonFriends.size(); i++){
				JSONObject friend = (JSONObject)JSONValue.parse(jsonFriends.get(new Integer(i).toString()).toString());
				// relative friend suitability is indicated by geographic closeness, thus doesn't require any size scaling
				writeHtmlLine(getCircleString("friend" + i,
											  Double.parseDouble(friend.get("latitude").toString()) * 2 + 180,
											  Double.parseDouble(friend.get("longitude").toString()) * 2 + 20,
											  friendColor,
											  friendRadius,
											  friend.get("first_name") + " " + friend.get("last_name") + " (" + friend.get("user_id") + ")",
											  friend.get("latitude").toString(),
											  friend.get("longitude").toString()
											  ),
							  bw,
							  1
							);
			}
	        
	        while((line = br.readLine()) != null){
	        	bw.write(line + "\n");
	        }
	        bw.close();

	        Desktop.getDesktop().browse(f.toURI());
		}catch(IllegalArgumentException e){
			System.out.println("Should not have thrown IllegalArgumentException on input:\nuser_id: " + userID + "\nmaxFriends: " + maxFriends);
		} catch (FileNotFoundException e) {
			System.out.println("Make sure template.html is in your project's top level directory.");
		} catch (IOException e) {
			System.out.println("Error reading from template.html or writing to VisualizeActivities.html.");
			e.printStackTrace();
		}
	}
	/**
	 * Generate a javascript definition of a Kinetic.Circle
	 * 
	 * @param varName
	 * @param xLoc
	 * @param yLoc
	 * @param color
	 * @param radius
	 * @param tooltip
	 * @param latDisp
	 * @param lonDisp
	 */
	private static String getCircleString(String varName, double xLoc, double yLoc, String color, double radius, String tooltip, String latDisp, String lonDisp){
		String ret = "var " + varName + " = new Kinetic.Circle({" +
						"x:" + xLoc + "," +
						"y:" + yLoc + "," +
						"fill:\"" + color + "\"," +
						"strokeWidth:0," +
						"radius:" + radius + "});" +
					 varName + ".on(\"mousemove\", function(){" +
						"var mousePos = stage.getMousePosition();" +
						"tooltip.setPosition(mousePos.x + 10, mousePos.y + 10);" +
						"tooltip.setText(\"" + tooltip + "\");" +
						"tooltip.show();" +
						"latitudeTooltip.setText(\"lat: " + latDisp + "\");" +
						"latitudeTooltip.show();" +
						"longitudeTooltip.setText(\"lon: " + lonDisp + "\");" +
						"longitudeTooltip.show();" +
						"tooltipLayer.draw();});" +
					 varName + ".on(\"mouseout\", function(){" +
						"tooltip.hide();" +
						"latitudeTooltip.hide();" +
						"longitudeTooltip.hide();" +
						"tooltipLayer.draw();});" +
					 "shapesLayer.add(" + varName + ");";
		return ret;
	}
	/**
	 * Add some formatting and write a line (with indentation = tabbing) to the buffer, bw
	 * 
	 * @param s
	 * @param bw
	 * @param tabbing
	 */
	private static void writeHtmlLine(String s, BufferedWriter bw, int tabbing) throws IOException{
		for(int i = 0; i < tabbing; i++){
			bw.write("\t");
		}
		bw.write(s + "\n");
	}
}
