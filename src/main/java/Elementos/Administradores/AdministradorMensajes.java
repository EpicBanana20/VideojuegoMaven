package Elementos.Administradores;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import Elementos.MensajeFlotante;

public class AdministradorMensajes {
    private ArrayList<MensajeFlotante> mensajes = new ArrayList<>();
    
    public void agregarMensaje(String texto, float x, float y, Color color, int duracion) {
        MensajeFlotante mensaje = new MensajeFlotante(texto, x, y, color, duracion);
        mensajes.add(mensaje);
    }
    
    public void update() {
        // Actualizar todos los mensajes y eliminar los inactivos
        ArrayList<MensajeFlotante> mensajesAEliminar = new ArrayList<>();
        
        for (MensajeFlotante mensaje : mensajes) {
            mensaje.update();
            if (!mensaje.isActivo()) {
                mensajesAEliminar.add(mensaje);
            }
        }
        
        mensajes.removeAll(mensajesAEliminar);
    }
    
    public void render(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (MensajeFlotante mensaje : mensajes) {
            mensaje.render(g, xLvlOffset, yLvlOffset);
        }
    }
}