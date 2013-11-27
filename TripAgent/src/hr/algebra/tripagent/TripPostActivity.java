package hr.algebra.tripagent;

import hr.algebra.tripagent.actionbar.BaseActivity;
import hr.algebra.tripagent.async.AsyncRequest;
import hr.algebra.tripagent.async.AsyncRequest.OnRequestDone;
import hr.algebra.tripagent.helpers.JSONParser;
import hr.algebra.tripagent.helpers.Settings;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Toast;

public class TripPostActivity extends BaseActivity implements OnClickListener, OnRequestDone {
	//variable for displaying error message
	private static final String TAG = AsyncRequest.class.getSimpleName();
	
	//Date related variables
	private int mYear;
	private int mMonth;
	private int mDay;
	
	//variables for implementing views, buttons etc
	private Button mButtonPostTrip;
	private ImageButton mImageButtonCalendar;
	
	//variables for trip details
	EditText mEditTextDate;
	AutoCompleteTextView mAutoCompleteTextviewCityStart;
	AutoCompleteTextView mAutoCompleteTextviewCityDestination;
	EditText mTotalCost;
	EditText mPerPersonCost;
	EditText mComment;
	NumberPicker mNumberPickerCarNumberofSeats;
	
	//dialog to run when we're waiting on response
	private ProgressDialog mDialog;
	
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trip_post);
		
		//instancing inpout controls
		mEditTextDate = (EditText)findViewById(R.id.editTextStartDate);
		mAutoCompleteTextviewCityDestination = (AutoCompleteTextView)findViewById(R.id.autocompleteCityDestination);
		mAutoCompleteTextviewCityStart = (AutoCompleteTextView)findViewById(R.id.autocompleteCityStart);
		mTotalCost = (EditText)findViewById(R.id.editTextTotalCost);
		mPerPersonCost = (EditText)findViewById(R.id.editTextPerPersonCost);
		mComment = (EditText)findViewById(R.id.editTextComment);
		
		//instancing button controls
		mButtonPostTrip = (Button)findViewById(R.id.buttonPostTrip);
		mImageButtonCalendar = (ImageButton)findViewById(R.id.imageButtonTripDate);
		
		//setting up listeneres on buttons
		mButtonPostTrip.setOnClickListener(this);
		mImageButtonCalendar.setOnClickListener(this);
		
		//instancing numberpicker
		mNumberPickerCarNumberofSeats = (NumberPicker)findViewById(R.id.numberPickerCarSeatNumber);

		//set the number of seats of a car, minus the driver
		mNumberPickerCarNumberofSeats.setMaxValue(Settings.getInstance().getUser().getSeatNumber()-1);
		mNumberPickerCarNumberofSeats.setValue(Settings.getInstance().getUser().getSeatNumber()-1);
		
		
		//Date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		
		//initialise action bar
		ActionBar at = getActionBar();
		//Show the action bar if it's hidden
		if (!at.isShowing()){
		   	at.show();
		}
		
		//get the list of cities
		try {
			//check to see if the relevant cache file exists without trying to open it
			File file = getBaseContext().getFileStreamPath("cities");
			Log.d(TAG, "pocetak provjere");
			if(file.exists() && 
					//check the validity of the file. If it's not older then a week, it's still valid
					((System.currentTimeMillis() - file.lastModified())/1000) < (60 * 60 * 24 * VALIDITY)) {
					Log.d(TAG, "provjera uspjesna");
					//populate autocompletetextviews
					//reading cache file				
					instanceCities();			
			} else {
				Log.d(TAG, "pocetak ponovno cachiranje liste gradova");
				//1st time logging in, setting up cache file
				AsyncRequest cities = new AsyncRequest(AsyncRequest.TYPE_GET_CITIES, null, mDialog, this);
				cities.execute();
			}
		}
		catch (Exception e) {
			e.printStackTrace();	
		}
	}

	
	
	//Method for determing actions based on which button was clicked
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		//when we're ready to post a trip
		case R.id.buttonPostTrip:
			try {				
				startRegisterProcess(AsyncRequest.TYPE_TRIP_POST);
			}
			catch (Exception e) {
				e.printStackTrace();	
			}
			break;
			
		//on calendar button image
		case R.id.imageButtonTripDate:
			//when we try to set the birth DATE for the user
			showDialog(ID_DATE_START);
			break;
		default:
			break;
		}
	}
	
	
	
	//DATE dialogue
	private final static int ID_DATE_START = 1;
		
	//DATE
	//fill the dialog
	@SuppressLint("NewApi")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id){
		case ID_DATE_START:
			return new DatePickerDialog(this, listenerDate_trip, mYear, mMonth, mDay);
		}
		return null;
	}
			
	private DatePickerDialog.OnDateSetListener listenerDate_trip = new DatePickerDialog.OnDateSetListener () {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			writeDate(mEditTextDate);
		}
	};
			
	private void writeDate(EditText inputbox) {
		inputbox.setText(new StringBuilder()
			.append(mYear)
			.append("-")
			.append(mMonth +1)
			.append("-")
			.append(mDay));
	}



	@Override
	public void onSuccess(int type, JSONObject json) {
		int code;
		String message;
		
		try{
			/*
			 * Check for generic error 
			 */
			code = getStatusCode(json);
			message = getStatusMessage(json);
			if(code != 0) {
				Log.d(TAG, getStatusMessage(json));
				Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
				return;
			}
			
			/*
			 * Process response object
			 */
			switch (type) {
				//for INSERTING user data
				case AsyncRequest.TYPE_TRIP_POST:
					//insert stuff once the server is done
					//On success full registration we return to login
					//startMyActivity(ACTIVITY_LOGIN);
					
				//for POPULATING AutoCompleteTextViews
				case AsyncRequest.TYPE_GET_CITIES:
					//Set the list of cities
					//Settings.getInstance().setCityList(JSONParser.getCityList(json));				
						
					// Save the list of entries to internal storage
					Settings.writeObject(this, "cities", JSONParser.getCityList(json));
										
					//populate autocompletetextviews
					instanceCities();
					break;
						
				default:
					break; 					
			}
		} catch (Exception e) {
			Log.e(TAG, getString(R.string.message_error),e);
		}
		
	}

	
	private int getStatusCode (JSONObject json) throws JSONException {
		return json.getJSONObject("status").getInt("code");
	}
	
	private String getStatusMessage (JSONObject json) throws JSONException {
		return json.getJSONObject("status").getString("message");
	}
	
	
	//on failed attempt to server connection message
	@Override
	public void onError(int type) {
		//Forming an string for displaying specific error message
		//Call the string with and ID "message_error_CallingRequestType" in the Strings.xml file
		//+ type of the request, and displaying the concated error message as a TOAST message
		String message = getString(R.string.message_error_CallingRequestType) + " " + type;
		Log.d(TAG, message);
		Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();		
	}
	
	private void startRegisterProcess(int async) throws UnsupportedEncodingException {
		//start the asynchronous process:
		//creaing a new thread, passing parameters on that thread to the server and action to be done on that sever with the passed parameters
		AsyncRequest request = new AsyncRequest(async, setValues(), mDialog, this);

		request.execute();	
	}
	
	private List<NameValuePair> setValues() {
		//creating an array that will hold the action and necessary values of the USER to pass to the server
			List<NameValuePair> values = new ArrayList<NameValuePair>();
			values.add(new BasicNameValuePair("departureDate", mEditTextDate.getText().toString()));		
			values.add(new BasicNameValuePair("totalCost", mTotalCost.getText().toString()));
			values.add(new BasicNameValuePair("costPerPerson", mPerPersonCost.getText().toString()));
			values.add(new BasicNameValuePair("locationStart", mAutoCompleteTextviewCityStart.getText().toString()));
			values.add(new BasicNameValuePair("locationEnd", mAutoCompleteTextviewCityDestination.getText().toString()));
			values.add(new BasicNameValuePair("comment", mComment.getText().toString()));
			values.add(new BasicNameValuePair("car_seat_number", mNumberPickerCarNumberofSeats.getDisplayedValues().toString()));
		return values;
	}
	
	
	
	//setting city lists
	@SuppressWarnings("unchecked")
	private void instanceCities () throws IOException, ClassNotFoundException {
		//initialsie variable for storing cahced entry
		List<String> cachedCities = null;

		// Retrieve the list from internal storage
		if (Settings.readObject(this, "cities") != null) {
			cachedCities = (List<String>) Settings.readObject(this, "cities");
		}

		//Adapter for the starting location
		mAutoCompleteTextviewCityStart.setAdapter(
				new ArrayAdapter<String>(
						this, android.R.layout.simple_list_item_1, cachedCities));
		
		//Adapter for the destination location
		mAutoCompleteTextviewCityDestination.setAdapter(
				new ArrayAdapter<String>(
						this, android.R.layout.simple_list_item_1, cachedCities));
	}
}
