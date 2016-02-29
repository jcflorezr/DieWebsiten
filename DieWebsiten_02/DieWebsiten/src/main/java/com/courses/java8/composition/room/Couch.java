package com.courses.java8.composition.room;

public class Couch {
	
	private String color;

	public Couch(String color) {
		this.color = color;
	}
	
	public void recline() {
		System.out.println("Couch of color '" + getColor() + "' is now reclined.");
	}
	
	public void straighten() {
		System.out.println("Couch of color '" + getColor() + "' is now sthraigthened.");
	}

	private String getColor() {
		return color;
	}

}
