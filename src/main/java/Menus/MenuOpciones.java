package Menus;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import Elementos.Audio.AudioManager;
import Juegos.EstadoJuego;
import Juegos.Juego;
import Utilz.LoadSave;

public class MenuOpciones {
    private Juego juego;
    private BufferedImage backgroundImg;
    private Boton botonRegresar;
    private BufferedImage[][] botonesImgs;
    
    // Sliders para volumen
    private Rectangle sliderMusicaFondo;
    private Rectangle sliderEfectos;
    private Rectangle sliderMusicaIndicador;
    private Rectangle sliderEfectosIndicador;
    
    private boolean arrastrando = false;
    private boolean arrastraVolMusica = false;
    
    // Variables para navegación con gamepad
    private int elementoSeleccionado = 0; // 0: música, 1: efectos, 2: botón regresar
    private int totalElementos = 3;
    
    // Valores actuales de volumen (0.0f a 1.0f)
    private float volumenMusica = 0.1f;
    private float volumenEfectos = 0.05f;
    
    // AudioManager para aplicar cambios
    private AudioManager audioManager;
    
    public MenuOpciones(Juego juego) {
        this.juego = juego;
        this.audioManager = AudioManager.getInstance();
        
        // Obtener valores actuales
        volumenMusica = audioManager.getMusicVolume();
        volumenEfectos = audioManager.getSfxVolume();
        
        cargarImagenes();
        cargarBotones();
        inicializarSliders();
    }
    
    private void cargarImagenes() {
        backgroundImg = LoadSave.GetSpriteAtlas("FONDOMENU.png");
        
        BufferedImage botonesSprite = LoadSave.GetSpriteAtlas("Botones 40x25.png");
        botonesImgs = new BufferedImage[6][3];
        
        for (int j = 0; j < botonesImgs.length; j++) {
            for (int i = 0; i < botonesImgs[j].length; i++) {
                botonesImgs[j][i] = botonesSprite.getSubimage(i * 40, j * 25, 40, 25);
            }
        }
    }
    
    private void cargarBotones() {
        // Botón Regresar (usar el índice 5 del sprite que contiene "Menu")
        botonRegresar = new Boton(
            Juego.GAME_WIDTH / 2,
            700,
            5,
            botonesImgs[5]
        );
    }
    
    private void inicializarSliders() {
        int sliderWidth = 400;
        int sliderHeight = 30;
        int indicatorWidth = 20;
        int indicatorHeight = 40;
        
        // Posicionar sliders en el centro de la pantalla
        int centerX = Juego.GAME_WIDTH / 2 - sliderWidth / 2;
        
        // Slider música
        sliderMusicaFondo = new Rectangle(centerX, 300, sliderWidth, sliderHeight);
        
        // Posición inicial del indicador de música (basado en el volumen actual)
        int musicIndicatorX = centerX + (int)(sliderWidth * volumenMusica) - indicatorWidth / 2;
        sliderMusicaIndicador = new Rectangle(musicIndicatorX, 295, indicatorWidth, indicatorHeight);
        
        // Slider efectos
        sliderEfectos = new Rectangle(centerX, 450, sliderWidth, sliderHeight);
        
        // Posición inicial del indicador de efectos (basado en el volumen actual)
        int sfxIndicatorX = centerX + (int)(sliderWidth * volumenEfectos) - indicatorWidth / 2;
        sliderEfectosIndicador = new Rectangle(sfxIndicatorX, 445, indicatorWidth, indicatorHeight);
    }
    
    public void update() {
        botonRegresar.update();
    }
    
