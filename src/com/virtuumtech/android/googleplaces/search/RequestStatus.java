/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * RequestStatus provides the status code & string for the GooglePlaces search results. 
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces.search;

public class RequestStatus {

	public static final int OK = 0;
	public static final int ZERO_RESULTS = 1;
	public static final int OVER_QUERY_LIMIT = 2;
	public static final int REQUEST_DENIED = 3;
	public static final int INVALID_REQUEST = 4;
	public static final int UNKNOWN_ERROR  = 5;
	public static final int NOT_FOUND = 6;
	public static final int ERROR = 99;
	
	
	private static final String STATUS_OK = "OK";
	private static final String STATUS_ZERO_RESULTS = "ZERO_RESULTS";
	private static final String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
	private static final String STATUS_REQUEST_DENIED = "REQUEST_DENIED";
	private static final String STATUS_INVALID_REQUEST = "INVALID_REQUEST";
	private static final String STATUS_UNKNOWN_ERROR = "UNKNOWN_ERROR";
	private static final String STATUS_NOT_FOUND = "NOT_FOUND";
	private static final String STATUS_ERROR = "ERROR";

	/**
	 * Returns the String i.e. associated GOOGLE Places API Status 
	 * @param statusValue
	 * @return
	 */
	public static int getStatusCode(String statusValue) {
		if (statusValue.equals(STATUS_OK)) {
			return OK;
		} else if (statusValue.equals(STATUS_ZERO_RESULTS)) {
			return ZERO_RESULTS;
		} else if (statusValue.equals(STATUS_OVER_QUERY_LIMIT)) {
			return OVER_QUERY_LIMIT;
		} else if (statusValue.equals(STATUS_REQUEST_DENIED)) {
			return REQUEST_DENIED;
		} else if (statusValue.equals(STATUS_INVALID_REQUEST)) {
			return INVALID_REQUEST;
		} else if (statusValue.equals(STATUS_UNKNOWN_ERROR)) {
			return UNKNOWN_ERROR;
		} else if (statusValue.equals(STATUS_NOT_FOUND)) {
			return NOT_FOUND;
		} else {
			return ERROR;
		}
	}
	
	/**
	 * Returns the code associated with Google places API status code
	 * @param statusCode
	 * @return
	 */
	public static String getStatusValue (int statusCode) {
		if (statusCode == OK ) {
			return STATUS_OK;
		} else if (statusCode == ZERO_RESULTS) {
			return STATUS_ZERO_RESULTS;
		} else if (statusCode == OVER_QUERY_LIMIT) {
			return STATUS_OVER_QUERY_LIMIT;
		} else if (statusCode == REQUEST_DENIED) {
			return STATUS_REQUEST_DENIED;
		} else if (statusCode == INVALID_REQUEST) {
			return STATUS_INVALID_REQUEST;
		} else if (statusCode == UNKNOWN_ERROR) {
			return STATUS_UNKNOWN_ERROR;
		} else if (statusCode == NOT_FOUND) {
			return STATUS_NOT_FOUND;
		} else if (statusCode == ERROR) {
			return STATUS_ERROR;
		}
		return STATUS_ERROR;
	}
	
	
}
