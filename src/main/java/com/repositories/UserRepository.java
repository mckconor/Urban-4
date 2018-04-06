package com.repositories;

import java.util.Date;

import org.springframework.data.repository.CrudRepository;

import com.entities.User;

public interface UserRepository extends CrudRepository<User, Long> {
	public User findByEmail(String email);
	public User findById(long id);
}