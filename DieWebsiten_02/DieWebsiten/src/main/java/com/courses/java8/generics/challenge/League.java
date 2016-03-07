package com.courses.java8.generics.challenge;

import java.util.ArrayList;
import java.util.List;

public class League<T extends Team> {
	
	private List<T> teams = new ArrayList<>();
	private String name;
	
	public League (String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addTeam(T team) {
		teams.add(team);
	}

}
