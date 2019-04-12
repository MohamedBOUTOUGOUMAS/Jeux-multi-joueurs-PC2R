package main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	static ServerSocket socketserver;

	static Socket socketduserveur;

	static long server_tickRate = 20;
	static Map<String, Vehicule> vehicules = new HashMap<>();
	static double objectifX = Math.floor(Math.random() * 600);
	static double objectifY = Math.floor(Math.random() * 800);
	static int winCap = 100;
	static String coords = "";
	static ArrayList<Socket> socks = new ArrayList<>();
	static int index = 0;
	static int turnit = 150;
	static int thrustit = 100;
	
//	public static void broadcast(String broadcastMessage, InetAddress address) throws IOException {
//		DatagramSocket socket = null;
//		socket = new DatagramSocket();
//		socket.setBroadcast(true);
//
//		byte[] buffer = broadcastMessage.getBytes();
//
//		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 2019);
//		socket.send(packet);
//		socket.close();
//	}

	
	public static void broadcast(String broadcastMessage) throws IOException {
		
		for(Socket s : socks) {
			PrintWriter bfOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())),
					true);
			
			bfOut.println(broadcastMessage);
		}
		
		
	}
	
	public static void broadcast(String broadcastMessage, int ind) throws IOException {
		
		for(int i=0; i<socks.size();i++) {
			if(ind != i) {
				PrintWriter bfOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socks.get(i).getOutputStream())),
						true);
				
				bfOut.println(broadcastMessage);
			}
		}
		
		
	}
	
	
	public static void main(String[] zero) {
		try {
			socketserver = new ServerSocket(1664);
			ExecutorService pool = Executors.newFixedThreadPool(20);

			// Broadcast les MAJ a tous les clients
            Timer time = new Timer();
            time.schedule(new TimerTask() {
           	 @Override
           		public void run() {
           		 	//System.out.println("MAJ");
           		 	//TICK/vcoords
           			String str = "TICK/";
           			for(Entry<String, Vehicule> e : vehicules.entrySet()) {
           				str += e.getKey()+":X"+e.getValue().posX+"Y"+e.getValue().posY+"VX"+e.getValue().vitessX
           						+"VY"+e.getValue().vitessY+"T"+e.getValue().angle+"|";
           			}
           			try {
           				str = str.substring(0, str.length()-1);
           			}catch (Exception e) {
						// TODO: handle exception
					}
           			
           			try {
//           			broadcast(str, InetAddress.getByName("255.255.255.255"));
           				broadcast(str);
           			} catch (IOException e1) {
           				// TODO Auto-generated catch block
           				e1.printStackTrace();
           			}
           		}

            }, 22000, 2000);

//         // lancement de la session.

			Timer sess = new Timer();
			sess.schedule(new TimerTask() {
				@Override
				public void run() {
					//System.out.println("sess");
					
					String tmp = "SESSION/";
					for(Entry<String, Vehicule> e : vehicules.entrySet()) {
						// pour changer le mode de jeux des nouveaux vehicule de attente a jeu !!!!!
						if( ! e.getValue().phase) {
							e.getValue().phase = true;
						}
						
						tmp += e.getKey() + ":X" + e.getValue().posX + "Y" + e.getValue().posY + "|";
					}
					tmp = tmp.substring(0, tmp.length() - 1);
					tmp += "/X" + objectifX + "Y" + objectifY;
					try {
//						broadcast(tmp, InetAddress.getByName("255.255.255.255"));
						broadcast(tmp);
						System.out.println(vehicules.size());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, /*20000, */20000);

//			final Duration timeout = Duration.ofSeconds(20);
//
//			final Future<String> handler = pool.submit(new Callable<String>() {
//				@Override
//				public String call() throws Exception {
//					String ret =  "SESSION/" + coords + "/X" + objectifX + "Y" + objectifY;
//					return ret;
//				}
//			});
//			try {
//				handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}

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
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	}

}
