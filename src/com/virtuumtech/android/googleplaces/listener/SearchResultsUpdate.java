package com.virtuumtech.android.googleplaces.listener;

import java.util.ArrayList;

import com.virtuumtech.android.googleplaces.PlacesList;

public interface SearchResultsUpdate {
	void onSearchResultsUpdate(int statusCode, ArrayList<PlacesList> listPOIs);
}
