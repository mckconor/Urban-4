package com.helpers;

import org.json.JSONObject;

public class RegistrationHelper {

	public static String parseRole(String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			String role = jsonObj.getString("role");
			return role;
		} catch (Exception e) {
			return null;
		}		
	}
	

	public static String parseEmail(String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			String email = jsonObj.getString("email");
			return email;
		} catch (Exception e) {
			return null;
		}		
	}
}
