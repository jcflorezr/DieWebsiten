package com.courses.java8.generics;

import java.util.ArrayList;

public class Team<T extends Player> {
	
	private String name;
	int played = 0;
	int won = 0;
	int lost = 0;
	int tied = 0;
	
	private ArrayList<Player> members = new ArrayList<>();
	
	
	public Team(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public boolean addPlayer(T player) {
		if(members.contains(player)) {
			System.out.println(player.getName() + " is already on this team.");
			return false;
		} else {
			members.add(player);
			System.out.println(player.getName() + " picked for team " + getName());
			return true;
		}
	}
	
	public int numPlayers() {
		return members.size();
	}
	
	public void matchResult(Team<T> opponent, int theirScore, int ourScore) {
		String message;
		if (ourScore > theirScore) {
			won++;
			message = " won against ";
		} else if (ourScore == theirScore) {
			tied++;
			message = " drew against ";
		} else {
			lost++;
			message = " lost against ";
		}
		played++;
		if(opponent != null) {
			System.out.println(getName() + message + opponent.getName());
			opponent.matchResult(null, theirScore, ourScore);
		}
	}
	
	public int ranking() {
		return won * 2 + tied;
	}

}
