/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * FetchAddressService class gets the details of the location using Intent service. 
 * Once address details retrieved this program invokes, LocationUpdateListener.onAddressUpdate(status, List<Address>) callback method.
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class FetchAddressService extends IntentService {
	
	private static final String TAG = "fetchAddressService";
	String errorMessage = "";
	
	private ResultReceiver mResultReceiver;
	private Geocoder geocoder;
	private List<Address> mAddresses;

	private double mLatitude;
	private double mLongitude;
	
	public FetchAddressService () {
		super(TAG);
		mAddresses = new ArrayList<Address>();
	}

	public FetchAddressService(String name) {
		super(name);
		mAddresses = new ArrayList<Address>();
	}

	/** OnHandleIntent is called when StartService(intent) is used
	 *  The Intent will be having all the data stored in the calling method.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG,"onHandleIntent");
		
		// Retrieve the values passed in Intent object
		mResultReceiver = intent.getParcelableExtra(MyAddress.RECEIVER);
		Location location = intent.getParcelableExtra(MyAddress.LOCATION);
		Log.d(TAG,"Location: "+location.toString());
		
		mLatitude = location.getLatitude();
		mLongitude = location.getLongitude();
		
		// Use the geocoder to get the information
		Log.i(TAG, "Intent service is using geocoder");
		int maxResults = intent.getIntExtra(MyAddress.MAXRESULTS, 1);
		getAddressUsingGeocoder(maxResults);

	}
	
	/** Get the address using geocoder from Android
	 *  
	 * @param maxResults 
	 * 				Maximum number of address results to retrive. 
	 */
	private void getAddressUsingGeocoder (int maxResults) {
		Log.v(TAG, "In getAddressUsingGeocoder");

		geocoder = new Geocoder(this, Locale.getDefault());
		
		try {
			mAddresses = geocoder.getFromLocation(mLatitude, mLongitude, maxResults);
		} catch (IllegalArgumentException illException) {
			// Catch the Illegal Longitude or Latitude
			errorMessage = "Latitude:"+mLatitude+", Longitude:"+mLongitude+" values are not correct";
			Log.e(TAG,errorMessage,illException);
			updateResults(MyAddress.ILLEGAL_LOCATION,mAddresses);
		} catch (IOException ioException) {
			// Catch the network issues
			errorMessage = "Issues with accessing network";
			Log.e(TAG,errorMessage,ioException);
			updateResults(MyAddress.NETWORK_ERROR,mAddresses);
		}
		//Check the empty values of address query
		if (mAddresses == null || mAddresses.size() == 0) {
			if (errorMessage.isEmpty()) {
				errorMessage = "No address found for the location "+mLatitude+" "+mLongitude;
				Log.e(TAG,errorMessage);
				updateResults(MyAddress.FAILURE,mAddresses);
			}
		} 
		
		updateResults(MyAddress.SUCCESS,mAddresses);
	}
	
	/** To update the result to the callback method 
	 * 
	 * @param resultCode
	 * 				geocoder result code to determine the success or failure
	 * @param addresses
	 * 				Address list of location.
	 */
	private void updateResults(int resultCode, List<Address> addresses) {
		Log.v(TAG,"updateResults");
		Bundle resultData = new Bundle();
		resultData.putParcelableArrayList(MyAddress.RESULT_DATA, (ArrayList) addresses);
		mResultReceiver.send(resultCode, resultData);
	}
	
}


