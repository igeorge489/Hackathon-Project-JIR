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
        // 1. Data Setup
        library = new ImageLibrary();
        library.preloadAll("src/main/resources/images");
        allLevels = GameData.loadLevels("src/main/resources/_annotations.coco.json");

        if (allLevels.isEmpty()) {
            System.out.println("No levels found! Check your JSON and image paths.");
            return;
        }
        Collections.shuffle(allLevels);

        // 2. UI Setup
        frame = new JFrame("Marine Debris Guardian");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Header Panel: Holds Score, Energy, and the Action Button
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scoreLabel = new JLabel("Score: 0 | Energy: 100");
        
        JButton cleanOceanBtn = new JButton("Collect Debris (+Energy)");
        cleanOceanBtn.setFocusable(false); // Prevents the button from stealing arrow-key focus
        cleanOceanBtn.setBackground(new Color(46, 204, 113)); // A nice "Eco Green"
        cleanOceanBtn.setForeground(Color.WHITE);
        
        cleanOceanBtn.addActionListener(e -> {
            if (currentState == GameState.EXPLORING) {
                startMinigame();
            }
        });
        
        statsPanel.add(scoreLabel);
        statsPanel.add(cleanOceanBtn);
        
        gamePanel = new GamePanel(); 

        frame.add(statsPanel, BorderLayout.NORTH);
        frame.add(gamePanel, BorderLayout.CENTER);

        // 3. Click Logic
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentState == GameState.MINIGAME) {
                    handleMinigameClick(e.getX(), e.getY());
                }
            }
        });

        frame.setSize(800, 550);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void startMinigame() {
        if (allLevels.isEmpty()) return;
        
        currentState = GameState.MINIGAME;
        missCount = 0;
        
        Level current = allLevels.get(currentIndex);
        BufferedImage img = library.getImage(current.getImageFileName());
        
        if (img != null) {
            gamePanel.enterMinigame(img, current);
            frame.setTitle("SCANNER ACTIVE: Find the Hidden Debris!");
        } else {
            System.out.println("Error: Could not find image " + current.getImageFileName());
            // Skip to next if image is missing
            currentIndex = (currentIndex + 1) % allLevels.size();
            currentState = GameState.EXPLORING;
        }
    }

    private static void handleMinigameClick(int x, int y) {
        Level current = allLevels.get(currentIndex);
        BufferedImage rawImg = library.getImage(current.getImageFileName());

        if (rawImg == null) return;

        // Scaling check: Does the click hit the target in original image space?
        if (current.checkClick(x, y, gamePanel.getWidth(), gamePanel.getHeight(), rawImg)) {
            
            score += 100;
            gamePanel.rechargeTurtle(35); // Boost energy by 35%
            
            // Update UI
            scoreLabel.setText("Score: " + score + " | Energy: " + gamePanel.getTurtleEnergy());
            JOptionPane.showMessageDialog(frame, "Target Neutralized! Energy +35%");
            
            // Cycle level and return to swimming
            currentIndex = (currentIndex + 1) % allLevels.size();
            currentState = GameState.EXPLORING;
            gamePanel.exitMinigame(); 
            frame.setTitle("Marine Debris Guardian - Exploring");

        } else {
            missCount++;
            if (missCount >= 3) {
                gamePanel.triggerScanner(); // Reveal hitboxes for 2 seconds
            }
        }
    }
}