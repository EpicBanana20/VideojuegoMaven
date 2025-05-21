package Elementos.Enemigos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import Elementos.Enemigo;
import Elementos.Audio.AudioManager;
import Juegos.Juego;
import Utilz.LoadSave;
import Utilz.Animaciones;

public class BOSS3 extends Enemigo {
    // Constantes específicas
    private static final int ANCHO_DEFAULT = 246;
    private static final int ALTO_DEFAULT = 190;
    private static final int VIDA_DEFAULT = 20000;
    
    // Estados del jefe
    private static final int FASE_NORMAL = 0;
    private static final int FASE_ENOJADO = 1;
    private static final int FASE_FURIOSO = 2;
    
    // Estado actual
    private int faseActual = FASE_NORMAL;
    
    private boolean activated = false;
    // Para ataques
    private BufferedImage[] explosion_sprites;
    private int explosion_duracion = 15;
    private int explosion_velocidad = 50; // Controla la velocidad de la animación (más alto = más lento)
    
    // Para ataques de rayo vertical
    private BufferedImage[] rayoMagico_sprites; // Para la animación del rayo
    private int rayoDuracion = 10; // Duración de la animación del rayo
    private int rayoVelocidad = 10; // Velocidad de la animación del rayo
    
    // Control de ataques
    private int cooldownExplosion = 0;
    private int maxCooldownExplosion = 120; // 2 segundos a 60 FPS
    
    // Para controlar el cooldown del ataque de rayo
    private int cooldownRayo = 0;
    private int maxCooldownRayo = 180; // 3 segundos a 60 FPS
    
    // Para el nuevo ataque de rayo vertical con advertencia
    private int rayoDamagePerSecond = 10; // Daño por segundo del rayo vertical
    private int rayoAdvertenciaDuracion = 120; // 2 segundos a 60 FPS
    private int rayoDuracionAtaque = 240; // Duración del ataque en frames (4 segundos)
    
    private Random random = new Random();
    
    // Lista para manejar múltiples explosiones
    private ArrayList<ExplosionData> explosiones = new ArrayList<>();
    
    // Para manejar los rayos verticales
    private ArrayList<RayoData> rayos = new ArrayList<>();
    private boolean mostrandoAdvertencia = false;
    private int tiempoAdvertencia = 60; // 1 segundo a 60 FPS
    private int contadorAdvertencia = 0;
    
    // Clase interna para manejar múltiples explosiones
    private class ExplosionData {
        float x, y;
        int frame = 0;
        int tickCount = 0;
        boolean damageApplied = false;
        
