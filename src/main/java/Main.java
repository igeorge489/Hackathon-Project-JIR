import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.List;
public class Main {
	public static void main(String[] args) {
	    // 1. Load everything
	    ImageLibrary library = new ImageLibrary();
	    library.preloadAll("src/main/resources/images");
	    
	    List<Level> allLevels = GameData.loadLevels("src/main/resources/_annotations.coco.json");
	    
	    if (allLevels.isEmpty()) {
	        System.out.println("❌ No levels loaded. Check your JSON path!");
	        return;
	    }

	    // 2. Start at Level 1
	    Level currentLevel = allLevels.get(0);
	    BufferedImage img = library.getImage(currentLevel.getImageFileName());

	    // 3. Create the Window
	    JFrame frame = new JFrame("Marine Debris Detector - Level 1");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
	    JLabel displayLabel = new JLabel(new ImageIcon(img));
	    frame.add(displayLabel);

	    // 4. THE CLICK LOGIC
	    displayLabel.addMouseListener(new java.awt.event.MouseAdapter() {
	        @Override
	        public void mouseClicked(java.awt.event.MouseEvent e) {
	            int x = e.getX();
	            int y = e.getY();
	            
	            if (currentLevel.checkClick(x, y)) {
	                System.out.println("✅ TARGET HIT at: " + x + "," + y);
	                JOptionPane.showMessageDialog(frame, "Trash Detected! Great job, Eco-Guardian!");
	                // Logic to go to next level could go here!
	            } else {
	                System.out.println("miss at: " + x + "," + y);
	            }
	        }
	    });

	    frame.pack();
	    frame.setVisible(true);
	}

}