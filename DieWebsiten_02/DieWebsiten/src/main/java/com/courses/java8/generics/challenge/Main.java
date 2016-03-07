package com.courses.java8.generics.challenge;

public class Main {

	
	
	public static void main(String[] args) {
		Team<LigaBBVA> realMadrid = new Team<>("Real Madrid CF");
		Team<LigaBBVA> barcelona = new Team<>("FC Barcelona");
		Team<PremierLeague> leicester = new Team<>("Leicester");
		barcelona.addMatchResult(4, 0, realMadrid);
		barcelona.printMatchResults();
		realMadrid.addMatchResult(0, 4, barcelona);
		realMadrid.printMatchResults();
		LigaBBVA liga = new LigaBBVA();
		liga.addRanking(barcelona);
		liga.addRanking(realMadrid);
		liga.printRanking();
		
	}

}
