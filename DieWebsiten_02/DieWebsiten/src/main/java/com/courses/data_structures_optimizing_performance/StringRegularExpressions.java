package com.courses.data_structures_optimizing_performance;

import java.util.Arrays;
import java.util.List;

public class StringRegularExpressions {

	public static void main(String[] args) {
		
		String text = "si te esfueras; si me srives; si entregas lo mejor de ti para mí; 2930 aquí te la terminaré y esa será tu morada. bendito el nombre de Dios! entonces como sera su morada? será grande? será pequeña? lo importante es que alcance la vida eterna; amén hermanos? lo importante es que usted se entregue a Dios, lo importante es que usted le de lo mejor, amén hermanos?";
		//String regex = "[.?!0-9]+"; // by sentence
		String regex = "[^a-zA-Zá-ú]+"; // by word
		System.out.println(getTokens(text, regex));

	}
	
	
	private static List<String> getTokens(String s, String regex) {
		
		return Arrays.asList(s.split(regex));		
		
	}

}
