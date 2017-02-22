package net.mchs_u.mc.aiwolf.nlp.agito;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import net.mchs_u.mc.aiwolf.nlp.common.GameInfoTranslater;
import net.mchs_u.mc.aiwolf.nlp.common.NaturalLanguageToProtocol;
import net.mchs_u.mc.aiwolf.nlp.common.ProtocolToNaturalLanguage;

public class McreNlpPlayer implements Player {
	private Player player;
	private ProtocolToNaturalLanguage mouth;
	private NaturalLanguageToProtocol ear;

	public McreNlpPlayer() {
		player = new net.mchs_u.mc.aiwolf.curry.McrePlayer();
		mouth = new Mouth();
		ear = new Ear();
	}
	
	public void update(GameInfo gameInfo) {
		player.update(new GameInfoTranslater(gameInfo, ear));
	}
	
	public String talk() {
		return mouth.toNaturalLanguage(player.talk());
	}
	
	public String whisper() {
		return mouth.toNaturalLanguage(player.whisper());
	}
	
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
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
