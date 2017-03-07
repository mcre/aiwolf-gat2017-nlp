package net.mchs_u.mc.aiwolf.nlp.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mychaelstyle.nlp.KNP;

import net.mchs_u.mc.aiwolf.nlp.agito.Clause;

public class KNPChecker {
	
	public static void detail(String text) throws IOException, InterruptedException {
		KNP knp = new KNP();
		ObjectNode root = knp.parse(text);
		JsonNode view = root.get("clauseas");

		Iterator<JsonNode> e1 =  view.elements();
		while(e1.hasNext()) {
			System.out.println("*");
			JsonNode n1 = e1.next();
			
			Iterator<Entry<String,JsonNode>> e2 = n1.fields();
			while(e2.hasNext()) {
				Entry<String, JsonNode> n2 = e2.next();
				if(!n2.getKey().equals("phrases")) {
					System.out.println(n2.getKey() + " => " + n2.getValue());
					continue;
				}
				System.out.println("phrases => ");
				
				Iterator<JsonNode> e3 =  n2.getValue().elements();
				while(e3.hasNext()) {
					JsonNode n3 = e3.next();
					System.out.println("    *");
					
					Iterator<Entry<String,JsonNode>> e4 = n3.fields();
					while(e4.hasNext()) {
						Entry<String, JsonNode> n4 = e4.next();
						if(!n4.getKey().equals("morphemes")) {
							System.out.println("    " + n4.getKey() + " => " + n4.getValue());
							continue;
						}
						System.out.println("    morphemes => ");
						
						Iterator<JsonNode> e5 =  n4.getValue().elements();
						while(e5.hasNext()) {
							JsonNode n5 = e5.next();
							System.out.println("        " + n5);
						}
					}
				}
			}
		}
	}

	public static void simple(String text) throws IOException, InterruptedException {
		KNP knp = new KNP();
		ObjectNode root = knp.parse(text);
		JsonNode view = root.get("clauseas");

		Iterator<JsonNode> e1 =  view.elements();
		while(e1.hasNext()) {
			System.out.println("*");
			JsonNode n1 = e1.next();
			
			Iterator<Entry<String,JsonNode>> e2 = n1.fields();
			while(e2.hasNext()) {
				Entry<String, JsonNode> n2 = e2.next();
				if(!n2.getKey().equals("phrases")) {
					if(n2.getKey().equals("clausea"))
						System.out.println(n2.getValue().asText().replace("\"", ""));
					else
						System.out.println(n2.getKey() + " => " + n2.getValue());
					continue;
				}
				
				Iterator<JsonNode> e3 =  n2.getValue().elements();
				while(e3.hasNext()) {
					JsonNode n3 = e3.next();
					System.out.println("    *");
					
					Iterator<Entry<String,JsonNode>> e4 = n3.fields();
					while(e4.hasNext()) {
						Entry<String, JsonNode> n4 = e4.next();
						if(!n4.getKey().equals("morphemes")) {
							if(n4.getKey().equals("phrase"))
								System.out.println("    " + n4.getValue().asText().replace("\"", ""));
							else
								System.out.println("    " + n4.getKey() + " => " + n4.getValue());
							continue;
						}
						
						Iterator<JsonNode> e5 =  n4.getValue().elements();
						while(e5.hasNext()) {
							JsonNode n5 = e5.next();
							System.out.println("        *");
							Iterator<Entry<String,JsonNode>> e6 = n5.fields();
							while(e6.hasNext()) {
								Entry<String, JsonNode> n6 = e6.next();
								if(n6.getKey().equals("reading") 
										|| n6.getKey().equals("prototype")
										|| n6.getKey().equals("meanings")
										|| n6.getKey().startsWith("part")
										|| n6.getKey().startsWith("conjugated"))
									continue;
								if(n6.getKey().equals("signage"))
									System.out.println("        " + n6.getValue().asText().replace("\"", ""));
								else
									System.out.println("        " + n6.getKey() + " => " + n6.getValue());
							}
						}
					}
				}
			}
		}
	}
	
	private static String getTargetClausea(String target, JsonNode clauseas) {		
		
		if(target.endsWith("D")) {
			int id = Integer.parseInt(target.replace("D", ""));
			if(id < 0) {
				// return " ( <= " + node.get(-id).get("clausea") + " ) "; // 引き算しなきゃかも
				return "";
			} else {
				return " ( => " + clauseas.get(id).get("clausea") + " ) ";
			}
		} else if(target.endsWith("P")) {
			int id = Integer.parseInt(target.replace("P", ""));
			return " ( || " + clauseas.get(id).get("clausea") + " ) ";
		} else {
			return " ( " + target + " ) ";
		}

	}
	
	public static void verySimple(String text) throws IOException, InterruptedException {
		KNP knp = new KNP();
		ObjectNode root = knp.parse(text);
		JsonNode clauseas = root.get("clauseas");

		Iterator<JsonNode> clauseasIterator =  clauseas.elements();
		
		int i = 1;
		while(clauseasIterator.hasNext()) {		
			JsonNode clausea = clauseasIterator.next();
			System.out.print((i++) + ": " + clausea.get("clausea").asText());
			System.out.println(getTargetClausea(clausea.get("target").asText(), clauseas));
			System.out.println("   " + clausea.get("attributes"));

			Iterator<JsonNode> phrasesIterator = clausea.get("phrases").elements();
			while(phrasesIterator.hasNext()) {
				JsonNode phrase = phrasesIterator.next();				
				System.out.println("        " + phrase.get("phrase").asText());
				System.out.println("        " + phrase.get("attributes"));
			}
		}
	
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		//String talk = "太郎と二郎と三郎と四郎は楽しくて嬉しい";
		//String talk = "私の彼の頭は禿げています";
		String talk = "私は人狼ではありません";
		
		detail(talk);
		System.out.println("---------");
		verySimple(talk);
		System.out.println("---------");
		for(Clause c: Clause.createClauseas(talk))
			System.out.println(c);
	}

}
