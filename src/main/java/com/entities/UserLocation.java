package com.entities;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Null;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "userlocation")
public class UserLocation {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Getter
	private long id;
	
	@Getter @Setter
	private long recordingId;
	
	@Getter @Setter
	private String email;
	
	@Getter @Setter
	private long userId;
	
	@Getter @Setter
	private double lat, lng;
	
	@Getter @Setter
	private Date timeAt;
	
	@Getter @Setter
	private long aqiId;
}
