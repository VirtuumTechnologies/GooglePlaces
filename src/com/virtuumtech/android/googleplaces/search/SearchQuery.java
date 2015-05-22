/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * SearchQuery is the base class provides common methods for Google Places API search. 
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.virtuumtech.android.googleplaces.GPConstants;
import com.virtuumtech.android.googleplaces.NetworkService;
import com.virtuumtech.android.googleplaces.PlacesList;
import com.virtuumtech.android.googleplaces.listener.SearchResultsUpdate;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

public abstract class SearchQuery extends ResultReceiver {

	private static final String TAG = "SearchQuery";
	private static final long QUERY_INTERNVAL = 3000; //Query internal for next page token

	private String openNow = "";
	private String urlQuery = "";
	private String pageToken = "";
	private int statusCode ;
	private Context mContext;
	private long lastQueryTime = 0;
	private SearchResultsUpdate mUpdateListener;
	
	//To store the list of parameters required for google search
	private HashMap<String, String> searchParameters = new HashMap<String, String>();

	//Constructor of SearchQuery
	public SearchQuery(Context context, String apikey, String queryUrl) {
		super(new Handler());
		mContext = context;
		urlQuery = queryUrl;
		mUpdateListener = (SearchResultsUpdate) context;
		addParameter("key", apikey);
	}

	/**
	 * Get the status code of the search request result
	 * @return
	 */
	public int getStatusCode () {
		return statusCode;
	}

	/**
	 * Set the Search code of search request result using code
	 * @param code
	 */
	protected void setStatusCode(int code) {
		statusCode = code;
	}

	/**
	 * Set the search code of search request result using GOOGLE API Status code
	 * @param status
	 */
	protected void setStatusCode(String status) {
		statusCode = RequestStatus.getStatusCode(status);
	}

	/**
	 * Set the last query time, which used to check the time diff before submitting next query
	 */
	private void setLastQueryTime() {
		lastQueryTime = System.currentTimeMillis();
	}
	
	/**
	 * Wait for next time interval to submit next query using page token
	 */
	protected void waitForQueryInterval() {
		long timeDiff = System.currentTimeMillis() - lastQueryTime;
		if (timeDiff < QUERY_INTERNVAL) {
			SystemClock.sleep(QUERY_INTERNVAL - timeDiff);
		}
	}

	/**
	 * Set location for the query
	 * @param location
	 */
	public void setLocation (Location location) {
		addParameter("location",location.getLatitude()+","+location.getLongitude());
	}
	
	/** 
	 * Set type for the Query
	 * @param types
	 */
	public void setTypes (String types) {
		addParameter("types", types);
	}

	/**
	 * Remove type from the search parameters 
	 * @return
	 */
	public boolean removeTypes() {
		return removeParameter("types");
	}
	
	/**
	 * Set radius for the search query
	 * @param radius
	 */
	public void setRadius(String radius) {
		addParameter("radius",radius);
	}
	
	/**
	 * Remove radius from the search query
	 * @return
	 */
	public boolean removeRadius() {
		return removeParameter("radius");
	}
	
	/** 
	 * To include open now in the search query 
	 */
	public void setOpenNow () {
		openNow = "opennow";
	}

	/** 
	 * Remove opennow from the search query 
	 * @return
	 */
	public boolean removeOpenNow() {
		openNow = "";
		return true;
	}

	// To add a parameters to search query
	protected void addParameter(String key, String value) {
		searchParameters.put(key, value);
	}
	
	// To remove a parameters from search query
	protected boolean removeParameter(String key) {
		return searchParameters.remove(key) != null ? true : false;
	}
	
	// Clear all the parameters from the search query
	protected void clearParameters() {
		searchParameters.clear();
	}
	
	/**
	 * Construct the search query using the set parameters. 
	 * @return
	 */
	protected String getUrlString () {
		Log.v(TAG,"Inside getURLString");
		StringBuffer strBuffer = new StringBuffer("");
		//Get the parameters from the hash and construct the query
		for (Map.Entry<String, String> entry : searchParameters.entrySet()){
			strBuffer.append(entry.getKey()+"="+entry.getValue()+"&");
		}
		//Add the open now parameter
		String urlStr = urlQuery+strBuffer.toString();
		if (openNow != "") {
			urlStr = strBuffer.toString()+openNow+"&";
		}
		
		Log.d(TAG,urlStr);
		return urlStr;
	}

	// Remove the page token when there is no further data
	protected void removePageToken() {
		pageToken = "";
	}
	
	// Set the page token to get next set of data
	protected void setPageToken(String pagetoken) {
		Log.v(TAG,"Inside setPageToken");
		Log.d(TAG,pagetoken);
		pageToken = pagetoken;
	}
	
	/**
	 * To check is there any further data exist
	 * @return true/false for whether next data exist
	 */
	public boolean isNext() {
		Log.v(TAG,"Inside isNext");
		if (pageToken.isEmpty()) {
			Log.d(TAG,"No page token available");
			return false;
		}
		return true;
	}
	
