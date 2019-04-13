package main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

public class Tools {

	public static void startSession() {
		Timer sess = new Timer();
		sess.schedule(new TimerTask() {
			@Override
			public void run() {
				// System.out.println("sess");

				String tmp = "SESSION/";
				for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
					// pour changer le mode de jeux des nouveaux vehicule de attente a jeu !!!!!
					if (!e.getValue().phase) {
						e.getValue().phase = true;
					}

					tmp += e.getKey() + ":X" + e.getValue().posX + "Y" + e.getValue().posY + "|";
				}
				tmp = tmp.substring(0, tmp.length() - 1);
				tmp += "/X" + Server.objectifX + "Y" + Server.objectifY + "/";
				for (Obstacle o : Server.obstacles) {
					tmp += "X" + o.posX + "Y" + o.posY + "|";
				}
				tmp = tmp.substring(0, tmp.length() - 1);
				try {
//					broadcast(tmp, InetAddress.getByName("255.255.255.255"));
					broadcast(tmp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, /* 20000, */20000);

	}

	public static void updateGame() {
		// Broadcast les MAJ a tous les clients
		Timer time = new Timer();
		time.schedule(new TimerTask() {
			@Override
			public void run() {
				// System.out.println("MAJ");
				// TICK/vcoords
				String str = "TICK/";
				for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
					str += e.getKey() + ":X" + e.getValue().posX + "Y" + e.getValue().posY + "VX" + e.getValue().vitessX
							+ "VY" + e.getValue().vitessY + "T" + e.getValue().angle + "|";

				}
				try {
					str = str.substring(0, str.length() - 1);
				} catch (Exception e) {
				}

				try {
//		           			broadcast(str, InetAddress.getByName("255.255.255.255"));
					broadcast(str);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		}, 20000, 3000);

	}

	public static void broadcast(String broadcastMessage) throws IOException {

		for (Socket s : Server.socks) {
			PrintWriter bfOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);

			bfOut.println(broadcastMessage);
		}

	}

	public static void broadcast(String broadcastMessage, int ind) throws IOException {

		for (int i = 0; i < Server.socks.size(); i++) {
			if (ind != i) {
				PrintWriter bfOut = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(Server.socks.get(i).getOutputStream())), true);

				bfOut.println(broadcastMessage);
			}
		}

	}

//	public static void broadcast(String broadcastMessage, InetAddress address) throws IOException {
//	DatagramSocket socket = null;
//	socket = new DatagramSocket();
//	socket.setBroadcast(true);
//
//	byte[] buffer = broadcastMessage.getBytes();
//
//	DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 2019);
//	socket.send(packet);
//	socket.close();
//}

	public static void connect(String client, String cmd, PrintWriter bfOut, int numberThread) throws IOException {

		if (Server.vehicules.containsKey(cmd)) {

			String str = "DENIED/";
			bfOut.println(str);

		} else {
			client = cmd;
			Vehicule v = new Vehicule();

			Server.vehicules.put(cmd, v);

			Boolean b = Server.vehicules.get(cmd).phase;

			String str = "WELCOME/";
			if (b)
				str += "jeu/";
			else
				str += "attente/";
			String scores = "";
			for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
				scores += e.getKey() + ":" + e.getValue().score + "|";
			}

			// Server.broadcast("NEWPLAYER/" + cmd[1],
			// InetAddress.getByName("255.255.255.255"));
			Tools.broadcast("NEWPLAYER/" + cmd, numberThread);
			scores = scores.substring(0, scores.length() - 1);
			str += scores + "/";
			str += "X" + Server.objectifX + "Y" + Server.objectifY + "/";
			for (Obstacle o : Server.obstacles) {
				str += "X" + o.posX + "Y" + o.posY + "|";
			}
			str = str.substring(0, str.length() - 1);
			bfOut.println(str);
		}

	}

	public static void exit(String cmd, int numberThread) throws IOException {
		System.out.println("A player is gone : " + cmd);
		Server.vehicules.remove(cmd.toString());
		// Server.broadcast("PLAYERLEFT/" +
		// arg[1],InetAddress.getByName("255.255.255.255"));
		Tools.broadcast("PLAYERLEFT/" + cmd, numberThread);
		Server.socks.remove(numberThread);
	}

