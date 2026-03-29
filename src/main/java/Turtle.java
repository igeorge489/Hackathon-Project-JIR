import java.awt.Color;

public class Turtle {
    private int y = 200;
    private int energy = 100; 
    private Color color = new Color(34, 139, 34);
    private final int MOVE_DISTANCE = 2; // Always 100 pixels now

    public void move(int direction) {
        // Only move if we have energy left
        if (energy > 0) {
            y += (direction * MOVE_DISTANCE);
            
            // Keep turtle on screen
            if (y < 50) y = 50; 
            if (y > 400) y = 400;

            // Reduce energy per move
            energy = Math.max(0, energy - 10); 
        }
    }

    public int getY() { return y; }
    public void setY(int y) {this.y = y;}
    public int getEnergy() { return energy; }
    public Color getColor() { return color; }
    public void setColor(Color c) { this.color = c; }
    public void resetEnergy() {this.energy = 100; }
    public void addEnergy(int amount) {
    	this.energy += amount;
    	if (this.energy > 100) {
    		this.energy = 100; 
    	}
    }
    public void reset() {
    	this.y = 250;
    	this.energy = 100;
    }
}
