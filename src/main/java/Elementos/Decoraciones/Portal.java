package Elementos.Decoraciones;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import Juegos.Juego;
import Utilz.LoadSave;

public class Portal extends DecoracionAnimada {
    private boolean activo = true;
    private float distanciaInteraccion = 50f * Juego.SCALE;
    private boolean jugadorCerca = false;
    
    public Portal(float x, float y) {
        super(x, y, 
              (int)(32 * Juego.SCALE), 
              (int)(64 * Juego.SCALE), 
              cargarSprites(), 0, new int[]{8});
    }
    
    private static BufferedImage[][] cargarSprites() {
        BufferedImage spriteSheet = LoadSave.GetSpriteAtlas("decoraciones/PORTAL 32x64.png");
        BufferedImage[][] animationFrames = new BufferedImage[1][3]; // 1 animación, 8 frames
        
        int frameWidth = 32;
        int frameHeight = 64;
        
        for (int i = 0; i < 3; i++) {
            animationFrames[0][i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
        
        return animationFrames;
    }
    
    @Override
    public void update() {
        super.update();
        
        if (Juego.jugadorActual != null && activo) {
            float playerX = Juego.jugadorActual.getXCenter();
            float playerY = Juego.jugadorActual.getYCenter();
            
            float portalX = x + width/2;
            float portalY = y + height/2;
            
            float distX = portalX - playerX;
            float distY = portalY - playerY;
            float distancia = (float) Math.sqrt(distX * distX + distY * distY);
            
            jugadorCerca = distancia <= distanciaInteraccion;
            
            if (jugadorCerca) {
                activarPortal();
            }
        }
    }
    
    @Override
    public void render(Graphics g, int xLvlOffset, int yLvlOffset) {
        super.render(g, xLvlOffset, yLvlOffset);
        
        if (jugadorCerca) {
            g.setColor(Color.WHITE);
            String mensaje = "¡Entra al portal para avanzar!";
            int textX = (int)(x + width/2 - g.getFontMetrics().stringWidth(mensaje)/2) - xLvlOffset;
            int textY = (int)(y - 10) - yLvlOffset;
            g.drawString(mensaje, textX, textY);
        }
    }
    
    private void activarPortal() {
        if (activo && Juego.jugadorActual != null) {
            Juego.INSTANCIA_ACTUAL.siguienteNivel();
            activo = false;
        }
    }
}