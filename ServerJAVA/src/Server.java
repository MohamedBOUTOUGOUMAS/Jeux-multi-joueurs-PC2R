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
	static double objectifX = Math.random() * 1000;
	static double objectifY = Math.random() * 1000;
	static double winCap = 100;
	static String coords = "";

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
			ExecutorService pool = Executors.newFixedThreadPool(2);
			
			
			
			// Broadcast les MAJ a tous les clients
            Timer time = new Timer();
            time.schedule(new TimerTask() {
           	 @Override
           		public void run() {
           		 	System.out.println("MAJ");
           			String str = "";
           			for(Entry<String, Vehicule> e : vehicules.entrySet()) {
           				vehicules.get(e.getKey()).phase = true;
           				str += e.getKey()+":X"+e.getValue().posX+"Y"+e.getValue().posY+"|";
           			}
           			try {
           				str = str.substring(0, str.length()-2);
           			}catch (Exception e) {
						// TODO: handle exception
					}
           			
           			try {
           				broadcast(str, InetAddress.getByName("255.255.255.255"));
           				
           			} catch (UnknownHostException e1) {
           				// TODO Auto-generated catch block
           				e1.printStackTrace();
           			} catch (IOException e1) {
           				// TODO Auto-generated catch block
           				e1.printStackTrace();
           			}
           		}

            }, 0, 2000);

         // lancement de la session.
            
            Timer sess = new Timer();
            sess.schedule(new TimerTask() {
				@Override
				public void run() {
					String tmp = "SESSION/"+coords+"/X"+objectifX+"Y"+objectifY;
					try {
						broadcast(tmp, InetAddress.getByName("255.255.255.255"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
            }, 20000);
			
			while(true) {
				
	    	Runnable work = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
		            	 socketduserveur = socketserver.accept();
		            	 
		            	 BufferedReader bf = new BufferedReader(new InputStreamReader(socketduserveur.getInputStream()));
		                 System.out.println(bf.readLine());
		                 
		                 PrintWriter bfOut = new PrintWriter(
		                         new BufferedWriter(
		                            new OutputStreamWriter(socketduserveur.getOutputStream())),
		                         true);
		                 
		                 String cmd = bf.readLine();
		                 String [] arg = cmd.split("/");
		                 
		                 if(arg[0] == "CONNECT") {
		                	 
		                 	if(vehicules.containsKey(arg[1])) {
		                 		
		                 		String str = "DENIED/";
		                 		bfOut.write(str);
		                 		
		                 	}else {
		                 		
		                 		Vehicule v = new Vehicule();
		                 		
		                 		coords += arg[1]+":X"+v.posX+"Y"+v.posY+"|";
		                 		
		                     	vehicules.put(arg[1], v);
		                     	
		                     	Boolean b = vehicules.get(arg[1]).phase;
		                     	
		                 		String str = "WELCOME/";
		                     	if(b) str +="jeu"; else str += "attente";
		                     	String scores = "";
		                     	for(Entry<String, Vehicule> e : vehicules.entrySet()) {
		                     		scores+=e.getKey()+":"+e.getValue().score+"|";
		                     	}
		                     	
		                     	broadcast("NEWPLAYER/"+arg[1], InetAddress.getByName("255.255.255.255"));
		                     	scores = scores.substring(0, scores.length()-2);
		                     	str += scores;
		                     	str += "X"+objectifX+"Y"+objectifY;
		                     	bfOut.write(str);
		                 	}
		                 	
		                 	
		                 }
		                 
		                 
		                 if(arg[0] == "Exit") {
		                 	vehicules.remove(vehicules.get(arg[1]));
	                     	broadcast("PLAYERLEFT/"+arg[1], InetAddress.getByName("255.255.255.255"));
		                 }
			            
		                 if(arg[0] == "NEWPCOM") {
		                	 String [] com = arg[1].split("T");
		                	 double a = Double.parseDouble(com[0].substring(1, com[0].length()-1));
		                	 int nb = Integer.parseInt(com[1]);
		                	 
		                	 
		                 }

			        }catch (IOException e) {

			            e.printStackTrace();

			        }
					
				}
			};
			pool.submit(work);
			}
			
			
			
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}


		
                
    }

}
