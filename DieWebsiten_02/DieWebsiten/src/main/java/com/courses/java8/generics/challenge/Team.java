package com.courses.java8.generics.challenge;

import java.util.ArrayList;
import java.util.List;

public class Team<T extends League> {

	private List<String> matchResults;
	private String name;
	private int won;
	private int tied;
	

	public String getName() {
		return name;
	}



	public Team(String name) {
		this.name = name;
		matchResults = new ArrayList<>();
	}



	public void addMatchResult(int ourScore, int theirScore, Team<T> opponent) {
		matchResults.add(ourScore + " : " + theirScore + " " + opponent.getName());
		if(ourScore > theirScore) {
			won++;
		} else if (ourScore == theirScore) {
			tied++;
		}
	}
	
	public void printMatchResults() {
		for (String matchResult : matchResults) {
			System.out.println(getName() + " " + matchResult);
		}
	}
	
	public int getPoints() {
		return (won * 3) + tied;
	}
	
}
