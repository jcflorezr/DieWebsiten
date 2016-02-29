package com.courses.java8.composition.room;

public class TV {

	private String brand;
	private String size;
	
	public TV(String brand, String size) {
		this.brand = brand;
		this.size = size;
	}
	
	public void turnOn() {
		System.out.println("TV of brand '" + getBrand() + "' and size of '" + getSize() + "' is now turned on.");
	}
	
	public void turnOff() {
		System.out.println("TV of brand '" + getBrand() + "' and size of '" + getSize() + "' is now turned off.");
	}

	private String getSize() {
		return size;
	}

	private String getBrand() {
		return brand;
	}

}
