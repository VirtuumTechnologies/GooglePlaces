/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * Places is the class to get the places details from Google Places API search. 
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces.search;

/**
 * 
    geocode - instructs the Place Autocomplete service to return only geocoding results, rather than business results. Generally, you use this request to disambiguate results where the location specified may be indeterminate.
    address - instructs the Place Autocomplete service to return only geocoding results with a precise address. Generally, you use this request when you know the user will be looking for a fully specified address.
    establishment - instructs the Place Autocomplete service to return only business results.
    the (regions) type collection instructs the Places service to return any result matching the following types:
        locality
        sublocality
        postal_code
        country
        administrative_area_level_1
        administrative_area_level_2
    the (cities) type collection instructs the Places service to return results that match locality or administrative_area_level_3.

 */

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.virtuumtech.android.googleplaces.GPConstants;
import com.virtuumtech.android.googleplaces.NetworkService;
import com.virtuumtech.android.googleplaces.PlaceDetails;
import com.virtuumtech.android.googleplaces.PlacesList;
import com.virtuumtech.android.googleplaces.listener.AutoCompleteUpdate;
import com.virtuumtech.android.googleplaces.listener.PlaceDetailsUpdate;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class AutoComplete extends ResultReceiver {

	private static final String TAG = "AutoComplete";
	private static final String queryUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";

	
	private int statusCode ;
	private String mApiKey;
	private Context mContext;
	
	private String types="";
	private int radius  = 0;
	private Location location;

	private List<String> predictions;
	private List<String> placeIDs;
	
	private AutoCompleteUpdate mUpdateListener;
	
	public AutoComplete() {
		super(new Handler());
	}

	public AutoComplete(Context context, String apikey) {
		super(new Handler());
		Log.v(TAG,"Places Constructor");
		mApiKey=apikey;
		mContext=context;
		mUpdateListener = (AutoCompleteUpdate) context;
	}
	
	public AutoComplete(Context context, String apikey, String placeID) {
		this(context,apikey);
		Log.v(TAG,"Places Constructor");
	}

	/**
	 * Set the type for auto complete search.
	 * @param type The type can be geocode|address|establishment|locality|sublocality|postal_code|country|administrative_area_level_1|administrative_area_level_2|administrative_area_level_3
	 */
	public void setSearchType(String type) {
		this.types = type;
	}

	/**
	 * Set the radius to search from given location
	 * @param location - Provide the location to search
	 */
	public void setSearchLocation(Location location) {
		this.location = location;
	}

	/**
	 * Set the radius to search from given location
	 * @param location - Provide the location to search
	 * @param radius - Provide the radius to search from the location
	 */
	public void setSearchRadius(Location location, int radius) {
		this.location = location;
		this.radius = radius;
	}
	
	/**
	 * Request the POI details using google places web API. 
	 */
	public void getAutocompletePredictions (String keyword) {
		String url = queryUrl+"input="+keyword+"&key="+mApiKey;
		if (!types.isEmpty()) {
			url = url+"&types="+types;
		}
		if (location!=null) {
			url = url+"&location="+location.getLatitude()+","+location.getLongitude();
		}
		if (radius != 0) {
			url = url+"&radius="+radius;
		}
		
		startService(url,"AUTOCOMPLETE");
	}
	
	/**
	 * Returns the predicted string from auto complete
	 * @return List of string values predicted by google places
	 */
	public List<String> getPredictionStrings () {
		return predictions;
	}
	
	/**
	 * Returns the place id of the given index
	 * @param index
	 * @return Place id of the given index
	 */
	public String getPlaceID(int index) {
		return placeIDs.get(index);
	}

	/**
	 * Returns the place id for the given name
	 * @param placeName
	 * @return String
	 */
	public String getPlaceID(String placeName) {
		int size = predictions.size();
		for (int i=0; i < size; i++) {
			if (placeName.equals(predictions.get(i))) {
				return placeIDs.get(i);
			}
		}
		return "";
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
	 * Download the data for the given URL using NetworkService
	 * THe results will be received by ResultReceiver
	 * @param urlStr
	 */
	protected void startService (String urlStr, String actionType) {
		Log.v(TAG,"Inside StartService");
		Intent intent = new Intent(mContext, NetworkService.class);
		intent.putExtra(GPConstants.SERVICE, GPConstants.ACTION_URL_REQUEST);
		intent.putExtra(GPConstants.URL, urlStr);
		intent.putExtra(GPConstants.TYPE, actionType);
		intent.putExtra(GPConstants.RECEIVER, this);
		mContext.startService(intent);
	}
	
	/**
	 * Result receiver for the search quries. The result file will be received using Bundle 
	 */
	public void onReceiveResult(int resultCode, Bundle bundle) {
		Log.v(TAG,"Inside onReceiveResult");
		Log.d(TAG,"Result Code:"+resultCode+" - "+RequestStatus.getStatusValue(resultCode));
		
		String data = bundle.getCharSequence(GPConstants.DATA).toString();
		Log.d(TAG,data);

		predictions.clear();
		placeIDs.clear();
		if (resultCode != RequestStatus.OK) {
			//Check the query is not successful for main request
			Log.e(TAG,"Google Query is not successful: "+RequestStatus.getStatusValue(resultCode));
			setStatusCode(resultCode);
		} else {
			//Parse the data if query is successful.
			Log.i(TAG,"Google Query is successful");
			parseJsonFile(data);
		}
		
		mUpdateListener.onAutoCompleteUpdate(getStatusCode());
	}


	/**
	 * Read the result (json file) and parse it as AutoComplete object. 
	 * @param data
	 * @return
	 */
	protected void parseJsonFile (String data) {
		Log.v(TAG,"readJsonFileForPlaceDetails");
		JSONObject jsonFile;
		JSONArray jsonArray;
		
		//Define array to store the PlaceSummary
		
		try {
			jsonFile= new JSONObject(data);
			//set the page token for next query & status
			setStatusCode(jsonFile.optString("status"));
			
			if(jsonFile.has("predictions")) {
				jsonArray = jsonFile.getJSONArray("predictions");
				for (int i = 0; i < jsonArray.length(); i++ ) {
					JSONObject jsonObj = jsonArray.getJSONObject(i);
					predictions.add(jsonObj.optString("description"));
					placeIDs.add(jsonObj.optString("place_id"));
				}
			} else {
				Log.d(TAG,"No result exist in json file");
			}
		} catch (JSONException e) {
			Log.e(TAG,"Error in parsing Json file",e);
		}
	}

}

