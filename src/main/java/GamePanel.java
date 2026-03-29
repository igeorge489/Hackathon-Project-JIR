
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    // --- Original Swimming Variables ---
    private final int SCROLL_SPEED = 5;
    private final int TURTLE_X = 100;
    private int worldX = 0;
    private Turtle turtle; 
    private boolean isGameOver = false;
    private Timer timer;
    private ArrayList<Rectangle> trashList;
    private Random random = new Random();

    // --- New Minigame & UI Variables ---
    private boolean isMinigameActive = false;
    private BufferedImage minigameImg;
    private Level currentLevel;
    private boolean showScanner = false;

    public GamePanel() {
        this.setLayout(null);
        this.setBackground(new Color(20, 100, 180));
        this.turtle = new Turtle();
        this.trashList = new ArrayList<>();

        // Initial Trash Obstacles for the swimming portion
        for (int i = 0; i < 5; i++) spawnTrash(600 + (i * 400));

        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Ignore movement keys if we are in the minigame or game over
                if (isGameOver || isMinigameActive) return;
                if (e.getKeyCode() == KeyEvent.VK_UP) turtle.move(-20);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) turtle.move(20);
            }
        });

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    // --- Bridge Methods for Main.java ---

    public void enterMinigame(BufferedImage img, Level level) {
        this.isMinigameActive = true;
        this.minigameImg = img;
        this.currentLevel = level;
        this.showScanner = false;
        repaint();
    }

    public void exitMinigame() {
        this.isMinigameActive = false;
        // We no longer auto-reset energy here; Main calls rechargeTurtle() instead
        repaint();
    }

    public void rechargeTurtle(int amount) {
        turtle.addEnergy(amount);
        repaint();
    }

    public int getTurtleEnergy() {
        return turtle.getEnergy();
    }

    public void triggerScanner() {
        this.showScanner = true;
        repaint();
        // Hide red boxes after 2 seconds
        Timer t = new Timer(2000, e -> {
            showScanner = false;
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    // --- Game Logic ---

    @Override
    public void actionPerformed(ActionEvent e) {
        // Only run swimming logic if minigame is NOT active
        if (!isGameOver && !isMinigameActive) {
            worldX += SCROLL_SPEED;
            checkCollisions();
            
            // Loop obstacles
            for (Rectangle t : trashList) {
                if (t.x - worldX < -100) { 
                    t.x = worldX + getWidth() + random.nextInt(300); 
                    t.y = random.nextInt(getHeight() - 100) + 50;
                }
            }

            // Optional: Auto-trigger minigame if energy hits zero
            if (turtle.getEnergy() <= 0) {
                Main.startMinigame();
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
                JOptionPane.showMessageDialog(this, "The turtle hit trash! Game Over.");
            }
        }
    }

    private void spawnTrash(int x) {
        trashList.add(new Rectangle(x, random.nextInt(300) + 70, 30, 50));
    }

    // --- Drawing Logic ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2d = (Graphics2D) g;
        
        if (isMinigameActive) {
            drawMinigame(g2d);
        } else {
            drawSwimmingScene(g2d);
        }
    }

    private void drawSwimmingScene(Graphics2D g2d) {
        // 1. Draw Trash Obstacles
        g2d.setColor(new Color(200, 200, 255, 150));
        for (Rectangle t : trashList) {
            g2d.fillRect(t.x - worldX, t.y, t.width, t.height);
        }

        // 2. Draw Turtle
        g2d.setColor(turtle.getColor());
        g2d.fillOval(TURTLE_X, turtle.getY(), 50, 30);

        // 3. Draw Energy Bar (optional, since it's also in the North panel)
        drawEnergyBar(g2d);
    }

    private void drawMinigame(Graphics2D g2d) {
        if (minigameImg != null) {
            // 1. Draw filtered image stretched to panel size
            g2d.drawImage(applyTurtleVision(minigameImg), 0, 0, getWidth(), getHeight(), null);

            // 2. Draw Scaled Scanner Boxes
            if (showScanner && currentLevel != null) {
                double scaleX = (double) getWidth() / minigameImg.getWidth();
                double scaleY = (double) getHeight() / minigameImg.getHeight();

                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3));
                for (Rectangle rect : currentLevel.getHitboxes()) {
                    int sx = (int) (rect.x * scaleX);
                    int sy = (int) (rect.y * scaleY);
                    int sw = (int) (rect.width * scaleX);
                    int sh = (int) (rect.height * scaleY);
                    g2d.drawRect(sx, sy, sw, sh);
                }
            }
        }
    }

    private BufferedImage applyTurtleVision(BufferedImage src) {
        int num = src.getColorModel().getNumComponents();
        float[] factors = (num == 4) ? new float[]{0.2f, 0.8f, 1.1f, 1.0f} : new float[]{0.2f, 0.8f, 1.1f};
        float[] offsets = new float[num];
        RescaleOp op = new RescaleOp(factors, offsets, null);
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        return op.filter(src, dest);
    }

    private void drawEnergyBar(Graphics2D g2d) {
        int x = 10, y = 10, w = 200, h = 20;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, y, w, h);
        g2d.setColor(Color.GREEN);
        int fillWidth = (int) (w * (turtle.getEnergy() / 100.0));
        g2d.fillRect(x, y, fillWidth, h);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, w, h);
    }
}