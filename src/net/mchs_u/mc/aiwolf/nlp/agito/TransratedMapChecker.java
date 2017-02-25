package net.mchs_u.mc.aiwolf.nlp.agito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TransratedMapChecker {
	public static void main(String[] args) {
		Map<String, String> transratedMap = Ear.load(); 

		List<String> keys = new ArrayList<>(transratedMap.keySet());
		Collections.sort(keys);
		
		for(String k: keys) {
			System.out.println(k + " -> " + transratedMap.get(k));
		}
	}
}