    public void draw(Graphics g) {
        // Fondo
        g.drawImage(backgroundImg, 0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT, null);
        
        // Título
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String titulo = "OPCIONES";
        int tituloWidth = g.getFontMetrics().stringWidth(titulo);
        g.drawString(titulo, Juego.GAME_WIDTH / 2 - tituloWidth / 2, 150);
        
        // Subtítulos
        g.setFont(new Font("Arial", Font.BOLD, 25));
        
        String musicaLabel = "VOLUMEN MÚSICA";
        int musicaLabelWidth = g.getFontMetrics().stringWidth(musicaLabel);
        g.drawString(musicaLabel, Juego.GAME_WIDTH / 2 - musicaLabelWidth / 2, 270);
        
        String efectosLabel = "VOLUMEN EFECTOS";
        int efectosLabelWidth = g.getFontMetrics().stringWidth(efectosLabel);
        g.drawString(efectosLabel, Juego.GAME_WIDTH / 2 - efectosLabelWidth / 2, 420);
        
        // Dibujar sliders
        g.setColor(new Color(100, 100, 100));
        g.fillRect(sliderMusicaFondo.x, sliderMusicaFondo.y, sliderMusicaFondo.width, sliderMusicaFondo.height);
        g.fillRect(sliderEfectos.x, sliderEfectos.y, sliderEfectos.width, sliderEfectos.height);
        
        // Dibujar indicadores
        if (elementoSeleccionado == 0) {
            g.setColor(new Color(255, 220, 0)); // Amarillo
        } else {
            g.setColor(new Color(0, 150, 255)); // Azul
        }
        g.fillRect(sliderMusicaIndicador.x, sliderMusicaIndicador.y, sliderMusicaIndicador.width, sliderMusicaIndicador.height);
        
        if (elementoSeleccionado == 1) {
            g.setColor(new Color(255, 220, 0)); // Amarillo
        } else {
            g.setColor(new Color(0, 150, 255)); // Azul
        }
        g.fillRect(sliderEfectosIndicador.x, sliderEfectosIndicador.y, sliderEfectosIndicador.width, sliderEfectosIndicador.height);
        
        // Valores porcentuales
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        
        String musicaVolStr = (int)(volumenMusica * 100) + "%";
        g.drawString(musicaVolStr, sliderMusicaFondo.x + sliderMusicaFondo.width + 20, sliderMusicaFondo.y + 20);
        
        String efectosVolStr = (int)(volumenEfectos * 100) + "%";
        g.drawString(efectosVolStr, sliderEfectos.x + sliderEfectos.width + 20, sliderEfectos.y + 20);
        
        // Dibujar botón de regreso
        botonRegresar.draw(g);
        
        // Instrucciones gamepad
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Usa ← → para ajustar volumen", sliderMusicaFondo.x, sliderMusicaFondo.y + 60);
        g.drawString("Usa ↑ ↓ para cambiar opción", sliderMusicaFondo.x, sliderMusicaFondo.y + 85);
    }
    
