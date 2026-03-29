import java.awt.Color;

public class Turtle {
    private int y = 250;
    private int energy = 100;
    private Color color = new Color(34, 139, 34); // Forest Green

    public void move(int amount) {
        y += amount;
        if (y < 0) y = 0;
        if (y > 450) y = 450;
        
        energy -= 1; 
        if (energy < 0) energy = 0;
    }

    public void reset() {
        this.y = 250;
        this.energy = 100;
    }

    public void addEnergy(int amount) {
        this.energy += amount;
        if (this.energy > 100) this.energy = 100;
    }

    public int getY() { return y; }
    public int getEnergy() { return energy; }
    public Color getColor() { return color; }
}