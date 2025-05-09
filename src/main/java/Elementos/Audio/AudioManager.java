package Elementos.Audio;

import java.io.IOException;
import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import Juegos.EstadoJuego;

public class AudioManager {
    
    // Singleton para acceso global
    private static AudioManager instance;
    
    // Música actual reproduciéndose
    private Music currentMusic;
    
    // Mapas para almacenar música y efectos de sonido
    private Map<String, Music> musicTracks = new HashMap<>();
    private Map<String, SoundEffect> soundEffects = new HashMap<>();
    
    // Controles de volumen (0.0 a 1.0)
    private float musicVolume = 0.5f;
    private float sfxVolume = 0.7f;
    
    // Mapa para asociar estados del juego con pistas de música
    private Map<EstadoJuego, String> gameStateMusicMap = new HashMap<>();
    
    // Mapa para asociar niveles con pistas de música
    private Map<Integer, String> levelMusicMap = new HashMap<>();
    
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    
    private AudioManager() {
        initializeMusicMappings();
    }
    
    private void initializeMusicMappings() {
        // Mapeo de estados del juego a pistas de música
        gameStateMusicMap.put(EstadoJuego.MENU, "menu");
        gameStateMusicMap.put(EstadoJuego.SELECCION_PERSONAJE, "menu");
        
        // Mapeo de niveles a pistas de música
        levelMusicMap.put(0, "world1");
        levelMusicMap.put(1, "world2");
        levelMusicMap.put(2, "world3");
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    public void loadMusic(String id, String path) {
        try {
            Music music = new Music(path);
            musicTracks.put(id, music);
            System.out.println("Música cargada: " + id);
        } catch (Exception e) {
            System.err.println("Error al cargar música '" + id + "': " + e.getMessage());
        }
    }
    
    public void loadSoundEffect(String id, String path) {
        try {
            SoundEffect sound = new SoundEffect(path);
            soundEffects.put(id, sound);
            System.out.println("Efecto de sonido cargado: " + id);
        } catch (Exception e) {
            System.err.println("Error al cargar efecto '" + id + "': " + e.getMessage());
        }
    }
    
    public void playMusic(String id) {
        if (!musicEnabled) return;
        
        // Detener la música actual si hay alguna
        stopMusic();
        
        Music music = musicTracks.get(id);
        if (music != null) {
            currentMusic = music;
            music.setVolume(musicVolume);
            music.play();
            System.out.println("Reproduciendo música: " + id);
        } else {
            System.err.println("Música no encontrada: " + id);
        }
    }
    
    public void playSoundEffect(String id) {
        if (!soundEnabled) return;
        
        SoundEffect sound = soundEffects.get(id);
        if (sound != null) {
            sound.setVolume(sfxVolume);
            sound.play();
        } else {
            System.err.println("Efecto de sonido no encontrado: " + id);
        }
    }
    
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }
    
    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }
    
    public void resumeMusic() {
        if (currentMusic != null && musicEnabled) {
            currentMusic.resume();
        }
    }
    
    public void updateGameState(EstadoJuego state, int currentLevel) {
        // Si es PLAYING, usar la música del nivel actual
        if (state == EstadoJuego.PLAYING) {
            String musicId = levelMusicMap.get(currentLevel);
            if (musicId != null) {
                playMusic(musicId);
            }
        } 
        // Para otros estados, usar la música asociada al estado
        else {
            String musicId = gameStateMusicMap.get(state);
            if (musicId != null) {
                playMusic(musicId);
            } else if (state == EstadoJuego.PAUSA) {
                // En pausa, simplemente pausar la música actual
                pauseMusic();
            } else if (state == EstadoJuego.MUERTE) {
                // Posiblemente efecto de sonido de muerte y pausar música
                playSoundEffect("death");
                pauseMusic();
            }
        }
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }
    
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        } else {
            // Reanudar la música del estado actual si está disponible
            // (Esto requeriría acceso al estado actual del juego)
        }
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public void cleanup() {
        stopMusic();
        
        // Liberar recursos
        for (Music music : musicTracks.values()) {
            music.cleanup();
        }
        for (SoundEffect sound : soundEffects.values()) {
            sound.cleanup();
        }
        
        musicTracks.clear();
        soundEffects.clear();
    }
}