package net.mchs_u.mc.aiwolf.nlp.agito;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.mychaelstyle.nlp.KNP;

import net.mchs_u.mc.aiwolf.nlp.common.NaturalLanguageToProtocol;

// TODO 5人人狼以外も考慮する場合
// 霊能結果認識の実装
// Whisperの実装
// TODO １発言で２内容来た場合片方しか対応できない（占COと同時に占い結果言う場合とか）
public class Ear implements NaturalLanguageToProtocol{
	private static final String DAT_FILE = "dic/translatedMap.dat";

	private KNP knp = null;
	private Map<String, String> translatedMap = null; 

	public Ear() {
		knp = new KNP();
		translatedMap = load();
	}
	
	// TODO もっときれいに認識できないか？
	// TODO コードが長すぎて見にくい
	// TODO 占いかCOかは、対象「ガ・ヲ」が自分か、そうじゃないかで分けるのが良さそう。過去形使ってもいいけど
	// TODO 解析結果にもっとヒントが隠れてないか
	// TODO いろんな言い回しでうまく変換できるかを確認したい

	public String toProtocolForTalk(GameInfo gameInfo, Agent talker, String naturalLanguage) {
		if(translatedMap.containsKey(naturalLanguage)) {
			return translatedMap.get(naturalLanguage);
		} else if(naturalLanguage.contains(Talk.SKIP)) {
			translatedMap.put(naturalLanguage, Talk.SKIP);
			return Talk.SKIP;
		} else if(naturalLanguage.contains(Talk.OVER)) {
			translatedMap.put(naturalLanguage, Talk.OVER);
			return Talk.OVER;
		}
		
		String ret = Talk.SKIP;
		
		try {
			String nl = naturalLanguage;
			nl.replaceFirst("^>>Agent\\[..\\] ", "");
			nl = hankakuToZenkaku(nl);
			JsonNode clauseas = knp.parse(nl).get("clauseas");
			
			if(clauseas.toString().contains("人狼知能,役職")) { // 役職CO・占い結果 の処理
				for(JsonNode clausea: getTaggedClauseas(clauseas, "人狼知能,役職")) {
					Set<String> atts = new HashSet<>();
					for(JsonNode att: clausea.get("attributes"))
						atts.add(att.asText());
					
					if(atts.contains("状態述語") && atts.contains("時制-過去") && !atts.contains("否定表現")) {
						Agent target = getAgent(gameInfo, talker, getKakukankei(clausea, 'ガ'));							
						if(target != null) {
							Species result = null;
							if(clausea.toString().contains("人狼知能,役職,人狼"))
								result = Species.WEREWOLF;
							else if(clausea.toString().contains("人狼知能,役職,人間"))
								result = Species.HUMAN;
							ret = new Content(new DivinedResultContentBuilder(target, result)).getText();	
						}
					} else if(atts.contains("状態述語") && !atts.contains("否定表現")) {
						Agent target = getAgent(gameInfo, talker, getKakukankei(clausea, 'ガ'));							
						if(target != null) {
							Role role = null;
							if(clausea.toString().contains("人狼知能,役職,占い師"))
								role = Role.SEER;
							else if(clausea.toString().contains("人狼知能,役職,人狼"))
								role = Role.WEREWOLF;
							else if(clausea.toString().contains("人狼知能,役職,狂人"))
								role = Role.POSSESSED;
							else if(clausea.toString().contains("人狼知能,役職,人間"))
								role = Role.VILLAGER;
							ret = new Content(new ComingoutContentBuilder(target, role)).getText();	
						}
					}
				}
			} else if(clauseas.toString().contains("人狼知能,行為,投票")) { // 投票依頼 の処理
				for(JsonNode clausea: getTaggedClauseas(clauseas, "人狼知能,行為,投票")) {
					Set<String> atts = new HashSet<>();
					for(JsonNode att: clausea.get("attributes"))
						atts.add(att.asText());
					if(atts.contains("モダリティ-依頼Ａ") && !atts.contains("否定表現")) {
						Agent target = getAgent(gameInfo, talker, getKakukankei(clausea, 'ニ'));							
						if(target != null)	
							ret = new Content(new RequestContentBuilder(null, new Content(new VoteContentBuilder(target)))).getText();
					}
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
			ret = Talk.SKIP;
		}
		
		translatedMap.put(naturalLanguage, ret);
		return ret;
	}
	
	public String toProtocolForWhisper(GameInfo gameInfo, Agent talker, String naturalLanguage) {		
		return Talk.SKIP;
	}
	
	private static boolean isFirstPersonPronoun(String s) {
		KNP knp = new KNP();
		try {
			return knp.parse(s).toString().contains("一人称");
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}
	
	private static Agent getAgent(GameInfo gameInfo, Agent talker, String zenkaku) {
		if(zenkaku == null)
			return null;
		
		if(isFirstPersonPronoun(zenkaku))
			return talker;
		
		Agent agent = null;
		switch (zenkaku) {
		case "Ａｇｅｎｔ［０１］": agent = gameInfo.getAgentList().get(0); break;
		case "Ａｇｅｎｔ［０２］": agent = gameInfo.getAgentList().get(1); break;
		case "Ａｇｅｎｔ［０３］": agent = gameInfo.getAgentList().get(2); break;
		case "Ａｇｅｎｔ［０４］": agent = gameInfo.getAgentList().get(3); break;
		case "Ａｇｅｎｔ［０５］": agent = gameInfo.getAgentList().get(4); break;
		default: break;
		}
		
		return agent;
	}
	
	private static List<JsonNode> getTaggedClauseas(JsonNode clauseas, String tag) {
		List<JsonNode> ret = new ArrayList<>();
		Iterator<JsonNode> clauseasIterator =  clauseas.elements();
		
		while(clauseasIterator.hasNext()) {		
			JsonNode clausea = clauseasIterator.next();
			if(clausea.toString().contains(tag)) {
				ret.add(clausea);
			}
		}
		return ret;
	}
	
	// 手抜き
	private static String getKakukankei(JsonNode clausea, char teniwoha) {
		String regex = "\"格関係.\":\"" + teniwoha + ":(.*?)\"";
		Matcher m = Pattern.compile(regex).matcher(clausea.toString());
		if(m.find())
			return m.group(1);
		return null;
	}

	// juman辞書に半角文字登録できなさそうなので
	private static String hankakuToZenkaku(String value) {
		StringBuilder sb = new StringBuilder(value);
		for (int i = 0; i < sb.length(); i++) {
			int c = (int) sb.charAt(i);
			if ((c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x5A) || (c >= 0x61 && c <= 0x7A))
				sb.setCharAt(i, (char) (c + 0xFEE0));
			if (c == '[')
				sb.setCharAt(i, '［');
			if (c == ']')
				sb.setCharAt(i, '］');
			if (c == ' ')
				sb.setCharAt(i, '、');
			if (c == '　')
				sb.setCharAt(i, '、');
			
		}
		value = sb.toString();
		return value;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> load() {
		Map<String, String> ret = null;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(DAT_FILE));
			ret = (Map<String, String>)ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			ret = new HashMap<>();
		} finally {
			try { ois.close(); } catch (Exception e) {}
		}
		return ret;
	}

	private static void save(Map<String, String> map) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(DAT_FILE));
			oos.writeObject(map);
		} catch (IOException e) {
		} finally {
			try { oos.close(); } catch (Exception e) {}
		}
	}

	public void save() {
		save(translatedMap);
	}
}
