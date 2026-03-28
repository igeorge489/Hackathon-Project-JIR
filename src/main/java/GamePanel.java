import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private final int SCROLL_SPEED = 5;
    private final int TURTLE_X = 100;
    
    private int worldX = 0;
    private Turtle turtle; // Our Turtle Object
    private boolean isGameOver = false;
    
    private Timer timer;
    private ArrayList<Rectangle> trashList;
    private Random random = new Random();

    public GamePanel() {
        this.setLayout(null);
        this.setBackground(new Color(20, 100, 180));
        this.turtle = new Turtle();
        this.trashList = new ArrayList<>();

        // Color Change Button
        JButton colorBtn = new JButton("Change Color");
        colorBtn.setBounds(10, 40, 120, 25);
        colorBtn.setFocusable(false);
        colorBtn.addActionListener(e -> {
            turtle.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        });
        this.add(colorBtn);

        // Initial Trash
        for (int i = 0; i < 5; i++) spawnTrash(600 + (i * 400));

        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isGameOver) return;
                if (e.getKeyCode() == KeyEvent.VK_UP) turtle.move(-20);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) turtle.move(20);
            }
        });

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    private void spawnTrash(int x) {
        trashList.add(new Rectangle(x, random.nextInt(300) + 70, 30, 50));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Stop the game if energy reaches 0
        if (turtle.getEnergy() <= 0) {
            isGameOver = true;
            timer.stop();
        }

        if (!isGameOver) {
            worldX += SCROLL_SPEED;
            checkCollisions();
            
            // Trash recycling logic
            for (Rectangle t : trashList) {
                // If the trash is off-screen to the left
                if (t.x - worldX < -100) { 
                    // Move it to the right of the CURRENT camera position
                    t.x = worldX + getWidth() + random.nextInt(300); 
                    t.y = random.nextInt(getHeight() - 100) + 50;
                }
            }
        }
        repaint();
    }

    // Key listener remains the same, but now calls turtle.move() 
    // which handles the slowing logic internally.

    private void checkCollisions() {
        Rectangle turtleRect = new Rectangle(TURTLE_X, turtle.getY(), 50, 30);
        for (Rectangle t : trashList) {
            if (turtleRect.intersects(new Rectangle(t.x - worldX, t.y, t.width, t.height))) {
                isGameOver = true;
                timer.stop();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 1. Clear screen (Draws Background Color)
        Graphics2D g2d = (Graphics2D) g;
        
        // 2. Draw Trash FIRST (so it's behind the UI)
        g2d.setColor(new Color(200, 200, 255, 150));
        for (Rectangle t : trashList) {
            // This math is vital: Map Position - Camera Position
            int screenX = t.x - worldX; 
            g2d.fillRect(screenX, t.y, t.width, t.height);
        }

        // 3. Draw Turtle
        g2d.setColor(turtle.getColor());
        g2d.fillOval(TURTLE_X, turtle.getY(), 50, 30);

        // 4. Draw UI / Energy Bar LAST (so it's on top of everything)
        drawEnergyBar(g2d);
    }

    private void drawEnergyBar(Graphics2D g2d) {
        int x = 10, y = 10, w = 200, h = 20;
        int currentEnergy = turtle.getEnergy();

        // Background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, y, w, h);

        // Change color based on fatigue
        if (currentEnergy > 50) g2d.setColor(Color.GREEN);
        else if (currentEnergy > 20) g2d.setColor(Color.ORANGE);
        else g2d.setColor(Color.RED);

        // Draw the actual bar
        int fillWidth = (int) (w * (currentEnergy / 100.0));
        g2d.fillRect(x, y, fillWidth, h);

        // Outline
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, w, h);
        
        if (currentEnergy == 0) {
            g2d.drawString("EXHAUSTED - CAN'T MOVE!", x, y + 40);
        }
    }
}