package net.mchs_u.mc.aiwolf.nlp.common;

import org.aiwolf.common.net.GameInfo;

public interface ProtocolToNaturalLanguage {
	public String toNaturalLanguageForTalk(GameInfo gameInfo, String protocol);
	public String toNaturalLanguageForWhisper(GameInfo gameInfo, String protocol);
}
