package com.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.components.Coordinates;
import com.entities.AqiObject;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.repositories.UserLocationRepository;
import com.repositories.UserRepository;

public class DataCollectionHelper {

	@Autowired
	private UserRepository userDao;
	@Autowired
	private UserLocationRepository userLocationDao;

	public void DuplicateAqiCleaner(List<AqiObject> aqiRecords) {
		aqiRecords = new ArrayList<>(new HashSet<>(aqiRecords));
		System.out.println(aqiRecords.size());
	}
	
private static String apiToken = "811c428d0c51cd20a304a455ecd9dcc376fadfb9";
	
	public static void GetData () throws IOException {
		try {
			URL url = new URL("http://api.waqi.info/feed/here/?token=" + apiToken);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			
			String x = "";
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
			
			x = content.toString();
	
			PostToDatabase(x);
		} catch (Exception ex) {
			
		}
	}

	public static AqiObject GetDataLocationBased (double lat, double lng) throws IOException {
		try {
			URL url = new URL("https://api.waqi.info/feed/geo:" + lat +";" + lng + "/?token=" + apiToken);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			
			String x = "";
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
			
			x = content.toString();
	
			return PostToDatabase(x);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static AqiObject PostToDatabase(String body) {
		String location = locationParser(body);
		
		int pm25 = particleDensityParser(body, "pm25");
		int pm10 = particleDensityParser(body, "pm10");
		int o3 = particleDensityParser(body, "o3");
		int no2 = particleDensityParser(body, "no2");
		int so2 = particleDensityParser(body, "so2");
		
		String dominantParticle = dominantParticleParser(body);
		
		Date timeUpdated = timeUpdatedParser(body);
		
		int aqi = aqiParser(body);
		
		String status = statusParser(body);
		
		Coordinates geoCoords = coordinateParser(body);
		
		
		Datastore datastore;
		KeyFactory keyFactory;
		datastore = DatastoreOptions.getDefaultInstance().getService();
		keyFactory = datastore.newKeyFactory().setKind("aqi");
		
		IncompleteKey key = keyFactory.newKey();
		FullEntity<IncompleteKey> incAqiEntity = Entity.newBuilder(key)
				.set("status", status)
				.set("aqi", aqi)
				
				.set("location", location)
				.set("lat", geoCoords.latitude)
				.set("lng", geoCoords.longitude)
				
				.set("dominantParticle", dominantParticle)
				.set("pm25", pm25)
				.set("pm10", pm10)
				.set("o3", o3)
				.set("no2", no2)
				.set("so2", so2)
				
				.set("time", timeUpdated.getTime())

				.build();
		Entity aqiEntity = datastore.add(incAqiEntity);
		
		System.out.println(body);
		
		AqiObject aqiObject = new AqiObject();
		aqiObject.setAqi(aqi);
		aqiObject.setDatastoreId(aqiEntity.getKey().getId());
		aqiObject.setLat(geoCoords.latitude);
		aqiObject.setLng(geoCoords.longitude);
		aqiObject.setLocation(location);
		aqiObject.setTime(new Date(timeUpdated.getTime()));
		
		return aqiObject;
	}
	
	public static String locationParser (String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			JSONObject dataObj = jsonObj.getJSONObject("data");
			JSONObject cityObj = dataObj.getJSONObject("city");
			return cityObj.getString("name");
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Coordinates coordinateParser(String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			JSONObject dataObj = jsonObj.getJSONObject("data");
			JSONObject cityObj = dataObj.getJSONObject("city");

			JSONArray geoArray = cityObj.getJSONArray("geo");
			double lat = geoArray.getDouble(1);
			double lng = geoArray.getDouble(0);
			
			return new Coordinates(lat, lng);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int particleDensityParser (String body, String name) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			JSONObject dataObj = jsonObj.getJSONObject("data");
			JSONObject particleObj = dataObj.getJSONObject("iaqi");
			JSONObject namedParticleObj = particleObj.getJSONObject(name);
			
			return namedParticleObj.getInt("v");
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static String dominantParticleParser (String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			JSONObject dataObj = jsonObj.getJSONObject("data");
			return dataObj.getString("dominentpol");
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Date timeUpdatedParser (String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			JSONObject dataObj = jsonObj.getJSONObject("data");
			JSONObject timeObj = dataObj.getJSONObject("time");
			
			Date date = new Date();
			SimpleDateFormat formatter1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return formatter1.parse(timeObj.getString("s"));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int aqiParser (String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			JSONObject dataObj = jsonObj.getJSONObject("data");
			return dataObj.getInt("aqi");
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static String statusParser (String body) {
		try {
			JSONObject jsonObj = new JSONObject(body);
			return jsonObj.getString("status");
		} catch (Exception e) {
			return null;
		}
	}
	
}
