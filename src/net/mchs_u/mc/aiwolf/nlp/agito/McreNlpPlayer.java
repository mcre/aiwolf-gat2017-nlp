package net.mchs_u.mc.aiwolf.nlp.agito;

import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class McreNlpPlayer implements Player {
	private Player player = null;
	private GameInfo gameInfo = null;
	private Mouth mouth;
	private Ear ear;
	private int printHead; // トークをどこまでprintしたかの管理

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
		
		for(int i = printHead; i < talkList.size(); i++){
			System.out.println("　○log : " + gameInfo.getAgent() + " " + getName() + "\t" + talkList.get(i) + " ( -> " + prTalkList.get(i).getText() + " ) ");
			
			printHead++;
		}
	}
	
	public String talk() {
		String pr = player.talk();
		String nl = mouth.toNaturalLanguageForTalk(gameInfo, pr);
		System.out.println("　●talk: " + gameInfo.getAgent() + " " + getName() + "\t" + nl + " ( <- " + pr + " ) ");
		return nl;
	}
	
	public String whisper() {
		String pr = player.whisper();
		String nl = mouth.toNaturalLanguageForWhisper(gameInfo, pr);
		System.out.println("　●whis: " + gameInfo.getAgent() + " " + getName() + "\t" + nl + " ( <- " + pr + " ) ");
		return nl;
	}
	
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		this.gameInfo = gameInfo;
		player.initialize(gameInfo, gameSetting);
	}
	
	public void dayStart() {
		printHead = 0;
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
