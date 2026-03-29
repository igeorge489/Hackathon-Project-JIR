
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
    private Level currentLevel; 
    private boolean showScanner = false;
    
    private JButton startBtn;
    private JButton restartBtn;

    public GamePanel() {
        this.setLayout(null);
        this.setBackground(new Color(20, 100, 180));
        this.turtle = new Turtle();
        this.trashList = new ArrayList<>();
        // Default level to prevent null pointer before first load
        this.currentLevel = new Level("ocean.png", new ArrayList<>());

        for (int i = 0; i < 5; i++) spawnTrash(600 + (i * 400));

        startBtn = new JButton("START MISSION");
        startBtn.setBounds(300, 350, 200, 50);
        startBtn.setFocusable(false);
        startBtn.addActionListener(e -> {
            Main.currentState = Main.GameState.EXPLORING;
            startBtn.setVisible(false);
            this.requestFocusInWindow();
        });
        this.add(startBtn);

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
                if (isGameOver || isMinigameActive || Main.currentState == Main.GameState.START_MENU) return;
                if (e.getKeyCode() == KeyEvent.VK_UP) turtle.move(-25);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) turtle.move(25);
            }
        });

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    public void enterMinigame(BufferedImage img, Level minigameData) {
        this.isMinigameActive = true;
        this.minigameImg = img;
        this.currentLevel.updateHitboxes(minigameData.getHitboxes());
        this.showScanner = false;
    }

    public void exitMinigame() { 
        this.isMinigameActive = false; 
        this.requestFocusInWindow();
    }

    public void rechargeTurtle(int amount) { turtle.addEnergy(amount); }
    
    public int getTurtleEnergy() { return turtle.getEnergy(); }

    private void spawnFish() {
        fish = new Rectangle(worldX + 800, random.nextInt(300) + 100, 30, 20);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Main.currentState != Main.GameState.EXPLORING || isGameOver || isMinigameActive) {
            // Auto-trigger minigame if energy hits zero during exploration
            if (turtle.getEnergy() <= 0 && !isMinigameActive && !isGameOver && Main.currentState != Main.GameState.START_MENU) {
                Main.startMinigame();
            }
            repaint();
            return;
        }

        worldX += SCROLL_SPEED;
        fishSpawnTimer++; 
        if (fish == null && fishSpawnTimer > 300) { spawnFish(); fishSpawnTimer = 0; }
        if (fish != null && (fish.x - worldX) < -100) fish = null; 
        
        checkCollisions();

        // Recycle trash objects to create infinite loop
        for (Rectangle t : trashList) {
            if (t.x - worldX < -100) {
                int gap = Math.max(100, 400 - (currentLevel.getLevelNumber() * 30));
                t.x = worldX + getWidth() + random.nextInt(gap);
                t.y = random.nextInt(getHeight() - 100) + 50;
            }
        }

        if (turtle.getEnergy() <= 0) triggerGameOver();
        repaint();
    }

    private void checkCollisions() {
        Rectangle turtleRect = new Rectangle(TURTLE_X, turtle.getY(), 50, 30);
        for (Rectangle t : trashList) {
            if (turtleRect.intersects(new Rectangle(t.x - worldX, t.y, t.width, t.height))) triggerGameOver();
        }
        if (fish != null) {
            if (turtleRect.intersects(new Rectangle(fish.x - worldX, fish.y, fish.width, fish.height))) {
                currentLevel.incrementLevel();
                fish = null;                   
            }
        }
    }

    public void triggerGameOver() { isGameOver = true; restartBtn.setVisible(true); }

    private void restartGame() {
        isGameOver = false; worldX = 0; turtle.reset(); 
        restartBtn.setVisible(false); trashList.clear();
        for (int i = 0; i < 5; i++) spawnTrash(600 + (i * 400));
        this.requestFocusInWindow();
    }

    private void spawnTrash(int x) { trashList.add(new Rectangle(x, random.nextInt(300) + 70, 30, 50)); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (Main.currentState == Main.GameState.START_MENU) {
            drawIntro(g2d);
        } else {
            if (isMinigameActive) drawMinigame(g2d);
            else drawSwimming(g2d);
            
            drawEnergyBar(g2d);
            if (isGameOver) drawGameOver(g2d);
        }
    }

    private void drawIntro(Graphics2D g2d) {
        g2d.setColor(new Color(10, 30, 70));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.drawString("DEEP SEA SEARCH", 220, 150);
    }

    private void drawSwimming(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 255, 150));
        for (Rectangle t : trashList) g2d.fillRect(t.x - worldX, t.y, t.width, t.height);
        
        if (fish != null) {
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(fish.x - worldX, fish.y, fish.width, fish.height);
        }
        
        g2d.setColor(turtle.getColor());
        g2d.fillOval(TURTLE_X, turtle.getY(), 50, 30);
    }

    private void drawMinigame(Graphics2D g2d) {
        if (minigameImg == null) return;
        g2d.drawImage(applyTurtleVision(minigameImg), 0, 0, getWidth(), getHeight(), null);
        
        if (showScanner) {
            double sx = (double) getWidth() / minigameImg.getWidth();
            double sy = (double) getHeight() / minigameImg.getHeight();
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.RED);
            for (Rectangle r : currentLevel.getHitboxes()) {
                g2d.drawRect((int)(r.x*sx), (int)(r.y*sy), (int)(r.width*sx), (int)(r.height*sy));
            }
        }
    }

    private void drawEnergyBar(Graphics2D g2d) {
        int energy = turtle.getEnergy();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(20, 20, 200, 20);
        g2d.setColor(energy > 50 ? Color.GREEN : (energy > 20 ? Color.ORANGE : Color.RED));
        g2d.fillRect(20, 20, (int)(200 * (energy/100.0)), 20);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(20, 20, 200, 20);
        g2d.drawString("Level: " + currentLevel.getLevelNumber(), 20, 60);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.drawString("GAME OVER", 260, 250);
    }

    private BufferedImage applyTurtleVision(BufferedImage src) {
        int num = src.getColorModel().getNumComponents();
        float[] factors = new float[num];
        factors[0] = 0.2f; factors[1] = 0.9f; factors[2] = 1.1f;
        if (num == 4) factors[3] = 1.0f;
        RescaleOp op = new RescaleOp(factors, new float[num], null);
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        return op.filter(src, dest);
    }

    public void triggerScanner() {
        showScanner = true; 
        repaint();
        new Timer(2000, e -> { showScanner = false; repaint(); }).start();
    }
}