import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map.Entry;

public class Work implements Runnable {

	Socket sock = null;

	public Work(Socket s) {
		sock = s;
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

				System.out.println(cmd);

				if (arg[0].equals("CONNECT")) {
					if (Server.vehicules.containsKey(arg[1])) {

						String str = "DENIED/";
						bfOut.println(str);

					} else {
						client = arg[1];
						Vehicule v = new Vehicule();

						Server.coords += arg[1] + ":X" + v.posX + "Y" + v.posY + "|";

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

						Server.broadcast("NEWPLAYER/" + arg[1], InetAddress.getByName("255.255.255.255"));
						scores = scores.substring(0, scores.length() - 1);
						str += scores;
						str += "X" + Server.objectifX + "Y" + Server.objectifY;
						bfOut.println(str);
					}

				}

				if (arg[0].equals("Exit")) {
					Server.vehicules.remove(Server.vehicules.get(arg[1]));
					Server.broadcast("PLAYERLEFT/" + arg[1], InetAddress.getByName("255.255.255.255"));
				}

				if (arg[0].equals("NEWPCOM")) {
					String[] com = arg[1].split("T");
					double a = Double.parseDouble(com[0].substring(1, com[0].length()));
					int nb = Integer.parseInt(com[1]);

					// a revoir plus tard !!!!!!!!!!!!!!!
					Vehicule veh = Server.vehicules.get(client);
					veh.angle = nb % 360;
					veh.posX = veh.posX + a;
					veh.posY = veh.posY + a;
					Server.vehicules.put(client, veh);
					// le vehicule passe a proximité d'un objectif
					if (Server.objectifX - veh.posX < 50 && Server.objectifY - veh.posY < 50) {
						veh.score++;
						Server.objectifX = Math.random() * 1000;
						Server.objectifY = Math.random() * 1000;
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
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
