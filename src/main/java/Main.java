import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public class Main {
    public enum GameState { EXPLORING, MINIGAME }
    public static GameState currentState = GameState.EXPLORING;

    private static List<Level> allLevels;
    private static int currentIndex = 0;
    private static int score = 0;
    private static int missCount = 0;

    private static JFrame frame;
    private static GamePanel gamePanel;
    private static ImageLibrary library;
    private static JLabel scoreLabel;

    public static void main(String[] args) {
        library = new ImageLibrary();
        // Adjust these paths if your project folder structure is different!
        library.preloadAll("src/main/resources/images");
        allLevels = GameData.loadLevels("src/main/resources/_annotations.coco.json");

        if (allLevels.isEmpty()) {
            System.out.println("No levels found!");
            return;
        }
        Collections.shuffle(allLevels);

        frame = new JFrame("Marine Debris Guardian");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        scoreLabel = new JLabel("Score: 0 | Energy: 100");
        gamePanel = new GamePanel(); 

        frame.add(scoreLabel, BorderLayout.NORTH);
        frame.add(gamePanel, BorderLayout.CENTER);

        // Click Logic: Only active during MINIGAME state
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentState == GameState.MINIGAME) {
                    handleMinigameClick(e.getX(), e.getY());
                }
            }
        });

        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // This is called by GamePanel when energy hits 0
    public static void startMinigame() {
        if (allLevels.isEmpty()) return;
        
        currentState = GameState.MINIGAME;
        missCount = 0;
        
        Level current = allLevels.get(currentIndex);
        BufferedImage img = library.getImage(current.getImageFileName());
        
        // Match the method name in your GamePanel
        gamePanel.enterMinigame(img, current);
        
        frame.setTitle("SCANNER ACTIVE: Find the Debris!");
    }

    private static void handleMinigameClick(int x, int y) {
        Level current = allLevels.get(currentIndex);
        
        if (current.checkClick(x, y)) {
            score += 100;
            scoreLabel.setText("Score: " + score + " | Energy: 100");
            JOptionPane.showMessageDialog(frame, "Trash Collected! Turtle Recharged.");
            
            // Prepare for the next time they run out of energy
            currentIndex = (currentIndex + 1) % allLevels.size();
            
            // Switch back to swimming
            currentState = GameState.EXPLORING;
            gamePanel.exitMinigame(); 
            frame.setTitle("Marine Debris Guardian - Exploring");
        } else {
            missCount++;
            if (missCount >= 3) {
                // Match the method name in your GamePanel
                gamePanel.triggerScanner();
            }
        }
    }
}