        public ExplosionData(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // Clase interna para manejar los rayos
    private class RayoData {
        float x;
        int frame = 0;
        int tickCount = 0;
        boolean damageApplied = false;
        boolean warningPhase = true; // Fase de advertencia
        int tiempoAdvertencia; // Tiempo restante de advertencia
        int tiempoDamage = 0; // Para controlar el daño por segundo
        
        public RayoData(float x, int tiempoAdvertencia) {
            this.x = x;
            this.tiempoAdvertencia = tiempoAdvertencia;
            // Imprimir mensaje de debug al crear el rayo
            System.out.println("Nuevo RayoData creado en X: " + x + " con tiempo advertencia: " + tiempoAdvertencia);
        }
    }

    public BOSS3(float x, float y) {
        super(x, y, 
            (int)(ANCHO_DEFAULT * Juego.SCALE), 
            (int)(ALTO_DEFAULT * Juego.SCALE), 
            VIDA_DEFAULT);
        
        // Inicializar propiedades - sin movimiento
        inicializarEnemigo(50, 50, 146, 140, false, false);
        this.patrullando = false; // No patrulla
        this.rangoDeteccionJugador = 300 * Juego.SCALE; // Rango muy amplio
        
        // Cargar sprites de explosión
        cargarSpritesExplosion();
        
        // Cargar sprites de rayo
        cargarSpritesRayo();
        
        // Cargar animaciones
        cargarAnimaciones();
    }
    
    private void cargarSpritesExplosion() {
        BufferedImage spriteSheet = LoadSave.GetSpriteAtlas("balas/EXPLOSION.png");
        explosion_sprites = new BufferedImage[explosion_duracion];
        
        int frameWidth = 300;
        int frameHeight = 300;
        
        for (int i = 0; i < explosion_duracion; i++) {
            explosion_sprites[i] = spriteSheet.getSubimage(
                i * frameWidth, 0, frameWidth, frameHeight);
        }
    }
    
    private void cargarSpritesRayo() {
    try {
        BufferedImage spriteSheet = LoadSave.GetSpriteAtlas("balas/RAYO.png");
        // If file doesn't exist, this will throw an exception
        
        rayoMagico_sprites = new BufferedImage[rayoDuracion];
        
        int frameWidth = 100;
        int frameHeight = 500;
        
        for (int i = 0; i < rayoDuracion; i++) {
            rayoMagico_sprites[i] = spriteSheet.getSubimage(
                i * frameWidth, 0, frameWidth, frameHeight);
        }
    } catch (Exception e) {
        // Create a simple placeholder sprite if the real one fails to load
        System.out.println("Error cargando sprites de rayo: " + e.getMessage());
        rayoMagico_sprites = new BufferedImage[rayoDuracion];
        for (int i = 0; i < rayoDuracion; i++) {
            rayoMagico_sprites[i] = new BufferedImage(100, 500, BufferedImage.TYPE_INT_ARGB);
        }
    }
}
    
    @Override
    protected void cargarAnimaciones() {
        // Cargar sprite de Bagarok para las 3 fases, con 3 frames cada fase
        BufferedImage img = LoadSave.GetSpriteAtlas("enemigos/Bagarok 264x190.png");
        
        // 3 fases con 3 frames por fase
        spritesEnemigo = new BufferedImage[3][3];
        
        // Ancho y alto de cada frame
        int frameWidth = 264;
        int frameHeight = 190;
        
        // Extraer cada frame para cada fase
        for (int fase = 0; fase < 3; fase++) {
            for (int frame = 0; frame < 3; frame++) {
                // Calcular la posición en el spritesheet
                // Asumiendo que están organizados en filas, cada fila es una fase
                // y cada columna es un frame de la animación
                spritesEnemigo[fase][frame] = img.getSubimage(
                    frame * frameWidth,  // Columna (frame)
                    fase * frameHeight,  // Fila (fase)
                    frameWidth, frameHeight);
            }
        }
        
        // Crear animaciones
        animaciones = new Animaciones(spritesEnemigo);
        
        // Configurar número de frames por animación
        animaciones.setNumFramesPorAnimacion(FASE_NORMAL, 3);
        animaciones.setNumFramesPorAnimacion(FASE_ENOJADO, 3);
        animaciones.setNumFramesPorAnimacion(FASE_FURIOSO, 3);
        
        // Hacer que la animación sea más lenta
        animaciones.setAnimVelocidad(20);
        
        // Animación inicial
        animaciones.setAccion(FASE_NORMAL);
    }
    
    @Override
    public void update() {
        if (!activated && Juego.jugadorActual != null) {
            // Check if player is within detection range
            float playerX = Juego.jugadorActual.getXCenter();
            float playerY = Juego.jugadorActual.getYCenter();
            float bossX = hitbox.x + hitbox.width/2;
            float bossY = hitbox.y + hitbox.height/2;
            
            float dx = playerX - bossX;
            float dy = playerY - bossY;
            float distance = (float) Math.sqrt(dx*dx + dy*dy);
            
            // Activation range - adjust as needed
            float activationRange = 500 * Juego.SCALE;
            
            if (distance <= activationRange) {
                activated = true;
                System.out.println("¡BOSS3 ha sido activado!");
                AudioManager.getInstance().playMusic("boss3");
            } else {
                // If not activated yet, don't update any boss behaviors
                return;
            }
        }

        if (!activo) return;
        
        // Actualizar fase según vida restante
        actualizarFase();
        
        // Comportamiento según fase
        switch (faseActual) {
            case FASE_NORMAL:
                comportamientoFaseNormal();
                break;
            case FASE_ENOJADO:
                comportamientoFaseEnojado();
                break;
            case FASE_FURIOSO:
                comportamientoFaseFuriosa();
                break;
        }
        
        // Actualizar explosiones
        actualizarExplosiones();
        
        // Actualizar rayos
        actualizarRayos();
        
        // Actualizar cooldowns
        if (cooldownExplosion > 0) {
            cooldownExplosion--;
        }
        
        if (cooldownRayo > 0) {
            cooldownRayo--;
        }
        
        // Actualizar animación según fase
        animaciones.setAccion(faseActual);
        animaciones.actualizarAnimacion();
    }
    
    private void actualizarFase() {
        int porcentajeVida = (vida * 100) / vidaMaxima;
        
        if (porcentajeVida < 30 && faseActual != FASE_FURIOSO) {
            cambiarFase(FASE_FURIOSO);
        } else if (porcentajeVida < 65 && faseActual != FASE_ENOJADO && faseActual != FASE_FURIOSO) {
            cambiarFase(FASE_ENOJADO);
        }
    }
    
    private void cambiarFase(int nuevaFase) {
        this.faseActual = nuevaFase;
        
        // Cambiar propiedades según la fase
        switch (faseActual) {
            case FASE_NORMAL:
                this.maxCooldownExplosion = 120;
                this.maxCooldownRayo = 0; // No usa rayos en fase normal
                break;
            case FASE_ENOJADO:
                this.maxCooldownExplosion = 90;
                this.maxCooldownRayo = 180;
                System.out.println("¡BOSS3 entró a FASE ENOJADO! Iniciando ataque de rayos...");
                // Forzar un ataque de rayos al iniciar esta fase
                cooldownRayo = 0;
                iniciarAtaqueRayosVerticales();
                break;
            case FASE_FURIOSO:
                this.maxCooldownExplosion = 60;
                this.maxCooldownRayo = 120; // Más frecuente en fase furiosa
                System.out.println("¡BOSS3 entró a FASE FURIOSA!");
                break;
        }
    }
    
    private void comportamientoFaseNormal() {
        // Intentar disparar explosión
        if (cooldownExplosion <= 0 && random.nextFloat() < 0.02) {
            lanzarExplosion();
        }
    }
    
    private void comportamientoFaseEnojado() {
        // Intentar disparar explosión con mayor frecuencia
        if (cooldownExplosion <= 0 && random.nextFloat() < 0.04) {
            lanzarExplosion();
            
            // Posibilidad de lanzar una segunda explosión
            if (random.nextFloat() < 0.3) {
                lanzarExplosionAleatoria();
            }
        }
        
        // Ataque de rayos verticales - aumentado a 10% de probabilidad para que sea más frecuente
        if (cooldownRayo <= 0 && random.nextFloat() < 0.15) { // Aumentado a 15% para que sea más frecuente
            System.out.println("BOSS3: Iniciando ataque de rayos verticales en fase enojado");
            iniciarAtaqueRayosVerticales();
        }
    }
    
    private void comportamientoFaseFuriosa() {
        // Intentar disparar explosión con alta frecuencia
        if (cooldownExplosion <= 0 && random.nextFloat() < 0.06) {
            lanzarExplosion();
            
            // Alta posibilidad de lanzar explosiones adicionales
            if (random.nextFloat() < 0.7) {
                lanzarExplosionAleatoria();
                lanzarExplosionAleatoria();
            }
        }
        
        // Ataque de rayos más frecuente en fase furiosa
        if (cooldownRayo <= 0 && random.nextFloat() < 0.04) {
            iniciarAtaqueRayosVerticales();
        }
    }
    
    private void lanzarExplosion() {
        if (Juego.jugadorActual == null) return;
        
        // Obtener la posición del jugador
        float jugadorX = Juego.jugadorActual.getXCenter();
        float jugadorY = Juego.jugadorActual.getYCenter();
        
        // Crear una nueva explosión
        explosiones.add(new ExplosionData(jugadorX, jugadorY));
        
        // Reiniciar cooldown
        cooldownExplosion = maxCooldownExplosion;
    }
    
    private void lanzarExplosionAleatoria() {
        if (Juego.jugadorActual == null) return;
        
        // Obtener la posición del jugador como base
        float jugadorX = Juego.jugadorActual.getXCenter();
        float jugadorY = Juego.jugadorActual.getYCenter();
        
        // Añadir un offset aleatorio
        float offsetX = (random.nextFloat() * 400 - 200) * Juego.SCALE;
        float offsetY = (random.nextFloat() * 400 - 200) * Juego.SCALE;
        
        // Crear una nueva explosión en una posición cercana al jugador
        explosiones.add(new ExplosionData(jugadorX + offsetX, jugadorY + offsetY));
    }
    
    private void iniciarAtaqueRayosVerticales() {
        if (Juego.jugadorActual == null) return;
        
        float jugadorX = Juego.jugadorActual.getXCenter();
        
        // Crear 3 rayos verticales bien separados para cubrir gran parte del área
        // Separación basada en el ancho de la pantalla para garantizar cobertura
        float pantallaAncho = Juego.GAME_WIDTH;
        float separacion = pantallaAncho / 4; // Dividimos la pantalla en cuartos
        
        System.out.println("BOSS3: Creando rayos verticales. Ancho pantalla: " + pantallaAncho);
        
        // Rayo a la izquierda (1/4 de la pantalla desde el lateral izquierdo)
        float xIzquierda = jugadorX - separacion;
        rayos.add(new RayoData(xIzquierda, rayoAdvertenciaDuracion));
        System.out.println("- Rayo izquierda en X: " + xIzquierda);
        
        // Rayo central (centrado en el jugador)
        rayos.add(new RayoData(jugadorX, rayoAdvertenciaDuracion));
        System.out.println("- Rayo central en X: " + jugadorX);
        
        // Rayo a la derecha (1/4 de la pantalla desde el lateral derecho)
        float xDerecha = jugadorX + separacion;
        rayos.add(new RayoData(xDerecha, rayoAdvertenciaDuracion));
        System.out.println("- Rayo derecha en X: " + xDerecha);
        
        // Reproducir un efecto de sonido si está disponible
        try {
            AudioManager.getInstance().playSoundEffect("laser");
        } catch (Exception e) {
            // Ignorar si el sonido no existe
        }
        
        // Reiniciar cooldown
        cooldownRayo = maxCooldownRayo;
    }
    
    private void actualizarExplosiones() {
        ArrayList<ExplosionData> explosionesTerminadas = new ArrayList<>();
        
        for (ExplosionData explosion : explosiones) {
            // Incrementar contador de ticks
            explosion.tickCount++;
            
            // Avanzar frame solo cuando se alcanza la velocidad deseada
            if (explosion.tickCount >= explosion_velocidad) {
                explosion.frame++;
                explosion.tickCount = 0;
                
                // Aplicar daño en el frame 6
                if (explosion.frame == 6 && !explosion.damageApplied) {
                    aplicarDañoExplosion(explosion);
                    explosion.damageApplied = true;
                }
            }
            
            // Marcar para eliminar si terminó
            if (explosion.frame >= explosion_duracion) {
                explosionesTerminadas.add(explosion);
            }
        }
        
        // Eliminar explosiones terminadas
        explosiones.removeAll(explosionesTerminadas);
    }
    
    private void actualizarRayos() {
        ArrayList<RayoData> rayosTerminados = new ArrayList<>();
        
        // Imprimir número de rayos activos para depuración
        if (!rayos.isEmpty()) {
            System.out.println("Rayos activos: " + rayos.size());
        }
        
        for (RayoData rayo : rayos) {
            if (rayo.warningPhase) {
                // Fase de advertencia (hitbox roja)
                rayo.tiempoAdvertencia--;
                
                // Cuando termine la advertencia, cambiar a la fase de ataque
                if (rayo.tiempoAdvertencia <= 0) {
                    rayo.warningPhase = false;
                    rayo.frame = 0; // Reiniciar frame para fase de ataque
                    System.out.println("BOSS3: Rayo en X:" + rayo.x + " cambia de advertencia a ataque");
                    
                    // Reproducir un efecto de sonido para el ataque
                    try {
                        AudioManager.getInstance().playSoundEffect("shootfranco");
                    } catch (Exception e) {
                        // Ignorar si el sonido no existe
                    }
                }
            } else {
                // Fase de ataque (rayo blanco)
                rayo.tickCount++;
                rayo.frame++;
                
                // El daño se aplica continuamente basado en el tiempo
                rayo.tiempoDamage++;
                
                // Aplicar daño cada 60 frames (1 segundo)
                if (rayo.tiempoDamage >= 60) {
                    aplicarDañoRayo(rayo);
                    rayo.tiempoDamage = 0;
                } else {
                    // Comprobar si el jugador está en el rayo para feedback visual
                    // incluso si no es momento de hacer daño
                    comprobarJugadorEnRayo(rayo);
                }
                
                // El rayo dura 4 segundos como ataque (o el tiempo configurado)
                if (rayo.frame >= rayoDuracionAtaque) {
                    rayosTerminados.add(rayo);
                    System.out.println("BOSS3: Rayo en X:" + rayo.x + " finalizado");
                }
            }
        }
        
        // Eliminar rayos terminados
        if (!rayosTerminados.isEmpty()) {
            rayos.removeAll(rayosTerminados);
            System.out.println("Rayos eliminados: " + rayosTerminados.size() + ", quedan: " + rayos.size());
        }
    }
    
    // Método auxiliar para verificar si el jugador está en el rayo (para feedback)
    private boolean comprobarJugadorEnRayo(RayoData rayo) {
        if (Juego.jugadorActual == null) return false;
        
        float anchoRayo = 80 * Juego.SCALE;
        float jugadorX = Juego.jugadorActual.getXCenter();
        
        return Math.abs(jugadorX - rayo.x) <= anchoRayo / 2;
    }
    
    private void aplicarDañoExplosion(ExplosionData explosion) {
        if (Juego.jugadorActual == null) return;
        
        // Definir radio de la explosión
        float radioExplosion = 100 * Juego.SCALE;
        
        // Calcular distancia al jugador
        float dx = Juego.jugadorActual.getXCenter() - explosion.x;
        float dy = Juego.jugadorActual.getYCenter() - explosion.y;
        float distancia = (float) Math.sqrt(dx*dx + dy*dy);
        
        // Si el jugador está dentro del radio, aplicar daño
        if (distancia <= radioExplosion) {
            int dañoBase = 15;
            
            // Mayor daño según fase
            switch (faseActual) {
                case FASE_NORMAL:
                    dañoBase = 15;
                    break;
                case FASE_ENOJADO:
                    dañoBase = 20;
                    break;
                case FASE_FURIOSO:
                    dañoBase = 25;
                    break;
            }
            
            // El daño disminuye con la distancia
            float factorDistancia = 1.0f - (distancia / radioExplosion);
            int dañoFinal = Math.max(5, (int)(dañoBase * factorDistancia));
            
            Juego.jugadorActual.recibirDaño(dañoFinal);
        }
    }
    
    private void aplicarDañoRayo(RayoData rayo) {
        if (Juego.jugadorActual == null) return;
        
        // Ancho del hitbox del rayo
        float anchoRayo = 80 * Juego.SCALE;
        
        // Posición del jugador
        float jugadorX = Juego.jugadorActual.getXCenter();
        
        // Comprobar si el jugador está dentro del rayo
        if (Math.abs(jugadorX - rayo.x) <= anchoRayo / 2) {
            // Aplicar daño por segundo configurado
            Juego.jugadorActual.recibirDaño(rayoDamagePerSecond);
            System.out.println("BOSS3: ¡Rayo impacta al jugador! Daño: " + rayoDamagePerSecond);
            
            // Reproducir efecto de sonido de impacto
            try {
                AudioManager.getInstance().playSoundEffect("hit");
            } catch (Exception e) {
                // Ignorar si el sonido no existe
            }
        }
    }
    
    @Override
    public void render(Graphics g, int xLvlOffset, int yLvlOffset) {
        // Renderizar al jefe
        if (!activo) return;
        super.render(g, xLvlOffset, yLvlOffset);
        if (activated) {
            renderHealthBar(g, xLvlOffset, yLvlOffset);
        }
        
        // Dibujar BOSS3
        int drawX = (int) (hitbox.x - xDrawOffset) - xLvlOffset;
        int drawY = (int) (hitbox.y - yDrawOffset) - yLvlOffset;
        
        // No necesita voltear la imagen ya que no se mueve
        g.drawImage(animaciones.getImagenActual(),
            drawX, drawY,
            w, h, null);
        
        // PRIMERO, renderizar los rayos verticales (para que estén detrás de otros elementos)
        renderizarRayosVerticales(g, xLvlOffset, yLvlOffset);
        
        // Renderizar explosiones activas
        for (ExplosionData explosion : explosiones) {
            if (explosion.frame < explosion_duracion) {
                int explosionSize = (int)(150 * Juego.SCALE);
                
                // Dibujar la explosión centrada en su posición
                g.drawImage(explosion_sprites[explosion.frame],
                    (int)(explosion.x - explosionSize/2) - xLvlOffset,
                    (int)(explosion.y - explosionSize/2) - yLvlOffset,
                    explosionSize, explosionSize, null);
            }
        }
    }
    
    /**
     * Método específico para renderizar los rayos verticales.
     * Dibuja grandes líneas que cubren toda la pantalla de arriba a abajo.
     */
    private void renderizarRayosVerticales(Graphics g, int xLvlOffset, int yLvlOffset) {
        // Solo dibujamos los rayos si hay alguno
        if (rayos.isEmpty()) {
            return;
        }
        
        // Altura que cubra toda la pantalla más extra para asegurar que se vea
        int alturaTotal = Juego.GAME_HEIGHT * 3; // Triplicamos para asegurar visibilidad
        
        // Necesitamos dibujar desde arriba de la pantalla
        int yStart = -alturaTotal/3 - yLvlOffset;
        
        // Dibujar cada rayo
        for (RayoData rayo : rayos) {
            // Ancho del rayo (importante que sea visible)
            int anchoRayo = (int)(100 * Juego.SCALE);
            
            // Posición X central del rayo, ajustada por el offset del nivel
            int rayoX = (int)(rayo.x - xLvlOffset);
            
            // Si está en fase de advertencia, dibujar un rectángulo rojo translúcido
            if (rayo.warningPhase) {
                // Rojo semi-transparente
                g.setColor(new Color(255, 0, 0, 180));
                g.fillRect(rayoX - anchoRayo/2, yStart, anchoRayo, alturaTotal);
                
                // Añadir un borde más visible
                g.setColor(Color.RED);
                g.drawRect(rayoX - anchoRayo/2, yStart, anchoRayo, alturaTotal);
                
                // Añadir un texto de advertencia
                g.setColor(Color.WHITE);
                Font originalFont = g.getFont();
                g.setFont(new Font("Arial", Font.BOLD, 30));
                String warning = "¡CUIDADO!";
                int warningWidth = g.getFontMetrics().stringWidth(warning);
                
                // Dibujar el texto en varios puntos a lo largo del rayo
                for (int y = yStart + 100; y < yStart + alturaTotal; y += 200) {
                    if (y >= 0 && y <= Juego.GAME_HEIGHT) {
                        g.drawString(warning, rayoX - warningWidth/2, y);
                    }
                }
                g.setFont(originalFont);
                
                // Debug message
                System.out.println("Dibujando advertencia de rayo en X:" + rayoX + ", tiempo restante: " + rayo.tiempoAdvertencia);
            } 
            // Si está en fase de ataque, dibujar un rayo blanco brillante
            else {
                // Primero dibujamos un aura exterior para que destaque
                g.setColor(new Color(100, 100, 255, 150));
                g.fillRect(rayoX - anchoRayo/2 - 5, yStart, anchoRayo + 10, alturaTotal);
                
                // Ahora el rayo principal
                g.setColor(new Color(255, 255, 255, 200));
                g.fillRect(rayoX - anchoRayo/2, yStart, anchoRayo, alturaTotal);
                
                // Añadir un núcleo central más brillante
                g.setColor(Color.WHITE);
                g.fillRect(rayoX - 10, yStart, 20, alturaTotal);
                
                // Debug message
                System.out.println("Dibujando ataque de rayo en X:" + rayoX + ", frame: " + rayo.frame);
                
                // Si el jugador está dentro del rayo, mostrar efecto de impacto
                if (comprobarJugadorEnRayo(rayo) && Juego.jugadorActual != null) {
                    int jugadorX = (int)(Juego.jugadorActual.getXCenter() - xLvlOffset);
                    int jugadorY = (int)(Juego.jugadorActual.getYCenter() - yLvlOffset);
                    
                    // Dibujar un círculo brillante alrededor del jugador
                    g.setColor(Color.WHITE);
                    g.fillOval(jugadorX - 30, jugadorY - 30, 60, 60);
                    g.setColor(Color.RED);
                    g.drawOval(jugadorX - 30, jugadorY - 30, 60, 60);
                }
            }
        }
    }

    @Override
    protected void renderHealthBar(Graphics g, int xLvlOffset, int yLvlOffset) {
        if (!healthBarLoaded) {
            loadHealthBarSprites();
        }
        
        // Calculate health percentage
        float healthPercentage = (float) vida / vidaMaxima;
        
        // Select the appropriate sprite (0 = full health, 10 = empty)
        int spriteIndex = Math.min(17, 17 - (int)(healthPercentage * 17));
        
        // Position the health bar above the enemy
        int barX = (int) (hitbox.x + hitbox.width/2 - 32*Juego.SCALE) - xLvlOffset;
        int barY = (int) (hitbox.y - 60*Juego.SCALE) - yLvlOffset;
        
        // Draw the health bar with scaling
        g.drawImage(healthBarSprites[spriteIndex], 
                    barX, barY, 
                    (int)(64*Juego.SCALE), (int)(32*Juego.SCALE), null);
    }

    @Override
    public void recibirDaño(int cantidad, String tipoDaño) {
        if (!activo)
            return;

        float multiplicador = obtenerMultiplicadorDaño(tipoDaño);
        int dañoFinal = (int)(cantidad * multiplicador);

        vida -= dañoFinal;

        if (vida <= 0) {
            vida = 0;
            morir();
        }
    }

    @Override
    protected void disparar(float angulo) {
        // No tiene disparos convencionales
    }

    @Override
    protected void determinarAnimacion() {
        // No necesitamos lógica adicional, ya que la animación se controla por fase
    }
    
    @Override
    protected float obtenerMultiplicadorDaño(String tipoDaño) {
        if (tipoDaño == null) return 1.0f;
        
        switch (tipoDaño) {
            case "Corrosivo":
                return 0.5f; // Resistente a corrosión
            case "Luz":
                return 0.5f; // Débil a luz
            case "Mutagenico":
                return 1.5f; // Muy resistente a fuego
            default:
                return 1.0f;
        }
    }
    
    // Setters para configuración del rayo
    public void setRayoDamagePerSecond(int damage) {
        this.rayoDamagePerSecond = damage;
    }
    
    public void setRayoAdvertenciaDuracion(int frames) {
        this.rayoAdvertenciaDuracion = frames;
    }
    
    public void setRayoDuracionAtaque(int frames) {
        this.rayoDuracionAtaque = frames;
    }
    
    public boolean isActivated() {
        return activated;
    }
}