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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.virtuumtech.android.googleplaces.GPConstants;
import com.virtuumtech.android.googleplaces.NetworkService;
import com.virtuumtech.android.googleplaces.PlaceDetails;
import com.virtuumtech.android.googleplaces.listener.PlaceDetailsUpdate;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class Places extends ResultReceiver {

	private static final String TAG = "Places";
	private static final String queryUrl = "https://maps.googleapis.com/maps/api/place/details/json?";
	private static final String wikiUrl  = "https://en.wikipedia.org/w/api.php?action=query&prop=coordinates|extracts&exsectionformat=plain&explaintext&exintro&exsentences=3&format=json&titles=";
	
	private boolean isWikiData = false;
	private int statusCode ;
	private String mPlaceID;
	private String mApiKey;
	private Context mContext;
	private PlaceDetails mPlaceDetails;

	private PlaceDetailsUpdate mUpdateListener;
	
	public Places() {
		super(new Handler());
	}

	public Places(Context context, String apikey) {
		super(new Handler());
		Log.v(TAG,"Places Constructor");
		mApiKey=apikey;
		mContext=context;
		mUpdateListener = (PlaceDetailsUpdate) context;
	}
	
	public Places(Context context, String apikey, String placeID) {
		this(context,apikey);
		Log.v(TAG,"Places Constructor");
		mPlaceID = placeID;
	}

	/**
	 * Set the place id to get the details of Google Place
	 * 
	 * @param placeID
	 */
	public void setPlaceID (String placeID) {
		mPlaceID = placeID;
	}
	
	/**
	 * Request the POI details using google places web API. 
	 */
	public void requestPlaceDetails () {
		String url = queryUrl+"placeid="+mPlaceID+"&key="+mApiKey;
		startService(url,"PLACEDETAILS");
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
	 * Set the flag if wiki data required to be downloaded.
	 * @param isWikiDataRequired
	 */
	public void setWikiDataDownload (boolean isWikiDataRequired) {
		isWikiData=isWikiDataRequired;
	}
	
	/**
	 * Set the search code of search request result using GOOGLE API Status code
	 * @param status
	 */
	protected void setStatusCode(String status) {
		statusCode = RequestStatus.getStatusCode(status);
	}
	
	/**
	 * Get the POI details read from retrieved file. 
	 */
	public PlaceDetails getPlaceDetails() {
		return mPlaceDetails;
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
	
	private void downloadWikiData () {
		String name = mPlaceDetails.getName().replace(" ", "_");
		String url = wikiUrl+name;
		startService(url,"WIKIDOWNLOAD");
	}
	
	
	
	/**
	 * Result receiver for the search quries. The result file will be received using Bundle 
	 */
	public void onReceiveResult(int resultCode, Bundle bundle) {
		Log.v(TAG,"Inside onReceiveResult");
		Log.d(TAG,"Result Code:"+resultCode+" - "+RequestStatus.getStatusValue(resultCode));
		
		String data = bundle.getCharSequence(GPConstants.DATA).toString();
		String action = bundle.getCharSequence(GPConstants.TYPE).toString();
		Log.d(TAG,action);
		Log.d(TAG,data);

		// Check the request for google place details
		if (action.equals("PLACEDETAILS")) {
			if (resultCode != RequestStatus.OK) {
				//Check the query is not successful for main request
				Log.e(TAG,"Google Query is not successful: "+RequestStatus.getStatusValue(resultCode));
				setStatusCode(resultCode);
				mUpdateListener.onPlaceDetailsUpdate(getStatusCode());
			} else {
				//Parse the data if query is successful.
				Log.i(TAG,"Google Query is successful");
				processResult(data,action);
			}
		}
		
		// Check the request for Wikidownload
		if (action.equals("WIKIDOWNLOAD")) {
			if (resultCode != RequestStatus.OK) {
				Log.i(TAG,"Wiki request is not successful");
				//// Set wiki page doesn't exist
			} else {
				//Parse the data if request is successful.
				Log.i(TAG,"Wiki request is successful");
				processResult(data,action);				
			}
		}
		
		if (action.equals("PLACEDETAILS") && isWikiData == false) {
			mUpdateListener.onPlaceDetailsUpdate(getStatusCode());
		} else {
			mUpdateListener.onPlaceDetailsUpdate(getStatusCode());
		}
	}

	// Process the downloaded data to structure format. 
	private void processResult(String data, String action) {
		Log.v(TAG,"Inside processResult");
		
		if (action.equals("PLACEDETAILS")) {
			Log.v(TAG,"Parsing data for PlaceDetails");
			readJsonFileForPlaceDetails(data);
			if (mPlaceDetails == null) {
				Log.d(TAG,"Place details is null");
				mUpdateListener.onPlaceDetailsUpdate(getStatusCode());
				return;
			}
			
/*			if (mPlaceDetails.getGoogleURL().isEmpty()) {
				Log.d(TAG,"Google Plus URL is empty");
				mUpdateListener.onPlaceDetailsUpdate(getStatusCode());
				return;
			}
			//startService(mPlaceDetails.getGoogleURL(),"GOOGLEPLUS");
*/			if (isWikiData) {
				downloadWikiData();
			}
		} else if (action.equals("WIKIDOWNLOAD")){
			Log.d(TAG,"Wikipage downloaded");
			readWikiData(data);
		}
	}
	
	// pase the wikidata
	private void readWikiData(String data) {
		Log.v(TAG,"readJsonFileForPlaceDetails");
		JSONObject jsonFile;
		JSONObject jsonObj;
		
		try {
			jsonFile= new JSONObject(data);
			if(jsonFile.has("query")) {
				if (jsonFile.getJSONObject("query").has("pages")) {
					JSONObject jsonPageObj = jsonFile.getJSONObject("query").getJSONObject("pages");
					JSONArray jsonArray = jsonPageObj.names();
					if (jsonArray.length()!=0) {
						jsonObj= jsonPageObj.getJSONObject(jsonArray.getString(0));
						mPlaceDetails.setWikiDesc(jsonObj.getString("extract"));
						Log.d(TAG,mPlaceDetails.getWikiDesc());
						if (!mPlaceDetails.getWikiDesc().isEmpty()) {
							if (jsonObj.has("coordinates")) {
								jsonArray = jsonObj.getJSONArray("coordinates");
								Location loc = new Location ("");
								loc.setLatitude(jsonArray.getJSONObject(0).optDouble("lat"));
								loc.setLongitude(jsonArray.getJSONObject(0).optDouble("lon"));
								float dist = loc.distanceTo(mPlaceDetails.getLocation());
								Log.d(TAG,loc.getLatitude()+"--"+loc.getLongitude()+"-Distance-"+dist);
								if (dist < 100) {
									String poiURL = "https://en.wikipedia.org/wiki/"+mPlaceDetails.getName().replace(" ", "_");
									Log.d(TAG,poiURL);
									mPlaceDetails.setWikiURL(poiURL);
								}
							}
						}
					}
				}
		   } 
		} catch (JSONException e) {
			Log.e(TAG,"Error in parsing Json file from wiki",e);
		}
	}
	
	/**
	 * Read the result (json file) and parse it as PlaceSummary object. 
	 * The results will be stored in ArrayList.
	 * @param data
	 * @return
	 */
	protected void readJsonFileForPlaceDetails (String data) {
		Log.v(TAG,"readJsonFileForPlaceDetails");
		JSONObject jsonFile;
		JSONArray jsonArray;
		
		//Define array to store the PlaceSummary
		
		try {
			jsonFile= new JSONObject(data);
			//set the page token for next query & status
			setStatusCode(jsonFile.optString("status"));
			
			if(jsonFile.has("result")) {
				mPlaceDetails = new PlaceDetails();
				JSONObject jsonObj = jsonFile.getJSONObject("result");
				mPlaceDetails.setPlaceID(jsonObj.optString("place_id"));
				mPlaceDetails.setName(jsonObj.optString("name",""));
				mPlaceDetails.setRating(jsonObj.optDouble("rating", 0.0));
				if (jsonObj.has("formatted_address")) {
					mPlaceDetails.setAddress(jsonObj.optString("formatted_address",""));
				} else if (jsonObj.has("vicinity")) {
					mPlaceDetails.setAddress(jsonObj.optString("vicinity",""));
				} 
				if (jsonObj.has("international_phone_number")) {
					mPlaceDetails.setPhone(jsonObj.optString("international_phone_number",""));
				} else if (jsonObj.has("formatted_phone_number")) {
					mPlaceDetails.setPhone(jsonObj.optString("formatted_phone_number",""));
				} 
				if (jsonObj.has("website")) {
					mPlaceDetails.setWeb(jsonObj.optString("website",""));
				}
				if (jsonObj.has("url")) {
					mPlaceDetails.setGoogleURL(jsonObj.optString("url",""));
				}
				if(jsonObj.has("opening_hours")) {
					mPlaceDetails.setOpenNow(jsonObj.getJSONObject("opening_hours").optString("open_now",""));
					if (jsonObj.getJSONObject("opening_hours").has("weekday_text")) {
						//placeDetails.setOpenTime(jsonObj.getJSONObject("opening_hours").optString("weekday_text",""));
						jsonArray = jsonObj.getJSONObject("opening_hours").getJSONArray("weekday_text");
						String str = jsonArray.toString();
						mPlaceDetails.setOpenTime(str.substring(1,str.length()-1));
					}
				} 
				Location loc = new Location ("");
				loc.setLatitude(jsonObj.getJSONObject("geometry").getJSONObject("location").optDouble("lat"));
				loc.setLongitude(jsonObj.getJSONObject("geometry").getJSONObject("location").optDouble("lng"));
				
				//Parsing photo details
				if(jsonObj.has("photos")){
					jsonArray = jsonObj.getJSONArray("photos");
					for (int i =0; i< jsonArray.length(); i++) {
						mPlaceDetails.addPhotoRef(jsonArray.getJSONObject(i).optString("photo_reference"));
					}
					
				}
				mPlaceDetails.setLocation(loc);
			} else {
				Log.d(TAG,"No result exist in json file");
				mPlaceDetails = null;
			}
		} catch (JSONException e) {
			Log.e(TAG,"Error in parsing Json file",e);
			mPlaceDetails = null;
		}
	}
		
	
	// Ref Meenakshi Amman Temple - https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJh2nkYYTFADsRA2co5RxiNPE&key=AIzaSyAaV4KZzUuB1z9ofClSb_2PCd55wpo1olU
	// Ref sample Temple - https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJG6ve4l0RrjsRbNMN7Xi0WGM&key=AIzaSyAaV4KZzUuB1z9ofClSb_2PCd55wpo1olU

}

