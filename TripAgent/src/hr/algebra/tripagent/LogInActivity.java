package hr.algebra.tripagent;

import hr.algebra.tripagent.actionbar.BaseActivity;
import hr.algebra.tripagent.async.AsyncRequest;
import hr.algebra.tripagent.async.AsyncRequest.OnRequestDone;
import hr.algebra.tripagent.dao.User;
import hr.algebra.tripagent.helpers.Settings;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogInActivity extends BaseActivity implements OnClickListener, OnRequestDone {
	//variable for displaying error message
	private static final String TAG = AsyncRequest.class.getSimpleName();
		
	//variables for implementing views, buttons etc
	private Button mButtonLogIn;
	private Button mButtonRegister;
	private Button mButtonPreview;
	
	//dialog to run when we're waiting on response
	private ProgressDialog mDialog;
	
	//user variables
	EditText user;
	EditText pass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		
		//we set the instance of the button LogIn to the variable mButtonLogIn
		mButtonLogIn = (Button)findViewById(R.id.buttonLogIn);
		mButtonRegister = (Button)findViewById(R.id.buttonRegister);
		mButtonPreview = (Button)findViewById(R.id.buttonPreview);
				
		//setting listneres to the buttons 
		mButtonLogIn.setOnClickListener(this);
		mButtonRegister.setOnClickListener(this);
		mButtonPreview.setOnClickListener(this);
		
		//setting the progress dialog
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.message_progressDialog));
			
		//Getting the instances of the username and password input boxes
		user = (EditText)findViewById(R.id.editTextUsername);
		pass = (EditText)findViewById(R.id.editTextPassword);
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
		return true;		
	}
	
	
	//Case function to determine which button we're pressing and what happens when we press it
	public void onClick(View v){
		switch (v.getId()) {
		
		//When we try to log in
		case R.id.buttonLogIn:
			try {				
				startLoginProcess();
			}
			catch (Exception e) {
				e.printStackTrace();	
			}
		break;
		
		//When we try to log out
		/*case R.id.buttonLogOut:
			//Sets the existing user to null
			Settings.getInstance().setUser(null);
			showButtons();
		break;*/
		
		//When we try to Register
		case R.id.buttonRegister:
			//Change activities
			startMyActivity(ACTIVITY_REGISTER_USER);
			
			
			
			//Transfering DATA from one activity to another
			//Bundle bundle = new Bundle();
			//bundle.putInt(RegisterUserActivity.KEY_ACTIVITY_TYPE, RegisterUserActivity.ACTIVITY_TYPE_REGSTER);
			//i.putExtras(bundle);									   		
		break;
		
		//When we try to Preview
		case R.id.buttonPreview:
			//Change activities
			/*Intent in = new Intent(this, SearchActivity.class);
			try {				
				startActivity(in);			
			} catch (Exception e) {
				Log.e(TAG, getString(R.string.message_error),e);
			}
			finish();		
			*/
			startMyActivity(ACTIVITY_SEARCH);
		break;
		
		default:
			break;
		}
	}
	
	
	//LogIn process
	private void startLoginProcess() throws UnsupportedEncodingException {			
		//start the asynchronuous process:
		//creaing a new thread, passing parameters on that thread to the server and action to be done on that sever with the passed parameters
		AsyncRequest request = new AsyncRequest(AsyncRequest.TYPE_LOG_IN, setValues(), mDialog, this);
		request.execute();
	}


	//Function for succes
	@Override
	public void onSuccess(int type, JSONObject json) {
		int code;
		String message;
		Log.i(TAG, "AsynReqest uspješno");
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
			case AsyncRequest.TYPE_LOG_IN:
				/*
				 * Set active user session
				 */
				User user = User.getUser(json);
				if (user == null) {
					throw new Exception("No user in json object");
				} else {
					Settings.getInstance().setUser(user);
					checkUserStatus();
					
					//Change activities
					/*
					Intent i = new Intent(this, SearchActivity.class);
					try {				
						startActivity(i);			
					} catch (Exception e) {
						Log.e(TAG, getString(R.string.message_error),e);
					}
					finish();
					*/
					startMyActivity(ACTIVITY_SEARCH);
				}
				break;
			default:
				break;
			}
		}catch (Exception e) {
			Log.e(TAG, getString(R.string.message_error),e);
		}
	}


	
	//Error message 
	@Override
	public void onError(int type) {
		Log.i(TAG, "AsynReqest error");
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
	
	private void checkUserStatus() {
		//we check if the user was succesfully logged or not, and show appropriate buttons
		if(Settings.getInstance().isUserLoggedIn()){
		} else {	
			Toast.makeText(getBaseContext(), getString(R.string.message_error_NoUser), Toast.LENGTH_SHORT).show();
		}
	}
	
	
	//set the values of variables to be sent to the server in a list
	private List<NameValuePair> setValues() {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("username", user.getText().toString()));
		values.add(new BasicNameValuePair("password", pass.getText().toString()));
		
		return values;
	}
}

