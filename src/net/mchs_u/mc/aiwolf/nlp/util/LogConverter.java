package net.mchs_u.mc.aiwolf.nlp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogConverter {
	private String convertedLog = null;

	public LogConverter(String filepath, List<String> names) throws FileNotFoundException, IOException {
		String day = "";
		String turn = "";
		StringBuffer sb = new StringBuffer();
		Map<String, String> roleMap = new HashMap<>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(filepath))) {
			String line = null;
			while((line = br.readLine()) != null) {
				String[] s = line.split(",");
				if(!day.equals(s[0])){
					day = s[0];
					sb.append("\n=========== " + day + "日目 ===========\n");
					turn = "";
				}
				
				switch (s[1]) {
				case "status":
					roleMap.put(s[2], s[3]);
					sb.append(doaToString(s[4]) + " " + roleToString(s[3]) + names.get(Integer.parseInt(s[2]) - 1) + "\n");	
					break;
				case "talk":
					if(!s[5].equals("Skip") && !s[5].equals("Over")) {
						if(!turn.equals(s[3])){
							turn = s[3];
							sb.append("---\n");
						}
						sb.append(roleToString(roleMap.get(s[4])) + names.get(Integer.parseInt(s[4]) - 1) + "\n   " + replaceAgentName(s[5], names) + "\n");
					}
					break;
				case "divine":
					sb.append("🔮 [" +
							roleToString(roleMap.get(s[2])) + names.get(Integer.parseInt(s[2]) - 1) + "]は[" + 
							roleToString(roleMap.get(s[3])) + names.get(Integer.parseInt(s[3]) - 1) + "]を占った。結果は【" + 
							speciesToString(s[4]) + "】だった！\n");
					break;
				case "vote":
					sb.append("📝 [" +
							roleToString(roleMap.get(s[2])) + names.get(Integer.parseInt(s[2]) - 1) + "]は[" + 
							roleToString(roleMap.get(s[3])) + names.get(Integer.parseInt(s[3]) - 1) + "]に投票した。\n");
					break;
				case "execute":
					sb.append("🔫 [" +
							roleToString(roleMap.get(s[2])) + names.get(Integer.parseInt(s[2]) - 1) + "]が処刑された💀！\n");
					break;
				case "attack":
					if(s[3].equals("true"))
						sb.append("👊 [" +
								roleToString(roleMap.get(s[2])) + names.get(Integer.parseInt(s[2]) - 1) + "]が人狼に襲撃された💀！\n");
					break;
				case "result":
					if(s[4].equals("WEREWOLF"))
						sb.append("---\n🎉 👹👅人狼チームの勝利！");
					else
						sb.append("---\n🎉 😁🔮😁村人チームの勝利！");
					break;
					
				default:
					break;
				}
			}
		}
		
		convertedLog = sb.toString();
	}
	
	private static String roleToString(String role) {
		switch (role) {
		case "POSSESSED":
			return "👅";
		case "SEER":
			return "🔮";
		case "VILLAGER":
			return "😁";
		case "WEREWOLF":
			return "👹";
		default:
			return null;
		}
	}
	
	private static String speciesToString(String species) {
		switch (species) {
		case "HUMAN":
			return "😁人間";
		case "WEREWOLF":
			return "👹人狼";
		default:
			return null;
		}
	}
	
	private static String doaToString(String doa) {
		switch (doa) {
		case "ALIVE":
			return "💖";
		case "DEAD":
			return "💀";
		default:
			return null;
		}
	}
	
	private static String replaceAgentName(String talk, List<String> names) {
		String ret = talk;
		ret = ret.replaceFirst("^>>Agent\\[..\\] ", "");
		for(int i = 0; i < names.size(); i++) {
			ret = ret.replace("Agent[0" + (i + 1) + "]", names.get(i));
		}
		return ret;
	}

	@Override
	public String toString() {
		return convertedLog;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		List<String> names = new ArrayList<>();
		names.add("アンパンマン");
		names.add("ばいきんまん");
		names.add("カレーパンマン");
		names.add("ドキンちゃん");
		names.add("しょくぱんまん");
		File logDir = new File("log");
		String[] files = logDir.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				if(name.length() != 17)
					return false;
				return name.compareTo("1488817914283.txt") > 0;
			}
		});
		Arrays.sort(files);
		for(String file: files) {
			String s = (new LogConverter(logDir + "/" + file, names)).toString();
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(logDir + "/" + file.substring(0, 13) + "_c.txt"))) {
				bw.write(s);
			}
		}
		
	}

}
