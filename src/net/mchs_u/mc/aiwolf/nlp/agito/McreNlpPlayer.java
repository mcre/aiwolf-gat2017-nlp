package net.mchs_u.mc.aiwolf.nlp.agito;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import net.mchs_u.mc.aiwolf.nlp.common.GameInfoTranslater;

public class McreNlpPlayer implements Player {
	private Player player = null;
	private GameInfo gameInfo = null;
	private Mouth mouth;
	private Ear ear;

	public McreNlpPlayer() {
		player = new net.mchs_u.mc.aiwolf.curry.McrePlayer();
		mouth = new Mouth();
		ear = new Ear();
	}
	
	public void update(GameInfo gameInfo) {
		GameInfo prGameInfo = new GameInfoTranslater(gameInfo, ear);
		player.update(prGameInfo);
	}
	
	public String talk() {
		String pr = player.talk();
		String nl = mouth.toNaturalLanguageForTalk(gameInfo, pr);
		System.out.println("☆ " + pr + " -> " + nl);
		return nl;
	}
	
	public String whisper() {
		String pr = player.whisper();
		String nl = mouth.toNaturalLanguageForWhisper(gameInfo, pr);
		System.out.println("★ " + pr + " -> " + nl);
		return nl;
	}
	
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		this.gameInfo = gameInfo;
		player.initialize(gameInfo, gameSetting);
	}
	
	public void dayStart() {
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
