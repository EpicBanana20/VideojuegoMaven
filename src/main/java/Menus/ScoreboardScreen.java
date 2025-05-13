package Menus;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import Elementos.Audio.AudioManager;
import Juegos.EstadoJuego;
import Juegos.Juego;
import Juegos.ScoreTracker;
import Utilz.LoadSave;

public class ScoreboardScreen {
    private Juego juego;
    private BufferedImage backgroundImg;
    private ScoreTracker scoreTracker;
    
    // For name input
    private String playerName = "";
    private boolean nameSubmitted = false;
    private Rectangle nameInputBox;
    private Rectangle submitButton;
    private boolean submitHovered = false;
    private boolean submitPressed = false;
    
    // For displaying high scores
    private List<ScoreTracker.ScoreEntry> highScores;
    private boolean scoresLoaded = false;
    
    // Constants
    private static final int INPUT_BOX_WIDTH = 300;
    private static final int INPUT_BOX_HEIGHT = 40;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 40;
    private static final int MAX_NAME_LENGTH = 15;
    
    public ScoreboardScreen(Juego juego, ScoreTracker scoreTracker) {
        this.juego = juego;
        this.scoreTracker = scoreTracker;
        loadImages();
        
        // Create UI elements
        int centerX = Juego.GAME_WIDTH / 2;
        nameInputBox = new Rectangle(centerX - INPUT_BOX_WIDTH/2, 500, INPUT_BOX_WIDTH, INPUT_BOX_HEIGHT);
        submitButton = new Rectangle(centerX - BUTTON_WIDTH/2, 550, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    private void loadImages() {
        backgroundImg = LoadSave.GetSpriteAtlas("FONDOSELECCION.jpg"); // Reuse selection background or create new
    }
    
    public void update() {
        if (nameSubmitted && !scoresLoaded) {
            // Load high scores after submission
            highScores = ScoreTracker.getTopScores(10);
            scoresLoaded = true;
        }
    }
    
    public void draw(Graphics g) {
        // Draw background
        g.drawImage(backgroundImg, 0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT, null);
        
        // Draw semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT);
        
        // Draw title
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "GAME COMPLETED!", Juego.GAME_WIDTH / 2, 100);
        
        // Draw player stats
        g.setFont(new Font("Arial", Font.BOLD, 25));
        drawCenteredString(g, "Your Score: " + scoreTracker.getFinalScore(), Juego.GAME_WIDTH / 2, 180);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredString(g, "Enemies Killed: " + scoreTracker.getEnemiesKilled(), Juego.GAME_WIDTH / 2, 220);
        drawCenteredString(g, "Weapons Collected: " + scoreTracker.getWeaponsCollected(), Juego.GAME_WIDTH / 2, 250);
        
        // Format and display time
        long timeSeconds = scoreTracker.getElapsedTimeSeconds();
        long minutes = timeSeconds / 60;
        long seconds = timeSeconds % 60;
        drawCenteredString(g, "Time: " + String.format("%02d:%02d", minutes, seconds), Juego.GAME_WIDTH / 2, 280);
        
        if (!nameSubmitted) {
            // Draw name input section
            g.setFont(new Font("Arial", Font.BOLD, 25));
            drawCenteredString(g, "Enter Your Name:", Juego.GAME_WIDTH / 2, 470);
            
            // Draw input box
            g.setColor(Color.WHITE);
            g.fillRect(nameInputBox.x, nameInputBox.y, nameInputBox.width, nameInputBox.height);
            g.setColor(Color.BLACK);
            g.drawRect(nameInputBox.x, nameInputBox.y, nameInputBox.width, nameInputBox.height);
            
            // Draw input text
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString(playerName + (System.currentTimeMillis() % 1000 < 500 ? "|" : ""), 
                    nameInputBox.x + 10, nameInputBox.y + 27);
            
            // Draw submit button
            if (submitHovered) {
                g.setColor(submitPressed ? new Color(0, 100, 0) : new Color(0, 150, 0));
            } else {
                g.setColor(new Color(0, 120, 0));
            }
            g.fillRect(submitButton.x, submitButton.y, submitButton.width, submitButton.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            drawCenteredString(g, "SUBMIT", submitButton.x + submitButton.width / 2, submitButton.y + 27);
        } else {
            // Draw high scores
            g.setFont(new Font("Arial", Font.BOLD, 25));
            drawCenteredString(g, "HIGH SCORES", Juego.GAME_WIDTH / 2, 400);
            
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("RANK", 100, 440);
            g.drawString("NAME", 200, 440);
            g.drawString("SCORE", 400, 440);
            g.drawString("KILLS", 500, 440);
            g.drawString("WEAPONS", 600, 440);
            g.drawString("TIME", 700, 440);
            
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            int y = 470;
            for (int i = 0; i < highScores.size(); i++) {
                ScoreTracker.ScoreEntry entry = highScores.get(i);
                g.setColor(entry.getPlayerName().equals(playerName) ? Color.YELLOW : Color.WHITE);
                
                g.drawString("#" + (i + 1), 100, y);
                g.drawString(entry.getPlayerName(), 200, y);
                g.drawString(String.valueOf(entry.getScore()), 400, y);
                g.drawString(String.valueOf(entry.getKills()), 500, y);
                g.drawString(String.valueOf(entry.getWeapons()), 600, y);
                g.drawString(entry.getFormattedTime(), 700, y);
                
                y += 30;
            }
            
            // Return to menu instruction
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            drawCenteredString(g, "Press ESC to return to menu", Juego.GAME_WIDTH / 2, 700);
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if (nameSubmitted) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                juego.setEstadoJuego(EstadoJuego.MENU);
            }
            return;
        }
        
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            submitName();
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !playerName.isEmpty()) {
            playerName = playerName.substring(0, playerName.length() - 1);
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            // Allow ESC to bypass name input and return to menu
            juego.setEstadoJuego(EstadoJuego.MENU);
        } else {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-') {
                if (playerName.length() < MAX_NAME_LENGTH) {
                    playerName += c;
                }
            }
        }
    }
    
    public void mousePressed(MouseEvent e) {
        if (nameSubmitted) return;
        
        if (submitButton.contains(e.getX(), e.getY())) {
            submitPressed = true;
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (nameSubmitted) return;
        
        if (submitPressed && submitButton.contains(e.getX(), e.getY())) {
            submitName();
        }
        submitPressed = false;
    }
    
    public void mouseMoved(MouseEvent e) {
        if (nameSubmitted) return;
        
        submitHovered = submitButton.contains(e.getX(), e.getY());
    }
    
    private void submitName() {
        if (playerName.trim().isEmpty()) {
            playerName = "Unknown Player";
        }
        
        // Save score to file
        scoreTracker.saveScore(playerName);
        nameSubmitted = true;
        
        // Play sound effect
        AudioManager.getInstance().playSoundEffect("confirm");
    }
    
    private void drawCenteredString(Graphics g, String text, int x, int y) {
        int stringWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x - stringWidth / 2, y);
    }
}