package com.courses.junit_and_mockito.bo.exception;

public class BOException extends Exception {

	private static final long serialVersionUID = 1L;

	public BOException(Exception e) {
		super(e);
	}

}
