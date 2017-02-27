package net.mchs_u.mc.aiwolf.nlp.starter;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Random;

import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.net.TcpipClient;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.net.TcpipServer;
import org.aiwolf.server.util.FileGameLogger;

import net.mchs_u.mc.aiwolf.nlp.agito.McreNlpPlayer;

public class Starter {

	public static void startServer(int port, int gameNum) throws SocketTimeoutException, IOException {
		int playerNum = 5;
		GameSetting gameSetting = GameSetting.getDefaultGame(playerNum);
		gameSetting.setValidateUtterance(false);
		gameSetting.setTimeLimit(5000);
		
        new Thread() {
            public void run() {
            	try {
            		TcpipServer gameServer = new TcpipServer(port, playerNum, gameSetting);
            		gameServer.waitForConnection();
            		AIWolfGame game = new AIWolfGame(gameSetting, gameServer);

            		for(int i = 0; i < gameNum; i++){
            			game.setRand(new Random(i));
            			game.setGameLogger(new FileGameLogger(new File("log/" + (new Date()).getTime() + ".txt")));
            			game.start();
            		}
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }.start();
	}
	
	public static void startClient(String host, int port, int num) throws InstantiationException, IllegalAccessException {
		for(int i = 0; i < num; i++) {
			TcpipClient client = new TcpipClient(host, port);
			client.connect(new McreNlpPlayer());
			client.setName("m_cre");
		}
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SocketTimeoutException, IOException  {
        startServer(10000, 1);
		startClient("localhost", 10000, 5);
	}

}
