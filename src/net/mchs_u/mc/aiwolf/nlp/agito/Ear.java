package net.mchs_u.mc.aiwolf.nlp.agito;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
// TODO １発言で２内容来た場合片方しか対応できない（占COと同時に占い結果言う場合とか）。2文も多分うまくいかないと思う。
public class Ear{
	private static final String DAT_FILE = "dic/translatedMap.dat";

	private Map<String, String> translatedMap = null; 
	private Map<String, String> qas = null; // Mouthに渡すQA集

	public Ear() {
		translatedMap = load();
	}
	
	public void initialize(GameInfo gameInfo) {
	}
	
	public void dayStart() {
		qas = new HashMap<>();
	}
	
	public String toProtocolForTalk(GameInfo gameInfo, Agent talker, String naturalLanguage) {
		String key = talker + ":" + naturalLanguage;
		String ret = Talk.SKIP;
		try {			
			Agent questionTo = null;
			if(naturalLanguage.startsWith(">>Agent["))
				questionTo = Agent.getAgent(Integer.parseInt(naturalLanguage.substring(8, 10)));
			
			if(translatedMap.containsKey(key)) { // 履歴にある場合
				if(questionTo != gameInfo.getAgent() || qas.containsKey(key)) // 自分宛ての問いかけでない場合か、QA履歴にある場合
					return translatedMap.get(key); // 履歴から返す
			} else if(naturalLanguage.contains(Talk.SKIP)) {
				translatedMap.put(key, Talk.SKIP);
				return Talk.SKIP;
			} else if(naturalLanguage.contains(Talk.OVER)) {
				translatedMap.put(key, Talk.OVER);
				return Talk.OVER;
			}
			
			String nl = naturalLanguage;
			
			nl = nl.replaceFirst("^>>Agent\\[..\\] ", "");
			nl = hankakuToZenkaku(nl);
			
			Content content = talkToContent(gameInfo, talker, questionTo, key, Clause.createClauses(nl));
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
	
	private Content talkToContent(GameInfo gameInfo, Agent talker, Agent questionTo, String key, List<Clause> clauses) {
		Clause roleClause   = Clause.findAiwolfTypeClause(clauses, "役職");
		Clause roleCoClause = Clause.findAiwolfTypeClause(clauses, "役職CO");
		Clause actionClause = Clause.findAiwolfTypeClause(clauses, "行為");
		Clause playerClause = Clause.findAiwolfTypeClause(clauses, "プレイヤー");
		Clause tmp = null;
		
		if(playerClause != null && roleClause != null && actionClause != null) {
			// ☆占い結果「Agent[04]さんを占ったら人狼でした」「昨日の占い結果です。Agent[04]さんは人狼でした」
			// 一文に、プレイヤー、占い、人＊でした、があれば占い結果とする
			if(
					actionClause.getAiwolfWordMeaning().equals("占い") &&
					roleClause.getAiwolfWordMeaning().startsWith("人") &&
					roleClause.getAttributes().contains("状態述語") &&
					!actionClause.isNegative() && !roleClause.isNegative()) {
				Agent target = Agent.getAgent(Integer.parseInt(playerClause.getAiwolfWordMeaning()));
				switch (roleClause.getAiwolfWordMeaning()) {
				case "人狼":		return new Content(new DivinedResultContentBuilder(target, Species.WEREWOLF));
				case "人間":		return new Content(new DivinedResultContentBuilder(target, Species.HUMAN));
				}
			}
		}
		
		if(roleClause != null && !roleClause.isNegative()) {				
			// ☆役職CO「私は占い師です」
			tmp = roleClause.getKakuMap().get("ガ");
			if(tmp != null && tmp.getAttributes().contains("一人称")) {
				switch (roleClause.getAiwolfWordMeaning()) {
				case "占い師":	return new Content(new ComingoutContentBuilder(talker, Role.SEER));
				case "人狼":		return new Content(new ComingoutContentBuilder(talker, Role.WEREWOLF));
				case "狂人":		return new Content(new ComingoutContentBuilder(talker, Role.POSSESSED));
				case "人間":		return new Content(new ComingoutContentBuilder(talker, Role.VILLAGER));
				}
			}
			
			// ☆占い結果「Agent[04]さんは人狼です」
			tmp = roleClause.getKakuMap().get("ガ");
			Clause child = roleClause.getChild();
			if(tmp != null && tmp.getAiwolfWordType() != null && 
					tmp.getAiwolfWordType().equals("プレイヤー") && 
					!roleClause.getModalities().contains("疑問") &&
					!(child != null && child.getMain().equals("思う"))) { // 「人狼だと思う」の回避
				Agent target = Agent.getAgent(Integer.parseInt(tmp.getAiwolfWordMeaning()));
				switch (roleClause.getAiwolfWordMeaning()) {
				case "人狼":		return new Content(new DivinedResultContentBuilder(target, Species.WEREWOLF));
				case "人間":		return new Content(new DivinedResultContentBuilder(target, Species.HUMAN));
				}
			}
		}
			
		if(roleCoClause != null && !roleCoClause.isNegative()) {
			// ☆役職CO「占い師COします」
			if(roleCoClause.getAttributes().contains("動態述語")) {
				switch (roleCoClause.getAiwolfWordMeaning()) {
				case "占い師":	return new Content(new ComingoutContentBuilder(talker, Role.SEER));
				case "人狼":		return new Content(new ComingoutContentBuilder(talker, Role.WEREWOLF));
				case "狂人":		return new Content(new ComingoutContentBuilder(talker, Role.POSSESSED));
				case "人間":		return new Content(new ComingoutContentBuilder(talker, Role.VILLAGER));
				}
			}
		}
			
		if(actionClause != null && !actionClause.isNegative()) {
			if(actionClause.getAiwolfWordMeaning().equals("投票")) {
				// ☆投票依頼「Agent[04]さんに投票してください」
				Set<String> m = actionClause.getModalities();
				if((m.contains("依頼Ａ") || m.contains("勧誘")) && !m.contains("意志")) {
					int agentId = -1;
					tmp = actionClause.getKakuMap().get("ニ");
					if(tmp != null && tmp.getAiwolfWordType().equals("プレイヤー"))
						agentId = Integer.parseInt(tmp.getAiwolfWordMeaning());
					
					tmp = actionClause.getKakuMap().get("ヲ");
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
		
		if(questionTo == gameInfo.getAgent()) { // 自分宛て問いかけの場合
			if(
					Clause.findModalityClause(clauses, "勧誘") != null || // 一緒に遊ぼうよ。, 今日はAgent[01]さんに投票しましょうよ
					Clause.findModalityClause(clauses, "意志") != null || // 今日はAgent[01]さんに投票しましょう
					Clause.findModalityClause(clauses, "依頼Ａ") != null) { // 今日はAgent[01]さんに投票してください
				qas.put(key, ">>" + talker + " " + talker + "さん、うーん……。");
			}
			
			if(Clause.findModalityClause(clauses, "疑問") != null) {
				if(roleClause != null && roleClause.getAiwolfWordMeaning().equals("人狼")) {
					String main = roleClause.getKakuMap().get("ガ").getMain();
					if(Clause.findMainClause(clauses, "誰") != null) { // 誰が人狼だと思う？
						qas.put(key, ">>" + talker + " " + talker + "さん、僕は#さんが怪しいと思うよ。");
					} else if(main.equals("君") || main.equals("あなた") || main.equals("御前")) { // あなたが人狼なんでしょう？, あなたが人狼なんですか！？
						qas.put(key, ">>" + talker + " " + talker + "さん、僕は人狼じゃないよ。");
					} else {
						qas.put(key, ">>" + talker + " " + talker + "さん、ちょっとわからないなあ。");
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
	
	public Collection<String> getAnswers() {
		return qas.values();
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
