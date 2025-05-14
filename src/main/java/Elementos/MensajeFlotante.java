package Elementos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class MensajeFlotante {
    private String texto;
    private float x, y;
    private int duracion; // en frames
    private Color color;
    private boolean activo;
    private float velocidadY = 0.5f; // Velocidad a la que sube el mensaje
    
    public MensajeFlotante(String texto, float x, float y, Color color, int duracion) {
        this.texto = texto;
        this.x = x;
        this.y = y;
        this.color = color;
        this.duracion = duracion;
        this.activo = true;
    }
    
    public void update() {
        if (activo) {
            duracion--;
            y -= velocidadY;
            if (duracion <= 0) {
                activo = false;
            }
        }
    }
    
    public void render(Graphics g, int xLvlOffset, int yLvlOffset) {
        if (activo) {
            Font originalFont = g.getFont();
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(color);
            g.drawString(texto, (int)(x - xLvlOffset), (int)(y - yLvlOffset));
            g.setFont(originalFont);
        }
    }
    
    public boolean isActivo() {
        return activo;
    }
}