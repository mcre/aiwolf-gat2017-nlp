package net.mchs_u.mc.aiwolf.nlp.agito;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import net.mchs_u.mc.aiwolf.nlp.common.ProtocolToNaturalLanguage;

public class Mouth implements ProtocolToNaturalLanguage {	
	public String toNaturalLanguageForTalk(GameInfo gameInfo, String protocol) {
		if(!Content.validate(protocol)) {
			System.err.println("Mouth: 内部エージェントがプロトコル以外を喋ってる -> " + protocol);
			return Talk.SKIP;
		}
		Content content = new Content(protocol);
		
		switch (content.getTopic()) {
		case OVER:
			return Talk.OVER;
		case SKIP:
			// TODO 雑談
			return Talk.SKIP;
		case COMINGOUT:
			if(!content.getTarget().equals(gameInfo.getAgent()))
				return Talk.SKIP;
			if(content.getRole() == Role.WEREWOLF)
				return "わおーん　僕は人狼だよ";
			return "僕は" + roleToString(content.getRole()) + "だよ";
		case DIVINED:
			return content.getTarget() + "さんを占ったら" + speciesToString(content.getResult()) + "だったよ";
		case IDENTIFIED:
			return content.getTarget() + "さんの霊能結果は" + speciesToString(content.getResult()) + "だったよ";
		case OPERATOR:
			Content c = content.getContentList().get(0);
			if(c.getTopic() != Topic.VOTE)
				return Talk.SKIP;
			return c.getTarget() + "さんに投票してください";
		case VOTE:
			// TODO 2回目以降は「やっぱり」をつけたい
			return "今日は" + content.getTarget() + "さんに投票しようかな";
		default:
			return Talk.SKIP;
		}
	}
	
	public String toNaturalLanguageForWhisper(GameInfo gameInfo, String protocol) {		
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
				return "僕は潜伏するよ";
			 return Talk.SKIP;
		case ATTACK:
			return content.getTarget() + "を襲撃するよ"; 
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
