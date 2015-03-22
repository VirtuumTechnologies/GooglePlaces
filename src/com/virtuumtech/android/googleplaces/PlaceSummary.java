/**
 * Copyright 2015 Virtuum Technologies. All Rights Reserved.
 */

/**
 * PlaceSummary has the details of the place returned by GooglePlaces Search API. Implemented Parcelable to facilitate passing it between activities 
 * Since the google places search returns only overview of the places, this will also has only overview of the classes. 
 * Can be used for showing in the lists as part of places list. Use PlaceDetails for detailed information of places. 
 * @author  
 * @version 1.0
 */

package com.virtuumtech.android.googleplaces;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

//Implements Parcelable, so that this class can be passed between processes and activities. 
public class PlaceSummary implements Parcelable {

	private String placeID;
	private String name;
	private String openNow = "Not Known";
	private String address = "";
	private double rating;
	private Location location;
	
	
	public PlaceSummary() {
		super();
	}

	//To read from parcel. Reading in the same order the members are written. 
	public PlaceSummary(Parcel source) {
		super();
		placeID = source.readString();
		name = source.readString();
		openNow = source.readString();
		address = source.readString();
		rating = source.readDouble();
		location = source.readParcelable(Location.class.getClassLoader());
	}

	/** 
	 * Returns PlaceID of the POI
	 * @return
	 */
	public String getPlaceID() {
		return placeID;
	}
	
	/**
	 * Set the place_id of the POI
	 * @param placeID - The google places id
	 */
	public void setPlaceID(String placeID) {
		this.placeID = placeID;
	}

	/**
	 * Returns the Name of the POI
	 * @return
	 */
	public String getName() {
		return name;
	}

	/** 
	 * Set the Name of the POI
	 * @param name of the POI
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/** 
	 * Returns open now status of the POI. 
	 * @return - returns true/false/Not Known based on the info provided by google places
	 */
	public String getOpenNow() {
		return openNow;
	}

	/**
	 * Set the open now status
	 * @param openNow
	 */
	public void setOpenNow(String openNow) {
		this.openNow = openNow;
	}
	
	/**
	 * Returns the address of the POI
	 * @return
	 */
	public String getAddress() {
		return address;
	}

	/** 
	 * Set the address of POI
	 * @param address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Returns the rating of POI
	 * @return double Rating of POI
	 */
	public double getRating() {
		return rating;
	}

	/** 
	 * Set the rating of the POI
	 * @param rating
	 */
	public void setRating(double rating) {
		this.rating = rating;
	}

	/**
	 * Returns the location of the POI
	 * @return Location
	 */
	public Location getLocation() {
		return location;
	}

	/** 
	 * Set the Location of the POI
	 * @param location
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	

	//Overriding the describeContents from Parcelable
	@Override
	public int describeContents() {
		return 0;
	}

	// Writing the members of the class to Parcel, which will be read in the same order
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(placeID);
		dest.writeString(name);
		dest.writeString(openNow);
		dest.writeString(address);
		dest.writeDouble(rating);
		dest.writeParcelable(location, 0);
	}
	
	// Mandatory definition on implementing parcelable
	public static final Parcelable.Creator<PlaceSummary> CREATOR = new Parcelable.Creator<PlaceSummary>() {

		// Creating new PlaceSummary by reading from parcel
		@Override
		public PlaceSummary createFromParcel(Parcel source) {
			return new PlaceSummary(source);
		}

		// Reading from Parcel when Array of PlaceSummary is used
		@Override
		public PlaceSummary[] newArray(int size) {
			return new PlaceSummary[size];
		}

	};
 
	/**
	 * Returns the string of PlaceSummary
	 */
	@Override
	public String toString() {
		return "PlaceDetails [placeID=" + placeID + ", name=" + name
				+ ", openNow=" + openNow + ", address=" + address + ", rating="
				+ rating + ", location=" + location + "]";
	}

}

