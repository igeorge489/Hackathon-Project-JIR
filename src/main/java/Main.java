import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public class Main {
    public enum GameState { START_MENU, EXPLORING, MINIGAME }
    public static GameState currentState = GameState.START_MENU;

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
        // Adjust these paths to your project structure
        library.preloadAll("src/main/resources/images");
        allLevels = GameData.loadLevels("src/main/resources/_annotations.coco.json");

        if (allLevels == null || allLevels.isEmpty()) {
            System.out.println("No levels found! Check your JSON path and image folder.");
            return;
        }
        Collections.shuffle(allLevels);

        frame = new JFrame("Marine Debris Guardian");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        gamePanel = new GamePanel();
        
        // Stats Panel (Top UI)
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scoreLabel = new JLabel("Score: 0 | Energy: 100");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton cleanOceanBtn = new JButton("Gain Energy");
        cleanOceanBtn.setFocusable(false);
        cleanOceanBtn.addActionListener(e -> {
            if (currentState == GameState.EXPLORING) startMinigame();
        });

        statsPanel.add(scoreLabel);
        statsPanel.add(cleanOceanBtn);

        frame.add(statsPanel, BorderLayout.NORTH);
        frame.add(gamePanel, BorderLayout.CENTER);

        // Click Detection for Minigame
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentState == GameState.MINIGAME) {
                    handleMinigameClick(e.getX(), e.getY());
                }
            }
        });

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void startMinigame() {
        if (allLevels == null || allLevels.isEmpty()) return;
        
        currentState = GameState.MINIGAME;
        missCount = 0;
        
        Level current = allLevels.get(currentIndex);
        BufferedImage img = library.getImage(current.getImageFileName());
        
        if (img != null) {
            gamePanel.enterMinigame(img, current);
        } else {
            System.out.println("Could not load image: " + current.getImageFileName());
            // Skip to next level if image is missing
            currentIndex = (currentIndex + 1) % allLevels.size();
            currentState = GameState.EXPLORING;
        }
    }

    private static void handleMinigameClick(int x, int y) {
        Level current = allLevels.get(currentIndex);
        BufferedImage rawImg = library.getImage(current.getImageFileName());
        if (rawImg == null) return;

        // Check if the user clicked one of the trash hitboxes
        if (current.checkClick(x, y, gamePanel.getWidth(), gamePanel.getHeight(), rawImg)) {
            score += 100;
            gamePanel.rechargeTurtle(40); // Restore energy
            
            // Sync UI Label
            scoreLabel.setText("Score: " + score + " | Energy: " + gamePanel.getTurtleEnergy());
            
            // Move to next level and return to swimming
            currentIndex = (currentIndex + 1) % allLevels.size();
            currentState = GameState.EXPLORING;
            gamePanel.exitMinigame();
        } else {
            missCount++;
            // If they miss 3 times, show them where the trash is
            if (missCount >= 3) {
                gamePanel.triggerScanner();
            }
        }
    }
}