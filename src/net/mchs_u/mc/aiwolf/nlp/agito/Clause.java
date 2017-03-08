package net.mchs_u.mc.aiwolf.nlp.agito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mychaelstyle.nlp.KNP;

public class Clause {
	private int id = -1;
	private String text = null;
	private String main = null;
	private Set<String> signages = null;
	private String target = null;
	private Clause child = null;
	private Set<Clause> parents = null;
	private Clause paraForward = null;
	private Clause paraBack = null;
	
	private Set<String> modalities = null;
	private String kaku = null;
	private Map<String, Clause> kakuMap = null;
	
	private Set<String> attributes = null;
	
	private String aiwolfWordType = null;
	private String aiwolfWordMeaning = null;
	private boolean negative = false;
	
	private Clause(int id, String text, String main, String target) {
		this.id = id;
		this.text = text;
		this.main = main;
		this.target = target;
		
		signages = new HashSet<>();
		attributes = new HashSet<>();
		parents = new HashSet<>();
		kakuMap = new HashMap<>();
		modalities = new HashSet<>();
	}
	
	private void addSignage(String signage) {
		signages.add(signage);
	}
	
	private void addAttribute(String attribute) {
		if(attribute.equals(""))
			return;
		
		String att = attribute.replace("\"", "");
		attributes.add(att);
		
		if(att.startsWith("人狼知能,")){
			String[] split = att.split(",");
			aiwolfWordType = split[1];
			aiwolfWordMeaning = split[2];
		}
		
		if(att.equals("否定表現"))
			negative = true;
		
		if(att.startsWith("{解析格:"))
			kaku = att.replace("{解析格:", "").replace("}", "");
		
		if(att.startsWith("モダリティ-"))
			modalities.add(att.replace("モダリティ-", ""));
	}
	
	private void createKakuMap(List<Clause> clauses) {
		for(String att: attributes) {
			if(att.startsWith("{格関係")) {
				//TODO 本来は親だけをたどるべきなのだけど実装が面倒そうなので・・・
				String[] sp = att.replace("}", "").split(":");
				Clause c = null;
				for(Clause clause: clauses) {
					if(clause.kaku != null && clause.kaku.equals(sp[1]) && clause.signages.contains(sp[2])){
						c = clause;
						break;
					}
				}
				kakuMap.put(sp[1], c);
			}
		}
	}
	
	public static List<Clause> createClauses(String text) throws IOException, InterruptedException {
		List<Clause> clauses = new ArrayList<>();
		
		KNP knp = new KNP();
		ObjectNode root = knp.parse(text);
		
		for(JsonNode clauseNode: root.get("clauseas")){
			Clause clause = new Clause(
					clauses.size(),
					clauseNode.get("clausea").asText(),
					clauseNode.get("attributes").findValues("正規化代表表記").get(0).get(0).asText(),
					clauseNode.get("target").asText());
			
			for(JsonNode clAtt: clauseNode.get("attributes"))
				clause.addAttribute(clAtt.toString());
			
			for(JsonNode phraseNode: clauseNode.get("phrases")) {
				for(JsonNode clPhr: phraseNode.get("attributes"))
					clause.addAttribute(clPhr.toString());
				
				for(JsonNode morphemeNode: phraseNode.get("morphemes")) {
					clause.addSignage(morphemeNode.get("signage").asText());
					for(JsonNode clMor: morphemeNode.get("attributes"))
						clause.addAttribute(clMor.toString());
				}
			}
			
			clauses.add(clause);
		}
		
		for(Clause clause: clauses) { // target Pの処理
			if(!clause.getTarget().endsWith("P"))
				continue;
			
			int id = Integer.parseInt(clause.getTarget().replace("P", ""));
			clause.paraForward = clauses.get(id);
			clauses.get(id).paraBack = clause;
		}
		
		for(Clause clause: clauses) { // target Dの処理
			if(!clause.getTarget().endsWith("D"))
				continue;
			
			int id = Integer.parseInt(clause.getTarget().replace("D", ""));
			if(id > -1) {
				clause.child = clauses.get(id);
				clauses.get(id).parents.add(clause);
			}
		}
		
		for(Clause clause: clauses) {
			clause.createKakuMap(clauses);
		}
		
		return clauses;
	}

	
	public String getTarget() {
		return target;
	}
	
	public int getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getMain() {
		return main;
	}

	public Set<String> getSignages() {
		return signages;
	}

	public Clause getChild() {
		return child;
	}

	public Set<Clause> getParents() {
		return parents;
	}

	public Clause getParaForward() {
		return paraForward;
	}

	public Clause getParaBack() {
		return paraBack;
	}

