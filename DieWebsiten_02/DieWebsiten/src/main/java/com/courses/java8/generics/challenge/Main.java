package com.courses.java8.generics.challenge;

public class Main {

	
	
	public static void main(String[] args) {
		SoccerTeam barcelona = new SoccerTeam("FC Barcelona");
		SoccerTeam realMadrid = new SoccerTeam("Real Madrid CF");
		
		League<SoccerTeam> ligaBBVA = new League<>("Liga BBVA");
		ligaBBVA.getName();
		ligaBBVA.addTeam(barcelona);
	}

}
