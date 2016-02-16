package com.courses.junit_and_mockito.bo;

import com.courses.junit_and_mockito.bo.exception.BOException;
import com.courses.junit_and_mockito.dto.Order;

public interface OrderBO {
	
	boolean placeOrder(Order order) throws BOException;
	
	boolean cancelOrder(int orderId) throws BOException;
	
	boolean deleteOrder(int orderId) throws BOException;

}
