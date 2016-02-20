package com.courses.junit_and_mockito.orders.bo;

import com.courses.junit_and_mockito.orders.bo.exception.BOException;
import com.courses.junit_and_mockito.orders.dto.Order;

public interface OrderBO {
	
	boolean placeOrder(Order order) throws BOException;
	
	boolean cancelOrder(int orderId) throws BOException;
	
	boolean deleteOrder(int orderId) throws BOException;

}
