/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * Interface definition for Location and Address Updates
 * 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces;

import java.util.List;

import android.location.Address;
import android.location.Location;

public interface LocationUpdateListener {
	void onLocationUpdate(Location location);
	void onLocationDisabled();
	void onGooglePlayError(int errorCode);
	void onGeocoderDisabled();
	void onAddressesUpdate(int status, List<Address> addresses);
}
