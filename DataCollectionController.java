package com.application;

import org.apache.tomcat.util.http.*;
import org.springframework.ui.Model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.components.Coordinates;
import com.entities.UserLocation;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.helpers.DataParser;
import com.repositories.UserLocationRepository;
import com.repositories.UserRepository;

@Controller
public class DataCollectionController {

	@Autowired
	private UserRepository userDao;
	@Autowired
	private UserLocationRepository userLocationDao;
	
	@RequestMapping("/addLocation")
	public void AddLocation(HttpEntity<String> httpEntity, HttpServletRequest request, HttpServletResponse response) {
		Coordinates userCoords = DataParser.locationDataParser(httpEntity.getBody());
		String userEmail = DataParser.userEmailParser(httpEntity.getBody());
		Date timestamp = DataParser.timeParser(httpEntity.getBody());
		int recordingId = DataParser.locationIdParser(httpEntity.getBody());
		
		UserLocation x = new UserLocation();
		x.setEmail(userEmail);
		x.setRecordingId(recordingId);
		x.setUserId(userDao.findByEmail(userEmail).getId());
		x.setLat(userCoords.latitude);
		x.setLng(userCoords.longitude);
		x.setTimeAt(timestamp);
		
		//Already exist? ignore
		if(userLocationDao.findByTimeAt(timestamp) == null) {
			System.out.println(x.getEmail() + ": ("+ x.getLat() + ", "+ x.getLng() +") : " + x.getTimeAt().toString());
			userLocationDao.save(x);
		}

		//Google Datastore
		ToGoogleDataStore(x);
	}
	
	void ToGoogleDataStore(UserLocation x) {
		
		//Google Datastore
		Datastore datastore;
		KeyFactory keyFactory;
		datastore = DatastoreOptions.getDefaultInstance().getService();
		keyFactory = datastore.newKeyFactory().setKind("userlocation");
		
		IncompleteKey key = keyFactory.newKey();
		FullEntity<IncompleteKey> incUserLocEntity = Entity.newBuilder(key)
				.set("email", x.getEmail())
				.set("userId", x.getUserId())
				.set("lat", x.getLat())
				.set("lng", x.getLng())
				.set("time", x.getTimeAt().toString())
				.build();
		Entity userLocEntity = datastore.add(incUserLocEntity);
		
	}
	
	@RequestMapping("/sendHistoricalData")
	public void SendHistoricalData() {
		//Send historical data to Google Datastore

		Datastore datastore;
		KeyFactory keyFactory;
		datastore = DatastoreOptions.getDefaultInstance().getService();
		keyFactory = datastore.newKeyFactory().setKind("userlocation");
		
			for(UserLocation x : userLocationDao.findAll()) {
			
				IncompleteKey key = keyFactory.newKey();
				FullEntity<IncompleteKey> incUserLocEntity = Entity.newBuilder(key)
						.set("email", x.getEmail())
						.set("userId", x.getUserId())
						.set("lat", x.getLat())
						.set("lng", x.getLng())
						.set("time", x.getTimeAt().toString())
						.build();
				Entity userLocEntity = datastore.add(incUserLocEntity);
				
			}
	}

	@RequestMapping("/showHeatMap")
	public String heatMap(Model model) {
		model.addAttribute("locations", userLocationDao.findAll());
		return "heatmapo";
	}
	
	@RequestMapping("/timeOfDay")
	public String tODHeatmap(@RequestParam String start, @RequestParam String end, Model model) throws ParseException {
		DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy"); 
		Date date1 = (Date)formatter.parse(start);
		Date date2 = (Date)formatter.parse(end);
		model.addAttribute("locations", userLocationDao.findByTimeAtBetween(date1, date2));
		return "heatmapo";
	}
}
