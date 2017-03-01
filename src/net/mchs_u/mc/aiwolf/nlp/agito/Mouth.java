package net.mchs_u.mc.aiwolf.nlp.agito;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

public class Mouth {
	private final static String PATH_CHATS = "dic/chats.txt";
	private final static double CHAT_RATE = 0.3d; // 話題がないときにチャットリストを消費する確率
	
	private List<String> chats = null;
	private boolean todayVoteDeclared = false;
	private int day = Integer.MAX_VALUE;

	public String toNaturalLanguageForTalk(GameInfo gameInfo, Map<Agent, Role> coMap, String protocol) {
		if(!Content.validate(protocol)) {
			System.err.println("Mouth: 内部エージェントがプロトコル以外を喋ってる -> " + protocol);
			return Talk.SKIP;
		}
		Content content = new Content(protocol);
		
		if(gameInfo.getDay() != day) { //日付変わった
			if(gameInfo.getDay() < day) { //ニューゲーム
				loadChats(new Random((new Date()).getTime() + gameInfo.getAgent().getAgentIdx() * 222));
			}
			todayVoteDeclared = false;
			day = gameInfo.getDay();
		}
		
		switch (content.getTopic()) {
		case OVER:
			return Talk.OVER;
		case SKIP:
			String chat = createChat(gameInfo);
			if(chat != null)
				return chat;
			return Talk.SKIP;
		case COMINGOUT:
			if(!content.getTarget().equals(gameInfo.getAgent()))
				return Talk.SKIP;
			if(content.getRole() == Role.WEREWOLF)
				return "わおーん、僕は人狼だよ。";
			return "僕は" + roleToString(content.getRole()) + "だよ。";
		case DIVINED:
			switch ((int)(Math.random() * 6)) {
			case 0: return content.getTarget() + "さんの占い結果は、" + speciesToString(content.getResult()) + "だったよ。";
			case 1: return content.getTarget() + "さんの占いの結果は、" + speciesToString(content.getResult()) + "だったよ。";
			case 2: return content.getTarget() + "さんを占ったら、" + speciesToString(content.getResult()) + "だったよ。";
			case 3: return content.getTarget() + "さんを占った結果は、" + speciesToString(content.getResult()) + "だったよ。";
			case 4: return content.getTarget() + "さんは" + speciesToString(content.getResult()) + "だったよ。";
			case 5: return "昨日の占い結果だよ、" + content.getTarget() + "さんは" + speciesToString(content.getResult()) + "だったよ。";
			}
		case IDENTIFIED:
			return content.getTarget() + "さんの霊能結果は、" + speciesToString(content.getResult()) + "だったよ。";
		case OPERATOR:
			Content c = content.getContentList().get(0);
			if(c.getTopic() != Topic.VOTE)
				return Talk.SKIP;
			return c.getTarget() + "さんに投票してね。";
		case VOTE:
			if(!todayVoteDeclared)
				return "今日は" + content.getTarget() + "さんに投票しようかな。";
			else
				return "やっぱり" + content.getTarget() + "さんに投票しようかな。";
		default:
			return Talk.SKIP;
		}
	}
	
	private void loadChats(Random rnd) {
		chats = new LinkedList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(PATH_CHATS))) {
			String line = null;
			while((line = br.readLine()) != null) {
				chats.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		Collections.shuffle(chats, rnd);
	}
	
	private String createChat(GameInfo gameInfo) {
		if(chats.size() < 1)
			return null;
		
		if(Math.random() < CHAT_RATE) {
			String ret = chats.get(0);
			chats.remove(0);
			List<Agent> targets = new ArrayList<Agent>(gameInfo.getAgentList());
			targets.remove(gameInfo.getAgent());
			int x = (int)(Math.random() * targets.size());
			return ret.replace("@", targets.get(x).toString());
		}
		return null;
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
