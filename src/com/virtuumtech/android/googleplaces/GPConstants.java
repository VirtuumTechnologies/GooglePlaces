package com.virtuumtech.android.googleplaces;

public interface GPConstants {

	public static final String PACKAGE_NAME = "com.virtuumtech.android.googleplaces.GPConstants";

	public static final String LOCATION = PACKAGE_NAME + ".LOCATION";
	public static final String MAXRESULTS = PACKAGE_NAME + ".MAXRESULTS";
	public static final String RESULT_DATA = PACKAGE_NAME + ".RESULT_DATA";

	public static final String SERVICE = PACKAGE_NAME+".SERVICE";
	public static final String DATA = PACKAGE_NAME+".DATA";
	public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
	
	public static final String ACTION_LOCATION_ADDRESS = "LOCATION_ADDRESS";
	public static final String ACTION_URL_REQUEST = "URL_REQUEST";
	public static final String ACTION_PHOTO_DOWNLOAD = "PHOTO_DOWNLOAD";
	
	public static final String URL = PACKAGE_NAME+".URL";
	public static final String TYPE = PACKAGE_NAME+".TYPE";
	
	public static final int SUCCESS = 1;
	public static final int FAILURE = 0;
	public static final int NETWORK_ERROR = -1;
	public static final int ILLEGAL_LOCATION = -2;	

}
