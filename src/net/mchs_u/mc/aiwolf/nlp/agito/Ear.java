package net.mchs_u.mc.aiwolf.nlp.agito;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mychaelstyle.nlp.KNP;

import net.mchs_u.mc.aiwolf.nlp.common.NaturalLanguageToProtocol;

public class Ear implements NaturalLanguageToProtocol{
	private static final String DAT_FILE = "dic/translatedMap.dat";

	private KNP knp = null;
	private Map<String, String> translatedMap = null; 

	public Ear() {
		knp = new KNP();
		translatedMap = load();
	}

	public String toProtocol(GameInfo gameInfo, String naturalLanguage) {		
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
			nl = hankakuToZenkaku(nl);
			ObjectNode json = knp.parse(nl);
			
			System.out.println(json);
			
			// 役職カミングアウト
			// 占い結果
			// 霊能結果 // ５人人狼では不要
			// 投票依頼（誰からでもいい）

		} catch(Exception e) {
			e.printStackTrace();
			ret = Talk.SKIP;
		}
		
		translatedMap.put(naturalLanguage, ret);
		return ret;
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
