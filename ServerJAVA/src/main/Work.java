package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map.Entry;

public class Work implements Runnable {

	Socket sock = null;
	int numberThread = -1;

	public Work(Socket s, int i) {
		sock = s;
		numberThread = i;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		BufferedReader bf;
		String client = "";
		try {
			bf = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			PrintWriter bfOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())),
					true);

			while (true) {
				String cmd = bf.readLine();
				String[] arg = cmd.split("/");

				//System.out.println(cmd);
				synchronized (this) {

					switch (arg[0]) {

					case "CONNECT":
						if (Server.vehicules.containsKey(arg[1])) {

							String str = "DENIED/";
							bfOut.println(str);

						} else {
							client = arg[1];
							Vehicule v = new Vehicule();

							Server.vehicules.put(arg[1], v);

							Boolean b = Server.vehicules.get(arg[1]).phase;

							String str = "WELCOME/";
							if (b)
								str += "jeu/";
							else
								str += "attente/";
							String scores = "";
							for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
								scores += e.getKey() + ":" + e.getValue().score + "|";
							}
							
							// Server.broadcast("NEWPLAYER/" + arg[1],
							// InetAddress.getByName("255.255.255.255"));
							Tools.broadcast("NEWPLAYER/" + arg[1], numberThread);
							scores = scores.substring(0, scores.length() - 1);
							str += scores + "/";
							str += "X" + Server.objectifX + "Y" + Server.objectifY+"/";
							for(Obstacle o : Server.obstacles) {
								str+= "X"+o.posX+"Y"+o.posY+"|";
							}
							str = str.substring(0, str.length() - 1);
							bfOut.println(str);
						}
						break;

					case "EXIT":
						System.out.println("A player is gone : " + arg[1]);
						Server.vehicules.remove(arg[1].toString());
						// Server.broadcast("PLAYERLEFT/" +
						// arg[1],InetAddress.getByName("255.255.255.255"));
						Tools.broadcast("PLAYERLEFT/" + arg[1], numberThread);
						Server.socks.remove(numberThread);
						break;
					
					case "ENVOI":
						Tools.broadcast("RECEPTION/"+arg[1]);
						break;

					case "NEWCOM":
						String[] com = arg[1].split("T");
						double a = Double.parseDouble(com[0].substring(1, com[0].length()));
						int nb = Integer.parseInt(com[1]);
						// a revoir plus tard !!!!!!!!!!!!!!!
						Vehicule veh = Server.vehicules.get(client);
						veh.setAngle(a);
						
//						veh.vitessX = Server.turnit * Math.cos(veh.angle) * Server.thrustit*nb ;
//						veh.vitessY = Server.turnit * Math.sin(veh.angle) * Server.thrustit*nb  ; // Tres bonne version !!!!!

						int max = 200;
						if (nb > 0 && (veh.vitessX < max && veh.vitessY < max)) {
							double v_x = veh.vitessX + Server.turnit * Math.cos(veh.angle) *  nb;
							double v_y = veh.vitessY + Server.turnit * Math.sin(veh.angle) *  nb;
							
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
						/* on teste si la position de l'objectif 
						 * est entre l'ancienne position et la nouvelle*/
						
						if(((Math.abs(old_X - Server.objectifX) < 50) && (Math.abs(Server.objectifX - veh.posX) < 50))
								&&
						   ((Math.abs(Server.objectifY - old_Y) < 50) && (Math.abs(veh.posY - Server.objectifY) < 50)))
						{
							System.out.println(Server.objectifX + "  VechX " + veh.posX);
							System.out.println(" diifff X" + Math.abs(Server.objectifX - veh.posX));
							System.out.println(" diifff Y"  + Math.abs(Server.objectifY - veh.posY));
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
							System.out.println(" diifff Y"  + Math.abs(Server.objectifY - veh.posY));
							veh.score++;
							Server.objectifX = Math.random() * 600;
							Server.objectifY = Math.random() * 800;
							String scores = "";

							for (Entry<String, Vehicule> e : Server.vehicules.entrySet()) {
								scores += e.getKey() + ":" + e.getValue().score + "|";
							}
							scores = scores.substring(0, scores.length() - 1);
							bfOut.println("NEWOBJ/X" + Server.objectifX + "Y" + Server.objectifY + "/" + scores);
						}else if (Math.abs(Server.objectifX - old_X) < 50 && Math.abs(Server.objectifY - old_Y) < 50) {
							System.out.println(Server.objectifX + "  VechX " + veh.posX);
							System.out.println(" diifff X" + Math.abs(Server.objectifX - veh.posX));
							System.out.println(" diifff Y"  + Math.abs(Server.objectifY - veh.posY));
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
						
						break;
						
					
						
					default:
						break;
					}

				}

			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
