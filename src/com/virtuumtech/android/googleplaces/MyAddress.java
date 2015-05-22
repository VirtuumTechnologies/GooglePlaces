/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * MyAddress class implements the methods to get the Address details by running as service.  
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.virtuumtech.android.googleplaces.listener.LocationUpdateListener;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class MyAddress implements ConnectionCallbacks,
		OnConnectionFailedListener {
	private static final String TAG = "MyAddress";
	private Location mLocation;
	private List<Address> mAddressList;
	private Context mContext;
	private GoogleApiClient mGoogleApiClient;
	private LocationUpdateListener addressListener;
	private Address mAddress;
	private int maxResults;
	

	public MyAddress(Context context) {
		mContext = context;
	}

	//Check the locations are equal. 
	private boolean isLocationEqual(Location location) {
		Log.v(TAG, "Inside isLocationEqual");
		if (mLocation == null) {
			Log.i(TAG, "Cache Location is null");
			return false;
		}

		if ((mLocation.getLatitude() == location.getLatitude())
				&& (mLocation.getLongitude() == location.getLongitude())) {
			Log.i(TAG, "Cached and passed location are same");
			return true;
		}

		return false;
	}

	/** getAddressDetails retrieves the address with maxResult as 1
	 * 
	 * @param adressListener The object which implements the LocationUpdateListener
	 * 
	 * @param location Location for which address needs to be retrieved
	 * 
	 * @param maxResults Maximum addresses can be retrieved for the given location. Recommended to keep it minimal. 
	 */
	public void getAddressDetails(LocationUpdateListener updateListener,
			Location location, int maxResults) {
		Log.v(TAG, "Inside getAddress");

		addressListener = (LocationUpdateListener) updateListener;

		// Return the cached value if query is for same location
		if (location == null) {
			Log.i(TAG, "Location is null");
			addressListener.onAddressesUpdate(GPConstants.ILLEGAL_LOCATION, mAddressList);
			return;
		}

		//If Location is not changed, return the cached address
		if (isLocationEqual(location) && mAddressList != null) {
			Log.i(TAG, "Location & Cached Location are same");
			addressListener.onAddressesUpdate(GPConstants.SUCCESS, mAddressList);
			return;
		}

		mLocation = location;
		this.maxResults = maxResults;

		if (mAddressList != null) {
			mAddressList.clear();
		}

		// Check Geocoder present in the OS
		Log.i(TAG, "Find address using Geocoder");
		if (!Geocoder.isPresent()) {
			Log.e(TAG, "Geocode does notexists");
			addressListener.onGeocoderDisabled();
		}

		// Using Geocoder to get address & defining GoogleApiClient to
		// connect the Google Play Services
		mGoogleApiClient = new GoogleApiClient.Builder(mContext)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
		mGoogleApiClient.connect();
	}

	/** getAddressDetails retrieves the address with maxResult as 1
	 * 
	 * @param adressListener The object which implements the LocationUpdateListener
	 * 
	 * @param location Location for which address needs to be retrieved
	 */
	public void getAddressDetails(LocationUpdateListener adressListener,
			Location location) {
		Log.v(TAG, "Inside getAddress");

		int maxResults = 1;
		getAddressDetails(adressListener, location, maxResults);

	}

	//Implementation of ConnectionCallback Inferface abstract method 
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
		//Return the error to the calling object
		addressListener.onGooglePlayError(result.getErrorCode());
	}

	//Implementation of ConnectionCallback Inferface abstract method 
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.v(TAG, "onConnected");
		//Start service to get addresses when GooglePlay connection established 
		startIntentService();
	}

	//Implementation of ConnectionCallback Inferface abstract method 
	@Override
	public void onConnectionSuspended(int cause) {
		Log.v(TAG, "onConnectionSuspended");
		//reconnect on suspended
		mGoogleApiClient.connect();
	}

	/** Get status for the error codes
	 * 
	 * @param status Status message received during address lookup
	 * 
	 * @return String returns the error message associated with status message
	 */
	public String getStatusString (int status) {
		Log.v(TAG,"getStatusString");
		String statusMsg = "Invalid Status Code";
		
		if (status == GPConstants.SUCCESS) {
			statusMsg = "Address details retrieved";
		} else if (status == GPConstants.FAILURE) {
			statusMsg = "No address found";
		} else if (status == GPConstants.NETWORK_ERROR) {
			statusMsg = "The network service is not available";
		} else if (status == GPConstants.ILLEGAL_LOCATION) {
			statusMsg = "The location is invalid";
		}
		
		return statusMsg;
	}
	
	// Runs the geocoder in service mode to get the addresses of the given location
	private void startIntentService() {
		Log.v(TAG, "startIntentService");
		
		//Implementation of ResultReceiver to get the result using callback
		ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
			public void onReceiveResult(int resultCode, Bundle resultData) {
				Log.v(TAG, "onReciveResult");
				if (resultCode == GPConstants.SUCCESS) {
					Log.i(TAG,"Address Request is successful");
					mAddressList = resultData
							.getParcelableArrayList(GPConstants.RESULT_DATA);
					if (mAddressList.isEmpty() == true) {
						mAddress = null;
						resultCode = GPConstants.FAILURE;
					} else {
						// mAddressList = (List) mAddressList;
						mAddress = mAddressList.get(0);
					}
				} else {
					Log.e(TAG,"Address Request is not successful");
					mAddress = null;
					resultCode = GPConstants.FAILURE;
				}
				addressListener.onAddressesUpdate(resultCode, mAddressList);
			}
		};

		//Passing the values to service class NetworkService to get the address using intent
		Intent intent = new Intent(mContext, NetworkService.class);
		intent.putExtra(GPConstants.SERVICE, GPConstants.ACTION_LOCATION_ADDRESS);
		intent.putExtra(GPConstants.LOCATION, mLocation);
		intent.putExtra(GPConstants.RECEIVER, resultReceiver);
		intent.putExtra(GPConstants.MAXRESULTS, maxResults);
		mContext.startService(intent);
	}
	
	/** Returns the first address for the given location
	 * 
	 * @return Address returns the address of the given location
	 * 
	 */
	public Address getAddress() {
		return mAddress;
	}
	
	/** To get the street name after address details retrieved 
	 * 
	 * @return String Returns the Street name
	 */
	
	public String getStreetName() {
		String addressStr = "";
		if (mAddress != null) {
			addressStr = mAddress.getAddressLine(0);
		}
		return addressStr;
	}
	
	/** To get the city name after address details retrieved 
	 * 
	 * @return String Returns the city name
	 */
	public String getCityName() {
		String addressStr = "";
		if (mAddress != null) {
			addressStr = mAddress.getLocality()+", "+mAddress.getAdminArea();
		}
		return addressStr;		
	}
	
	/** To get the Location Name 
	 * 
	 * @return String Returns the city name
	 */
	public String getLocationName() {
		String addressStr = "";
		if (mAddress != null) {
			addressStr = mAddress.getAddressLine(0)+", "+ mAddress.getLocality()+", "+ mAddress.getAdminArea();
		}
		return addressStr;		
	}
}
