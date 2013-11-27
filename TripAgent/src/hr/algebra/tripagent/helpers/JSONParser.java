package hr.algebra.tripagent.helpers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {
	//variable for displaying error messages idn logCat
	//private static final String TAG = User.class.getSimpleName();
		
		
	//variables for user
	private int id;
	private String name;
	
	
	//getters/setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String city) {
		this.name = city;
	}
	
	
	//we get the data of the user
	public static List<String> getCityList(JSONObject json) throws JSONException {
		List<String> cities = new ArrayList<String>();
		JSONArray citiesList;
		
		//Checks to see if we have or not the user found
		if(!json.has("cities")){
			return null;		
		}
		
//		adds the data of the city to an "array" (city profile object) and returns it for use 	
		citiesList = json.getJSONArray("cities");
		for (int i = 0; i < citiesList.length(); i++) {
			cities.add(citiesList.getString(i));
		}
			
		return cities;
	}
	
}
