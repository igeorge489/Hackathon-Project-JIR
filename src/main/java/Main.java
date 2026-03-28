import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
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
	    frame.setSize(800, 450);
	    
	    JPanel gamePanel = new GamePanel();
	    gamePanel.setSize(800, 450);
	    frame.add(gamePanel);
	    
//	    JButton addEnergy = new JButton("Add Energy");
//	    
//	    addEnergy.addActionListener(this);
	    
	  /*  JLabel displayLabel = new JLabel(new ImageIcon(img));
	    frame.add(displayLabel);*/

	    // 4. THE CLICK LOGIC
	   /* displayLabel.addMouseListener(new java.awt.event.MouseAdapter() {
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
	    });*/

	    frame.pack();
	    frame.setVisible(true);
	}
//    private static int currentIndex = 0;
//    private static int score = 0;
//    private static int missCount = 0; 
//    private static JLabel displayLabel;
//    private static JFrame frame;
//    private static List<Level> allLevels;
//    private static ImageLibrary library;
//
//    public static void main(String[] args) {
//        // 1. Setup Data
//        library = new ImageLibrary();
//        library.preloadAll("src/main/resources/images");
//        allLevels = GameData.loadLevels("src/main/resources/_annotations.coco.json");
//
//        if (!allLevels.isEmpty()) {
//            Collections.shuffle(allLevels); 
//            System.out.println("Deck shuffled! Starting with a random image.");
//        } else {
//            return;
//        }
//
//        // 2. Setup UI
//        frame = new JFrame("Marine Debris Guardian");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLayout(new BorderLayout());
//
//        // Top Panel for Score
//        JPanel statsPanel = new JPanel();
//        JLabel scoreLabel = new JLabel("Score: 0");
//        statsPanel.add(scoreLabel);
//        frame.add(statsPanel, BorderLayout.NORTH);
//
//        // Center Image
//        displayLabel = new JLabel();
//        frame.add(displayLabel, BorderLayout.CENTER);
//
//        // 3. Click Logic
//        displayLabel.addMouseListener(new java.awt.event.MouseAdapter() {
//        	@Override
//        	public void mouseClicked(java.awt.event.MouseEvent e) {
//        	    Level current = allLevels.get(currentIndex);
//        	    if (current.checkClick(e.getX(), e.getY())) {
//        	        // SUCCESS
//        	        score += 100;
//        	        scoreLabel.setText("Score: " + score);
//        	        JOptionPane.showMessageDialog(frame, "Trash Collected! +100 Points");
//        	        nextLevel(); 
//        	    } else {
//        	        // MISS
//        	        missCount++;
//        	        
//        	        if (missCount >= 3) { // Trigger on the 3rd miss
//        	            showScannerFeedback(current);
//        	            // Optional: reset it after showing so they have to miss 3 more times to see it again
//        	            // missCount = 0; 
//        	        } else {
//        	            System.out.println("Miss! Click " + missCount + " of 3 before scanner activates.");
//        	        }
//        	    }
//        	}
//        });
//
//        loadLevel(0); // Load first level
//        frame.setVisible(true);
//    }
//
//    private static void loadLevel(int index) {
//        if (index >= allLevels.size()) {
//            JOptionPane.showMessageDialog(frame, "Ocean Cleaned! Final Score: " + score);
//            System.exit(0);
//            return;
//        }
//        missCount = 0;
//        
//        currentIndex = index;
//        Level level = allLevels.get(index);
//        BufferedImage rawImg = library.getImage(level.getImageFileName());
//        
//        if (rawImg != null) {
//            // APPLY THE FILTER HERE
//            BufferedImage turtleView = applyTurtleVision(rawImg);
//            
//            displayLabel.setIcon(new ImageIcon(turtleView));
//            frame.setTitle("Level " + (index + 1) + " - Debris Hunt");
//            frame.pack();
//            frame.setLocationRelativeTo(null); 
//        }
//    }
//
//    private static void nextLevel() {
//        loadLevel(currentIndex + 1);
//    }
//    private static BufferedImage applyTurtleVision(BufferedImage src) {
//        // 1. Check how many components the image actually has (usually 3 or 4)
//        int numComponents = src.getColorModel().getNumComponents();
//        
//        // 2. Create a factors array that matches that exact size
//        float[] factors = new float[numComponents];
//        float[] offsets = new float[numComponents];
//
//        // 3. Fill the factors (Red, Green, Blue)
//        factors[0] = 0.2f; // Red
//        factors[1] = 0.9f; // Green
//        factors[2] = 1.1f; // Blue
//        
//        // 4. If there is a 4th channel (Alpha/Transparency), keep it at 1.0 (no change)
//        if (numComponents == 4) {
//            factors[3] = 1.0f;
//        }
//
//        // 5. Apply the filter
//        java.awt.image.RescaleOp op = new java.awt.image.RescaleOp(factors, offsets, null);
//        
//        // It's safer to create a destination image of the same type
//        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
//        return op.filter(src, dest);
//    }
//    
//    private static void showScannerFeedback(Level level) {
//        BufferedImage currentImg = library.getImage(level.getImageFileName());
//        List<Rectangle> boxes = level.getHitboxes();
//
//        if (currentImg == null) return;
//        
//        // DEBUG: Check if there are actually boxes in the list
//        System.out.println("--- Scanner Debug for: " + level.getImageFileName() + " ---");
//        System.out.println("Found " + boxes.size() + " hitboxes in JSON.");
//
//        // 1. Create the feedback image
//        BufferedImage feedbackImg = applyTurtleVision(currentImg);
//        Graphics2D g2d = feedbackImg.createGraphics();
//
//        // 2. High-Visibility Style
//        g2d.setColor(Color.RED); 
//        g2d.setStroke(new BasicStroke(5)); // Make it even thicker
//
//        for (Rectangle rect : boxes) {
//            // DEBUG: Print the exact coordinates to see if they are huge or tiny
//            System.out.println("Drawing Red Box: x=" + rect.x + " y=" + rect.y + " w=" + rect.width + " h=" + rect.height);
//            
//            // Draw the box
//            g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
//            
//            // Add a bright yellow label so it's impossible to miss
//            g2d.setColor(Color.YELLOW);
//            g2d.drawString("DETECTED", rect.x, rect.y - 5);
//            g2d.setColor(Color.RED); // Switch back for next box
//        }
//        
//        g2d.dispose();
//
//        // 3. Update the Label and FORCE it to refresh
//        displayLabel.setIcon(new ImageIcon(feedbackImg));
//        displayLabel.revalidate();
//        displayLabel.repaint();
//
//        // 4. Longer Timer (3 seconds) so you have time to see them
//        Timer timer = new Timer(3000, e -> {
//            displayLabel.setIcon(new ImageIcon(applyTurtleVision(currentImg)));
//        });
//        timer.setRepeats(false);
//        timer.start();
//    }
}