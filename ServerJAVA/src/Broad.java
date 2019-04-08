import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Broad {

	public static void main(String[] args) {
		try {
			Server.broadcast("hello", InetAddress.getByName("255.255.255.255"));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
