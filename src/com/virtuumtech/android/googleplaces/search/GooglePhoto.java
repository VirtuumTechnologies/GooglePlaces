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

import com.virtuumtech.android.googleplaces.GPConstants;
import com.virtuumtech.android.googleplaces.NetworkService;
import com.virtuumtech.android.googleplaces.listener.PhotoUpdate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

public class GooglePhoto extends ResultReceiver implements Parcelable {

	private static final String TAG = "GooglePhoto";
	private static final String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?";

	private String mApiKey;
	private Context mContext;
	private PhotoUpdate mUpdateListener;
	private int statusCode ;
	
	private int height;
	private int width;
	private String ref;
	private Bitmap bitmap;

	
	public GooglePhoto() {
		super(new Handler());
	}

	public GooglePhoto(Context context, String apikey) {
		super(new Handler());
		Log.v(TAG,"Places Constructor");
		mApiKey=apikey;
		mContext=context;
		mUpdateListener = (PhotoUpdate) context;
	}
	
	/**
	 * Request to download the Photo. On completion it will call the call back function.  
	 */
	public void downloadPhoto (String ref, int height, int width) {
		this.ref = ref;
		this.height = height;
		this.width = width;
		
		String url = photoUrl+"maxwidth="+width+"&maxheight="+height+"&key="+mApiKey+"&photoreference="+ref;
		startService(url,"PHOTODOWNLOAD");
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
		intent.putExtra(GPConstants.SERVICE, GPConstants.ACTION_PHOTO_DOWNLOAD);
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
		
		byte[] byteArray = bundle.getByteArray(GPConstants.DATA);
		if (resultCode != RequestStatus.OK) {
			//Check the query is not successful for main request
			Log.e(TAG,"Google Photo Query is not successful: "+RequestStatus.getStatusValue(resultCode));
			setStatusCode(resultCode);
		} else {
			//Convert the byte array to bitmap
			bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		}
		mUpdateListener.onPhotoDownload(getStatusCode());
	}
}