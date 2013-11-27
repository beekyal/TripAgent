package hr.algebra.tripagent;

import hr.algebra.tripagent.actionbar.BaseActivity;
import hr.algebra.tripagent.async.AsyncRequest;
import hr.algebra.tripagent.async.AsyncRequest.OnRequestDone;
import hr.algebra.tripagent.helpers.JSONParser;
import hr.algebra.tripagent.helpers.Settings;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SearchActivity extends BaseActivity implements OnClickListener, OnRequestDone, TextWatcher {
	//variable for displaying error message
	private static final String TAG = AsyncRequest.class.getSimpleName();
		

	//setting variables for buttons on the activity
	ImageButton mImmageButtonStartDate;
	ImageButton mImmageButtonDepartureDate;
	Button mButtonSearch;
	
	//setting variables for input fields on this activity
	EditText startDate;
	EditText endDate;
	
	//Date related variables
	private int mYear;
	private int mMonth;
	private int mDay;
	
	//dialog to run when we're waiting on response
	private ProgressDialog mDialog;
	
	//Autocomplete textview
	AutoCompleteTextView mAutoCompleteTextviewCityStart;
	AutoCompleteTextView mAutoCompleteTextviewCityDestination;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		//instancing buttons
		mImmageButtonStartDate = (ImageButton)findViewById(R.id.imageButtonStartDate);
		mImmageButtonDepartureDate = (ImageButton)findViewById(R.id.imageButtonDepartureDate);
		mButtonSearch = (Button)findViewById(R.id.buttonSearch);
		
		//instancing input fields
		startDate = (EditText)findViewById(R.id.editTextStartDate);
		endDate = (EditText)findViewById(R.id.editTextFinishDate);
		
		//setting up listeners for buttons
		mImmageButtonDepartureDate.setOnClickListener(this);
		mImmageButtonStartDate.setOnClickListener(this);
		mButtonSearch.setOnClickListener(this);
		
		//initialise action bar
		ActionBar at = getActionBar();
		//Show the action bar if it's hidden
		if (!at.isShowing()){
	    	at.show();
	    }
		
		//Date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		
		//setting the progress dialog
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.message_progressDialog));	
		
		//Instancing AutoCompleteTextview
			//instancing controls
			mAutoCompleteTextviewCityStart = (AutoCompleteTextView)findViewById(R.id.autocompleteCityStart);
			mAutoCompleteTextviewCityDestination = (AutoCompleteTextView)findViewById(R.id.autocompleteCityDestination);
			
			//setting listeners
			mAutoCompleteTextviewCityStart.addTextChangedListener(this);
			mAutoCompleteTextviewCityDestination.addTextChangedListener(this);
			
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
	
	
	//functions for button click events
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		Log.d(TAG, "switch");
		switch (v.getId()) {
		
		//when we try to get results
		case R.id.buttonSearch:
			try {				
				//startRegisterProcess(AsyncRequest.TYPE_SEARCH);
				
				//Transfering DATA from one activity to another
				//DATA to be transfered to a new activity
				Bundle bundle = new Bundle();
				bundle.putString("dateStart", startDate.getText().toString());
				bundle.putString("dateEnd", endDate.getText().toString());
				bundle.putString("locationStart", mAutoCompleteTextviewCityStart.getText().toString());
				bundle.putString("locationEnd", mAutoCompleteTextviewCityDestination.getText().toString());
				
				//Activity to transfer to
				Intent i = new Intent(this, SearchResultsActivity.class);
				
				//adding DATA to the new activity
				i.putExtras(bundle);	
				
				//starting defined activity
				startActivity(i);
			}
			catch (Exception e) {
				e.printStackTrace();	
			}
			break;
		
		//when we try to enter starting date for searching
		case R.id.imageButtonStartDate:
			showDialog(ID_DATE_START);
			break;
			
		//when we try to enter ending date for searching
		case R.id.imageButtonDepartureDate:
			showDialog(ID_DATE_END);
			break;
			
		default:
			break;
		}
	}

	//DATE
	//DATE dialogue
	private final static int ID_DATE_START = 1;
	private final static int ID_DATE_END = 2;
	
	//Filling up dialog
	@SuppressLint("NewApi")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id){
		
		//creating dialog for start date
		case ID_DATE_START:
			return new DatePickerDialog(this, listenerDate_start, mYear, mMonth, mDay);
	
		//creating dialog for end date
		case ID_DATE_END:
			return new DatePickerDialog(this, listenerDate_end, mYear, mMonth, mDay);
		}
		
		return null;
	}
	
	//dialog for start date
	private DatePickerDialog.OnDateSetListener listenerDate_start = new DatePickerDialog.OnDateSetListener () {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			writeDate(startDate);
		}
	};
	
	//dialog for end date
	private DatePickerDialog.OnDateSetListener listenerDate_end = new DatePickerDialog.OnDateSetListener () {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			writeDate(endDate);
		}
	};	
	
	//setting the date type
	private void writeDate(EditText inputbox) {
		inputbox.setText(new StringBuilder()
			.append(mYear)
			.append("-")
			.append(mMonth +1)
			.append("-")
			.append(mDay));
	}


	
	//Function for success
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
				//for SEARCHING active rides data
				case AsyncRequest.TYPE_SEARCH:
					//On successfull searc we display the results
					//startMyActivity(ACTIVITY_SEARCH_RESULTS);
					break;
				
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


	
	@Override
	public void onError(int type) {
		//Forming an string for displaying specific error message
		//Call the string with and ID "message_error_CallingRequestType" in the Strings.xml file
		//+ type of the request, and displaying the concated error message as a TOAST message
		String message = getString(R.string.message_error_CallingRequestType) + " " + type;
		Log.d(TAG, message);
		Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();	
	}
	
	private int getStatusCode (JSONObject json) throws JSONException {
		return json.getJSONObject("status").getInt("code");
	}
	
	private String getStatusMessage (JSONObject json) throws JSONException {
		return json.getJSONObject("status").getString("message");
	}
	
	
	
	//method for setting the values for storing data
	/*private List<NameValuePair> setValues() {
	//creating an array that will hold the action and necessary values of the USER to pass to the server
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("dateStart", startDate.getText().toString()));		
		values.add(new BasicNameValuePair("dateEnd", endDate.getText().toString()));
		values.add(new BasicNameValuePair("locationStart", mAutoCompleteTextviewCityStart.getText().toString()));
		values.add(new BasicNameValuePair("locationEnd", mAutoCompleteTextviewCityDestination.getText().toString()));
		return values;
	}
	*/	
	
	/*
	private void startRegisterProcess(int typeRegisterProfile) {
		//start the asynchronous process:
		//creaing a new thread, passing parameters on that thread to the server and action to be done on that sever with the passed parameters
		AsyncRequest request = new AsyncRequest(typeRegisterProfile, setValues(), mDialog, this);

		request.execute();	
	}
	*/


	//Methods for AutoComplete textview
	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
	
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
