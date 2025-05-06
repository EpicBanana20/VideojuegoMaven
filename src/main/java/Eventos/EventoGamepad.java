package Eventos;

import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;

import Juegos.PanelJuego;
import Juegos.EstadoJuego;

public class EventoGamepad {
    private PanelJuego panelJuego;
    private int gamepadID = -1;
    private GLFWGamepadState gamepadState;
    private float deadzone = 0.25f;
    
    // Para manejar estado anterior de botones
    private boolean[] prevButtonState = new boolean[GLFW_GAMEPAD_BUTTON_LAST + 1];
    private float lastValidRightX = 0;
    private float lastValidRightY = 0;
    private boolean joystickAimActive = false;

    private boolean gamepadEnabled = true;
    
    public EventoGamepad(PanelJuego panelJuego) {
        this.panelJuego = panelJuego;
        
        // Inicializar GLFW para detección de gamepad
        if (!glfwInit()) {
            System.err.println("Error al inicializar GLFW para detección de gamepad");
            return;
        }
        
        this.gamepadState = GLFWGamepadState.create();
        detectarGamepad();
    }
    
    public void detectarGamepad() {
        // Buscar mandos conectados
        for (int i = GLFW_JOYSTICK_1; i <= GLFW_JOYSTICK_LAST; i++) {
            if (glfwJoystickPresent(i) && glfwJoystickIsGamepad(i)) {
                gamepadID = i;
                System.out.println("Mando detectado: " + glfwGetJoystickName(i));
                return;
            }
        }
        System.out.println("No se detectó ningún mando. Conecta un mando y reinicia el juego.");
        gamepadID = -1;
    }
    
    public void update() {
        if(!gamepadEnabled || gamepadID == -1) {
            return; // Si el gamepad está deshabilitado, no procesar entradas
        }

        // Comprobar si el mando sigue conectado
        if (!glfwJoystickPresent(gamepadID)) {
            System.out.println("Mando desconectado");
            gamepadID = -1;
            return;
        }
        
        // Obtener estado del gamepad
        if (!glfwGetGamepadState(gamepadID, gamepadState)) {
            return;
        }
        
        // Procesar según el estado del juego
        switch (panelJuego.getGame().getEstadoJuego()) {
            case PLAYING:
                procesarInputsJuego();
                break;
            case MENU:
            case PAUSA:
            case MUERTE:
            case SELECCION_PERSONAJE:
        }
        
        // Actualizar estado anterior
        for (int i = 0; i < GLFW_GAMEPAD_BUTTON_LAST; i++) {
            prevButtonState[i] = gamepadState.buttons(i) == 1;
        }
    }
    
    private void procesarInputsJuego() {
        // Movimiento (stick izquierdo)
        float axisX = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_X);
        float axisY = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y);
        
        panelJuego.getGame().getPlayer().setLeft(axisX < -deadzone);
        panelJuego.getGame().getPlayer().setRight(axisX > deadzone);
        panelJuego.getGame().getPlayer().setDown(axisY > deadzone);
        
        // Salto
        panelJuego.getGame().getPlayer().setJump(axisY < -deadzone);

        // Disparar (botón RB)
        panelJuego.getGame().getPlayer().setAttacking(gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) > 0.5f);
        
        // Recargar (botón B)
        panelJuego.getGame().getPlayer().setHacerDodgeRoll(gamepadState.buttons(GLFW_GAMEPAD_BUTTON_X) == 1);

        if(gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) > 0.5f) {
            panelJuego.getGame().getPlayer().usarHabilidadEspecial(); 
        }


        
        // Cambiar arma (botón Y - solo al presionar)
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_Y) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_Y]) {
            panelJuego.getGame().getPlayer().cambiarArma();
        }
        
        // Interactuar (botón X - solo al presionar)
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_X) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_X]) {
            panelJuego.getGame().interactuarConEstacionQuimica();
        }
        
        
        // Apuntar con stick derecho
        float rightX = gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_X);
        float rightY = gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_Y);

        if (Math.abs(rightX) > deadzone || Math.abs(rightY) > deadzone) {
            // Al mover el stick fuera del deadzone, actualizar las últimas posiciones válidas
            lastValidRightX = rightX;
            lastValidRightY = rightY;
            joystickAimActive = true;
        }
        
        // Siempre que el joystick esté activo, usamos la última posición válida
        if (joystickAimActive) {
            panelJuego.getGame().updateAimFromGamepad(lastValidRightX, lastValidRightY);
        }
        // Pausa (botón Start)
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_START) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_START]) {
            panelJuego.getGame().setEstadoJuego(EstadoJuego.PAUSA);
        }
    }

    public void toggleGamepad() {
        gamepadEnabled = !gamepadEnabled;
        
        if (gamepadEnabled) {
            // Re-detect gamepad only when specifically toggling on
            detectarGamepad();
            if (gamepadID != -1) {
                System.out.println("Gamepad activado");
            } else {
                gamepadEnabled = false; // Couldn't find a gamepad
            }
        } else {
            System.out.println("Gamepad desactivado - Usando teclado y mouse");
            joystickAimActive = false; // Reset aim state
        }
    }
    
    public void shutdown() {
        glfwTerminate();
    }

    public boolean isGamepadEnabled() {
        return gamepadEnabled;
    }
    
    public boolean isJoystickAimActive() {
        return joystickAimActive;
    }
}