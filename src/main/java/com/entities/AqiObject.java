package com.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "aqi")
public class AqiObject {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Getter
	private long id;

	@Getter @Setter
	private int aqi;

	@Getter @Setter
	private String location, status;
	
	@Getter @Setter
	private Date time;
	
	@Getter @Setter
	private double lat, lng;
	
	@Getter @Setter
	private long datastoreId;
	
	@Getter @Setter
	private int no2, o3, pm10, pm25, so2;
}
