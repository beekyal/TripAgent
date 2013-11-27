package hr.algebra.tripagent;

import hr.algebra.tripagent.async.AsyncRequest;
import hr.algebra.tripagent.async.AsyncRequest.OnRequestDone;
import hr.algebra.tripagent.dao.Trip;
import hr.algebra.tripagent.dao.User;
import hr.algebra.tripagent.helpers.Settings;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

public class HistoryActivity extends ListActivity implements OnRequestDone {
	//variable for displaying error message
	private static final String TAG = AsyncRequest.class.getSimpleName();
	
    AccessoriesAdapter mAdapter;
   
    //dialog to run when we're waiting on response
  	private ProgressDialog mDialog;
  	
  	//list of search results in a list
  	private List<Trip> searchList;
  	
  	//list that will contain search parameters from the Search activity
  	List<NameValuePair> values = new ArrayList<NameValuePair>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// receiving DATA from SearchActivity
		//Bundle bundle = getIntent().getExtras();

		//paramteres for the history search results
		values.add(new BasicNameValuePair("username", Settings.getInstance().getUser().getUsername().toString()));
		
		search();
	}

	private void search() {
		//Request to the server for results
		AsyncRequest search = new AsyncRequest(AsyncRequest.TYPE_TRIP_HISTORY, values, mDialog, this);
		search.execute();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    

    
    //message displayed on list item click
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	/*
    	Toast.makeText(SearchResultsActivity.this
    			, R.string.message_info_clickOnButton
    			, searchList.get(position).getId() + ""
    			, Toast.LENGTH_SHORT).show();
    	*/
    	
    	
    	// custom dialog
    	final Dialog dialog = new Dialog(this);
    	//Custom dialog which we're instancing
    	dialog.setContentView(R.layout.alert_dialog_details_history);
    	dialog.setTitle(R.string.title_alert_dialog_details);
    	//set the custom dialog to keep up after touching the screen outside of it
    	dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    	
    	//set the custom dialog components - text, image and button
    	TextView departureDate = (TextView) dialog.findViewById(R.id.textViewDepartureDate_details);
    	TextView totalCost = (TextView) dialog.findViewById(R.id.textViewTotalCost_details);
    	TextView perPersonCost = (TextView) dialog.findViewById(R.id.textViewPerPersonCost_details);
    	TextView numberofSeats = (TextView) dialog.findViewById(R.id.textViewNumberofSeats_details);
    	TextView comment = (TextView) dialog.findViewById(R.id.textViewComment_details);
    	LinearLayout users = (LinearLayout) dialog.findViewById(R.id.LinearLayoutUsers);
    	
    	//set the double format to be displayed 
    	DecimalFormat df = new DecimalFormat("#.##");
    	
    	//set the values of custom dialog components
    	departureDate.setText(searchList.get(position).getDepartureDate());
    	totalCost.setText(df.format(searchList.get(position).getTotalCose()));
    	perPersonCost.setText(df.format(searchList.get(position).getCostPerPerson()));
    	numberofSeats.setText("" + searchList.get(position).getNumberofSeats());      
    	comment.setText(searchList.get(position).getComment()); 	
    	setPicture(searchList.get(position).getUsers().get(0).getCarImage(),
    			dialog.findViewById(R.id.imageViewPictureCar_details));
    	
    	
    	//iteration of users in the selected search result   	
    	//we go through all of the users
    	for(int i = 0; i < searchList.get(position).getUsers().size(); i++){
    		//set a new textview which will hold the necessery details
    		TextView newUser = new TextView(this);
    		
    		//get the current user in itteration
    		User user = searchList.get(position).getUsers().get(i);
    		
    		//concat the string of the user details to be displayed in the textView  
    		newUser.setText(user.getUsername() + " (" +
    				user.getName() + " " +
    				user.getLastname() + ")");
    		
    		//we add the user to the linearlayout
    		users.addView(newUser);
    	}
    	
    	dialog.show(); 	
    }

    //Set the picture on the imageview asynchornus using AQuery
  	@SuppressWarnings("deprecation")
	private void setPicture(String picture, View view) {
  		//instancing new AQuery
  		AQuery aq = new AQuery(this);
  		
  		//disable memory caching for huge images.
  		boolean memCache = false;
  		boolean fileCache = true;
  		
  		//get the display parameters such as height, width, orientation
  		Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
        if (d.getOrientation() == 1) {
      		//fetch and set the image
      		aq.id(view).image(picture, memCache, fileCache, d.getHeight()/2, 0);
        } else {
        	aq.id(view).image(picture, memCache, fileCache, d.getWidth()/2, 0);
        }
  	}
 
    
    /**
     * A pretty basic ViewHolder used to keep references on children
     */
    private static class AccessoriesViewHolder {
    	//parent content
        public TextView userName;
        //child content
        public TextView content_small;
    }

    
    
    /**
     * The Adapter which is used
     */
    private class AccessoriesAdapter extends BaseAdapter { 	
        @Override
        public int getCount() {
            return searchList.size();
        }

        @Override
        public String getItem(int position) {
            return searchList.get(position).getUsers().get(0).getUsername(); 
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        
       //Data to be displayed in the list 
        @Override
        public View getView(int position, View convertView, ViewGroup group) {
        	//list view
        	Trip trip;
        	User user;
            AccessoriesViewHolder holder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.activity_history, group , false);

                holder = new AccessoriesViewHolder();

                holder.userName = (TextView) convertView.findViewById(R.id.content);
                holder.content_small = (TextView) convertView.findViewById(R.id.content_small);

                convertView.setTag(holder);
            } else {
                holder = (AccessoriesViewHolder) convertView.getTag();
            }
            
            trip = searchList.get(position);
           
            //Item list - 1st line
            user = trip.getUsers().get(0);
            holder.userName.setText(user.getUsername());
     
            //Item list - 2nd line
            //composed of: Date of departure, total cost of the trip, cost per person
            String smallText = trip.getDepartureDate() + " / " + trip.getTotalCose() + " / " + trip.getCostPerPerson();
            holder.content_small.setText(smallText);
            return convertView;
        }
    }

	@Override
	public void onSuccess(int type, JSONObject json) {
//		Log.d(TAG, "stuff to do");
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
				case AsyncRequest.TYPE_TRIP_HISTORY:
					Log.d(TAG, "json objekt:" + json);
					//On successfull search we set the results in an arraylist
					setSearchResults(json);
					
			        //instancing new adapter to be used
			        mAdapter = new AccessoriesAdapter();
			        setListAdapter(mAdapter);
			       
			        //setting up the adapter
					//lview.setAdapter(mAdapter);
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
	
	
	
	//grabbing the swearch results json object
	public void setSearchResults(JSONObject json) throws JSONException {
		searchList = new ArrayList<Trip>();
		Trip trip = null;
		
		//checking to see if we got any results
		if(!json.has("result")){
			Log.d(TAG, "No results!");
			return;
		}

		//declaring an array of results
		JSONArray searchJson;
		searchJson = json.getJSONArray("result");
		
		//iterating through all of the results, instancing objects 
		for (int i = 0; i < searchJson.length(); i++) {
			trip = Trip.getTrip(searchJson.getJSONObject(i));
			searchList.add(trip);
		}

		return;		
	}

	//case statement for when any other button is clicked that's not located on the list of the search results
	/*@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonBack:
			finish();
			break;
		default:
			break;
		}	
	}*/
}
