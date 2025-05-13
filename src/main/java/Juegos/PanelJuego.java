package Juegos;

import javax.swing.JPanel;
import Eventos.EventoMouse;
import Eventos.EventoTeclado;
import Eventos.EventosNivel;
import Utilz.LoadSave;
import Eventos.EventoGamepad;

import java.awt.*;
import java.awt.image.BufferedImage; // Añadir esta importación

public class PanelJuego extends JPanel {
    private EventoMouse ev;
    private EventoTeclado et;
    private EventosNivel en;
    private EventoGamepad eg;
    Juego game;
    
    // Añadir estas variables para la barra de vida
    private BufferedImage[] spritesBarraVida;
    private final int TOTAL_SPRITES_BARRA = 11;
    private final int SPRITE_WIDTH = 64;
    private final int SPRITE_HEIGHT = 32;

    public PanelJuego(Juego game) {
        eg = new EventoGamepad(this);
        ev = new EventoMouse(this);
        et = new EventoTeclado(this, eg);
        en = new EventosNivel(game);
        
        this.game = game;
        setPanelSize();
        cargarSpritesBarraVida(); // Llamar a este método aquí
        addKeyListener(et);
        addKeyListener(en); 
        addMouseListener(ev);
        addMouseMotionListener(ev);
    }

    private void setPanelSize() {
        Dimension size=new Dimension(1920,1080);
        this.setPreferredSize(size);
    }

    public void paint(Graphics g) {
        super.paint(g);
        Juego game = getGame();
        game.render(g);
        
        // Solo dibujar la barra de vida en estado PLAYING
        if (game.getEstadoJuego() == EstadoJuego.PLAYING) {
            dibujarBarraVida(g);
        }
    }

    public Juego getGame() {
        return game;
    }
    
    void updateGame() {
        // Actualiza la información del mouse en el jugador antes de la actualización general
        game.updateMouseInfo(ev.getMouseX(), ev.getMouseY());
        // La actualización normal del juego continúa en el método update() de la clase Juego
    }

    private void cargarSpritesBarraVida() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.BARRA_VIDA_SPRITES);
        spritesBarraVida = new BufferedImage[TOTAL_SPRITES_BARRA];
        
        for (int i = 0; i < TOTAL_SPRITES_BARRA; i++) {
            spritesBarraVida[i] = img.getSubimage(i * SPRITE_WIDTH, 0, SPRITE_WIDTH, SPRITE_HEIGHT);
        }
    }

    private void dibujarBarraVida(Graphics g) {
        int barraX = 20;
        int barraY = -10;
        
        // Dimensiones originales: 64x32
        // Definir un factor de escala o dimensiones específicas
        int barraWidth = 340; //192
        int barraHeight = 150; //96

        float porcentajeVida = game.getPlayer().getVidaActual() / 
                               game.getPlayer().getVidaMaxima();
        
        // Seleccionar el sprite apropiado (0 = vida completa, 10 = vida vacía)
        int indiceSprite = Math.min(10, 10 - (int)(porcentajeVida * 10));
        
        // Dibujar el sprite seleccionado con las nuevas dimensiones
        g.drawImage(spritesBarraVida[indiceSprite], barraX, barraY, barraWidth, barraHeight, null);
    }

    public EventoGamepad getEg() {
        return eg;
    }
}