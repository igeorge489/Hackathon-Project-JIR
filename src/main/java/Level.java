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

    public Level(int levelNumber, String imageFileName) {
        this.levelNumber = levelNumber;
        this.imageFileName = imageFileName;
        this.trashHitboxes = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.isCleared = false;
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

    public String getImageFileName() { return imageFileName; }
    
    public int getTime() {
        return (int)((System.currentTimeMillis() - startTime) / 1000);
    }
}