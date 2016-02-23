package com.courses.junit_and_mockito.useradmin.util;

public final class IDGenerator {
	
	static int i;
	
	public static final int generateID() {
		return i++;
	}

}
