package com.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "usercluster")
public class UserCluster {

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Getter
	private Long id;
	
}
