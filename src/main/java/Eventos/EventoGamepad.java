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
    }
    
    public void update() {
        if (gamepadID == -1) {
            // Intentar detectar nuevamente
            detectarGamepad();
            return;
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
                procesarInputsMenu();
                break;
            case SELECCION_PERSONAJE:
                procesarInputsSeleccionPersonaje();
                break;
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
            int centerX = panelJuego.getWidth() / 2;
            int centerY = panelJuego.getHeight() / 2;
            
            int mouseX = centerX + (int)(rightX * 300);
            int mouseY = centerY + (int)(rightY * 300);
            
            panelJuego.getGame().updateMouseInfo(mouseX, mouseY);
        }
        
        // Pausa (botón Start)
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_START) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_START]) {
            panelJuego.getGame().setEstadoJuego(EstadoJuego.PAUSA);
        }
    }
    
    private void procesarInputsMenu() {
        // Navegar menús con D-pad o stick
        boolean up = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 1 || 
                    gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y) < -deadzone;
                    
        boolean down = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 1 || 
                      gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y) > deadzone;
                      
        // Seleccionar con A
        boolean select = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 1 && 
                        !prevButtonState[GLFW_GAMEPAD_BUTTON_A];
                        
        // Volver con B
        boolean back = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_B) == 1 && 
                      !prevButtonState[GLFW_GAMEPAD_BUTTON_B];
        
        // Implementar navegación simple basada en estado
        if (select) {
            simulateMouseClick();
        }
        
        if (up || down) {
            simulateMouseMove(down);
        }
        
        // Despausa con Start (en estado PAUSA)
        if (panelJuego.getGame().getEstadoJuego() == EstadoJuego.PAUSA && 
            gamepadState.buttons(GLFW_GAMEPAD_BUTTON_START) == 1 && 
            !prevButtonState[GLFW_GAMEPAD_BUTTON_START]) {
            panelJuego.getGame().setEstadoJuego(EstadoJuego.PLAYING);
        }
    }
    
    private void procesarInputsSeleccionPersonaje() {
        boolean up = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 1 || 
                    gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y) < -deadzone;
                    
        boolean down = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 1 || 
                      gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y) > deadzone;
                      
        boolean select = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 1 && 
                        !prevButtonState[GLFW_GAMEPAD_BUTTON_A];
                        
        boolean back = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_B) == 1 && 
                      !prevButtonState[GLFW_GAMEPAD_BUTTON_B];
        
        if (up && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP]) {
            simulateKey(java.awt.event.KeyEvent.VK_W);
        }
        
        if (down && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN]) {
            simulateKey(java.awt.event.KeyEvent.VK_S);
        }
        
        if (select) {
            simulateKey(java.awt.event.KeyEvent.VK_ENTER);
        }
        
        if (back) {
            simulateKey(java.awt.event.KeyEvent.VK_ESCAPE);
        }
    }
    
    // Métodos para simular eventos de mouse/teclado
    private void simulateMouseClick() {
        try {
            java.awt.Robot robot = new java.awt.Robot();
            robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void simulateMouseMove(boolean down) {
        try {
            java.awt.Robot robot = new java.awt.Robot();
            java.awt.Point p = java.awt.MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(p.x, p.y + (down ? 50 : -50));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void simulateKey(int keyCode) {
        try {
            java.awt.Robot robot = new java.awt.Robot();
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void shutdown() {
        glfwTerminate();
    }
}