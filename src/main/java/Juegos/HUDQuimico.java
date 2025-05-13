package Juegos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Map;

import Elementos.Quimica.ElementoQuimico;
import Elementos.Quimica.SistemaQuimico;

public class HUDQuimico {
    private SistemaQuimico sistemaQuimico;
    
    public HUDQuimico(SistemaQuimico sistemaQuimico) {
        this.sistemaQuimico = sistemaQuimico;
    }
    
    public void render(Graphics g) {
        // Preparar fuentes
        Font elementoFont = new Font("Monospaced", Font.BOLD, 20);
        g.setFont(elementoFont);
        
        // Dibujar los últimos 5 elementos recogidos en la esquina superior derecha
        g.setColor(new Color(255, 255, 255, 180));
        g.fillRect(Juego.GAME_WIDTH - 150, 10, 140, 150);
        
        g.setColor(Color.BLACK);
        g.drawRect(Juego.GAME_WIDTH - 150, 10, 140, 150);
        
        g.drawString("ELEMENTOS", Juego.GAME_WIDTH - 140, 30);
        
        int y = 50;
        int count = 0;
        for (Map.Entry<String, ElementoQuimico> entry : 
                sistemaQuimico.getInventarioElementos().getElementos().entrySet()) {
            ElementoQuimico elemento = entry.getValue();
            if (elemento.getCantidad() > 0) {
                g.drawString(elemento.getSimbolo() + ": " + elemento.getCantidad(), 
                    Juego.GAME_WIDTH - 140, y);
                y += 20;
                count++;
                if (count >= 5) break;
            }
        }

    }
}