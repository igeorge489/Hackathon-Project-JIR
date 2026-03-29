import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    // --- Existing Swimming Variables ---
    private final int SCROLL_SPEED = 5;
    private final int TURTLE_X = 100;
    private int worldX = 0;
    private Turtle turtle; 
    private boolean isGameOver = false;
    private Timer timer;
    private ArrayList<Rectangle> trashList;
    private Random random = new Random();
    private Rectangle fish; // The fish to eat
    private int fishSpawnTimer = 0;

    // --- NEW: Minigame State Variables ---
    private boolean isMinigameActive = false;
    private BufferedImage minigameImg;
    private Level currentLevel;
    private boolean showScanner = false;

    public GamePanel() {
        this.setLayout(null);
        this.setBackground(new Color(20, 100, 180));
        this.turtle = new Turtle();
        this.trashList = new ArrayList<>();

        // Initial Trash Obstacles
        for (int i = 0; i < 5; i++) spawnTrash(600 + (i * 400));

        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Disable swimming movement if we are in the minigame
                if (isGameOver || isMinigameActive) return;
                if (e.getKeyCode() == KeyEvent.VK_UP) turtle.move(-20);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) turtle.move(20);
            }
        });

        this.currentLevel = new Level("ocean.png", new ArrayList<>());
        this.fish = null; // Fish starts off-screen
        
        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    private void spawnFish() {
        int spawnX = worldX + 800;
        int spawnY = 200; // Fixed Y for testing
        fish = new Rectangle(spawnX, spawnY, 30, 20);
        System.out.println("Fish spawned at WorldX: " + spawnX + " | Turtle is at ScreenX: " + TURTLE_X);
    }
    
    
    // --- NEW: Methods for Main.java to call ---
    
    public void enterMinigame(BufferedImage img, Level level) {
        this.isMinigameActive = true;
        this.minigameImg = img;
        this.currentLevel = level;
        this.showScanner = false;
        repaint();
    }

    public void exitMinigame() {
        this.isMinigameActive = false;
        this.turtle.resetEnergy(); // RECHARGE
        repaint();
    }

    public void triggerScanner() {
        this.showScanner = true;
        repaint();
        // Hide scanner after 2 seconds
        Timer t = new Timer(2000, e -> {
            showScanner = false;
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    private void spawnTrash(int x) {
        trashList.add(new Rectangle(x, random.nextInt(300) + 70, 30, 50));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    	
        // 1. Check if we should trigger the minigame transition
        if (turtle.getEnergy() <= 0 && !isMinigameActive) {
            Main.startMinigame(); // Signal to Main to swap modes
            return;
        }

        // 2. Normal Swimming Logic (only runs if minigame is OFF)
        if (!isGameOver && !isMinigameActive) {
            worldX += SCROLL_SPEED;
            
            fishSpawnTimer++; 
            if (fish == null && fishSpawnTimer > 300) { 
                spawnFish();      // <--- Call happens here
                fishSpawnTimer = 0; // Reset the clock
            }
            
        	if (fish != null && (fish.x - worldX) < -100) {
        	    fish = null; // This "frees up" the slot for a new fish to spawn
        	}
        	
            checkCollisions();
            
            for (Rectangle t : trashList) {
                if (t.x - worldX < -100) { 
                    t.x = worldX + getWidth() + random.nextInt(300); 
                    t.y = random.nextInt(getHeight() - 100) + 50;
                }
            }
        }
        repaint();
    }

    private void checkCollisions() {
        Rectangle turtleRect = new Rectangle(TURTLE_X, turtle.getY(), 50, 30);
        
        // 1. Trash Collision (Death)
        for (Rectangle t : trashList) {
            if (turtleRect.intersects(new Rectangle(t.x - worldX, t.y, t.width, t.height))) {
                isGameOver = true;
                timer.stop();
            }
        }

        // 2. Fish Collision (Level Up & Disappear)
        if (fish != null) {
            Rectangle screenFish = new Rectangle(fish.x - worldX, fish.y, fish.width, fish.height);
            if (turtleRect.intersects(screenFish)) {
                currentLevel.incrementLevel(); // Increase the level number
                fish = null;                   // Make the fish disappear
                
                // Optional: Give a small energy boost for eating
                // turtle.addEnergy(20); 
                
                System.out.println("Level Up! Current Level: " + currentLevel.getLevelNumber());
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2d = (Graphics2D) g;
        
        if (isMinigameActive) {
            // --- DRAW MINIGAME SCENE ---
            if (minigameImg != null) {
                BufferedImage filtered = applyTurtleVision(minigameImg);
                // Draw the photo to fill the whole panel
                g2d.drawImage(filtered, 0, 0, getWidth(), getHeight(), null);

                if (showScanner && currentLevel != null) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(4));
                    for (Rectangle rect : currentLevel.getHitboxes()) {
                        g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
                    }
                }
            }
        } else {
        	// 1. Draw Trash
            g2d.setColor(new Color(200, 200, 255, 150));
            for (Rectangle t : trashList) {
                g2d.fillRect(t.x - worldX, t.y, t.width, t.height);
            }

            // 2. ADD THIS: Draw the Fish
            if (fish != null) {
                g2d.setColor(Color.ORANGE);
                // Remember to subtract worldX so it moves with the camera!
                int fishScreenX = fish.x - worldX;
                g2d.fillOval(fishScreenX, fish.y, fish.width, fish.height);
                
                // Optional: Small tail to make it look like a fish
                int[] xPoints = {fishScreenX, fishScreenX - 10, fishScreenX - 10};
                int[] yPoints = {fish.y + 10, fish.y, fish.y + 20};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }

            // 3. Draw Turtle
            g2d.setColor(turtle.getColor());
            g2d.fillOval(TURTLE_X, turtle.getY(), 50, 30);

            drawEnergyBar(g2d);
        }
    }

    private BufferedImage applyTurtleVision(BufferedImage src) {
        int num = src.getColorModel().getNumComponents();
        float[] factors = (num == 4) ? new float[]{0.2f, 0.9f, 1.1f, 1.0f} : new float[]{0.2f, 0.9f, 1.1f};
        float[] offsets = new float[num];
        RescaleOp op = new RescaleOp(factors, offsets, null);
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        return op.filter(src, dest);
    }

    private void drawEnergyBar(Graphics2D g2d) {
        int x = 10, y = 10, w = 200, h = 20;
        int currentEnergy = turtle.getEnergy();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, y, w, h);
        
        if (currentEnergy > 50) g2d.setColor(Color.GREEN);
        else if (currentEnergy > 20) g2d.setColor(Color.ORANGE);
        else g2d.setColor(Color.RED);

        int fillWidth = (int) (w * (currentEnergy / 100.0));
        g2d.fillRect(x, y, fillWidth, h);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, w, h);
    }
}