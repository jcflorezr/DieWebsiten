package com.courses.java8.composition.room;

public class Main {

	public static void main (String[] args) {
		
		TV tv = new TV("lg", "42in");
		Lamp lamp = new Lamp("little lamp");
		Couch couch = new Couch("brown");
		
		Room room = new Room(tv, lamp, couch);
		room.havingFun();
		room.goingToSleep();
		
	}
	
}
