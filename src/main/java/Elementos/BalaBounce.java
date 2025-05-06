package Elementos;

import Juegos.Juego;

public class BalaBounce extends Bala {
    private int rebotesActuales = 1;
    private int rebotesMaximos = 10;
    private float anguloInterno; // Guarda el ángulo real de movimiento
    private int cooldownRebote = 1; // Para evitar múltiples rebotes seguidos

    public BalaBounce(float x, float y, float angulo, String tipoSprite, int daño, float velocidad) {
        super(x, y, angulo, tipoSprite, daño, velocidad);
        this.anguloInterno = angulo;
    }

    public BalaBounce(float x, float y, float angulo, String tipoSprite, int daño, float velocidad, String tipoDaño) {
        super(x, y, angulo, tipoSprite, daño, velocidad, tipoDaño);
        this.anguloInterno = angulo;
    }

    public BalaBounce(float x, float y, float angulo, String tipoSprite, int daño, float velocidad, String tipoDaño, int rebotesMaximos) {
        super(x, y, angulo, tipoSprite, daño, velocidad, tipoDaño);
        this.rebotesMaximos = rebotesMaximos;
        this.anguloInterno = angulo;
    }

    @Override
    public void update() {
        if (cooldownRebote > 0) {
            cooldownRebote--;
        }

        float oldX = hitbox.x;
        float oldY = hitbox.y;

        super.update();

        float movX = hitbox.x - oldX;
        float movY = hitbox.y - oldY;

        if (movX != 0 || movY != 0) {
            this.anguloInterno = (float) Math.atan2(movY, movX);
        }
    }

    @Override
    public boolean colisionConBloque() {
        if (cooldownRebote > 0) return false;

        float centerX = hitbox.x + hitbox.width / 2;
        float centerY = hitbox.y + hitbox.height / 2;

        boolean colisionArriba = Utilz.MetodoAyuda.isSolido(centerX, hitbox.y, Juego.NIVEL_ACTUAL_DATA);
        boolean colisionAbajo = Utilz.MetodoAyuda.isSolido(centerX, hitbox.y + hitbox.height, Juego.NIVEL_ACTUAL_DATA);
        boolean colisionIzquierda = Utilz.MetodoAyuda.isSolido(hitbox.x, centerY, Juego.NIVEL_ACTUAL_DATA);
        boolean colisionDerecha = Utilz.MetodoAyuda.isSolido(hitbox.x + hitbox.width, centerY, Juego.NIVEL_ACTUAL_DATA);

        if (colisionArriba || colisionAbajo || colisionIzquierda || colisionDerecha) {
            if (rebotesActuales >= rebotesMaximos) {
                this.desactivar();
                return true;
            }

            rebotesActuales++;

            float dx = (float) Math.cos(anguloInterno);
            float dy = (float) Math.sin(anguloInterno);

            if ((colisionArriba && dy < 0) || (colisionAbajo && dy > 0)) dy = -dy;
            if ((colisionIzquierda && dx < 0) || (colisionDerecha && dx > 0)) dx = -dx;

            float nuevoAngulo = (float) Math.atan2(dy, dx);
            this.angulo = nuevoAngulo;
            this.anguloInterno = nuevoAngulo;
            

            // Sacar la bala del bloque
            hitbox.x += dx * 10;
            hitbox.y += dy * 10;

            cooldownRebote = 0;

            return true;
        }

        return false;
    }

    public void setRebotesMaximos(int rebotesMaximos) {
        this.rebotesMaximos = rebotesMaximos;
    }

    public int getRebotesMaximos() {
        return rebotesMaximos;
    }

    public int getRebotesActuales() {
        return rebotesActuales;
    }
}
