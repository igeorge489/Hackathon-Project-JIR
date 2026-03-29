import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private final int SCROLL_SPEED = 5;
    private final int TURTLE_X = 100;
    private int worldX = 0;
    private Turtle turtle; 
    private boolean isGameOver = false;
    private Timer timer;
    private ArrayList<Rectangle> trashList;
    private Random random = new Random();
    private Rectangle fish; 
    private int fishSpawnTimer = 0;

    private boolean isMinigameActive = false;
    private BufferedImage minigameImg;
    private Level currentLevel; // Persistent level object
    private boolean showScanner = false;

    public GamePanel() {
        this.setLayout(null);
        this.setBackground(new Color(20, 100, 180));
        this.turtle = new Turtle();
        this.trashList = new ArrayList<>();

        // Initialize the Level once here. It will stay for the whole session.
        this.currentLevel = new Level("ocean.png", new ArrayList<>());

        for (int i = 0; i < 5; i++) spawnTrash(600 + (i * 400));

        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isGameOver || isMinigameActive) return;
                if (e.getKeyCode() == KeyEvent.VK_UP) turtle.move(-20);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) turtle.move(20);
            }
        });

        this.fish = null; 
        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    private void spawnFish() {
        int spawnX = worldX + 800;
        int spawnY = 200; 
        fish = new Rectangle(spawnX, spawnY, 30, 20);
    }
    
    // --- UPDATED: Transition Methods ---
    
    public void enterMinigame(BufferedImage img, Level minigameData) {
        this.isMinigameActive = true;
        this.minigameImg = img;
        
        // Instead of replacing currentLevel, we just copy the hitboxes for the scanner
        // This keeps the LevelNumber intact!
        this.currentLevel.updateHitboxes(minigameData.getHitboxes());
        
        this.showScanner = false;
        repaint();
    }

    public void exitMinigame() {
        this.isMinigameActive = false;
        this.turtle.resetEnergy(); // RECHARGE
        repaint();
    }

    // --- Logic Loop ---

    @Override
    public void actionPerformed(ActionEvent e) {
        if (turtle.getEnergy() <= 0 && !isMinigameActive && !isGameOver) {
            Main.startMinigame(); 
            return;
        }

        if (!isGameOver && !isMinigameActive) {
            worldX += SCROLL_SPEED;
            
            fishSpawnTimer++; 
            if (fish == null && fishSpawnTimer > 300) { 
                spawnFish();      
                fishSpawnTimer = 0; 
            }
            
            if (fish != null && (fish.x - worldX) < -100) {
                fish = null; 
            }
            
            checkCollisions();
            
            for (int i = 0; i < trashList.size(); i++) {
                Rectangle t = trashList.get(i);
                if (t.x - worldX < -100) {
                    // Difficulty scales with the permanent level number
                    int difficultyGap = Math.max(100, 400 - (currentLevel.getLevelNumber() * 30));
                    t.x = worldX + getWidth() + random.nextInt(difficultyGap);
                    t.y = random.nextInt(getHeight() - 100) + 50;

                    if (currentLevel.getLevelNumber() > 3 && random.nextInt(10) < 2) {
                         spawnTrash(t.x + 50); 
                    }
                }
            }
        }
        repaint();
    }

    private void checkCollisions() {
        Rectangle turtleRect = new Rectangle(TURTLE_X, turtle.getY(), 50, 30);
        for (Rectangle t : trashList) {
            if (turtleRect.intersects(new Rectangle(t.x - worldX, t.y, t.width, t.height))) {
                isGameOver = true;
                timer.stop();
            }
        }

        if (fish != null) {
            Rectangle screenFish = new Rectangle(fish.x - worldX, fish.y, fish.width, fish.height);
            if (turtleRect.intersects(screenFish)) {
                currentLevel.incrementLevel(); // Level increments permanently
                fish = null;                   
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2d = (Graphics2D) g;
        
        if (isMinigameActive) {
            if (minigameImg != null) {
                BufferedImage filtered = applyTurtleVision(minigameImg);
                g2d.drawImage(filtered, 0, 0, getWidth(), getHeight(), null);

                if (showScanner) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(4));
                    for (Rectangle rect : currentLevel.getHitboxes()) {
                        g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
                    }
                }
            }
        } else {
            // Draw Trash
            g2d.setColor(new Color(200, 200, 255, 150));
            for (Rectangle t : trashList) {
                g2d.fillRect(t.x - worldX, t.y, t.width, t.height);
            }

            // Draw Fish
            if (fish != null) {
                g2d.setColor(Color.ORANGE);
                int fishScreenX = fish.x - worldX;
                g2d.fillOval(fishScreenX, fish.y, fish.width, fish.height);
                int[] xPoints = {fishScreenX, fishScreenX - 10, fishScreenX - 10};
                int[] yPoints = {fish.y + 10, fish.y, fish.y + 20};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }

            // Draw Turtle
            g2d.setColor(turtle.getColor());
            g2d.fillOval(TURTLE_X, turtle.getY(), 50, 30);

            drawEnergyBar(g2d);
            
            // Draw Level UI
            g2d.setColor(Color.WHITE);
            g2d.drawString("Current Level: " + currentLevel.getLevelNumber(), 10, 50);
        }
    }

    // --- Utility Methods ---

    private void spawnTrash(int x) {
        trashList.add(new Rectangle(x, random.nextInt(300) + 70, 30, 50));
    }

    public void triggerScanner() {
        this.showScanner = true;
        repaint();
        Timer t = new Timer(2000, e -> {
            showScanner = false;
            repaint();
        });
        t.setRepeats(false);
        t.start();
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