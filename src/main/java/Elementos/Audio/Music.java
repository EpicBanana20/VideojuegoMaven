package Elementos.Audio;

import java.io.IOException;
import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;

public class Music {
    private Clip clip;
    private FloatControl volumeControl;
    private boolean isPaused;
    private long pausePosition;
    private String sourcePath; // Guardar la ruta para poder reconstruir si es necesario

    public Music(String path) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.sourcePath = path;
        loadAudio();
    }

    private void loadAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        try {
            URL url = getClass().getClassLoader().getResource(sourcePath);
            if (url == null) {
                File file = new File("recursos/" + sourcePath);
                if (file.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                    init(audioStream);
                } else {
                    throw new IOException("No se pudo encontrar el archivo de audio: " + sourcePath);
                }
            } else {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
                init(audioStream);
            }
        } catch (Exception e) {
            System.err.println("Error cargando música: " + e.getMessage());
            throw e;
        }
    }

    private void init(AudioInputStream audioStream) throws LineUnavailableException, IOException {
        clip = AudioSystem.getClip();
        clip.open(audioStream);
        try {
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (IllegalArgumentException e) {
            System.err.println("Control de volumen no disponible: " + e.getMessage());
        }

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                if (!isPaused) {
                    clip.setMicrosecondPosition(0);
                    clip.start();
                }
            }
        });

        audioStream.close();
    }

    public void play() {
        if (clip != null) {
            if (clip.isActive()) {
                clip.stop();
            }
            clip.setMicrosecondPosition(0);
            clip.start();
            isPaused = false;
        } else {
            // Si el clip se cerró, intentar recargarlo
            try {
                loadAudio();
                clip.setMicrosecondPosition(0);
                clip.start();
                isPaused = false;
            } catch (Exception e) {
                System.err.println("Error al reproducir música: " + e.getMessage());
            }
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
            isPaused = false;
        }
    }

    public void pause() {
        if (clip != null && clip.isActive()) {
            pausePosition = clip.getMicrosecondPosition();
            clip.stop();
            isPaused = true;
        }
    }

    public void resume() {
        if (clip != null && isPaused) {
            clip.setMicrosecondPosition(pausePosition);
            clip.start();
            isPaused = false;
        }
    }

    public void setVolume(float volume) {
        if (volumeControl != null) {
            float dB = 20f * (float) Math.log10(volume);
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            dB = Math.max(min, Math.min(max, dB));
            volumeControl.setValue(dB);
        }
    }

    public boolean isPlaying() {
        return clip != null && clip.isActive() && !isPaused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void cleanup() {
        // No cerramos el clip completamente, solo lo detenemos
        if (clip != null) {
            clip.stop();
            // NO usar clip.close() aquí
        }
    }

    
    
    public void dispose() {
        // Método para liberar recursos completamente cuando sea necesario
        // (por ejemplo, al cerrar el juego)
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}