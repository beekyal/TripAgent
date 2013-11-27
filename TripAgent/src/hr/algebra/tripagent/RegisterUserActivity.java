package hr.algebra.tripagent;

import hr.algebra.tripagent.actionbar.BaseActivity;
import hr.algebra.tripagent.async.AsyncRequest;
import hr.algebra.tripagent.async.AsyncRequest.OnRequestDone;
import hr.algebra.tripagent.dao.User;
import hr.algebra.tripagent.helpers.Settings;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.androidquery.AQuery;

@SuppressLint("SimpleDateFormat")
public class RegisterUserActivity extends BaseActivity  implements OnClickListener, OnRequestDone {
	//variable for displaying error message
	private static final String TAG = AsyncRequest.class.getSimpleName();
	
	/*
	public static final String KEY_ACTIVITY_TYPE = "activity_type";
	public static final int ACTIVITY_TYPE_REGSTER = 1;
	public static final int ACTIVITY_TYPE_EDIT = 2;
	
	public static final String KEY_NAME = "key_name";
	public static final String KEY_LASNAME = "key_name";
	*/
	
	//camera related variables and constants
	protected static final int PICTURE_RESULT = 1337;
	Uri imageUri;
	ContentValues values;
	String imageurl;
	String carPictureTitle;
	
	//Date related variables
	private int mYear;
	private int mMonth;
	private int mDay;
	
	//variables for implementing views, buttons etc
	private Button mButtonRegister;
	private Button mButtonUpdate;
	private Button mButtonCarPicture;
	private ImageButton mImageButtonCalendar;
	//private ImageView mImageViewPictureCar;
	
	//dialog to run when we're waiting on response
	private ProgressDialog mDialog;
	
