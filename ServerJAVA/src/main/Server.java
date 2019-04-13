package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	static ServerSocket socketserver;

	static Socket socketduserveur;

	static long server_tickrate = 20000;
	static Map<String, Vehicule> vehicules = new HashMap<>();
	static ArrayList<Obstacle> obstacles = new ArrayList<>();
	
	static double objectifX = Math.floor(Math.random() * 600);
	static double objectifY = Math.floor(Math.random() * 800);
	static int winCap = 100;
	static String coords = "";
	static ArrayList<Socket> socks = new ArrayList<>();
	static int index = 0;
	static double turnit = 0.01;
	static int thrustit = 100;


	public static void main(String[] zero) {
		try {
			socketserver = new ServerSocket(1664);
			ExecutorService pool = Executors.newFixedThreadPool(20);

			
			Tools.updateGame();
			Tools.startSession();
			Tools.createObstacles(5);
			
			while (true) {
				int i = 0;
				System.out.println("!!!!!!!");
				try {
					Socket s = socketserver.accept();
					socks.add(s);
					Work w = new Work(s, i);
					pool.submit(w);
					i++;
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		} catch (IOException e2) {
				e2.printStackTrace();
		}

	}

}
