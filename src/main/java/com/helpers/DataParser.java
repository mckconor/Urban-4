package com.helpers;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.components.Coordinates;

public class DataParser {

	public static Coordinates locationDataParser(String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
		
			double lat = jsonObj.getDouble("lat");		
			double lng = jsonObj.getDouble("lng");
		
			Coordinates coords = new Coordinates(lat, lng);
			return coords;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String userEmailParser(String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			return jsonObj.getString("email");
		} catch (Exception e) {
			return null;
		}
	}
	
	public static java.util.Date timeParser(String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
		
			double timestamp = jsonObj.getDouble("time");		
			java.util.Date time=new java.util.Date((long)timestamp);
		
			return time;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int locationIdParser (String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			return jsonObj.getInt("id");
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static Date dateParser (String body, String datePosition) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			Date x = new Date(jsonObj.getString(datePosition));
			return x;
		} catch (Exception e) {
			return null;
		}
	}
}
