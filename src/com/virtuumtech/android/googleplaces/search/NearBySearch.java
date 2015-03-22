/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * NearBySearch implements the near by search of Google Places web API.
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces.search;

import android.content.Context;
import android.util.Log;

public class NearBySearch extends SearchQuery {

	private static final String TAG = "NearBySearch";
	private static final String queryUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

	/**
	 * Constructor for NearBySearch
	 * @param context Context of the Activity creating obj.
	 * @param apikey GoogleAPI Key to do the query
	 */
	public NearBySearch(Context context, String apikey) {
		super(context, apikey, queryUrl);
		Log.v(TAG,"NearBySearch Constructor");
		addParameter("key", apikey);
	}
	
	/**
	 * Set the name parameter for the nearbysearch 
	 * @param name
	 */
	public void setName (String name) {
		addParameter("name", name);
	}

	/**
	 * Remove the name parameter in the nearbysearch
	 * @return
	 */
	public boolean removeName() {
		return removeParameter("name");
	}
	
	/** 
	 * Set the rankby in the nearby search. When this is set, Radius setting will be removed
	 * @param rankby
	 */
	public void setRankby (String rankby) {
		addParameter("rankby", rankby);
		removeRadius();
	}

	/**
	 * Remove the rankby parameter from the nearby search query
	 * @return
	 */
	public boolean removeRankby() {
		return removeParameter("rankby");
	}
	
	/**
	 * Sett the keyword for the nearby search query
	 * @param keyword
	 */
	public void setKeyword (String keyword) {
		addParameter("keyword", keyword);
	}

	/**
	 * Remove the keyword from nearby search query
	 * @return
	 */
	public boolean removeKeyword() {
		return removeParameter("keyword");
	}
	
	/**
	 * Set the radious for the nearbysearch query. When this is set, rankby will be removed
	 */
	public void setRadius(String radius) {
		addParameter("radius",radius);
		removeRankby();
	}
	

}
