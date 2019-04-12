package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Server {

	static ServerSocket socketserver;

	static Socket socketduserveur;

	static long server_tickRate = 20;
	static Map<String, Vehicule> vehicules = new HashMap<>();
//	static double objectifX = Math.random() * 1000;
//	static double objectifY = Math.random() * 1000;
	static double objectifX = Math.floor(Math.random() * 600);
	static double objectifY = Math.floor(Math.random() * 800);
	static int winCap = 100;
	static String coords = "";
	static ArrayList<Socket> socks = new ArrayList<>();
	static int index = 0;

	public static void broadcast(String broadcastMessage, InetAddress address) throws IOException {
		DatagramSocket socket = null;
		socket = new DatagramSocket();
		socket.setBroadcast(true);

		byte[] buffer = broadcastMessage.getBytes();

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 2019);
		socket.send(packet);
		socket.close();
	}

	public static void main(String[] zero) {
		try {
			socketserver = new ServerSocket(1664);
			ExecutorService pool = Executors.newFixedThreadPool(20);

//			// Broadcast les MAJ a tous les clients
//            Timer time = new Timer();
//            time.schedule(new TimerTask() {
//           	 @Override
//           		public void run() {
//           		 	System.out.println("MAJ");
//           		 	//TICK/vcoords
//           			String str = "TICK/";
//           			for(Entry<String, Vehicule> e : vehicules.entrySet()) {
//           				vehicules.get(e.getKey()).phase = true;
//           				str += e.getKey()+":X"+e.getValue().posX+"Y"+e.getValue().posY+"VX"+e.getValue().vitessX
//           						+"VY"+e.getValue().vitessY+"T"+e.getValue().angle+"|";
//           			}
//           			try {
//           				str = str.substring(0, str.length()-1);
//           			}catch (Exception e) {
//						// TODO: handle exception
//					}
//           			
//           			try {
//           				broadcast(str, InetAddress.getByName("255.255.255.255"));
//           				
//           			} catch (UnknownHostException e1) {
//           				// TODO Auto-generated catch block
//           				e1.printStackTrace();
//           			} catch (IOException e1) {
//           				// TODO Auto-generated catch block
//           				e1.printStackTrace();
//           			}
//           		}
//
//            }, 0, 2000);

//         // lancement de la session.

			Timer sess = new Timer();
			sess.schedule(new TimerTask() {
				@Override
				public void run() {
					System.out.println("sess");
					String tmp = "SESSION/" + coords + "/X" + objectifX + "Y" + objectifY;
					
					try {
						broadcast(tmp, InetAddress.getByName("255.255.255.255"));
						System.out.println(vehicules.size());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 20000, 20000);

			final Duration timeout = Duration.ofSeconds(20);

			final Future<String> handler = pool.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					String ret =  "SESSION/" + coords + "/X" + objectifX + "Y" + objectifY;
					return ret;
				}
			});
			try {
				handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			while (true) {
				System.out.println("!!!!!!!");
				try {
					Work w = new Work(socketserver.accept());
					pool.submit(w);
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
