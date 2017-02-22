package net.mchs_u.mc.aiwolf.nlp.starter;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Random;

import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.net.TcpipServer;
import org.aiwolf.server.util.FileGameLogger;
import org.aiwolf.server.util.GameLogger;
import org.aiwolf.server.util.MultiGameLogger;
import org.aiwolf.ui.GameViewer;
import org.aiwolf.ui.log.ContestResource;

public class ServerStarter {
	
	public ServerStarter(int port, int gameNum, boolean isVisualize) throws SocketTimeoutException, IOException {
		int playerNum = 5;
		GameSetting gameSetting = GameSetting.getDefaultGame(playerNum);
		
		TcpipServer gameServer = new TcpipServer(port, playerNum, gameSetting);
		gameServer.waitForConnection();
		AIWolfGame game = new AIWolfGame(gameSetting, gameServer);

		for(int i = 0; i < gameNum; i++){
			File logFile = new File("log/" + (new Date()).getTime() + ".txt"); 
			GameLogger logger = new FileGameLogger(logFile);
			if(isVisualize){
				ContestResource resource = new ContestResource(game);
				GameViewer gameViewer = new GameViewer(resource, game);
				gameViewer.setAutoClose(true);
				logger = new MultiGameLogger(logger, gameViewer);
			}
			game.setRand(new Random(i));
			game.setGameLogger(logger);
			game.start();
		}
	}
	
	public static void main(String[] args) throws SocketTimeoutException, IOException {
		new ServerStarter(10000, 100, true);
	}

}
