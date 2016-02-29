package com.courses.java8.composition.room;

public class Room {
	
	private TV tv;
	private Lamp lamp;
	private Couch couch;
	

	public Room(TV tv, Lamp lamp, Couch couch) {
		this.tv = tv;
		this.lamp = lamp;
		this.couch = couch;
	}

	public void havingFun() {
		getLamp().turnOn();
		getTv().turnOn();
		couch.recline();
	} 
	
	public void goingToSleep() {
		getLamp().turnOff();
		getTv().turnOff();
		getCouch().straighten();
	}

	private TV getTv() {
		return tv;
	}
	
	private Lamp getLamp() {
		return lamp;
	}
	
	private Couch getCouch() {
		return couch;
	}

}
