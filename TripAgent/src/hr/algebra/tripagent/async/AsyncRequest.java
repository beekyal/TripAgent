package hr.algebra.tripagent.async;

import hr.algebra.tripagent.helpers.Settings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncRequest extends AsyncTask<Void, Void, JSONObject> {
	
	private static final String TAG = AsyncRequest.class.getSimpleName();
	
	public static final int TYPE_LOG_IN = 1;
	public static final int TYPE_GET_PROFILE = 2;
	public static final int TYPE_REGISTER_PROFILE = 3;
	public static final int TYPE_UPDATE_PROFILE = 4;
	public static final int TYPE_SEARCH = 5;
	public static final int TYPE_TRIP_POST = 6;
	public static final int TYPE_GET_CITIES = 7;
	public static final int TYPE_TRIP_APPLY = 8;
	public static final int TYPE_TRIP_HISTORY = 9;

	private int mType;
	
	private List<NameValuePair> mValueList;
	private ProgressDialog mProgressDialog;
	private OnRequestDone mListener;
	
	
	//What happens when the request is done
	public interface OnRequestDone {
		void onSuccess(int type, JSONObject json);
		void onError(int type);
	}
	
	
	//Constructor for calling AsyncRequest class
	//Accepts the following attributes:
	//type of the request, values to be sent to the server, progress dialog, listener
	public AsyncRequest(int type, List<NameValuePair> values, ProgressDialog dialog, OnRequestDone listener) {
		this.mType = type;
		this.mValueList = values;
		this.mProgressDialog = dialog;
		this.mListener = listener;
		
		//check to see if we've sent an empty arraylist, if we did, create a new one
		if (mValueList == null) {
			mValueList = new ArrayList<NameValuePair>();
		}
		
		//we add the value of the action NameValuePair to the list of values we passed
		//based on the type of Activity we're doing
		mValueList.add(new BasicNameValuePair("action", getActionString()));
	}
	
	
	//we call the progress dialog to show that an action is being executed
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//Check to see if the progress is still being exceuted, if it is, show the message
		if(mProgressDialog != null) {
			mProgressDialog.show();
		}
	}
	
	
	//We create a request to the http server on a new thread
	@Override
	protected JSONObject doInBackground(Void... arg0) {
		JSONObject json = null;
		
		//Instancing a new thread
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse;
		BufferedReader bufferedReader;
		StringBuffer sb;
		String line = "";
		
		//Try/Catch block for executing the newly instanced thread
		try {
			//instancing a new thread
			HttpPost httpPost = new HttpPost(Settings.getInstance().getServerURL());
						
			//Passing the array to the server via new thread
			httpPost.setEntity(new UrlEncodedFormEntity(mValueList));

			//Execute the newly instanced thread
			httpResponse = httpClient.execute(httpPost);
			
			//Get the content for the http request (values, variables, process to be exevuted etc.)
			bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			sb = new StringBuffer();

			//store the values from the content from the bufferreader in the StringBuffer sb
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			
			//convert the StringBuffer sb to a JSON object
			Log.d("", sb.toString());				//Log message to see server response, DELETE afterwards
			json = new JSONObject(sb.toString());
		} catch (Exception e) {			
			//if there were any errors in the http request, throw an exception
			Log.e(TAG, "Error Connecting to the Server", e); //this error message is displayed in LogCat 
			//e.printStackTrace(); this error message is printed in console
		}
		
		//Method returns the json object to be used in the onPostExecute method
		return json;
	}
	
	
	//method that executes after the http has been resolved
	//accepts json object returned from the doInBackground method
	@Override
	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);
		//run progressDialog if the result is still loading
		if(mProgressDialog != null) {
			mProgressDialog.dismiss();
			}
		if(mListener != null) {
			if(result != null) {
				//if we got a result after the http request has been executed, we get the result
				mListener.onSuccess(mType, result);
			} else {
				//if we didn't get a result after the http request has been executed, we get an error
				mListener.onError(mType);
			}
		}
	}
	
	
	//Method for setting type of the action to be performed on the server based off the type of activity sent to the constructor
	private String getActionString() {
		String typeName;
		switch (mType) {
		case TYPE_LOG_IN:
			typeName = "login";
			break;
		case TYPE_REGISTER_PROFILE:
			typeName = "register";
			break;
		case TYPE_GET_PROFILE:
			typeName = "get";
			break;
		case TYPE_UPDATE_PROFILE:
			typeName = "update";
			break;
		case TYPE_SEARCH:
			typeName = "search";
			break;
		case TYPE_TRIP_POST:
			typeName = "trippost";
			break;
		case TYPE_GET_CITIES:
			typeName = "getcities";
			break;	
		case TYPE_TRIP_APPLY:
			typeName = "trip_applay";
			break;
		case TYPE_TRIP_HISTORY:
			typeName = "trip_history";
			break;
		default:
			typeName = "";
			break;
		}	
		return typeName;
	}
}
