package com.components;

import javax.persistence.Entity;

@Entity
public class Coordinates {

	public double latitude, longitude;
	
	public Coordinates(double a, double b) {
		latitude = a;
		longitude = b;
	}
}
