/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * Network Service implements running the request as service and responds the results using ResultReceiver. 
 * This supports 
 * 	 - Getting address details using geocoder
 * 	 - Downloading the response for GooglePlaces Web API
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import com.virtuumtech.android.googleplaces.search.RequestStatus;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class NetworkService extends IntentService {
	


	private static final String TAG = "NetworkService";
	
	private ResultReceiver mResultReceiver;

	
	public NetworkService(String name) {
		super(name);
	}
	
	public NetworkService() {
		super(TAG);
	}

	/** OnHandleIntent is called when StartService(intent) is used.
	 *  The Intent should have the details required to do the specified service.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG,"onHandleIntent");
		
		// Get the SERVICE required in NetworkService
		String CALLEDSERVICE = intent.getStringExtra(GPConstants.SERVICE);
		mResultReceiver = intent.getParcelableExtra(GPConstants.RECEIVER);
		Log.i(TAG,"Called Service is "+CALLEDSERVICE);

		switch (CALLEDSERVICE) {
			case "LOCATION_ADDRESS":
				// Use the geocoder to get the information
				Log.i(TAG, "Using geocoder to get the addresses");
				getAddressUsingGeocoder(intent);
				break;
			case "URL_REQUEST":
				// Download the URL to get the data form google places API
				Log.i(TAG,"Downloading URL request");
				downloadURL(intent);
				break;
			case "PHOTO_DOWNLOAD":
				//Download the photo from the given URL
				Log.i(TAG,"Download the Photo from url");
				downloadPhoto(intent);
			default :
				Log.e(TAG,"Invalid Service called in NetworkService: "+GPConstants.SERVICE);
		}
	}

	// To get the Address details of the location using geocoder. 
	private void getAddressUsingGeocoder (Intent intent) {
		Log.v(TAG, "In getAddressUsingGeocoder");

		// Get details specific to Geocoder from Intent object
		int maxResults = intent.getIntExtra(GPConstants.MAXRESULTS, 1);

		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		Bundle resultData = new Bundle();
		String errorMessage = "";
		List<Address> addresses = null;

		//Update the location details. 
		Location location = intent.getParcelableExtra(GPConstants.LOCATION);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		
		// Get location using Geocoder
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, maxResults);
		} catch (IllegalArgumentException illException) {
			// Catch the Illegal Longitude or Latitude
			errorMessage = "Latitude:"+latitude+", Longitude:"+longitude+" values are not correct";
			Log.e(TAG,errorMessage,illException);
			mResultReceiver.send(GPConstants.ILLEGAL_LOCATION,resultData);
			return;
		} catch (IOException ioException) {
			// Catch the network issues
			errorMessage = "Issues with accessing network";
			Log.e(TAG,errorMessage,ioException);
			mResultReceiver.send(GPConstants.NETWORK_ERROR,resultData);
			return;
		}
		
		//Check the empty values of address query
		if (addresses == null) {
			errorMessage = "Address returned null for the location "+latitude+" "+longitude;
			Log.e(TAG,errorMessage);
			mResultReceiver.send(GPConstants.FAILURE,resultData);
			return;
		} else if (addresses.isEmpty()) {
			errorMessage = "No address found for the location "+latitude+" "+longitude;
			Log.e(TAG,errorMessage);
			mResultReceiver.send(GPConstants.FAILURE,resultData);
			return;
		}
		
		resultData.putParcelableArrayList(GPConstants.RESULT_DATA, (ArrayList) addresses);
		mResultReceiver.send(GPConstants.SUCCESS, resultData);
	}
	
	// Downloads the Photo using https connection. 
	// THe downloaded content will be passed to respective callback using ResultReceiver.
	private void downloadPhoto (Intent intent) {
		String urlStr = intent.getStringExtra(GPConstants.URL);
		String actionType = intent.getStringExtra(GPConstants.TYPE);
		Bundle resultData = new Bundle();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int resultCode;

		
		try {
			// Create URL, establish connection and create buffered stream to read
			Log.d(TAG,urlStr);
			URL url = new URL(urlStr);
			HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
			
			//Read the data using stream & write it to byte array
			InputStream input = new BufferedInputStream(httpsConnection.getInputStream());
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = input.read(buffer)) != -1) {
				output.write(buffer);
			}
			
			input.close();
			resultCode = RequestStatus.OK;
		} catch (MalformedURLException e) {
			Log.e(TAG,"Invalid URL passed, MalformedURLException",e);
			resultCode = RequestStatus.INVALID_REQUEST;
		} catch (IOException e) {
			Log.e(TAG,"Issues in downloading content, IOException",e);
			resultCode = RequestStatus.INVALID_REQUEST;
		} catch (Exception e) {
			Log.e(TAG,"Exception on downloading URL",e);
			resultCode = RequestStatus.ERROR;
		}
		resultData.putByteArray(GPConstants.DATA, output.toByteArray());
		resultData.putCharSequence(GPConstants.TYPE, actionType);
		mResultReceiver.send(resultCode,resultData);
	}

	
	// Downloads the URL using https connection. 
	// THe downloaded content will be passed to respective callback using ResultReceiver.
	private void downloadURL (Intent intent) {
		String responseData = "";
		String urlStr = intent.getStringExtra(GPConstants.URL);
		String actionType = intent.getStringExtra(GPConstants.TYPE);
		Bundle resultData = new Bundle();
		int resultCode;
		
		try {
			// Create URL, establish connection and create buffered stream to read
			Log.d(TAG,urlStr);
			URL url = new URL(urlStr);
			HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
			
			//Read the data using stream
			InputStream in = new BufferedInputStream(httpsConnection.getInputStream());
			responseData = readStream (in);
			if(responseData == null) {
				Log.d(TAG,"There is no response for the URL request");
				responseData = "";
				resultCode = RequestStatus.ERROR;
			}
			//Log.d(TAG,responseData);
			resultCode = RequestStatus.OK;
		} catch (MalformedURLException e) {
			Log.e(TAG,"Invalid URL passed, MalformedURLException",e);
			resultCode = RequestStatus.INVALID_REQUEST;
		} catch (IOException e) {
			Log.e(TAG,"Issues in downloading content, IOException",e);
			resultCode = RequestStatus.INVALID_REQUEST;
		} catch (Exception e) {
			Log.e(TAG,"Exception on downloading URL",e);
			resultCode = RequestStatus.ERROR;
		}
		resultData.putCharSequence(GPConstants.DATA, responseData);
		resultData.putCharSequence(GPConstants.TYPE, actionType);
		mResultReceiver.send(resultCode,resultData);
	}

	// Reading the file/data from the InputStream in buffer method
	// Returns the downloaded file/content
	private String readStream(InputStream in) {
		Log.i(TAG,"inside readStream");
		
		// Define the Buffer classes to read the data from input stream
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer data = new StringBuffer("");
		String line = "";
		
		// Read the data
		try {
			while ((line = reader.readLine()) != null) {
				data.append(line);
			}
		} catch (IOException ioException) {
			Log.e(TAG,"IOException on reading from InputStream: ",ioException);
			data.delete(0,data.length());
		}
		try {
			reader.close();
		} catch (IOException ioException) {
			Log.e(TAG,"IOException on closing the InputStream: ",ioException);
		}
		return data.toString();
	}			
}