    public void mousePressed(MouseEvent e) {
        if (botonRegresar.getBounds().contains(e.getX(), e.getY())) {
            botonRegresar.setMousePressed(true);
            return;
        }
        
        // Verificar si se presionó en algún slider
        if (sliderMusicaIndicador.contains(e.getX(), e.getY()) || sliderMusicaFondo.contains(e.getX(), e.getY())) {
            arrastrando = true;
            arrastraVolMusica = true;
            actualizarPosicionSlider(e.getX(), true);
        } else if (sliderEfectosIndicador.contains(e.getX(), e.getY()) || sliderEfectos.contains(e.getX(), e.getY())) {
            arrastrando = true;
            arrastraVolMusica = false;
            actualizarPosicionSlider(e.getX(), false);
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        arrastrando = false;
        
        if (botonRegresar.getBounds().contains(e.getX(), e.getY()) && botonRegresar.isMousePressed()) {
            audioManager.playSoundEffect("confirm");
            juego.setEstadoJuego(EstadoJuego.MENU);
        }
        
        botonRegresar.resetBools();
    }
    
    public void mouseMoved(MouseEvent e) {
        botonRegresar.setMouseOver(false);
        
        if (botonRegresar.getBounds().contains(e.getX(), e.getY())) {
            botonRegresar.setMouseOver(true);
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        if (arrastrando) {
            actualizarPosicionSlider(e.getX(), arrastraVolMusica);
        }
    }
    
    private void actualizarPosicionSlider(int mouseX, boolean esMusica) {
        Rectangle sliderFondo = esMusica ? sliderMusicaFondo : sliderEfectos;
        Rectangle indicador = esMusica ? sliderMusicaIndicador : sliderEfectosIndicador;
        
        // Limitar posición dentro del slider
        int newX = Math.max(sliderFondo.x, Math.min(sliderFondo.x + sliderFondo.width, mouseX));
        
        // Actualizar posición del indicador
        indicador.x = newX - indicador.width / 2;
        
        // Calcular nuevo valor de volumen (0.0f a 1.0f)
        float nuevoVolumen = (float)(newX - sliderFondo.x) / sliderFondo.width;
        nuevoVolumen = Math.max(0.0f, Math.min(1.0f, nuevoVolumen));
        
        // Aplicar nuevo volumen
        if (esMusica) {
            volumenMusica = nuevoVolumen;
            audioManager.setMusicVolume(volumenMusica);
        } else {
            volumenEfectos = nuevoVolumen;
            audioManager.setSfxVolume(volumenEfectos);
        }
    }
    
    // Métodos para navegación con gamepad
    public void navegarArriba() {
        elementoSeleccionado--;
        if (elementoSeleccionado < 0) {
            elementoSeleccionado = totalElementos - 1;
        }
        
        // Actualizar estado de navegación visual
        if (elementoSeleccionado == 2) {
            botonRegresar.setMouseOver(true);
        } else {
            botonRegresar.setMouseOver(false);
        }
        
        audioManager.playSoundEffect("select");
    }
    
    public void navegarAbajo() {
        elementoSeleccionado++;
        if (elementoSeleccionado >= totalElementos) {
            elementoSeleccionado = 0;
        }
        
        // Actualizar estado de navegación visual
        if (elementoSeleccionado == 2) {
            botonRegresar.setMouseOver(true);
        } else {
            botonRegresar.setMouseOver(false);
        }
        
        audioManager.playSoundEffect("select");
    }
    
    public void navegarIzquierda() {
        if (elementoSeleccionado == 0) {
            // Ajustar volumen música
            volumenMusica = Math.max(0.0f, volumenMusica - 0.05f);
            actualizarSliderDesdeValor(true);
            audioManager.setMusicVolume(volumenMusica);
            audioManager.playSoundEffect("select");
        } else if (elementoSeleccionado == 1) {
            // Ajustar volumen efectos
            volumenEfectos = Math.max(0.0f, volumenEfectos - 0.05f);
            actualizarSliderDesdeValor(false);
            audioManager.setSfxVolume(volumenEfectos);
            audioManager.playSoundEffect("select");
        }
    }
    
    public void navegarDerecha() {
        if (elementoSeleccionado == 0) {
            // Ajustar volumen música
            volumenMusica = Math.min(1.0f, volumenMusica + 0.05f);
            actualizarSliderDesdeValor(true);
            audioManager.setMusicVolume(volumenMusica);
            audioManager.playSoundEffect("select");
        } else if (elementoSeleccionado == 1) {
            // Ajustar volumen efectos
            volumenEfectos = Math.min(1.0f, volumenEfectos + 0.05f);
            actualizarSliderDesdeValor(false);
            audioManager.setSfxVolume(volumenEfectos);
            audioManager.playSoundEffect("select");
        }
    }
    
    private void actualizarSliderDesdeValor(boolean esMusica) {
        Rectangle sliderFondo = esMusica ? sliderMusicaFondo : sliderEfectos;
        Rectangle indicador = esMusica ? sliderMusicaIndicador : sliderEfectosIndicador;
        float valor = esMusica ? volumenMusica : volumenEfectos;
        
        // Calcular nueva posición del indicador
        int newX = (int)(sliderFondo.x + sliderFondo.width * valor);
        indicador.x = newX - indicador.width / 2;
    }
    
    public void ejecutarBotonSeleccionado() {
        if (elementoSeleccionado == 2) {
            // Ejecutar acción de botón regresar
            audioManager.playSoundEffect("confirm");
            juego.setEstadoJuego(EstadoJuego.MENU);
        }
    }
    
    public void presionarBotonSeleccionado() {
        if (elementoSeleccionado == 2) {
            botonRegresar.setMousePressed(true);
        }
    }
}