	public String getKaku() {
		return kaku;
	}

	public Map<String, Clause> getKakuMap() {
		return kakuMap;
	}

	public Set<String> getAttributes() {
		return attributes;
	}

	public String getAiwolfWordType() {
		return aiwolfWordType;
	}

	public String getAiwolfWordMeaning() {
		return aiwolfWordMeaning;
	}
	
	public Set<String> getModalities() {
		return modalities;
	}

	public boolean isNegative() {
		return negative;
	}
	
	public static Clause findMainClause(List<Clause> clauses, String main) {
		for(Clause clause: clauses)
			if(clause.main.equals(main))
				return clause;
		return null;
	}
	
	public static Clause findModalityClause(List<Clause> clauses, String modality) {
		for(Clause clause: clauses)
			if(clause.modalities.contains(modality))
				return clause;
		return null;
	}	
	
	public static Clause findAttributeClause(List<Clause> clauses, String attribute) {
		for(Clause clause: clauses)
			if(clause.attributes.contains(attribute))
				return clause;
		return null;
	}
	
	public static Clause findAiwolfTypeClause(List<Clause> clauses, String type) {
		for(Clause clause: clauses)
			if(clause.aiwolfWordType != null && clause.aiwolfWordType.equals(type))
				return clause;
		return null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append("* " + text);
		if(child != null)
			sb.append(" ( -> " + child.text + " )");
		for(Clause parent: parents)
			sb.append(" ( <- " + parent.text + " )");
		
		if(paraForward != null)
			sb.append(" ( ||> " + paraForward.text + " )");
		if(paraBack != null)
			sb.append(" ( <|| " + paraBack.text + " )");		
		//sb.append(" " + target + " ");
		sb.append("\n");
		
		for(String key: kakuMap.keySet()) {
			if(kakuMap.get(key) == null)
				sb.append("<" + null + " " + key + ">\n");
			else
				sb.append("<" + kakuMap.get(key).main + " " + key + ">\n");
		}
		
		sb.append("・" + main + "\n");
		if(kaku != null)
			sb.append("・" + kaku + "格\n");
		if(aiwolfWordType != null)
			sb.append("☆" + aiwolfWordType + " = " + aiwolfWordMeaning + "\n");
		if(negative)
			sb.append("・否定\n");
		for(String m: modalities)
			sb.append("モダリティ = " + m + "\n");
		
		sb.append("　　" + attributes + "\n");
		
		return sb.toString();
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Clause other = (Clause) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		String talk = null;
		//talk = "私は人狼ではありません";
		//talk = "太郎と二郎と三郎と四郎は楽しくて嬉しかった";
		//talk = "そうです、彼じゃなく私が人狼なんだよ";
		//talk = "昨日彼を占ったら人狼でした";
		//talk = "人狼なんて居るのかなあ";
		//talk = "私は人狼のことを知らない";
		//talk = "いみがわからないんだけど";
		//talk = "意味がわからないんだけど";
		//talk = "わたしは人狼のことを知らない";
		//talk = "今日はＡｇｅｎｔ［０１］を吊ってね";
		talk = "今日はＡｇｅｎｔ［０１］さんに投票してください";
		//talk = "Ａｇｅｎｔ［０１］さんは人狼だったよ";
		//talk = "人狼ＣＯします";
		//talk = "人狼ＣＯするよ";
		//talk = "Ａｇｅｎｔ［０１］さんの占い結果は人狼だったよ";
		//talk = "Ａｇｅｎｔ［０１］さんを占ったら人狼だったよ";
		//talk = "Ａｇｅｎｔ［０１］さんを占ったら、結果は人狼だったよ";
		//talk = "Ａｇｅｎｔ［０１］さんの占いの結果は人狼だったよ";
		//talk = "Ａｇｅｎｔ［９９］さんが人狼なんでしょ？";
		//talk = "Ａｇｅｎｔ［９９］さん、いっしょに遊ぼうよ！";
		//talk = "君は誰が人狼だと思う？";
		//talk = "今日はＡｇｅｎｔ［０１］に投票しましょうよ";
		//talk = "彼は人狼らしいよ";
		//talk = "彼は人狼かもしれない";
		//talk = "彼は人狼だろう";
		//talk = "彼は人狼だというわけだ。";
		//talk = "君が人狼なんでしょう？";
		//talk = "人狼は誰だと思う？";
		//talk = "君が犯人なんだろう？";
		//talk = "Agent[01]に投票しようかな";
		talk = "Ａｇｅｎｔ［０１］は人狼でした";

		
		List<Clause> list = Clause.createClauses(talk);
		for(Clause c: list) {
			System.out.println(c);
		}
		
	}
	
	

}
