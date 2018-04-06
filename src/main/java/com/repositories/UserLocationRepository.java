package com.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.entities.UserLocation;

public interface UserLocationRepository extends CrudRepository<UserLocation, Long> {

	public UserLocation findByRecordingId(long id);
	public UserLocation findByTimeAt(Date date);
	public List<UserLocation> findByTimeAtBetween(Date date1, Date date2);
}
