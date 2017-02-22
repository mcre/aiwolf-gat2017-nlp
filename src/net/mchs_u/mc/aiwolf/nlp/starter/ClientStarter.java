package net.mchs_u.mc.aiwolf.nlp.starter;

import org.aiwolf.common.net.TcpipClient;

import net.mchs_u.mc.aiwolf.nlp.agito.McreNlpPlayer;


public class ClientStarter {
	
	public ClientStarter(String host, int port, int num) throws InstantiationException, IllegalAccessException {
		for(int i = 0; i < num; i++) {
			TcpipClient client = new TcpipClient(host, port);
			client.connect(new McreNlpPlayer());
			client.setName("m_cre" + (i == 0 ? "" : "_" + i));
		}
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException  {
		//new ClientStarter("kanolab.net", 10000, 1);
		//new ClientStarter("kanolab.net", 10000, 5);
		
		//new ClientStarter("localhost", 10000, 1);
		new ClientStarter("localhost", 10000, 5);
	}
}
