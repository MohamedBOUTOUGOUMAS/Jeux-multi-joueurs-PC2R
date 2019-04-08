
public class Vehicule {
	
	
	double posX;
	double posY;
	
	int angle;
	
	double vitessX;
	double vitessY;
	
	Boolean phase = false;
	
	int score = 0;
		
	
	public Vehicule() {
		posX = (Math.random()*1000);
		posY = (Math.random()*100);
		vitessX = 0;
		vitessY = 0;
		angle = 0;
	}
	
}
