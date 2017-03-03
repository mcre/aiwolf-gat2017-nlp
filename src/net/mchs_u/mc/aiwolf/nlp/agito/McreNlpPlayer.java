package net.mchs_u.mc.aiwolf.nlp.agito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class McreNlpPlayer implements Player {
	private Player player = null;
	private GameInfo gameInfo = null;
	private Mouth mouth;
	private Ear ear;
	private int listHead; // トークをどこまでprint/処理したかの管理
	
	private Map<Agent, Role> coMap = null;

	public McreNlpPlayer() {
		player = new net.mchs_u.mc.aiwolf.curry.McrePlayer();
		ear = new Ear();
		mouth = new Mouth();
	}
	
	public void update(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
		GameInfo prGameInfo = new GameInfoTranslater(gameInfo, ear);
		player.update(prGameInfo);
		
		List<Talk> talkList = gameInfo.getTalkList();
		List<Talk> prTalkList = prGameInfo.getTalkList();
		
		for(int i = listHead; i < talkList.size(); i++){
			System.out.println("　○log : " + gameInfo.getAgent() + " " + getName() + "\t" + talkList.get(i) + " ( -> " + prTalkList.get(i).getText() + " ) ");
			Content c = new Content(prTalkList.get(i).getText());
			if(c.getTopic() == Topic.COMINGOUT)
				coMap.put(c.getTarget(), c.getRole());
			listHead++;
		}
	}
	
	public String talk() {
		String pr = Talk.SKIP;
		if(gameInfo.getDay() > 0)
			pr = player.talk(); // 0日目はプロトコル版のtalkを呼ばない
		String nl = mouth.toNaturalLanguageForTalk(gameInfo, coMap, pr);
		System.out.println("　●talk: " + gameInfo.getAgent() + " " + getName() + "\t" + nl + " ( <- " + pr + " ) ");
		return nl;
	}
	
	public String whisper() {
		String pr = player.whisper();
		String nl = mouth.toNaturalLanguageForWhisper(gameInfo, coMap, pr);
		System.out.println("　●whis: " + gameInfo.getAgent() + " " + getName() + "\t" + nl + " ( <- " + pr + " ) ");
		return nl;
	}
	
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		coMap = new HashMap<>();
		player.initialize(gameInfo, gameSetting);
	}
	
	public void dayStart() {
		listHead = 0;
		player.dayStart();
	}

	public Agent attack() {
		return player.attack();
	}

	public Agent divine() {
		return player.divine();
	}

	public void finish() {
		ear.save();
		player.finish();
	}

	public String getName() {
		return player.getName();
	}

	public Agent guard() {
		return player.guard();
	}

	public Agent vote() {
		return player.vote();
	}

}
