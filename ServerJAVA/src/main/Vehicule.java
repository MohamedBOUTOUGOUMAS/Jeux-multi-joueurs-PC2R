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

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public void setVitessX(double vitessX) {
		this.vitessX = vitessX;
	}

	public void setVitessY(double vitessY) {
		this.vitessY = vitessY;
	}

	public void setPhase(Boolean phase) {
		this.phase = phase;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public double getPosX() {
		return posX;
	}

	public double getPosY() {
		return posY;
	}

	public double getAngle() {
		return angle;
	}

	public double getVitessX() {
		return vitessX;
	}

	public double getVitessY() {
		return vitessY;
	}

	public Boolean getPhase() {
		return phase;
	}

	public int getScore() {
		return score;
	}

}
