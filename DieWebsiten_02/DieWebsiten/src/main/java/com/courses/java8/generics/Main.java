package com.courses.java8.generics;

public class Main {

	public static void main(String[] args) {
		
		SoccerPlayer messi = new SoccerPlayer("Messi");
		BasketballPlayer james = new BasketballPlayer("James");
		BaseballPlayer canseco = new BaseballPlayer("Canseco");
		
		Team<BasketballPlayer> clevelandCavaliers = new Team<>("Cleveland Cavaliers");
		clevelandCavaliers.addPlayer(james);
		Team<SoccerPlayer> fcBarcelona = new Team<>("FC Barcelona");
		fcBarcelona.addPlayer(messi);
		Team<BaseballPlayer> redSox = new Team<>("Red Sox");
		redSox.addPlayer(canseco);
		
		
		Team<SoccerPlayer> realMadridCF = new Team<>("Real Madrid CF");
		
		Team<BasketballPlayer> bostonCeltics = new Team<>("Boston Celtics");
		
		fcBarcelona.matchResult(realMadridCF, 0, 4);
		
		clevelandCavaliers.matchResult(bostonCeltics, 112, 120);

	}

}
