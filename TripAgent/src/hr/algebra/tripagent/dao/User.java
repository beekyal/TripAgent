package hr.algebra.tripagent.dao;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	//variable for displaying error messages idn logCat
	//private static final String TAG = User.class.getSimpleName();
	
	
	//variables for user
	private int id;
	private String name;
	private String lastname;
	private String phonenumber;
	private String email;
	private String birthdate;
	private String carRegistration;
	private String carModel;
	private double carExpenditure;
	private int seatNumber;
	private String username;
	private String password;
	private String carImage;
	
	public String getCarImage() {
		return carImage;
	}

	public void setCarImage(String carImage) {
		this.carImage = carImage;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	//getters and sestters for user data
	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	public String getCarRegistration() {
		return carRegistration;
	}

	public void setCarRegistration(String carRegistration) {
		this.carRegistration = carRegistration;
	}

	public String getCarModel() {
		return carModel;
	}

	public void setCarModel(String carModel) {
		this.carModel = carModel;
	}

	public double getCarExpenditure() {
		return carExpenditure;
	}

	public void setCarExpenditure(double carExpenditure) {
		this.carExpenditure = carExpenditure;
	}

	public int getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(int seatNumber) {
		this.seatNumber = seatNumber;
	}

	public User(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;	
	}
	
	

		
	@Override
	public String toString() {
		return name + " " + lastname;
	}

	
	
	
	//we get the data of the user
	public static User getUser(JSONObject json) throws JSONException {
		User user = null;
		JSONObject userProfileJson;
		
		//Checks to see if we have or not the user found
		if(!json.has("user")) {
			//if we haven't found usder, we check to see if we have id passed
			//Check if we're doing search and populating the search object
			if(json.has("id")) {
				userProfileJson = json;
			} else {
				return null;
			}
		} else {
			userProfileJson = json.getJSONObject("user");
		}
		
		//adds the data of the user to an "array" (profile json object) and returns it for use 

		user = new User(userProfileJson.getInt("id"));
		user.setUsername(userProfileJson.getString("username"));
		//user.setPassword(userProfileJson.getString("password"));
		user.setName(userProfileJson.getString("name"));
		user.setLastname(userProfileJson.getString("lastname"));
		user.setPhonenumber(userProfileJson.getString("phonenumber"));
		user.setEmail(userProfileJson.getString("email"));
		user.setBirthdate(userProfileJson.getString("birthdate"));
		user.setCarRegistration(userProfileJson.getString("car_registration"));
		user.setCarModel(userProfileJson.getString("car_model"));
		user.setCarExpenditure(userProfileJson.getDouble("car_expenditure"));
		user.setSeatNumber(userProfileJson.getInt("car_seat_number"));
		user.setCarImage(userProfileJson.getString("car_image"));
		return user;
	}
}
