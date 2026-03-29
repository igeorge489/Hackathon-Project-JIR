/**
 * @author rayna_li
 * This class represents the levels that the game has
 */
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

public class Level {
    private String imageFileName;
    private List<Rectangle> trashHitboxes;
    private int levelNumber = 1;

    public Level(String imageFileName, List<Rectangle> trashHitboxes) {
        this.imageFileName = imageFileName;
        this.trashHitboxes = trashHitboxes;
    }
    
    public void updateHitboxes(List<Rectangle> newHitboxes) {
        this.trashHitboxes = newHitboxes;
    }
    
    public void incrementLevel() {
        this.levelNumber++;
    }

    public int getLevelNumber() { return levelNumber; }

    public boolean checkClick(int clickX, int clickY, int screenWidth, int screenHeight, BufferedImage originalImg) {
        if (originalImg == null) return false;

        double scaleX = (double) screenWidth / originalImg.getWidth();
        double scaleY = (double) screenHeight / originalImg.getHeight();

        for (Rectangle box : trashHitboxes) {
            int sx = (int) (box.x * scaleX);
            int sy = (int) (box.y * scaleY);
            int sw = (int) (box.width * scaleX);
            int sh = (int) (box.height * scaleY);
            
            if (new Rectangle(sx, sy, sw, sh).contains(clickX, clickY)) {
                return true;
            }
        }
        return false;
    }

    public List<Rectangle> getHitboxes() { return trashHitboxes; }
    public String getImageFileName() { return imageFileName; }
}