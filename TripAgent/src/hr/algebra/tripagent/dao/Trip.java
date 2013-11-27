package hr.algebra.tripagent.dao;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Trip {
	//variable for displaying LogCat messagess
	private static final String TAG = User.class.getSimpleName();
	
	//values of the object
	private int id;
	private String departureDate;
	private double totalCose;	//rename to totalCost
	private double costPerPerson;
	private String comment;
	private int numberofSeats;

	private List<User> passengers = new ArrayList<User>();
	
	//generated getters and setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDepartureDate() {
		return departureDate;
	}

	public void setDepartureDate(String departureDate) {
		this.departureDate = departureDate;
	}

	public double getTotalCose() {
		return totalCose;
	}

	public void setTotalCose(double totalCose) {
		this.totalCose = totalCose;
	}

	public double getCostPerPerson() {
		return costPerPerson;
	}

	public void setCostPerPerson(double costPerPerson) {
		this.costPerPerson = costPerPerson;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getNumberofSeats() {
		return numberofSeats;
	}

	public void setNumberofSeats(int numberofSeats) {
		this.numberofSeats = numberofSeats;
	}
	
	public void addUser(User user) {
		Log.d(TAG, "New user " + id + " user " + user.getUsername());
		passengers.add(user);
	}
	
	public List<User> getUsers() {
		Log.d(TAG, "getUsers() size " + passengers.size());
		return passengers;
	}
	
	//constructor for the class
	public static Trip getTrip(JSONObject json) throws JSONException {
		Trip trip;
		User user;
		JSONArray passengersArray;
		
		trip = new Trip();
		//mapping details of a trip
		trip.setId(json.getInt("id"));
		trip.setDepartureDate(json.getString("departureDate").toString());
		trip.setTotalCose(json.getDouble("totalCose"));
		trip.setCostPerPerson(json.getDouble("costPerPerson"));
		trip.setComment(json.getString("comment").toString());
		trip.setNumberofSeats(json.getInt("numberofSeats"));
		
		//check to see if the trip object has any passengers in it
		if(!json.has("passengers")){
			Log.d(TAG, "No passengers!");
			return null;
		}
		
		//declaring array of passengers registered for this trip object
		passengersArray = json.getJSONArray("passengers");
		Log.d(TAG, "passengersArray size " + passengersArray.length());
	
		//iterating through all of the passangers in the trip and adding them to the list
		for (int i = 0; i < passengersArray.length(); i++) {
			user = User.getUser(passengersArray.getJSONObject(i));
			trip.addUser(user);
		}
		
		return trip;
	}
}
