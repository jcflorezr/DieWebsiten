package com.courses.java8.generics.challenge;

import java.util.ArrayList;
import java.util.List;

public class LigaBBVA extends League {

	List<String> ranking = new ArrayList<>();
	
	public void addRanking (Team<LigaBBVA> team) {
		ranking.add(team.getName() + " --> " + team.getPoints());
	}

}
