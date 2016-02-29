package com.courses.java8.polymorphism.car;

public class Main {

	public static void main(String[] args) {
		Car car = new Ferrari("ferrari", 0, 0);
		car.assembleCar();
		car = new Lamborghini("laamb", 30, 10);
		car.assembleCar();
		car = new Bugati("bugati", 20, 20);
		car.assembleCar();
		System.out.println(car.getClass());
	}

}
