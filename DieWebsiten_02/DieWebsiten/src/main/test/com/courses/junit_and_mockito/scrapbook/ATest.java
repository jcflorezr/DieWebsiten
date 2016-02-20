package com.courses.junit_and_mockito.scrapbook;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class ATest {

	@Mock
	B b;
	private A a;
	
	@Before
	public void setUp () {
		MockitoAnnotations.initMocks(this);
		a = new A(b);
	}
	
	@Test
	public void usesVoidMethodShouldCallTheVoidMethod() throws Exception {
		doNothing().when(b).voidMethod();
		assertSame(1, a.usesVoidMethod());
		verify(b).voidMethod();
	}
	
	@Test(expected = RuntimeException.class)
	public void usesVoidMethodShouldThrowRuntimeException() throws Exception {
		doThrow(Exception.class).when(b).voidMethod();
		a.usesVoidMethod();
	}
	
	@Test(expected = RuntimeException.class)
	public void testConsecutiveCalls () throws Exception {
		doNothing().doThrow(Exception.class).when(b).voidMethod();
		
		doNothing().when(b).voidMethod();
		a.usesVoidMethod();
		
		doThrow(Exception.class).when(b).voidMethod();
		a.usesVoidMethod();
	}

}
