package com.application;

import org.apache.tomcat.util.http.*;
import org.springframework.ui.Model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.components.Coordinates;
import com.entities.AqiObject;
import com.entities.UserLocation;
import com.entities.UserLocationsList;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.helpers.DataCollectionHelper;
import com.helpers.DataParser;
import com.repositories.UserLocationRepository;
import com.repositories.UserRepository;

@Controller
public class DataCollectionController {

	@Autowired
	private UserRepository userDao;
	@Autowired
	private UserLocationRepository userLocationDao;

	List<UserLocation> userLocations;
	List<AqiObject> aqiRecords;
	
	@Scheduled(fixedDelay=18000)	//
	public void GetAqiData() {
		try {
			DataCollectionHelper.GetData();
			GetAQIFromGoogleDataStore();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void GetUserLocationsFromGoogleDataStore () {
		//Google Datastore
		Datastore datastore;
		KeyFactory keyFactory;
		datastore = DatastoreOptions.getDefaultInstance().getService();
		keyFactory = datastore.newKeyFactory().setKind("userlocation");
		
		Date thresholdDate = new Date();		//Collect for an hour prior
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -24);
		thresholdDate = calendar.getTime();
		
		userLocations = new ArrayList<UserLocation>();
		
		EntityQuery.Builder entityQueryBuilder = Query.newEntityQueryBuilder()
				.setKind("userlocation")
				.setFilter(PropertyFilter.ge("time", thresholdDate.getTime()));	//compare timestamp
		QueryResults<Entity> userLocationEntities = datastore.run(entityQueryBuilder.build());
		while(userLocationEntities.hasNext()) {
			UserLocation x = new UserLocation();
			try {
				Entity userLocationEntity = userLocationEntities.next();
				x.setLat(userLocationEntity.getDouble("lat"));
				x.setLng(userLocationEntity.getDouble("lng"));
				x.setEmail(userLocationEntity.getString("email"));
				x.setTimeAt(new Date(userLocationEntity.getLong("time")));
				x.setUserId(userLocationEntity.getLong("userId"));
				} catch (Exception ex)
			{}
			userLocations.add(x);
		}
	}
	
	void GetAQIFromGoogleDataStore() {
		//Google Datastore
		Datastore datastore;
		KeyFactory keyFactory;
		datastore = DatastoreOptions.getDefaultInstance().getService();
		keyFactory = datastore.newKeyFactory().setKind("aqi");

		aqiRecords = new ArrayList<AqiObject>();
		
		EntityQuery.Builder entityQueryBuilder = Query.newEntityQueryBuilder()
				.setKind("aqi")
				.setOrderBy(OrderBy.desc("time"))
				.setLimit(100);

		QueryResults<Entity> aqiEntities = datastore.run(entityQueryBuilder.build());
		while(aqiEntities.hasNext()) {
			AqiObject x = new AqiObject();
			
			Entity aqiEntity = aqiEntities.next();
			x.setAqi(Integer.parseInt(""+aqiEntity.getLong("aqi")));
			x.setLocation(aqiEntity.getString("location"));
			x.setStatus(aqiEntity.getString("status"));
			x.setTime(new Date(aqiEntity.getLong("time")));
			try {
				x.setLat(aqiEntity.getDouble("lat"));
				x.setLng(aqiEntity.getDouble("lng"));
			} catch (Exception ex) {}
			x.setDatastoreId(aqiEntity.getKey().getId());
			
			aqiRecords.add(x);
		}
	}

	@RequestMapping("/getData")
	public String GetData(Model model) {
		GetAQIFromGoogleDataStore();
		GetUserLocationsFromGoogleDataStore();
		
		DataCollectionHelper dataHelper = new DataCollectionHelper();
		dataHelper.DuplicateAqiCleaner(aqiRecords);
		
		model.addAttribute("aqi", aqiRecords);
		model.addAttribute("locations", userLocations);
		
		return "showData";
	}
	
	@RequestMapping("/getUserLocationsPerStationName")
	public String GetDataUserLocationsPerStationName(Model model, @RequestParam String location, @RequestParam String email) {
		Datastore datastore;
		KeyFactory keyFactory;
		datastore = DatastoreOptions.getDefaultInstance().getService();
		keyFactory = datastore.newKeyFactory().setKind("aqi");
		
		Query<Entity> query  = Query.newEntityQueryBuilder()
				.setKind("aqi")
				.setFilter(PropertyFilter.eq("location", location))
				.build();
		QueryResults<Entity> entities = datastore.run(query);
		
		List<UserLocation> specificUserLocations = new ArrayList<>();

		while(entities.hasNext()) {
			Entity x = entities.next();
			
			Query<Entity> userquery  = Query.newEntityQueryBuilder()
					.setKind("userlocation")
					.setFilter(PropertyFilter.eq("aqiId", x.getKey().getId()))
					.build();
			QueryResults<Entity> userentities = datastore.run(userquery);

			while(userentities.hasNext()) {
				Entity y = userentities.next();
				if(y.getString("email").equals(email)) {
					UserLocation newLoc = new UserLocation();
					newLoc.setLat(y.getDouble("lat"));
					newLoc.setLng(y.getDouble("lng"));
					specificUserLocations.add(newLoc);
				}
			}
		}
		
		model.addAttribute("userLocations", specificUserLocations);
		return "showHeatmap";
	}
	
	@RequestMapping("/showData")
	public String ShowData(Model model) {
		model.addAttribute("aqi", aqiRecords);
		model.addAttribute("locations", userLocations);
		
		return "showData";
	}


	@RequestMapping("/addLocation")
	public void AddLocation(HttpEntity<String> httpEntity, HttpServletRequest request, HttpServletResponse response) throws IOException {		
		Coordinates userCoords = DataParser.locationDataParser(httpEntity.getBody());
		String userEmail = DataParser.userEmailParser(httpEntity.getBody());
		Date timestamp = DataParser.timeParser(httpEntity.getBody());
		int recordingId = DataParser.locationIdParser(httpEntity.getBody());
		
		AqiObject aqiObject = DataCollectionHelper.GetDataLocationBased(userCoords.latitude, userCoords.longitude);

		UserLocation x = new UserLocation();
		try {
			x.setEmail(userEmail);
			x.setRecordingId(recordingId);
			x.setUserId(userDao.findByEmail(userEmail).getId());
			x.setLat(userCoords.latitude);
			x.setLng(userCoords.longitude);
			x.setTimeAt(timestamp);
			x.setAqiId(aqiObject.getDatastoreId());

			//Already exist? ignore
			if(userLocationDao.findByTimeAt(timestamp) == null) {
				System.out.println(x.getEmail() + ": ("+ x.getLat() + ", "+ x.getLng() +") : " + x.getTimeAt().toString());
				userLocationDao.save(x);
				
				//Google Datastore
				ToGoogleDataStore(x);
			}
			
		} catch (Exception ex) {
			GetAQIFromGoogleDataStore();
		}
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
				.set("time", x.getTimeAt().getTime())
				.set("aqiId", x.getAqiId())
				.build();
		Entity userLocEntity = datastore.add(incUserLocEntity);
		
	}

	@RequestMapping(value = "/getAirDataPastDay", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public UserLocationsList getAirDataPastDay(@RequestParam String email) {
		Datastore datastore;
		KeyFactory keyFactory;
		datastore = DatastoreOptions.getDefaultInstance().getService();
		keyFactory = datastore.newKeyFactory().setKind("aqi");
		
		List<UserLocation> specificUserLocations = new ArrayList<UserLocation>();
		
		Query<Entity> query  = Query.newEntityQueryBuilder()
				.setKind("userlocation")
				.setFilter(PropertyFilter.eq("email", email))
				.setOrderBy(OrderBy.desc("time"))
				.setLimit(10)
				.build();
		QueryResults<Entity> userLocationEntities = datastore.run(query);
		while(userLocationEntities.hasNext()) {
			UserLocation x = new UserLocation();
			try {
				Entity userLocationEntity = userLocationEntities.next();
				x.setLat(userLocationEntity.getDouble("lat"));
				x.setLng(userLocationEntity.getDouble("lng"));
				x.setEmail(userLocationEntity.getString("email"));
				x.setTimeAt(new Date(userLocationEntity.getLong("time")));
				x.setUserId(userLocationEntity.getLong("userId"));
				x.setAqiId(userLocationEntity.getLong("aqiId"));
				} catch (Exception ex)
			{}
			specificUserLocations.add(x);
		}
		
		Comparator<UserLocation> cmp = new Comparator<UserLocation>() {
			public int compare(UserLocation o1, UserLocation o2) {
				return o2.getTimeAt().compareTo(o1.getTimeAt());
			};
		};
		Collections.sort(specificUserLocations, cmp);
		
		UserLocationsList x = new UserLocationsList();
		x.setLocations(specificUserLocations);	
		return x;
	}
	
	@RequestMapping(value = "/getAQIById", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public AqiObject getAQIById(@RequestParam long id) {
		System.out.println(id);
		Datastore datastore;
		KeyFactory keyFactory;
		datastore = DatastoreOptions.getDefaultInstance().getService();
		keyFactory = datastore.newKeyFactory().setKind("aqi");
		
		Query<Entity> query  = Query.newEntityQueryBuilder()
				.setKind("aqi")
				.setFilter(PropertyFilter.eq("__key__", keyFactory.newKey(id)))
				.build();
		QueryResults<Entity> entities = datastore.run(query);
		Entity result = entities.next();
		
		AqiObject x = new AqiObject();
		x.setAqi(Integer.parseInt(""+result.getLong("aqi")));
		x.setDatastoreId(id);
		x.setLat(result.getDouble("lat"));
		x.setLat(result.getDouble("lng"));
		x.setLocation(result.getString("location"));
		x.setStatus(result.getString("status"));
		x.setTime(new Date(result.getLong("time")));
		x.setStatus(result.getString("status"));
		x.setNo2(Integer.parseInt(""+result.getLong("no2")));
		x.setO3(Integer.parseInt(""+result.getLong("o3")));
		x.setPm10(Integer.parseInt(""+result.getLong("pm10")));
		x.setPm25(Integer.parseInt(""+result.getLong("pm25")));
		x.setSo2(Integer.parseInt(""+result.getLong("so2")));
		
		return x;
	}
}
