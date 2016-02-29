package com.courses.java8.encapsulation.printer;

public class Main {

	public static void main(String[] args) {
		Printer p = new Printer();

		for (int i = 0; i < 22; i++) {
			p.print(true);
		}

		System.out.println("Number of pages printed: " + p.getPages());

	}

}
