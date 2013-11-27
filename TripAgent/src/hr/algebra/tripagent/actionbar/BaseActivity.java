package hr.algebra.tripagent.actionbar;

import hr.algebra.tripagent.HistoryActivity;
import hr.algebra.tripagent.LogInActivity;
import hr.algebra.tripagent.R;
import hr.algebra.tripagent.RegisterUserActivity;
import hr.algebra.tripagent.SearchActivity;
import hr.algebra.tripagent.SearchResultsActivity;
import hr.algebra.tripagent.TripPostActivity;
import hr.algebra.tripagent.helpers.Settings;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseActivity extends Activity {
	//variable for displaying error message
	private static final String TAG = BaseActivity.class.getSimpleName();
	
	//variables for activities to be instanced
	public static final int ACTIVITY_LOGIN = 1;
	public static final int ACTIVITY_REGISTER_USER = 2;
	public static final int ACTIVITY_SEARCH = 3;
	public static final int ACTIVITY_TRIP_POST = 4;
	public static final int ACTIVITY_SEARCH_RESULTS = 5;
	public static final int ACTIVITY_HISTORY = 6;
	
	//validity constant in days
	public static final int VALIDITY = 7;  
	
	/**
	 * @param args
	 * Integer variable for determing which activity to activate 
	 */
	public void startMyActivity(int activ) {
		Intent i = null;
		switch (activ) {
		
		case ACTIVITY_LOGIN:
			i = new Intent(this, LogInActivity.class);
			break;
			
		case ACTIVITY_REGISTER_USER:
			i = new Intent(this, RegisterUserActivity.class);
			break;
		
		case ACTIVITY_SEARCH:
			i = new Intent(this, SearchActivity.class);
			break;
		
		case ACTIVITY_TRIP_POST:
			i = new Intent(this, TripPostActivity.class);
			break;
		
		case ACTIVITY_SEARCH_RESULTS:
			i = new Intent(this, SearchResultsActivity.class);
			break;
		
		case ACTIVITY_HISTORY:
			i = new Intent(this, HistoryActivity.class);
			break;
			
		default:
			break;
		}
		
		//try catch block to see if the new activity can be instanced
		try {
			startActivity(i);
		} catch (Exception e) {
			Log.e(TAG, getString(R.string.message_error),e);
		}
		//closing current activity, if it's NOT activity History
		if (activ != ACTIVITY_HISTORY) {
			finish();	
		}
	}
	
	//instancing action bar
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			//getMenuInflater().inflate(R.menu.search, menu);
			
				
			//get the actionbar menu
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.action_bar, menu);
					
						
			//instancing the buttons 
			//logged in buttons
			MenuItem itemEditProfile = menu.findItem(R.id.action_bar_editProfile);
			MenuItem itemSearch = menu.findItem(R.id.action_bar_search);
			MenuItem itemLogOut = menu.findItem(R.id.action_bar_logOut);
			MenuItem itemHistory = menu.findItem(R.id.action_bar_history);
			MenuItem itemPostTrip = menu.findItem(R.id.action_bar_post_trip);
			
			//logged out buttons
			MenuItem itemLogIn = menu.findItem(R.id.action_bar_logIn);
			MenuItem itemRegister = menu.findItem(R.id.action_bar_register);
			
			//check to see if the user is logged in 
			if (Settings.getInstance().getUser() == null) {
				//if the user is NOT logged in
				
				//hiding the buttons
				itemEditProfile.setVisible(false);
				itemSearch.setVisible(false);
				itemLogOut.setVisible(false);
				itemHistory.setVisible(false);
				itemPostTrip.setVisible(false);
				
				//showing the buttons
				itemLogIn.setVisible(true);
				itemRegister.setVisible(true);
			} else {
				//if the user IS logged in
				
				//showing the buttons
				itemEditProfile.setVisible(true);
				itemSearch.setVisible(true);
				itemLogOut.setVisible(true);
				itemHistory.setVisible(true);
				itemPostTrip.setVisible(true);
				
				//hiding the buttons
				itemLogIn.setVisible(false);
				itemRegister.setVisible(false);
			}
			//request that states that the action bar has been altered
			this.invalidateOptionsMenu();
		
			return true;
		}
	
	//functions based on the click on the actionbar menu
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		//when we try to edit profile
		case R.id.action_bar_editProfile:		
			//call the activity login
			startMyActivity(ACTIVITY_REGISTER_USER);
			break;
			
		//when we go to history of trips
		case R.id.action_bar_history:
			//Log.e(TAG, "stiso si history");
			//call the activity history
			startMyActivity(ACTIVITY_HISTORY);
			break;
			
		//when we try to search
		case R.id.action_bar_search:
			//call the activity for searching
			startMyActivity(ACTIVITY_SEARCH);
			break;
			
		//when we try to log out
		case R.id.action_bar_logOut:
			//call the activity login
			startMyActivity(ACTIVITY_LOGIN);
			//Sets the existing user to null
			Settings.getInstance().setUser(null);
			break;
					
		//when we try to log out
		case R.id.action_bar_register:
			//call the activity login
			startMyActivity(ACTIVITY_REGISTER_USER);				
			break;
		
		//when we try to log in
		case R.id.action_bar_logIn:
			//call the activity login
			startMyActivity(ACTIVITY_LOGIN);			
			break;
			
		//when we try to post a trip
		case R.id.action_bar_post_trip:
			//call the activity post trip
			startMyActivity(ACTIVITY_TRIP_POST);
			break;
			
		default:
			break;
		}
		return true;
	}
}