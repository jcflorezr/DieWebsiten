package com.courses.java8.polymorphism.car;

public class CarImpl implements Car {

	private String engine;
	private int wheels;
	private int cylinders;
	private int maxSpeed;
	
	
	
	public CarImpl(String engine, int maxSpeed, int cylinders) {
		this.engine = engine;
		this.maxSpeed = maxSpeed;
		this.cylinders = cylinders;
	}

	@Override
	public void startEngine() {
		System.out.println("Engine started!");

	}

	@Override
	public void assembleCar() {
		System.out.println(engine);
		System.out.println(maxSpeed);
		System.out.println(cylinders);

	}
	
	
	
	

}
