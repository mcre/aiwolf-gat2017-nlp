package net.mchs_u.mc.aiwolf.nlp.agito;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

public class Mouth {
	private final static String PATH_POSI_CHATS = "dic/posichats.txt";
	private final static String PATH_NEGA_CHATS = "dic/negachats.txt";
	private final static String PATH_SEER_CHATS = "dic/seerchats.txt";

	private final static double CHAT_RATE = 0.2d; // 話題がないときにチャットリストを消費する確率

	private List<String> negaChats = null;
	private List<String> posiChats = null;
	private List<String> seerChats = null;

	private Set<String> talkedSet = null;
	
	public void initialize(GameInfo gameInfo) {
		talkedSet = new HashSet<>();
		loadChats(new Random((new Date()).getTime() + gameInfo.getAgent().getAgentIdx() * 222));
	}
	
	public void dayStart() {
	}

	public String toNaturalLanguageForTalk(GameInfo gameInfo, Map<Agent, Role> coMap, String protocol, List<String> answers) {
		if(!Content.validate(protocol)) {
			System.err.println("Mouth: 内部エージェントがプロトコル以外を喋ってる -> " + protocol);
			return Talk.SKIP;
		}
		Content content = new Content(protocol);

		if(gameInfo.getDay() == 0) { //　0日目は特殊
			if(!talkedSet.contains("0日目発言")){
				talkedSet.add("0日目発言");
				switch ((int)(Math.random() * 4)) {
				case 0: return "よろしくね";
				case 1: return "こんにちは";
				case 2: return "おはよう";
				case 3: return "おはようございます";
				}
			}
			return Talk.OVER;
		}

		switch (content.getTopic()) {
		case OVER:
			return Talk.OVER;
		case SKIP:
			return skipTalk(gameInfo, coMap, answers);
		case COMINGOUT:
			if(!content.getTarget().equals(gameInfo.getAgent()))
				return Talk.SKIP;
			if(content.getRole() == Role.WEREWOLF)
				return "わおーん、僕は人狼だよ。";
			return "僕は" + roleToString(content.getRole()) + "だよ。";
		case DIVINED:
			switch ((int)(Math.random() * 5)) {
			case 0: return content.getTarget() + "さんの占い結果は、" + speciesToString(content.getResult()) + "だったよ。";
			case 1: return content.getTarget() + "さんの占いの結果は、" + speciesToString(content.getResult()) + "だったよ。";
			case 2: return content.getTarget() + "さんを占ったら、" + speciesToString(content.getResult()) + "だったよ。";
			case 3: return content.getTarget() + "さんを占った結果は、" + speciesToString(content.getResult()) + "だったよ。";
			case 4: return "昨日の占い結果だよ、" + content.getTarget() + "さんは" + speciesToString(content.getResult()) + "だったよ。";
			}
		case IDENTIFIED:
			return content.getTarget() + "さんの霊能結果は、" + speciesToString(content.getResult()) + "だったよ。";
		case OPERATOR:
			Content c = content.getContentList().get(0);
			if(c.getTopic() != Topic.VOTE)
				return Talk.SKIP;
			return c.getTarget() + "さんに投票してね。";
		case VOTE:
			return content.getTarget() + "さんに投票するよ。";
		default:
			return Talk.SKIP;
		}
	}

	private static int countCoMap(Map<Agent, Role> coMap, Role role) {
		int ret = 0;
		for(Role r: coMap.values()) {
			if(r == role)
				ret++;
		}
		return ret;
	}
	
	private static Set<Agent> coAgents(Map<Agent, Role> coMap, Role role) {
		Set<Agent> ret = new HashSet<>();
		for(Map.Entry<Agent, Role> entry: coMap.entrySet()) {
			if(entry.getValue() == role)
				ret.add(entry.getKey());
		}
		return ret;
	}

	private String skipTalk(GameInfo gameInfo, Map<Agent, Role> coMap, List<String> answers) {
		if(countCoMap(coMap, Role.WEREWOLF) > 0) { // PPモード 
			if(!talkedSet.contains("パワープレイ反応")){
				talkedSet.add("パワープレイ反応");
				if(gameInfo.getRole() == Role.WEREWOLF) { // 人狼
					return "食べちゃうぞー！";
				} else if(gameInfo.getRole() == Role.POSSESSED) { // 狂人
					return "うひゃひゃひゃひゃひゃひゃひゃ！";
				} else { // 村人チーム
					return "え！　助けて！";
				}
			}
			return Talk.SKIP;
		}

		// 共通反応
		if(gameInfo.getLastDeadAgentList().size() > 0) { // 襲撃死した人がいる場合
			if(!talkedSet.contains("襲撃反応")){
				talkedSet.add("襲撃反応");
				switch ((int)(Math.random() * 5)) {
				case 0: return "本当に襲われるなんて……。";
				case 1: return gameInfo.getLastDeadAgentList().get(0) + "さん……。";
				case 2: return "死んじゃった……。";
				}
			}
		}

		if(coMap.get(gameInfo.getAgent()) == Role.SEER) { // 占い師COしてるとき
			if(countCoMap(coMap, Role.SEER) == 2) { //二人COしているとき
				if(!talkedSet.contains("対抗占い師反応")){
					talkedSet.add("対抗占い師反応");
					Set<Agent> coSeers = coAgents(coMap, Role.SEER);
					coSeers.remove(gameInfo.getAgent());
					Agent t = (Agent)coSeers.toArray()[0];
					
					switch ((int)(Math.random() * 5)) {
					case 0: return t + "さんは嘘をついています！";
					case 1: return t + "さんは嘘つきです！";
					case 2: return ">>" + t + " " + t + "さん、あなたが人狼だったんですね！";
					}
				}
			}
		}
		
		// COしてない人
		if(countCoMap(coMap, Role.SEER) == 2) { //二人COしているとき
			if(!talkedSet.contains("二人占い師反応")){
				talkedSet.add("二人占い師反応");
				switch ((int)(Math.random() * 5)) {
				case 0: return "どっちが本当の占い師なんだろう……。";
				}
			}
		}
		
		for(String answer: answers) { //Earから渡されたAnswer
			if(!talkedSet.contains("answer:" + answer)){
				talkedSet.add("answer:" + answer);
				return answer;
			}
		}

		return chat(gameInfo, coMap);
	}

