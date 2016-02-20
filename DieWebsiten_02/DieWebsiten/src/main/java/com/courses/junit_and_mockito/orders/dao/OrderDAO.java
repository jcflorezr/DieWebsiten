package com.courses.junit_and_mockito.orders.dao;

import java.sql.SQLException;

import com.courses.junit_and_mockito.orders.dto.Order;

public interface OrderDAO {
	
	int create(Order order) throws SQLException;
	
	Order read(int id) throws SQLException;
	
	int update(Order order) throws SQLException;
	
	int delete(int id) throws SQLException;

}
