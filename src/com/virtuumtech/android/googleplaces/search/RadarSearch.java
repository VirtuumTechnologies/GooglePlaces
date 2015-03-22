/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * RadarSearch implements the RadarSearch of Google Places web API.
 * 
 * @author  
 * @version 1.0
 */
package com.virtuumtech.android.googleplaces.search;

import android.content.Context;
import android.util.Log;


public class RadarSearch extends SearchQuery {

	private static final String TAG = "RadarSearch";
	private static final String queryUrl = "https://maps.googleapis.com/maps/api/place/radarsearch/json?";

	/**
	 * Constructor for RadarSearch
	 * @param context Context of the Activity creating obj.
	 * @param apikey GoogleAPI Key to do the query
	 */
	public RadarSearch(Context context, String apikey) {
		super(context, apikey, queryUrl);
		Log.v(TAG,"RadarSearch Constructor");
		addParameter("key", apikey);
	}
	
	/**
	 * Set name parameter for the radar search
	 * @param name
	 */
	public void setName (String name) {
		addParameter("name", name);
	}

	/**
	 * Remove the name parameter from radar search
	 * @return
	 */
	public boolean removeName() {
		return removeParameter("name");
	}
	
	/**
	 * set keyword for the radar search
	 * @param keyword
	 */
	public void setKeyword (String keyword) {
		addParameter("keyword", keyword);
	}

	/**
	 * Remove keyword for the radar search
	 * @return
	 */
	public boolean removeKeyword() {
		return removeParameter("keyword");
	}
}
