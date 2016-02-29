package com.courses.java8.polymorphism.car;

public class Bugati implements Car {

	private String engine;
	private int cylinders;
	private int maxSpeed;
	
	private CarImpl car;

	public Bugati(String engine, int cylinders, int maxSpeed) {
		this.engine = engine;
		this.cylinders = cylinders;
		this.maxSpeed = maxSpeed;
		car = new CarImpl(engine, cylinders, maxSpeed);
	}
	
	@Override
	public void startEngine() {
		// TODO Auto-generated method stub

	}

	@Override
	public void assembleCar() {
		car.assembleCar();
	}

}
