package Menus;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import Elementos.Audio.AudioManager;
import Juegos.EstadoJuego;
import Juegos.Juego;
import Juegos.LogrosTracker;
import Utilz.LoadSave;

public class MenuLogros {
    private Juego juego;
    private BufferedImage backgroundImg;
    private BufferedImage[] achievementImages;
    private BufferedImage[] achievementImagesGrayed;
    private String[] achievementNames;
    private String[] achievementDescriptions;
    
    private static final int TOTAL_ACHIEVEMENTS = 7;
    private static final int BADGE_SIZE = 64;
    private static final int ACHIEVEMENT_SPACING = 90;
    private static final int START_Y = 200;
    
    private LogrosTracker logrosTracker;
    
    public MenuLogros(Juego juego, LogrosTracker logrosTracker) {
        this.juego = juego;
        this.logrosTracker = logrosTracker;
        loadImages();
        initAchievements();
    }
    
    private void loadImages() {
        backgroundImg = LoadSave.GetSpriteAtlas("FONDOSELECCION.jpg");
        
        achievementImages = new BufferedImage[TOTAL_ACHIEVEMENTS];
        achievementImagesGrayed = new BufferedImage[TOTAL_ACHIEVEMENTS];
        
        try {
            achievementImages[0] = resizeImage(LoadSave.GetSpriteAtlas("logros/THE_LAST_HOPE.png"), BADGE_SIZE, BADGE_SIZE);
            achievementImages[1] = resizeImage(LoadSave.GetSpriteAtlas("logros/CABALLERO_CAIDO.png"), BADGE_SIZE, BADGE_SIZE);
            achievementImages[2] = resizeImage(LoadSave.GetSpriteAtlas("logros/AULLIDO_INTERNO.jpeg"), BADGE_SIZE, BADGE_SIZE);
            achievementImages[3] = resizeImage(LoadSave.GetSpriteAtlas("logros/LA_CURA_FATAL.jpeg"), BADGE_SIZE, BADGE_SIZE);
            achievementImages[4] = resizeImage(LoadSave.GetSpriteAtlas("logros/VELOCIDAD_LETAL.png"), BADGE_SIZE, BADGE_SIZE);
            achievementImages[5] = resizeImage(LoadSave.GetSpriteAtlas("logros/COLECCIONISTA.png"), BADGE_SIZE, BADGE_SIZE);
            achievementImages[6] = resizeImage(LoadSave.GetSpriteAtlas("logros/INTOCABLE.png"), BADGE_SIZE, BADGE_SIZE);
        } catch (Exception e) {
            System.err.println("Error loading achievement images: " + e.getMessage());
            // Create placeholder images
            for (int i = 0; i < TOTAL_ACHIEVEMENTS; i++) {
                achievementImages[i] = new BufferedImage(BADGE_SIZE, BADGE_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics g = achievementImages[i].getGraphics();
                g.setColor(Color.GRAY);
                g.fillRect(0, 0, BADGE_SIZE, BADGE_SIZE);
                g.dispose();
            }
        }
        
        // Create grayed versions of badges
        for (int i = 0; i < TOTAL_ACHIEVEMENTS; i++) {
            achievementImagesGrayed[i] = createGrayedImage(achievementImages[i]);
        }
    }
    
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        
        return resized;
    }
    
    private BufferedImage createGrayedImage(BufferedImage original) {
        BufferedImage grayImage = new BufferedImage(
            original.getWidth(), 
            original.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g = grayImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(original, 0, 0, null);
        g.setColor(new Color(100, 100, 100, 200));
        g.fillRect(0, 0, original.getWidth(), original.getHeight());
        g.dispose();
        
        return grayImage;
    }
    
    private void initAchievements() {
        achievementNames = new String[TOTAL_ACHIEVEMENTS];
        achievementDescriptions = new String[TOTAL_ACHIEVEMENTS];
        
        achievementNames[0] = "The Last Hope";
        achievementDescriptions[0] = "Terminar el juego con todos los logros";
        
        achievementNames[1] = "Caballero caído";
        achievementDescriptions[1] = "Termina el juego con el Valthor sin tener todos los logros";
        
        achievementNames[2] = "Aullido interno";
        achievementDescriptions[2] = "Termina el juego con el Freya sin tener todos los logros";
        
        achievementNames[3] = "La cura fatal";
        achievementDescriptions[3] = "Termina el juego con el Dr. Halan sin tener todos los logros";
        
        achievementNames[4] = "Velocidad Letal";
        achievementDescriptions[4] = "Terminar juego en menos de 10 minutos";
        
        achievementNames[5] = "Coleccionista";
        achievementDescriptions[5] = "Obtener todas las armas";
        
        achievementNames[6] = "Intocable";
        achievementDescriptions[6] = "No recibir nada de daño";
    }
    
    public void update() {
        // No continuous update logic needed
    }
    
    public void draw(Graphics g) {
        // Draw background
        g.drawImage(backgroundImg, 0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT, null);
        
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT);
        
        // Title
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "LOGROS", Juego.GAME_WIDTH / 2, 100);
        
        // Draw each achievement
        int centerX = Juego.GAME_WIDTH / 3;
        
        for (int i = 0; i < TOTAL_ACHIEVEMENTS; i++) {
            int y = START_Y + i * ACHIEVEMENT_SPACING;
            
            // Badge
            boolean unlocked = logrosTracker.isAchievementUnlocked(i);
            BufferedImage badgeImg = unlocked ? achievementImages[i] : achievementImagesGrayed[i];
            g.drawImage(badgeImg, centerX - BADGE_SIZE - 50, y, null);
            
            // Name and description
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(unlocked ? Color.YELLOW : Color.WHITE);
            g.drawString(achievementNames[i], centerX, y + 15);
            
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.setColor(Color.WHITE);
            g.drawString(achievementDescriptions[i], centerX, y + 40);
            
            // "COMPLETADO" text for unlocked achievements
            if (unlocked) {
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.setColor(new Color(0, 255, 0));
                g.drawString("COMPLETADO", centerX + 300, y + 15);
            }
        }
        
        // Return instruction
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "Presiona ESC para volver al menú", Juego.GAME_WIDTH / 2, Juego.GAME_HEIGHT - 50);
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            AudioManager.getInstance().playSoundEffect("confirm");
            juego.setEstadoJuego(EstadoJuego.MENU);
        }
    }
    
    private void drawCenteredString(Graphics g, String text, int x, int y) {
        int stringWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x - stringWidth / 2, y);
    }

    
}