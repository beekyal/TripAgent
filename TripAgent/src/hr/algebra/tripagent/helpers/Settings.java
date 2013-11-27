package hr.algebra.tripagent.helpers;

import hr.algebra.tripagent.dao.User;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import android.content.Context;

public class Settings {

	private static Settings INSTANCE;
	
	private User mUser;
	private List<String> mCityList;
	
	//Constructor that calls this class, it doesn't accept any parameters
	private Settings() {}
	
	//We create a new instance of the class if there aren't any
	public static Settings getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new Settings();
		}
		return INSTANCE;
	}
	
	//We set the parameters of the user
	public void setUser(User user) {
		this.mUser = user;
	}
	
	//Retrieve the parameters of the user
	public User getUser() {
		return mUser;
	}
	
	//we se the parameters of the cities
	public void setCityList(List<String> cityList) {
		this.mCityList = cityList;
	}
	
	public List<String> getCityList() {
		return mCityList;
	}
	
	//Check to see if the user is logged in or not
	public boolean isUserLoggedIn() {
		//if user isn't logged
		if(mUser == null || mUser.getId() == 0) {
			return false;
		}	
		//if user is logged
		return true;	
	}
	
	//writing objects to a cache file
	public static void writeObject(Context context, String key, Object object) throws IOException {
	      FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
	      ObjectOutputStream oos = new ObjectOutputStream(fos);
	      oos.writeObject(object);
	      oos.close();
	      fos.close();
	}
	
	//reading object from a cahce file
	public static Object readObject(Context context, String key) throws IOException, ClassNotFoundException {
		FileInputStream fis = context.openFileInput(key);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object object = ois.readObject();
		return object;
	}
	
	
	
	
	public String getServerURL () {
		return "http://test.ubertools.net/algebra/serversimulator1/";
	}
}
