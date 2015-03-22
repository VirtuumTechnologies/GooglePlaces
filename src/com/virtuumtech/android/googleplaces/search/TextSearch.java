/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * TextSearch implements the TextSearch of Google Places web API.
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces.search;

import android.content.Context;
import android.util.Log;

public class TextSearch extends SearchQuery {

	private static final String TAG = "TextSearch";
	private static final String queryUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?";

	/**
	 * Constructor for NearBySearch
	 * @param context Context of the Activity creating obj.
	 * @param apikey GoogleAPI Key to do the query
	 */
	public TextSearch(Context context, String apikey) {
		super(context, apikey, queryUrl);
		Log.v(TAG,"TextSearch Constructor");
		addParameter("key", apikey);
	}
	
	/**
	 * Set the query string for the text search
	 * @param name
	 */
	public void setQuery (String name) {
		addParameter("query", name);
	}

}
