import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map.Entry;

public class Work implements Runnable{

	Socket sock = null;
	
	public Work(Socket s) {
		sock = s;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		BufferedReader bf;
		try {
			bf = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			PrintWriter bfOut = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())),
					true);

			// bfOut.println("Azul");

			String cmd = bf.readLine();
			String[] arg = cmd.split("/");

			System.out.println(cmd);

			if (arg[0].equals("CONNECT")) {
				if (Server.vehicules.containsKey(arg[1])) {

					String str = "DENIED/";
					bfOut.println(str);

				} else {

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
				double a = Double.parseDouble(com[0].substring(1, com[0].length() - 1));
				int nb = Integer.parseInt(com[1]);

			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
