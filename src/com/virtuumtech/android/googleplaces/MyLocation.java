/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * MyLocation implements an callback program to get current location details using google play services. 
 * Once location is identified, this program invokes LocationUpdateListener.onLocationUpdate(Location location) callback method.
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MyLocation implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	
	// Implement to call onChangedLocation on time out  
	class CloseListener implements Runnable {
		@Override
		public void run() {
			Log.i(TAG,"Inside Runnable Run");
			mLocation = null;
			onLocationChanged(mLocation);			
		}
	};

	private Handler mHandler;
	private CloseListener mCloseListener;
	private static final String TAG = "MyLocation";
	private int lastUpdatedTime;
	
	private long FASTEST_UPDATE_INTERVAL = 2000; // Update will be received in 2000 ms
	private int NUMBER_OF_UPDATES = 1; 
	private long locationUpdateTimeout;

	private Context mContext;
	private Location mLocation;
	private LocationRequest mLocationReq;
	private GoogleApiClient mGoogleApiClient;
	private LocationUpdateListener mLocationListener;
	private LocationManager mLocationManager;
	
	/**
	 * Construct a new MyLocation 
	 * 
	 * By default looks for new location if last known location is before 60 secs
	 * 
	 * @param activity 
	 *            Object of the calling Activity 
	 */
	public MyLocation (Context context) {
		mContext = context;
		lastUpdatedTime = 0; 
		locationUpdateTimeout = 150 * 1000;

		mHandler = new Handler();
		mCloseListener = new CloseListener();
		mLocationReq = new LocationRequest();
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

		// Defining GoogleApiClient to connect the Google Play Services
		mGoogleApiClient = new GoogleApiClient.Builder(mContext)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}
	
	/**
	 * Construct a new MyLocation 
	 * 
	 * @param activity 
	 *            Object of the calling Activity 
	 * @param lastUpdatedTime
	 *            Expiry time in millisecs to use the cached last location.
	 */	
	public MyLocation (Context context, int lastKnownExpTime) {
		this(context);
		this.lastUpdatedTime = lastKnownExpTime; 
	}
	
	// Set the values of LocationRequest used for location updates 
	private void setLocationRequestValues() {
		mLocationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationReq.setNumUpdates(NUMBER_OF_UPDATES);
		mLocationReq.setFastestInterval(FASTEST_UPDATE_INTERVAL);
		mLocationReq.setExpirationDuration(locationUpdateTimeout); 
		mLocationReq.setInterval(locationUpdateTimeout);
	}

	/**
	 * Returns the Location 
	 *            
	 * @return Location this returns the location
	 */	
	public Location getLocation() {
		return mLocation;
	}

	/**
	 * Set the Location manually 
	 * 
	 * @param location 
	 *            Pass the location details to set manually 
	 */	
	public void setLocation(Location location) {
		this.mLocation = location;
	}

	/**
	 * Set the Location update timeout while no location is identified 
	 * 
	 * @param timeout 
	 *            time out to stop looking for the location
	 */	
	public void setLocationUpdateTimeout (long timeout) {
		locationUpdateTimeout = timeout;
	}
	
	// This is call back method when Google Play connection is established - Implementation of interface ConnectionCallbacks
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.v(TAG,"In onConnected");
		boolean isUpdateRequired = false;
		// Check the last known location and the time when it is cached. 
		mLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLocation != null) {
			Log.i(TAG,"Last known location is available");
			if (lastUpdatedTime != 0) {
				long currTime = System.currentTimeMillis();
				long locTime  = mLocation.getTime();
				long timeDiff = currTime - locTime;
				Log.i(TAG,"currTime:"+currTime+"  locTime:"+locTime+" Time Diff:"+timeDiff+" LastUpdatedTime: "+lastUpdatedTime);

				// Go for new location lookup if the cached time is higher than configured
				if (lastUpdatedTime <= timeDiff) {
					Log.i(TAG,"Last updated time is higher than configured");
					isUpdateRequired = true;
				}
			}
			
			// Send the Location update and disconnect the Google Play
			if (!isUpdateRequired) {
				Log.i(TAG,"Using last known location "+mLocation.toString());
				if (mGoogleApiClient.isConnected()) {
					mGoogleApiClient.disconnect();
				}
				mLocationListener.onLocationUpdate(mLocation);
				return;
			}
		}
		
		// Go for new location lookup
		Log.i(TAG,"Do new location update");
		mLocation = null;
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationReq, this);
		mHandler.postDelayed(mCloseListener, locationUpdateTimeout);
	}

	//Callback method when Google Play connection is suspended.  - Implementation of interface ConnectionCallbacks
	@Override
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();
	}
	
	// Procedure to handle when Google Play connection failed  - Implementation of interface OnConnectionFailedListener
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
		mLocationListener.onGooglePlayError(result.getErrorCode());		
	}

	//Callback method when location is changed - Implementation of interface LocationListener
	@Override
	public void onLocationChanged(Location location) {
		Log.v(TAG,"In onLocationChanged");

		mLocation = location;
		// Stop the timer thread if the location received
		if (mHandler != null) {
			mHandler.removeCallbacks(mCloseListener);
		}
		
		// Stop the location updates and disconnect the goolge play services
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
		mLocationListener.onLocationUpdate(location);
	}

	/**
	 * To get the current location based on the GPS or NETWORK PROVIDER. 
	 * 
	 * @param LocationUpdateListener  
	 *            The class where callback procedure is implemented. 
	 *            
	 * @return Returns error details (-1 failure) 
	 */	
	public int getLocationUpdate(LocationUpdateListener locationListener) {
		Log.v(TAG,"In getLocationUpdate");
		mLocationListener = (LocationUpdateListener) locationListener;
		
		// Check the Location services enabled.
	    if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
	    	Log.e(TAG,"Location service Disabled");
	    	mLocationListener.onLocationDisabled();
	    	return -1;
	    }
	    
	    // Check Google Play installed on the device
	    int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
		if ( errorCode != ConnectionResult.SUCCESS) {
	    	Log.e(TAG,"Google Play availability has error");
			mLocationListener.onGooglePlayError(errorCode);
		}

	    Log.i(TAG,"Using Google Play services to get location");
		setLocationRequestValues();
		mGoogleApiClient.connect();
		return 0;
	}
	
}

