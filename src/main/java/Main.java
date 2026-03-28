import org.json.JSONObject;
import javax.swing.*;
import java.awt.image.BufferedImage;
public class Main {
public static void main(String[] args) {
    // 1. You must create the library object first!
    ImageLibrary library = new ImageLibrary();

    // 2. Preload the images from your folder
    library.preloadAll("src/main/resources/images");

    // 3. Get the specific image from the HashMap
    // We store it in a variable so we can actually use it later
    var myImage = library.getImage("0_mp4-0020_jpg.rf.f30ba41f144752a2d767a32ee396f188.jpg");

    // 4. Quick check to see if it worked
    if (myImage != null) {
        System.out.println("Successfully retrieved image from memory.");
    } else {
        System.out.println("Image not found in library.");
    }
}

}