	private String chat(GameInfo gameInfo, Map<Agent, Role> coMap) {
		if(coMap.get(gameInfo.getAgent()) == Role.SEER) { //占い師COしているとき
			if(seerChats.size() < 1)
				return Talk.SKIP;

			if(Math.random() < CHAT_RATE) {
				String ret = seerChats.get(0);
				seerChats.remove(0);
				List<Agent> targets = new ArrayList<Agent>(gameInfo.getAliveAgentList());
				targets.remove(gameInfo.getAgent());
				int x = (int)(Math.random() * targets.size());
				return ret.replace("@", targets.get(x).toString());
			}
			return Talk.SKIP;
		}
		
		if(gameInfo.getAgentList().size() == gameInfo.getAliveAgentList().size()) { // まだだれも死んでない ⇒ posi
			if(posiChats.size() < 1)
				return Talk.SKIP;

			if(Math.random() < CHAT_RATE) {
				String ret = posiChats.get(0);
				posiChats.remove(0);
				List<Agent> targets = new ArrayList<Agent>(gameInfo.getAliveAgentList());
				targets.remove(gameInfo.getAgent());
				int x = (int)(Math.random() * targets.size());
				return ret.replace("@", targets.get(x).toString());
			}
			return Talk.SKIP;
		} else { // nega
			if(negaChats.size() < 1)
				return Talk.SKIP;

			if(Math.random() < CHAT_RATE) {
				String ret = negaChats.get(0);
				negaChats.remove(0);
				List<Agent> targets = new ArrayList<Agent>(gameInfo.getAliveAgentList());
				targets.remove(gameInfo.getAgent());
				int x = (int)(Math.random() * targets.size());
				return ret.replace("@", targets.get(x).toString());
			}
			return Talk.SKIP;
		}
	}

	private void loadChats(Random rnd) {
		negaChats = new LinkedList<>();
		posiChats = new LinkedList<>();
		seerChats = new LinkedList<>();

		try(BufferedReader br = new BufferedReader(new FileReader(PATH_NEGA_CHATS))) {
			String line = null;
			while((line = br.readLine()) != null) {
				negaChats.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		Collections.shuffle(negaChats, rnd);

		try(BufferedReader br = new BufferedReader(new FileReader(PATH_POSI_CHATS))) {
			String line = null;
			while((line = br.readLine()) != null) {
				posiChats.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		Collections.shuffle(posiChats, rnd);
		
		try(BufferedReader br = new BufferedReader(new FileReader(PATH_SEER_CHATS))) {
			String line = null;
			while((line = br.readLine()) != null) {
				seerChats.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		Collections.shuffle(seerChats, rnd);
	}

	public String toNaturalLanguageForWhisper(GameInfo gameInfo, Map<Agent, Role> coMap, String protocol) {		
		if(!Content.validate(protocol)) {
			System.err.println("Mouth: 内部エージェントがプロトコル以外を喋ってる -> " + protocol);
			return Talk.SKIP;
		}
		Content content = new Content(protocol);

		switch (content.getTopic()) {
		case OVER:
			return Talk.OVER;
		case SKIP:
			return Talk.SKIP;
		case COMINGOUT:
			if(content.getTarget().equals(gameInfo.getAgent()) && content.getRole() == Role.VILLAGER)
				return "僕は潜伏するよ。";
			return Talk.SKIP;
		case ATTACK:
			return content.getTarget() + "を襲撃するよ。"; 
		default:
			return Talk.SKIP;
		}
	}

	private static String roleToString(Role role) {
		switch (role) {
		case BODYGUARD:
			return "狩人";
		case MEDIUM:
			return "霊媒師";
		case POSSESSED:
			return "狂人";
		case SEER:
			return "占い師";
		case VILLAGER:
			return "村人";
		case WEREWOLF:
			return "人狼";
		default:
			return null;
		}
	}

	private static String speciesToString(Species species) {
		switch (species) {
		case HUMAN:
			return "人間";
		case WEREWOLF:
			return "人狼";
		default:
			return null;
		}
	}

}
