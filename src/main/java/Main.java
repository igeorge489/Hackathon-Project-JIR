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
        library.preloadAll("src/main/resources/images");
        allLevels = GameData.loadLevels("src/main/resources/_annotations.coco.json");

        if (allLevels.isEmpty()) {
            System.out.println("No levels found! Check paths.");
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
        
        JButton cleanOceanBtn = new JButton("Gain Energy");
        cleanOceanBtn.setFocusable(false);
        cleanOceanBtn.addActionListener(e -> {
            if (currentState == GameState.EXPLORING) startMinigame();
        });

        statsPanel.add(scoreLabel);
        statsPanel.add(cleanOceanBtn);

        frame.add(statsPanel, BorderLayout.NORTH);
        frame.add(gamePanel, BorderLayout.CENTER);

        // Click Detection
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
        }
    }

    private static void handleMinigameClick(int x, int y) {
        Level current = allLevels.get(currentIndex);
        BufferedImage rawImg = library.getImage(current.getImageFileName());
        if (rawImg == null) return;

        if (current.checkClick(x, y, gamePanel.getWidth(), gamePanel.getHeight(), rawImg)) {
            score += 100;
            gamePanel.rechargeTurtle(35);
            scoreLabel.setText("Score: " + score + " | Energy: " + gamePanel.getTurtleEnergy());
            
            currentIndex = (currentIndex + 1) % allLevels.size();
            currentState = GameState.EXPLORING;
            gamePanel.exitMinigame();
        } else {
            missCount++;
            if (missCount >= 3) gamePanel.triggerScanner();
        }
    }
}