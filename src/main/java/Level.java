/**
 * @author rayna_li
 * This class represents the levels that the game has
 */
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
public class Level {
    private int levelNumber;
    private String imageFileName;
    private List<Rectangle> trashHitboxes; // Stores where the trash is
    private long startTime;
    private boolean isCleared; 

    public Level(String imageFileName, List<Rectangle> trashHitboxes) {
        this.imageFileName = imageFileName;
        this.trashHitboxes = trashHitboxes;
    }
    
    public void updateHitboxes(List<Rectangle> newHitboxes) {
        this.trashHitboxes = newHitboxes;
    }
    
    public void incrementLevel() {
        this.levelNumber++;
        System.out.println("Level Up! Now at: " + levelNumber);
    }

    public int getLevelNumber() {
        return levelNumber;
    }
    
    // Add a hitbox found in the JSON
    public void addHitbox(int x, int y, int width, int height) {
        trashHitboxes.add(new Rectangle(x, y, width, height));
    }
    
    // Check if the user clicked on trash
    public boolean checkClick(int clickX, int clickY) {
        for (Rectangle box : trashHitboxes) {
            if (box.contains(clickX, clickY)) {
                return true;
            }
        }
        return false;
    }
    
    public List<Rectangle> getHitboxes() {
        return trashHitboxes;
    }

    public String getImageFileName() { return imageFileName; }
    
    public int getTime() {
        return (int)((System.currentTimeMillis() - startTime) / 1000);
    }
}