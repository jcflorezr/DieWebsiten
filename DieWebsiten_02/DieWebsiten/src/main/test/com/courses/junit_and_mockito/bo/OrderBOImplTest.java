package com.courses.junit_and_mockito.bo;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.courses.junit_and_mockito.bo.exception.BOException;
import com.courses.junit_and_mockito.dao.OrderDAO;
import com.courses.junit_and_mockito.dto.Order;

public class OrderBOImplTest {

	@Mock
	OrderDAO dao;
	
	private OrderBOImpl bo;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		bo = new OrderBOImpl();
		bo.setDao(dao);
	}
	
	@Test
	public void shouldCreateAnOrder() throws SQLException, BOException {
		
		Order order = new Order();
		when(dao.create(order)).thenReturn(new Integer(1));
		
		assertTrue(bo.placeOrder(order));
		verify(dao).create(order);
		
	}
	
	@Test
	public void shouldNotCreateAnOrder() throws SQLException, BOException {
		
		Order order = new Order();
		when(dao.create(order)).thenReturn(new Integer(0));
		
		assertFalse(bo.placeOrder(order));
		verify(dao).create(order);
		
	}
	
	@Test(expected = BOException.class)
	public void createAnOrderShouldThrowAnException() throws SQLException, BOException {
		
		Order order = new Order();
		when(dao.create(order)).thenThrow(SQLException.class);
		boolean result = bo.placeOrder(order);
		
	}
	
	@Test
	public void shouldCancelAnOrder() throws SQLException, BOException {
		
		Order order = new Order();
		when(dao.read(123)).thenReturn(order);
		when(dao.update(order)).thenReturn(1);
		
		assertTrue(bo.cancelOrder(123));
		
		verify(dao).read(123);
		verify(dao).update(order);
		
	}
	
	@Test
	public void shouldNotCancelAnOrder () throws SQLException, BOException {
		
		Order order = new Order();
		when(dao.read(123)).thenReturn(order);
		when(dao.update(order)).thenReturn(0);
		
		assertFalse(bo.cancelOrder(123));
		
		verify(dao).read(123);
		verify(dao).update(order);
		
	}
	
	@Test(expected = BOException.class)
	public void cancelAnOrderShouldThrowAnExceptionOnRead() throws SQLException, BOException {
		
		when(dao.read(123)).thenThrow(SQLException.class);
		boolean result = bo.cancelOrder(123);
		
	}
	
	@Test(expected = BOException.class)
	public void cancelAnOrderShouldThrowAnExceptionOnUpdate() throws SQLException, BOException {
		
		Order order = new Order();
		when(dao.read(123)).thenReturn(order);
		when(dao.update(order)).thenThrow(SQLException.class);
		boolean result = bo.cancelOrder(123);
		
	}

}