	public static void newCom(String client, String cmd, PrintWriter bfOut) {
		String[] com = cmd.split("T");
		double a = Double.parseDouble(com[0].substring(1, com[0].length()));
		int nb = Integer.parseInt(com[1]);
		// a revoir plus tard !!!!!!!!!!!!!!!
		Vehicule veh = Server.vehicules.get(client);
		veh.setAngle(a);

//		veh.vitessX = Server.turnit * Math.cos(veh.angle) * Server.thrustit*nb ;
//		veh.vitessY = Server.turnit * Math.sin(veh.angle) * Server.thrustit*nb  ; // Tres bonne version !!!!!

		int max = 200;
		if (nb > 0 && (veh.vitessX < max && veh.vitessY < max)) {
			double v_x = veh.vitessX + Server.turnit * Math.cos(veh.angle) * nb;
			double v_y = veh.vitessY + Server.turnit * Math.sin(veh.angle) * nb;

			veh.vitessX = v_x;
			veh.vitessY = v_y;
		} else if (nb == 0) {
			veh.vitessX = 0;
			veh.vitessY = 0;
		}
		double old_X = veh.posX;
		double old_Y = veh.posY;

		veh.posX = veh.posX + veh.vitessX;
		veh.posY = veh.posY + veh.vitessY;
		Server.vehicules.put(client, veh);

		// Detection de collision entre l'objectif et le vehicule
		/*
		 * on teste si la position de l'objectif est entre l'ancienne position et la
		 * nouvelle
		 */

		if (((Math.abs(old_X - Server.objectifX) < 50) && (Math.abs(Server.objectifX - veh.posX) < 50))
				&& ((Math.abs(Server.objectifY - old_Y) < 50) && (Math.abs(veh.posY - Server.objectifY) < 50))) {
			System.out.println(Server.objectifX + "  VechX " + veh.posX);
			System.out.println(" diifff X" + Math.abs(Server.objectifX - veh.posX));
			System.out.println(" diifff Y" + Math.abs(Server.objectifY - veh.posY));
			veh.score++;
			Server.objectifX = Math.random() * 600;
			Server.objectifY = Math.random() * 800;
			String scores = "";

			for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
				scores += e.getKey() + ":" + e.getValue().score + "|";
			}
			scores = scores.substring(0, scores.length() - 1);
			bfOut.println("NEWOBJ/X" + Server.objectifX + "Y" + Server.objectifY + "/" + scores);

		}

		if (Math.abs(Server.objectifX - veh.posX) < 50 && Math.abs(Server.objectifY - veh.posY) < 50) {
			System.out.println(Server.objectifX + "  VechX " + veh.posX);
			System.out.println(" diifff X" + Math.abs(Server.objectifX - veh.posX));
			System.out.println(" diifff Y" + Math.abs(Server.objectifY - veh.posY));
			veh.score++;
			Server.objectifX = Math.random() * 600;
			Server.objectifY = Math.random() * 800;
			String scores = "";

			for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
				scores += e.getKey() + ":" + e.getValue().score + "|";
			}
			scores = scores.substring(0, scores.length() - 1);
			bfOut.println("NEWOBJ/X" + Server.objectifX + "Y" + Server.objectifY + "/" + scores);

		} else if (Math.abs(Server.objectifX - old_X) < 50 && Math.abs(Server.objectifY - old_Y) < 50) {
			System.out.println(Server.objectifX + "  VechX " + veh.posX);
			System.out.println(" diifff X" + Math.abs(Server.objectifX - veh.posX));
			System.out.println(" diifff Y" + Math.abs(Server.objectifY - veh.posY));
			veh.score++;
			Server.objectifX = Math.random() * 600;
			Server.objectifY = Math.random() * 800;
			String scores = "";

			for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
				scores += e.getKey() + ":" + e.getValue().score + "|";
			}
			scores = scores.substring(0, scores.length() - 1);
			bfOut.println("NEWOBJ/X" + Server.objectifX + "Y" + Server.objectifY + "/" + scores);
		}

		// Winner
		if (veh.score == Server.winCap) {
			String scores = "";
			for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
				scores += e.getKey() + ":" + e.getValue().score + "|";
			}
			scores = scores.substring(0, scores.length() - 1);
			bfOut.println("WINNER/" + scores);
		}
	}

	public static void createObstacles(int nbObs) {
		// creation des obstacles
		for (int i = 0; i < nbObs; i++) {
			Server.obstacles.add(new Obstacle());
		}

	}

}
