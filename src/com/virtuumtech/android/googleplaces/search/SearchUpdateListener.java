 /**
  * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * Interface definition for getting updates of Search Query & PlacesDetails
 * 
 * @author  
 * @version 1.0
 */
 
package com.virtuumtech.android.googleplaces.search;

import java.util.ArrayList;

import com.virtuumtech.android.googleplaces.PlaceSummary;

public interface SearchUpdateListener {
	
	void onSearchResultsUpdate(int statusCode, ArrayList<PlaceSummary> listPOIs);
	
}
