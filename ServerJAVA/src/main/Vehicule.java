package main;

public class Vehicule {

	double posX;
	double posY;

	double angle;

	double vitessX;
	double vitessY;

	Boolean phase = false;

	int score = 0;

	public Vehicule() {
		
		posX =  Math.floor(Math.random() * 600);
		posY =  Math.floor(Math.random() * 800);
		vitessX = 0;
		vitessY = 0;
		angle = 0;
		
	}

}
