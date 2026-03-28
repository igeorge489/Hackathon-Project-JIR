import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    // Settings
    private final int FPS = 60;
    private final int SCROLL_SPEED = 5;
    private final int TURTLE_X = 100; // Stationary X position
    
    // Game State
    private int worldX = 0;
    private int turtleY = 200;
    private Timer timer;
    private ArrayList<Rectangle> obstacles;
    private Random random = new Random();

    public GamePanel() {
        this.setBackground(new Color(30, 144, 255)); // Deep Sky Blue (Ocean)
        this.obstacles = new ArrayList<>();
        
        // Create some initial testing obstacles
        for (int i = 0; i < 10; i++) {
            spawnObstacle(500 + (i * 300));
        }

        // Handle Movement
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) turtleY -= 15;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) turtleY += 15;
            }
        });

        // Start Game Loop
        timer = new Timer(1000 / FPS, this);
        timer.start();
    }

    private void spawnObstacle(int x) {
        int y = random.nextInt(350);
        obstacles.add(new Rectangle(x, y, 40, 40));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 1. Move the "World" (Camera)
        worldX += SCROLL_SPEED;

        // 2. Logic: If an obstacle goes off screen, wrap it around for testing
        for (Rectangle rect : obstacles) {
            if (rect.x - worldX < -50) {
                rect.x = worldX + 800; // Reposition far to the right
                rect.y = random.nextInt(350);
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- LAYER 1: MOVING BACKGROUND (Simple Grid/Bubbles) ---
        g2d.setColor(new Color(255, 255, 255, 50));
        int tileSize = 100;
        int offsetX = -(worldX % tileSize);
        for (int x = offsetX; x < getWidth(); x += tileSize) {
            g2d.drawLine(x, 0, x, getHeight());
        }

        // --- LAYER 2: OBSTACLES (Trash) ---
        g2d.setColor(Color.GRAY);
        for (Rectangle rect : obstacles) {
            // RELATIVE DRAWING: Subtract worldX from the object's map position
            int screenX = rect.x - worldX; 
            g2d.fillRect(screenX, rect.y, rect.width, rect.height);
        }

        // --- LAYER 3: STATIONARY TURTLE ---
        g2d.setColor(new Color(34, 139, 34)); // Forest Green
        g2d.fillOval(TURTLE_X, turtleY, 50, 30); // Body
        g2d.fillOval(TURTLE_X + 40, turtleY + 5, 20, 20); // Head
        
        // UI Overlay
        g2d.setColor(Color.WHITE);
        g2d.drawString("Distance: " + (worldX / 10) + "m", 20, 30);
    }
}