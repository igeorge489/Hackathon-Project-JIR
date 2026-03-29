import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    // --- Swimming Logic Variables ---
    private final int SCROLL_SPEED = 5;
    private final int TURTLE_X = 100;
    private int worldX = 0;
    private Turtle turtle; 
    private boolean isGameOver = false;
    private Timer timer;
    private ArrayList<Rectangle> trashList;
    private Random random = new Random();

    // --- Minigame & State Variables ---
    private boolean isMinigameActive = false;
    private BufferedImage minigameImg;
    private Level currentLevel;
    private boolean showScanner = false;
    
    // --- UI Components ---
    private JButton startBtn;
    private JButton restartBtn;

    public GamePanel() {
        this.setLayout(null); // Allows absolute positioning for buttons
        this.setBackground(new Color(20, 100, 180));
        this.turtle = new Turtle();
        this.trashList = new ArrayList<>();

        // Spawn initial obstacles
        for (int i = 0; i < 5; i++) spawnTrash(600 + (i * 400));

        // Start Button (Intro Screen)
        startBtn = new JButton("START MISSION");
        startBtn.setBounds(300, 350, 200, 50);
        startBtn.setFocusable(false);
        startBtn.addActionListener(e -> {
            Main.currentState = Main.GameState.EXPLORING;
            startBtn.setVisible(false);
            this.requestFocusInWindow();
        });
        this.add(startBtn);

        // Restart Button (Game Over Screen)
        restartBtn = new JButton("RETRY");
        restartBtn.setBounds(325, 300, 150, 40);
        restartBtn.setFocusable(false);
        restartBtn.setVisible(false);
        restartBtn.addActionListener(e -> restartGame());
        this.add(restartBtn);

        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Block movement if menu is open, game is over, or scanning
                if (isGameOver || isMinigameActive || Main.currentState == Main.GameState.START_MENU) return;
                if (e.getKeyCode() == KeyEvent.VK_UP) turtle.move(-20);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) turtle.move(20);
            }
        });

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    // --- Interaction Methods ---

    public void enterMinigame(BufferedImage img, Level level) {
        this.isMinigameActive = true;
        this.minigameImg = img;
        this.currentLevel = level;
        this.showScanner = false;
    }

    public void exitMinigame() {
        this.isMinigameActive = false;
    }

    public void rechargeTurtle(int amount) {
        turtle.addEnergy(amount);
    }

    public int getTurtleEnergy() {
        return turtle.getEnergy();
    }

    public void triggerScanner() {
        showScanner = true;
        repaint();
        Timer t = new Timer(2000, e -> {
            showScanner = false;
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    // --- Core Game Loop ---

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Main.currentState == Main.GameState.EXPLORING && !isGameOver && !isMinigameActive) {
            worldX += SCROLL_SPEED;
            checkCollisions();
            
            // Loop obstacles
            for (Rectangle t : trashList) {
                if (t.x - worldX < -100) { 
                    t.x = worldX + getWidth() + random.nextInt(300); 
                    t.y = random.nextInt(getHeight() - 150) + 50;
                }
            }

            if (turtle.getEnergy() <= 0) triggerGameOver();
        }
        repaint();
    }

    private void checkCollisions() {
        Rectangle tRect = new Rectangle(TURTLE_X, turtle.getY(), 50, 30);
        for (Rectangle t : trashList) {
            if (tRect.intersects(new Rectangle(t.x - worldX, t.y, t.width, t.height))) {
                triggerGameOver();
            }
        }
    }

    private void triggerGameOver() {
        isGameOver = true;
        restartBtn.setVisible(true);
    }

    private void restartGame() {
        isGameOver = false;
        worldX = 0;
        turtle.reset(); 
        restartBtn.setVisible(false);
        trashList.clear();
        for (int i = 0; i < 5; i++) spawnTrash(600 + (i * 400));
        this.requestFocusInWindow();
    }

    private void spawnTrash(int x) {
        trashList.add(new Rectangle(x, random.nextInt(300) + 70, 30, 50));
    }

    // --- Graphics Rendering ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (Main.currentState == Main.GameState.START_MENU) {
            drawIntro(g2d);
        } else {
            // Main Game Drawing
            if (isMinigameActive) {
                drawMinigame(g2d);
            } else {
                drawSwimming(g2d);
            }

            // UI Layer (Drawn last so it's always on top)
            drawEnergyBar(g2d);

            if (isGameOver) {
                drawGameOver(g2d);
            }
        }
    }

    private void drawIntro(Graphics2D g2d) {
        g2d.setColor(new Color(10, 30, 70));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.drawString("DEEP SEA SEARCH", 230, 150);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("Protect the turtle. Use Arrows to swim.", 240, 200);
        g2d.drawString("Find hidden trash to gain energy!", 260, 230);
    }

    private void drawSwimming(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 255, 100)); // Obstacles
        for (Rectangle t : trashList) g2d.fillRect(t.x - worldX, t.y, t.width, t.height);
        
        g2d.setColor(turtle.getColor()); // Turtle
        g2d.fillOval(TURTLE_X, turtle.getY(), 50, 30);
    }

    private void drawMinigame(Graphics2D g2d) {
        if (minigameImg == null) return;
        g2d.drawImage(applyTurtleVision(minigameImg), 0, 0, getWidth(), getHeight(), null);
        
        if (showScanner && currentLevel != null) {
            double sx = (double) getWidth() / minigameImg.getWidth();
            double sy = (double) getHeight() / minigameImg.getHeight();
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            for (Rectangle r : currentLevel.getHitboxes()) {
                g2d.drawRect((int)(r.x * sx), (int)(r.y * sy), (int)(r.width * sx), (int)(r.height * sy));
            }
        }
    }

    private void drawEnergyBar(Graphics2D g2d) {
        int x = 20, y = 50, w = 200, h = 20; 
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, y, w, h);
        
        // Color changes to Red if energy is low
        g2d.setColor(turtle.getEnergy() < 25 ? Color.RED : Color.GREEN);
        int fillWidth = (int) (w * (turtle.getEnergy() / 100.0));
        g2d.fillRect(x, y, fillWidth, h);
        
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, w, h);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        g2d.drawString("GAME OVER", 250, 250);
    }

    private BufferedImage applyTurtleVision(BufferedImage src) {
        float[] factors = {0.2f, 0.8f, 1.1f, 1.0f}; // Cyan/Underwater tint
        RescaleOp op = new RescaleOp(factors, new float[4], null);
        return op.filter(src, null);
    }
}