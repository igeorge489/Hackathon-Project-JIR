import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * represents the image library
 * @author Irene George
 */
public class ImageLibrary {
    // This is our "Filing Cabinet"
    private Map<String, BufferedImage> images = new HashMap<>();

    public void preloadAll(String folderPath) {
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();

        if (fileList != null) {
            for (File file : fileList) {
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                    try {
                        BufferedImage img = ImageIO.read(file);
                        images.put(file.getName(), img);
                        System.out.println("Stored: " + file.getName());
                    } catch (Exception e) {
                        System.out.println("Failed to load: " + file.getName());
                    }
                }
            }
        }
        System.out.println("Total images in memory: " + images.size());
    }

    // Call this whenever you need to draw an image
    public BufferedImage getImage(String name) {
        return images.get(name);
    }
}