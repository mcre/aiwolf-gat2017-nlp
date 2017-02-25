package net.mchs_u.mc.aiwolf.nlp.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;

public class GameInfoTranslater extends GameInfo {
	private GameInfo gameInfo = null;
	private NaturalLanguageToProtocol naturalLanguageToProtocol = null;

	public GameInfoTranslater(GameInfo gameInfo, NaturalLanguageToProtocol naturalLanguageToProtocol) {
		this.gameInfo = gameInfo;
		this.naturalLanguageToProtocol = naturalLanguageToProtocol;
	}
	
	public List<Talk> getTalkList() {
		List<Talk> ret = new ArrayList<>();
		for(Talk t: gameInfo.getTalkList())
			ret.add(new Talk(t.getIdx(), t.getDay(), t.getTurn(), t.getAgent(), naturalLanguageToProtocol.toProtocolForTalk(this, t.getText())));
		return ret;
	}
	
	public List<Talk> getWhisperList() {
		List<Talk> ret = new ArrayList<>();
		for(Talk t: gameInfo.getWhisperList())
			ret.add(new Talk(t.getIdx(), t.getDay(), t.getTurn(), t.getAgent(), naturalLanguageToProtocol.toProtocolForWhisper(this, t.getText())));
		return ret;
	}
	
	public boolean equals(Object obj) {
		return gameInfo.equals(obj);
	}

	public Agent getAgent() {
		return gameInfo.getAgent();
	}

	public List<Agent> getAgentList() {
		return gameInfo.getAgentList();
	}

	public List<Agent> getAliveAgentList() {
		return gameInfo.getAliveAgentList();
	}

	public List<Vote> getAttackVoteList() {
		return gameInfo.getAttackVoteList();
	}

	public Agent getAttackedAgent() {
		return gameInfo.getAttackedAgent();
	}

	public Agent getCursedFox() {
		return gameInfo.getCursedFox();
	}

	public int getDay() {
		return gameInfo.getDay();
	}

	public Judge getDivineResult() {
		return gameInfo.getDivineResult();
	}

	public Agent getExecutedAgent() {
		return gameInfo.getExecutedAgent();
	}

	public List<Role> getExistingRoles() {
		return gameInfo.getExistingRoles();
	}

	public Agent getGuardedAgent() {
		return gameInfo.getGuardedAgent();
	}

	public List<Agent> getLastDeadAgentList() {
		return gameInfo.getLastDeadAgentList();
	}

	public List<Vote> getLatestAttackVoteList() {
		return gameInfo.getLatestAttackVoteList();
	}

	public Agent getLatestExecutedAgent() {
		return gameInfo.getLatestExecutedAgent();
	}

	public List<Vote> getLatestVoteList() {
		return gameInfo.getLatestVoteList();
	}

	public Judge getMediumResult() {
		return gameInfo.getMediumResult();
	}

	public Map<Agent, Integer> getRemainTalkMap() {
		return gameInfo.getRemainTalkMap();
	}

	public Map<Agent, Integer> getRemainWhisperMap() {
		return gameInfo.getRemainWhisperMap();
	}

	public Role getRole() {
		return gameInfo.getRole();
	}

	public Map<Agent, Role> getRoleMap() {
		return gameInfo.getRoleMap();
	}

	public Map<Agent, Status> getStatusMap() {
		return gameInfo.getStatusMap();
	}

	public List<Vote> getVoteList() {
		return gameInfo.getVoteList();
	}

	public int hashCode() {
		return gameInfo.hashCode();
	}

	public String toString() {
		return gameInfo.toString();
	}

}
