package com.virtuumtech.android.googleplaces;

import java.util.List;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

//Implements Parcelable, so that this class can be passed between processes and activities. 
public class PlaceDetails implements Parcelable {
	
	private String placeID = "";
	private String name    = "";
	private String address = "";
	private String phone   = "";
	private String website = "";
	private String googleURL = "";
	private String wikiURL = "";
	private String wikiDesc = "";
	private double rating;
	private String openNow = "";
	private String openTime = "";
	private Location location;
	
	private List<String> photoRef;

	public PlaceDetails () {
		super();
	}

	//To read from parcel. Reading in the same order the members are written. 
	public PlaceDetails(Parcel source) {
		super();
		placeID = source.readString();
		name = source.readString();
		address = source.readString();
		phone = source.readString();
		website = source.readString();
		googleURL = source.readString();
		wikiURL = source.readString();
		wikiDesc = source.readString();
		rating = source.readDouble();
		openNow = source.readString();
		openTime = source.readString();
		location = source.readParcelable(Location.class.getClassLoader());
		source.readStringList(photoRef);
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
	 * @return - returns true/false based on the info provided by google places
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
	 * Returns open Timing status of the POI. 
	 * @return - returns open timing based on the info provided by google places
	 */
	public String getOpenTime() {
		return openTime;
	}

	/**
	 * Set the open Timing  status
	 * @param openTime
	 */
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
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
	 * Returns the phone number of the POI
	 * @return
	 */
	public String getPhone() {
		return phone;
	}

	/** 
	 * Set the phone number of POI
	 * @param phone
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}	

	/** 
	 * Set the web url of POI
	 * @param weburl
	 */
	public void setWeb(String weburl) {
		this.website = weburl;
	}

	/**
	 * Returns the google plus url of the POI
	 * @return
	 */
	public String getGoogleURL() {
		return googleURL;
	}

	/** 
	 * Set the google plus url of POI
	 * @param URL
	 */
	public void setGoogleURL(String url) {
		this.googleURL = url;
	}

	/**
	 * Returns the web url of the POI
	 * @return
	 */
	public String getWeb() {
		return website;
	}

	/** 
	 * Set the wiki page url of POI
	 * @param URL
	 */
	public void setWikiURL(String url) {
		this.wikiURL = url;
	}

	/**
	 * Returns the wiki url of the POI
	 * @return
	 */
	public String getWikiURL() {
		return wikiURL;
	}

	/** 
	 * Set the wiki description of POI
	 * @param description
	 */
	public void setWikiDesc(String desc) {
		wikiDesc = desc;
	}

	/**
	 * Returns the wiki Desc of the POI
	 * @return
	 */
	public String getWikiDesc() {
		return wikiDesc;
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
	 * Returns the count of photos in the Places Details POI
	 * @return
	 */
	public int getPhotosCount () {
		return photoRef.size();
	}
	
	/**
	 * Returns the reference of the photo of given index
	 * @param index - Index to get the reference 
	 * @return
	 */
	public String getPhotoRef (int index) {
		return photoRef.get(index);
	}
	
	/**
	 * Add the Photo Refer to the POI
	 * @param ref
	 */
	public void addPhotoRef (String ref) {
		photoRef.add(ref);
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
		dest.writeString(address);
		dest.writeString(phone);
		dest.writeString(website);
		dest.writeString(googleURL);
		dest.writeString(wikiURL);
		dest.writeString(wikiDesc);
		dest.writeDouble(rating);
		dest.writeString(openNow);
		dest.writeString(openTime);
		dest.writeParcelable(location, 0);
		dest.writeStringList(photoRef);
	}

	// Mandatory definition on implementing parcelable
	public static final Parcelable.Creator<PlaceDetails> CREATOR = new Parcelable.Creator<PlaceDetails>() {

		// Creating new PlaceDetails by reading from parcel
		@Override
		public PlaceDetails createFromParcel(Parcel source) {
			return new PlaceDetails(source);
		}

		// Reading from Parcel when Array of PlaceSummary is used
		@Override
		public PlaceDetails[] newArray(int size) {
			return new PlaceDetails[size];
		}

	};

	/**
	 * Returns the string of PlaceSummary
	 */
	@Override
	public String toString() {
		return "PlaceDetails [placeID=" + placeID + ", name=" + name
				+ ", address=" + address + ", phone="+ phone + ", website="+ website + ", googleURL="+ googleURL
				+ ", wikiurl =" + wikiURL + ", wikiDesc=" + wikiDesc + ", rating="
				+ rating + ", openNow=" + openNow + ", openTime=" + openTime + ", location=" + location + "]";
	}
}