	//Setting the values of the USER data
		EditText user;
		EditText pass;
		EditText name;
		EditText nameLast;
		EditText phone;
		EditText email;
		EditText birthdate;
	//Setting the values of the CAR data
		EditText carRegistration;
		EditText carModel;
		EditText carExpenditure;
		EditText carSeatNumber;
		

					
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_user);
		
		//unique photo name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		carPictureTitle = "Photo_" + timeStamp + "_";
		
		//we set the instance of the button LogIn to the variable mButtonLogIn
		mButtonRegister = (Button)findViewById(R.id.buttonRegister);
		mButtonUpdate = (Button)findViewById(R.id.buttonUpdate);
		mImageButtonCalendar = (ImageButton)findViewById(R.id.imageButtonBirthYear);
		mButtonCarPicture = (Button)findViewById(R.id.buttoncarPicture);
		//mImageViewPictureCar = (ImageView)findViewById(R.id.imageViewPictureCar);
		
		//setting listneres to the buttons 
		mButtonRegister.setOnClickListener(this);
		mButtonUpdate.setOnClickListener(this);
		mImageButtonCalendar.setOnClickListener(this);
		mButtonCarPicture.setOnClickListener(this);
		
		//setting the progress dialog
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.message_progressDialog));	
		
		//instancing inputs of the USER data
			user = (EditText)findViewById(R.id.editTextUsername);
			pass = (EditText)findViewById(R.id.editTextPassword);
			name = (EditText)findViewById(R.id.editTextNameFirst);
			nameLast = (EditText)findViewById(R.id.editTextNameLast);
			phone = (EditText)findViewById(R.id.editNumberPhone);
			email = (EditText)findViewById(R.id.editEmailAdress);
			birthdate = (EditText)findViewById(R.id.editBirthYear);
		//instancing inputs of the CAR data
			carRegistration = (EditText)findViewById(R.id.editCarRegistration);
			carModel = (EditText)findViewById(R.id.editCarModel);
			carExpenditure = (EditText)findViewById(R.id.editGassExpenditure);
			carSeatNumber = (EditText)findViewById(R.id.editNumberOfSeats);
		
		//Date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
			
		//we check the status of the user
		try {
			if(Settings.getInstance().isUserLoggedIn()) {
				//if the user is logged in, we get his\hers data and hide\show appropriate buttons
				clearData();
				readData();
				mButtonRegister.setVisibility(View.GONE);
				mButtonUpdate.setVisibility(View.VISIBLE);
			} else {
				clearData();
				mButtonRegister.setVisibility(View.VISIBLE);
				mButtonUpdate.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		
		//initialise action bar
		ActionBar at = getActionBar();
		//Show the action bar if it's hidden
		if (!at.isShowing()){
	    	at.show();
	    }
		
		/*
			//receiving DATA from another activity
			Bundle bundle = getIntent().getExtras();
			//Receiving type of Activity
			int activityType =  bundle.getInt(KEY_ACTIVITY_TYPE);
			
			//Actions based on type of activity
			switch (activityType) {
			case ACTIVITY_TYPE_EDIT:
				readData()
				break;
			case ACTIVITY_TYPE_REGSTER:
				
				break;
			default:
				break;
			}
		*/
	}
	
	//functions for button click events
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		//When we try to register
		case R.id.buttonRegister:
			try {				
				startRegisterProcess(AsyncRequest.TYPE_REGISTER_PROFILE); 
			}
			catch (Exception e) {
				e.printStackTrace();	
			}
			break;
			
		//When we UPDATE data of the existing user
		case R.id.buttonUpdate:
			try {				
				startRegisterProcess(AsyncRequest.TYPE_UPDATE_PROFILE); 
			}
			catch (Exception e) {
				e.printStackTrace();	
			}
			break;
		
		//when we try to set the birth DATE for the user
		case R.id.imageButtonBirthYear:
			showDialog(ID_DATE_BRITH);
			break;
			
		//when we try to take a PICTURE OF A CAR
		case R.id.buttoncarPicture:
			values = new ContentValues();
			
			values.put(MediaStore.Images.Media.TITLE, carPictureTitle);
			values.put(MediaStore.Images.Media.DESCRIPTION,
					"Image Processing original fotografija");
			imageUri = getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(intent, PICTURE_RESULT);
			break;
			
		default:
			break;
		}
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
				//for INSERTING user data
				case AsyncRequest.TYPE_REGISTER_PROFILE:
					//On success full registration we return to login
					startMyActivity(ACTIVITY_LOGIN);
					
				//for UPDATING user data
				case AsyncRequest.TYPE_UPDATE_PROFILE:
					//we clear input boxes of all the data currently stored in them
					clearData();
					
					//set the user object with new data
					User user = User.getUser(json);
					Settings.getInstance().setUser(user);
					
					//populate the input values with the new data
					readData();
					break;
					
				default:
					break; 					
			}
		} catch (Exception e) {
			Log.e(TAG, getString(R.string.message_error),e);
		}
	}
	
	//Error message 
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

	private void startRegisterProcess(int async) throws UnsupportedEncodingException {
		//start the asynchronous process:
		//creaing a new thread, passing parameters on that thread to the server and action to be done on that sever with the passed parameters
		AsyncRequest request = new AsyncRequest(async, setValues(), mDialog, this);

		request.execute();	
	}
	
	//method for setting the values for storing data
	private List<NameValuePair> setValues() {
		//formatting picture for server
		//format the picture into a bitmap
		Bitmap bitmap = BitmapFactory.decodeFile(imageurl);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//compressing into a .PNG
		bitmap.compress(CompressFormat.PNG, 20, stream);
		byte[] byte_err = stream.toByteArray();
		String image_string = Base64.encodeToString(byte_err, Base64.DEFAULT);
		
		//creating an array that will hold the action and necessary values of the USER to pass to the server
			List<NameValuePair> values = new ArrayList<NameValuePair>();
			values.add(new BasicNameValuePair("username", user.getText().toString()));
			values.add(new BasicNameValuePair("password", pass.getText().toString()));
			values.add(new BasicNameValuePair("name", name.getText().toString()));
			values.add(new BasicNameValuePair("lastname", nameLast.getText().toString()));
			values.add(new BasicNameValuePair("phonenumber", phone.getText().toString()));
			values.add(new BasicNameValuePair("email", email.getText().toString()));
			values.add(new BasicNameValuePair("birthdate", birthdate.getText().toString()));
		//creating an array that will hold the action and necessary values of the CAR to pass to the server
			values.add(new BasicNameValuePair("car_registration", carRegistration.getText().toString()));
			values.add(new BasicNameValuePair("car_model", carModel.getText().toString()));
			values.add(new BasicNameValuePair("car_expenditure", carExpenditure.getText().toString()));
			values.add(new BasicNameValuePair("car_seat_number", carSeatNumber.getText().toString()));			
			values.add(new BasicNameValuePair("car_image", image_string));
		return values;
	}
	
	//get the data stored in the user class and fill the appropriate input fields
	private void readData() {
		// prvi nacin koji necemo korisiti
		//Bundle bundle = getIntent().getExtras();
		//name.setText(bundle.getString(KEY_NAME));
		//lastname.setText(bundle.getString(KEY_LASNAME));

		//set the double format to be displayed 
		DecimalFormat df = new DecimalFormat("#.##");
		// drugi nacin koji ce mo korsiti 
		//Retrieve data stored in the User class
		user.setText(Settings.getInstance().getUser().getUsername());
		//pass.setText(Settings.getInstance().getUser().getPassword());
		name.setText(Settings.getInstance().getUser().getName());
		nameLast.setText(Settings.getInstance().getUser().getLastname());
		phone.setText(Settings.getInstance().getUser().getPhonenumber());
		email.setText(Settings.getInstance().getUser().getEmail());
		birthdate.setText(Settings.getInstance().getUser().getBirthdate());
		//Retrieve data for the car stored in the User class
		carRegistration.setText(Settings.getInstance().getUser().getCarRegistration());
		carModel.setText(Settings.getInstance().getUser().getCarModel());
		carExpenditure.setText(df.format(Settings.getInstance().getUser().getCarExpenditure()));
		carSeatNumber.setText("" + Settings.getInstance().getUser().getSeatNumber());
		setPicture(Settings.getInstance().getUser().getCarImage());
		
	}
	
	//Clear all the input values on this activity
	private void clearData() {
		//Clear the user data
		user.setText("");
		pass.setText("");
		name.setText("");
		nameLast.setText("");
		phone.setText("");
		email.setText("");
		birthdate.setText("");
		//Clear the car data
		carRegistration.setText("");
		carModel.setText("");
		carExpenditure.setText("");
		carSeatNumber.setText("");
		setPicture(null);
		
	}
	
	//DATE dialogue
	private final static int ID_DATE_BRITH = 1;
	
	//DATE
	//fill the dialog
	@SuppressLint("NewApi")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id){
		case ID_DATE_BRITH:
			return new DatePickerDialog(this, listenerDate_birth, mYear, mMonth, mDay);
		}
		return null;
	}
		
	private DatePickerDialog.OnDateSetListener listenerDate_birth = new DatePickerDialog.OnDateSetListener () {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			writeDate(birthdate);
		}
	};
	
	
	//Formatting the date display yyyy-mm-dd
	private void writeDate(EditText inputbox) {
		inputbox.setText(new StringBuilder()
			.append(mYear)
			.append("-")
			.append(mMonth +1)
			.append("-")
			.append(mDay));
	}
	
	
	
	//CAMERA
	//on succesfull picture, generate image and get it's url
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case PICTURE_RESULT:
			if (requestCode == PICTURE_RESULT)
				if (resultCode == Activity.RESULT_OK) {
					try {
						imageurl = getRealPathFromURI(imageUri);
						Log.d(TAG, "after the picture was taken URL " + imageurl);
						Log.d(TAG, "after the picture was taken URI " + imageUri);
//						Intent intent = new Intent(getApplicationContext(),
//								ProcessImage.class);
//						intent.putExtra("MYBITMAP", imageurl);
//						intent.putExtra("PHOTONAME", naslov);
//						startActivity(intent);
						
						//call the method for setting the picture
						setPicture(imageurl);
						
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			break;

			
		}
	}

	//grab picture URI
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = null;
		cursor = this.getContentResolver().query(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String s1 = cursor.getString(column_index);
		cursor.close();
		return s1;
	}
	
	//Set the picture on the imageview asynchornus using AQuery
	@SuppressWarnings("deprecation")
	private void setPicture(String picture) {
		//instancing new AQuery
		AQuery aq = new AQuery(this);
		
		//disable memory caching for huge images.
		boolean memCache = false;
		boolean fileCache = true;
		
  		//get the display parameters such as height, width, orientation
  		Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
        if (d.getOrientation() == 1) {
      		//fetch and set the image
      		aq.id(R.id.imageViewPictureCar).image(picture, memCache, fileCache, d.getHeight()/2, 0);
        } else {
        	aq.id(R.id.imageViewPictureCar).image(picture, memCache, fileCache, d.getWidth()/2, 0);
        }
	}
}