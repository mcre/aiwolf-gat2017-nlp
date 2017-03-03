package net.mchs_u.mc.aiwolf.nlp.agito;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

// TODO 5人人狼以外も考慮する場合
// 霊能結果認識の実装
// Whisperの実装
// TODO １発言で２内容来た場合片方しか対応できない（占COと同時に占い結果言う場合とか）
public class Ear{
	private static final String DAT_FILE = "dic/translatedMap.dat";

	private Map<String, String> translatedMap = null; 

	public Ear() {
		translatedMap = load();
	}
	
	public String toProtocolForTalk(GameInfo gameInfo, Agent talker, String naturalLanguage) {
		String key = talker + ":" + naturalLanguage;
		
		if(translatedMap.containsKey(key)) {
			return translatedMap.get(key);
		} else if(naturalLanguage.contains(Talk.SKIP)) {
			translatedMap.put(key, Talk.SKIP);
			return Talk.SKIP;
		} else if(naturalLanguage.contains(Talk.OVER)) {
			translatedMap.put(key, Talk.OVER);
			return Talk.OVER;
		}
		
		String ret = Talk.SKIP;
		
		try {
			String nl = naturalLanguage;
			
			nl.replaceFirst("^>>Agent\\[..\\] ", "");
			nl = hankakuToZenkaku(nl);
			
			Content content = talkToContent(talker, Clausea.createClauseas(nl));
			if(content == null)
				ret = Talk.SKIP;
			else
				ret = content.getText();
		} catch(Exception e) {
			e.printStackTrace();
			ret = Talk.SKIP;
		}
		
		translatedMap.put(key, ret);
		return ret;
	}
	
	private Content talkToContent(Agent talker, List<Clausea> clauseas) {
		Clausea roleClausea   = Clausea.findAiwolfTypeClausea(clauseas, "役職");
		Clausea roleCoClausea = Clausea.findAiwolfTypeClausea(clauseas, "役職CO");
		Clausea actionClausea = Clausea.findAiwolfTypeClausea(clauseas, "行為");
		Clausea playerClausea = Clausea.findAiwolfTypeClausea(clauseas, "プレイヤー");
		Clausea tmp = null;
		
		if(playerClausea != null && roleClausea != null && actionClausea != null) {
			// ☆占い結果「Agent[04]さんを占ったら人狼でした」「昨日の占い結果です。Agent[04]さんは人狼でした」
			// 一文に、プレイヤー、占い、人＊でした、があれば占い結果とする
			if(
					actionClausea.getAiwolfWordMeaning().equals("占い") &&
					roleClausea.getAiwolfWordMeaning().startsWith("人") &&
					roleClausea.getAttributes().contains("状態述語") &&
					!actionClausea.isNegative() && !roleClausea.isNegative()) {
				Agent target = Agent.getAgent(Integer.parseInt(playerClausea.getAiwolfWordMeaning()));
				switch (roleClausea.getAiwolfWordMeaning()) {
				case "人狼":		return new Content(new DivinedResultContentBuilder(target, Species.WEREWOLF));
				case "人間":		return new Content(new DivinedResultContentBuilder(target, Species.HUMAN));
				}
			}
		}
		
		if(roleClausea != null && !roleClausea.isNegative()) {				
			// ☆役職CO「私は占い師です」
			tmp = roleClausea.getKakuMap().get("ガ");
			if(tmp != null && tmp.getAttributes().contains("一人称")) {
				switch (roleClausea.getAiwolfWordMeaning()) {
				case "占い師":	return new Content(new ComingoutContentBuilder(talker, Role.SEER));
				case "人狼":		return new Content(new ComingoutContentBuilder(talker, Role.WEREWOLF));
				case "狂人":		return new Content(new ComingoutContentBuilder(talker, Role.POSSESSED));
				case "人間":		return new Content(new ComingoutContentBuilder(talker, Role.VILLAGER));
				}
			}
			
			// ☆占い結果「Agent[04]さんは人狼です」
			tmp = roleClausea.getKakuMap().get("ガ");
			if(tmp != null && tmp.getAiwolfWordType() != null && tmp.getAiwolfWordType().equals("プレイヤー") && !roleClausea.getAttributes().contains("モダリティ-疑問")) {
				Agent target = Agent.getAgent(Integer.parseInt(tmp.getAiwolfWordMeaning()));
				switch (roleClausea.getAiwolfWordMeaning()) {
				case "人狼":		return new Content(new DivinedResultContentBuilder(target, Species.WEREWOLF));
				case "人間":		return new Content(new DivinedResultContentBuilder(target, Species.HUMAN));
				}
			}
		}
			
		if(roleCoClausea != null && !roleCoClausea.isNegative()) {
			// ☆役職CO「占い師COします」
			if(roleCoClausea.getAttributes().contains("動態述語")) {
				switch (roleCoClausea.getAiwolfWordMeaning()) {
				case "占い師":	return new Content(new ComingoutContentBuilder(talker, Role.SEER));
				case "人狼":		return new Content(new ComingoutContentBuilder(talker, Role.WEREWOLF));
				case "狂人":		return new Content(new ComingoutContentBuilder(talker, Role.POSSESSED));
				case "人間":		return new Content(new ComingoutContentBuilder(talker, Role.VILLAGER));
				}
			}
		}
			
		if(actionClausea != null && !actionClausea.isNegative()) {
			if(actionClausea.getAiwolfWordMeaning().equals("投票")) {
				// ☆投票依頼「Agent[04]さんに投票してください」
				if(actionClausea.getAttributes().contains("モダリティ-依頼Ａ")) {
					int agentId = -1;
					tmp = actionClausea.getKakuMap().get("ニ");
					if(tmp != null && tmp.getAiwolfWordType().equals("プレイヤー"))
						agentId = Integer.parseInt(tmp.getAiwolfWordMeaning());
					
					tmp = actionClausea.getKakuMap().get("ヲ");
					if(tmp != null && tmp.getAiwolfWordType().equals("プレイヤー"))
						agentId = Integer.parseInt(tmp.getAiwolfWordMeaning());
						
					if(agentId >= 0) {
						Agent target = Agent.getAgent(agentId);
						if(target != null)	
							return new Content(new RequestContentBuilder(null, new Content(new VoteContentBuilder(target))));
					}
				}	
			}
		}
		
		return null;
	}
	
	public String toProtocolForWhisper(GameInfo gameInfo, Agent talker, String naturalLanguage) {		
		return Talk.SKIP;
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
