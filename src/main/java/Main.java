import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class Main {
    private static int currentIndex = 0;
    private static int score = 0;
    private static JLabel displayLabel;
    private static JFrame frame;
    private static List<Level> allLevels;
    private static ImageLibrary library;

    public static void main(String[] args) {
        // 1. Setup Data
        library = new ImageLibrary();
        library.preloadAll("src/main/resources/images");
        allLevels = GameData.loadLevels("src/main/resources/_annotations.coco.json");

        if (allLevels.isEmpty()) return;

        // 2. Setup UI
        frame = new JFrame("Marine Debris Guardian");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top Panel for Score
        JPanel statsPanel = new JPanel();
        JLabel scoreLabel = new JLabel("Score: 0");
        statsPanel.add(scoreLabel);
        frame.add(statsPanel, BorderLayout.NORTH);

        // Center Image
        displayLabel = new JLabel();
        frame.add(displayLabel, BorderLayout.CENTER);

        // 3. Click Logic
        displayLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Level current = allLevels.get(currentIndex);
                if (current.checkClick(e.getX(), e.getY())) {
                    score += 100;
                    scoreLabel.setText("Score: " + score);
                    JOptionPane.showMessageDialog(frame, "Trash Collected! +100 Points");
                    nextLevel();
                }
            }
        });

        loadLevel(0); // Load first level
        frame.setVisible(true);
    }

    private static void loadLevel(int index) {
        if (index >= allLevels.size()) {
            JOptionPane.showMessageDialog(frame, "Ocean Cleaned! Final Score: " + score);
            System.exit(0);
            return;
        }
        
        currentIndex = index;
        Level level = allLevels.get(index);
        BufferedImage rawImg = library.getImage(level.getImageFileName());
        
        if (rawImg != null) {
            // APPLY THE FILTER HERE
            BufferedImage turtleView = applyTurtleVision(rawImg);
            
            displayLabel.setIcon(new ImageIcon(turtleView));
            frame.setTitle("Level " + (index + 1) + " - Debris Hunt");
            frame.pack();
            frame.setLocationRelativeTo(null); 
        }
    }

    private static void nextLevel() {
        loadLevel(currentIndex + 1);
    }
    private static BufferedImage applyTurtleVision(BufferedImage src) {
        // 1. Check how many components the image actually has (usually 3 or 4)
        int numComponents = src.getColorModel().getNumComponents();
        
        // 2. Create a factors array that matches that exact size
        float[] factors = new float[numComponents];
        float[] offsets = new float[numComponents];

        // 3. Fill the factors (Red, Green, Blue)
        factors[0] = 0.2f; // Red
        factors[1] = 0.9f; // Green
        factors[2] = 1.1f; // Blue
        
        // 4. If there is a 4th channel (Alpha/Transparency), keep it at 1.0 (no change)
        if (numComponents == 4) {
            factors[3] = 1.0f;
        }

        // 5. Apply the filter
        java.awt.image.RescaleOp op = new java.awt.image.RescaleOp(factors, offsets, null);
        
        // It's safer to create a destination image of the same type
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        return op.filter(src, dest);
    }
}