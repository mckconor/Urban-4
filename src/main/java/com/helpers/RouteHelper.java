package com.helpers;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.components.Coordinates;

public class RouteHelper {
	
	public static double GetDistance (Coordinates a, Coordinates b) {
		double x1 = a.latitude;
		double x2 = b.latitude;

		double y1 = a.longitude;
		double y2 = b.longitude;
		
		double ans = Math.sqrt(Math.pow((x2-x1), 2) + Math.pow(y2-y1, 2));
		
		return ans;
	}
}
