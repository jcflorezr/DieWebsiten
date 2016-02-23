package com.courses.junit_and_mockito.useradmin.dao;

import com.courses.junit_and_mockito.useradmin.dto.User;
import com.courses.junit_and_mockito.useradmin.util.IDGenerator;

public class UserDAO {

	public int createUser(User user) {
		
		int id = IDGenerator.generateID();
		// Save the user object to the db
		return id;
		
	}
	
}
