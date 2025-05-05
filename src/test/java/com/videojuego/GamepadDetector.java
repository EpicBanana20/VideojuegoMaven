package com.videojuego;

import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;

public class GamepadDetector {
    public static void main(String[] args) {
        // Inicializar GLFW
        if (!glfwInit()) {
            System.err.println("Error al inicializar GLFW");
            return;
        }
        
        System.out.println("Buscando mandos conectados...");
        
        // Verificar cada posible joystick/gamepad
        for (int i = GLFW_JOYSTICK_1; i <= GLFW_JOYSTICK_LAST; i++) {
            if (glfwJoystickPresent(i)) {
                String nombre = glfwGetJoystickName(i);
                boolean esGamepad = glfwJoystickIsGamepad(i);
                
                System.out.println("Encontrado: " + nombre + 
                                  " (ID: " + i + ") - " + 
                                  (esGamepad ? "Es gamepad" : "Es joystick"));
                
                // Mostrar botones y ejes si es un gamepad
                if (esGamepad) {
                    GLFWGamepadState estado = GLFWGamepadState.create();
                    if (glfwGetGamepadState(i, estado)) {
                        System.out.println("  Estado inicial de botones:");
                        for (int boton = 0; boton < GLFW.GLFW_GAMEPAD_BUTTON_LAST; boton++) {
                            System.out.println("  - Botón " + boton + ": " + 
                                              (estado.buttons(boton) == 1 ? "Presionado" : "No presionado"));
                        }
                    }
                }
            }
        }
        
        System.out.println("Presiona cualquier botón en tu mando para terminar");
        
        // Esperar un poco antes de terminar
        try {
            Thread.sleep(1000); // Esperar 10 segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Liberar recursos
        glfwTerminate();
    }
}