	/**
	 * Get the POIs based on the defined search parameters using service
	 */
	public void getPlaces() {
		startService(getUrlString());
	}
	
	/**
	 * Get the next set of data using pagetoken of previous query
	 * @return status on the query success/invalid
	 */
	public int getNextPlaces() {
		Log.v(TAG,"Inside getNextPlace");
		if(isNext() == true) {
			String urlStr = getUrlString()+"pagetoken"+"="+pageToken;
			//wait till QUERY_INTERNVAL from last query
			waitForQueryInterval();
			startService(urlStr);
			return RequestStatus.OK;
		} else {
			return RequestStatus.INVALID_REQUEST;
		}
	}
	
	/**
	 * Download the data for the given URL using NetworkService
	 * THe results will be received by ResultReceiver
	 * @param urlStr
	 */
	protected void startService (String urlStr) {
		Log.v(TAG,"Inside StartService");
		Intent intent = new Intent(mContext, NetworkService.class);
		intent.putExtra(GPConstants.SERVICE, GPConstants.ACTION_URL_REQUEST);
		intent.putExtra(GPConstants.URL, urlStr);
		intent.putExtra(GPConstants.RECEIVER, this);
		mContext.startService(intent);
	}
	
	/**
	 * Read the result (json file) and parse it as PlaceSummary object. 
	 * The results will be stored in ArrayList.
	 * @param data
	 * @return
	 */
	protected ArrayList<PlacesList> readJsonFileForPlacesDetails (String data) {
		Log.v(TAG,"Inside readJsonFileForPlacesDetails");
		JSONObject jsonFile;
		JSONArray jsonArray;
		
		//Define array to store the PlaceSummary
		ArrayList<PlacesList> arrayPOI = new ArrayList<PlacesList>();
		
		try {
			jsonFile= new JSONObject(data);
			//set the page token for next query & status
			setPageToken(jsonFile.optString("next_page_token", ""));
			setStatusCode(jsonFile.optString("status"));
			
			if(jsonFile.has("results")) {
				jsonArray = jsonFile.getJSONArray("results");
				for (int i = 0; i < jsonArray.length(); i++ ) {
					JSONObject jsonObj = jsonArray.getJSONObject(i);
					//Read the values of each element and store it in PlaceSummary
					PlacesList placeDetails = new PlacesList();
					placeDetails.setPlaceID(jsonObj.optString("place_id"));
					placeDetails.setName(jsonObj.optString("name",""));
					placeDetails.setRating(jsonObj.optDouble("rating", 0.0));
					//Nearby Search address string
					if (jsonObj.has("vicinity")) {
						placeDetails.setAddress(jsonObj.optString("vicinity",""));
					}
					//Text Search address string
					if (jsonObj.has("formatted_address")) {
						placeDetails.setAddress(jsonObj.optString("formatted_address",""));
					}

					if(jsonObj.has("opening_hours")) {
						placeDetails.setOpenNow(jsonObj.getJSONObject("opening_hours").optString("open_now",""));
					} else {
						placeDetails.setOpenNow("");
					}
					Location loc = new Location ("");
					loc.setLatitude(jsonObj.getJSONObject("geometry").getJSONObject("location").optDouble("lat"));
					loc.setLongitude(jsonObj.getJSONObject("geometry").getJSONObject("location").optDouble("lng"));
					placeDetails.setLocation(loc);
					arrayPOI.add(placeDetails);
				}
			} else {
				Log.d(TAG,"No result exist in json file");
				arrayPOI.clear();
			}
		} catch (JSONException e) {
			Log.e(TAG,"Error in parsing Json file",e);
		}
		return arrayPOI;
	}
	
	/**
	 * Result receiver for the search quries. The result file will be received using Bundle 
	 */
	public void onReceiveResult(int resultCode, Bundle bundle) {
		Log.v(TAG,"Inside onReceiveResult");
		Log.d(TAG,"Result Code:"+resultCode+" - "+RequestStatus.getStatusValue(resultCode));
		ArrayList<PlacesList> pDetails;
		String data = bundle.getCharSequence(GPConstants.DATA).toString();
		Log.d(TAG,data);
		//Set the query time, hence next query time can be calculated
		setLastQueryTime();
		
		//Check the query is successful
		if (resultCode != RequestStatus.OK) {
			Log.e(TAG,"Search query is not successful: "+RequestStatus.getStatusValue(resultCode));
			setStatusCode(resultCode);
			pDetails = new ArrayList<PlacesList>();
			pDetails.clear();
		} else {
			Log.i(TAG,"Search Query is successful");
			pDetails = readJsonFileForPlacesDetails(data);
		}
		mUpdateListener.onSearchResultsUpdate(getStatusCode(),pDetails);
	}
}
