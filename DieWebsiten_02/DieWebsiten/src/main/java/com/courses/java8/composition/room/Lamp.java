package com.courses.java8.composition.room;

public class Lamp {
	
	private String brand;

	public Lamp(String brand) {
		this.brand = brand;
	}
	
	public void turnOn() {
		System.out.println("Lamp of brand '" + getBrand() + "' is now turned on.");
	}
	
	public void turnOff() {
		System.out.println("Lamp of brand '" + getBrand() + "' is now turned off.");
	}

	private String getBrand() {
		return brand;
	}
	
}
