package net.mchs_u.mc.aiwolf.nlp.common;

import org.aiwolf.common.net.GameInfo;

public interface NaturalLanguageToProtocol {
	public String toProtocolForTalk(GameInfo gameInfo, String naturalLanguage);
	public String toProtocolForWhisper(GameInfo gameInfo, String naturalLanguage);
}
