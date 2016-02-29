package com.courses.java8.polymorphism.car;

public class Ferrari implements Car {
	
	private String engine;
	private int cylinders;
	private int maxSpeed;
	
	private CarImpl car;

	public Ferrari(String engine, int cylinders, int maxSpeed) {
		this.engine = engine;
		this.cylinders = cylinders;
		this.maxSpeed = maxSpeed;
		car = new CarImpl(engine, cylinders, maxSpeed);
	}

	@Override
	public void startEngine() {
		car.startEngine();
		
	}

	@Override
	public void assembleCar() {
		car.assembleCar();
	}
	
	

}
