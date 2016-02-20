package com.courses.junit_and_mockito.orders.bo;

import com.courses.junit_and_mockito.orders.bo.exception.BOException;
import com.courses.junit_and_mockito.orders.dao.OrderDAO;
import com.courses.junit_and_mockito.orders.dto.Order;

public class OrderBOImpl implements OrderBO {
	
	private OrderDAO dao;

	@Override
	public boolean placeOrder(Order order) throws BOException {
		try {
			int result = dao.create(order);
			if (result == 0)
				return false;
		} catch (Exception e) {
			throw new BOException(e);
		}
		return true;
	}

	@Override
	public boolean cancelOrder(int orderId) throws BOException {
		try {
			Order order = dao.read(orderId);
			order.setStatus("cancelled");
			int result = dao.update(order);
			if (result == 0)
				return false;
		} catch (Exception e) {
			throw new BOException(e);
		}
		return true;
	}

	@Override
	public boolean deleteOrder(int orderId) throws BOException {
		try {
			Order order = dao.read(orderId);
			order.setStatus("deleted");
			int result = dao.delete(orderId);
			if (result == 0)
				return false;
		} catch (Exception e) {
			throw new BOException(e);
		}
		return true;
	}

	public OrderDAO getDao() {
		return dao;
	}

	public void setDao(OrderDAO dao) {
		this.dao = dao;
	}

}
