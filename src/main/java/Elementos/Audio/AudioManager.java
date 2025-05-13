package Elementos.Audio;

import java.util.HashMap;
import java.util.Map;
import Juegos.EstadoJuego;

public class AudioManager {
    private static AudioManager instance;
    
    // Música actual y tracks
    private Music currentMusic;
    private Map<String, Music> musicTracks = new HashMap<>();
    private Map<String, SoundEffect> soundEffects = new HashMap<>();
    
    // Volúmenes
    private float musicVolume = 0.1f;
    private float sfxVolume = 0.05f;
    
    // Mapeo de estados y niveles a música
    private Map<EstadoJuego, String> gameStateMusicMap = new HashMap<>();
    private Map<Integer, String> levelMusicMap = new HashMap<>();
    
    // Control de estados
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private EstadoJuego estadoAnterior = null;
    private boolean isPaused = false;
    @SuppressWarnings("unused")
    private String currentMusicId = null;
    
    private AudioManager() {
        initializeMusicMappings();
    }
    
    private void initializeMusicMappings() {
        // Música para estados específicos
        gameStateMusicMap.put(EstadoJuego.MENU, "menu");
        gameStateMusicMap.put(EstadoJuego.OPCIONES, "menu");
        gameStateMusicMap.put(EstadoJuego.SELECCION_PERSONAJE, "menu");
        
        // Música para niveles
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
        if (!musicEnabled) {
            System.out.println("Música deshabilitada, no se reproducirá: " + id);
            return;
        }
        
        System.out.println(">>> Cambiando música a: " + id);
        
        // Primero detener cualquier música que esté sonando
        stopAllMusic();
        
        // Verificamos si es la misma música que ya está asignada
        if (currentMusic != null && musicTracks.get(id) == currentMusic) {
            // Si es la misma música pero no está reproduciendo, reiniciarla
            System.out.println("Reiniciando la música actual: " + id);
            currentMusic.play();
            currentMusicId = id;
            return;
        }
        
        // Iniciar la nueva música
        Music music = musicTracks.get(id);
        if (music != null) {
            currentMusic = music;
            currentMusicId = id;
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        } else {
            System.err.println("Música no encontrada: " + id);
        }
    }
    
    // Método nuevo para detener toda la música
    public void stopAllMusic() {
        // Detener todas las pistas de música para evitar superposiciones
        for (Music music : musicTracks.values()) {
            if (music != null && music.isPlaying()) {
                music.pause();
            }
        }
    }
    
    public void playSoundEffect(String id) {
        if (!soundEnabled)
            return;
            
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
        }
    }
    
    public void pauseMusic() {
        if (currentMusic != null && !isPaused && currentMusic.isPlaying()) {
            System.out.println("Pausando música");
            currentMusic.pause();
            isPaused = true;
        }
    }
    
    public void resumeMusic() {
        if (currentMusic != null && isPaused && musicEnabled) {
            System.out.println("Reanudando música");
            currentMusic.resume();
            isPaused = false;
        }
    }
    
    public void updateGameState(EstadoJuego state, int currentLevel) {
        System.out.println("Cambiando estado de juego: " + state + " (anterior: " + estadoAnterior + ")");
        
        // Caso especial: volviendo de PAUSA a PLAYING
        if (state == EstadoJuego.PLAYING && estadoAnterior == EstadoJuego.PAUSA) {
            resumeMusic();
        }
        // PLAYING (primer inicio o cambio de nivel)
        else if (state == EstadoJuego.PLAYING) {
            isPaused = false;
            String musicId = levelMusicMap.get(currentLevel);
            if (musicId != null) {
                playMusic(musicId);
            }
        }
        // PAUSA
        else if (state == EstadoJuego.PAUSA) {
            pauseMusic();
        }
        // MUERTE
        else if (state == EstadoJuego.MUERTE) {
            playSoundEffect("death");
            pauseMusic();
        }
        // Otros estados (MENU, OPCIONES, etc.)
        else {
            isPaused = false;
            String musicId = gameStateMusicMap.get(state);
            if (musicId != null) {
                playMusic(musicId);
            }
        }
        
        // Actualizar estado anterior
        estadoAnterior = state;
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
        System.out.println("Música " + (enabled ? "habilitada" : "deshabilitada"));
        this.musicEnabled = enabled;
        
        if (enabled && isPaused) {
            resumeMusic();
        } else if (!enabled) {
            stopAllMusic();
        }
    }
    
    public void setSoundEnabled(boolean enabled) {
        System.out.println("Efectos " + (enabled ? "habilitados" : "deshabilitados"));
        this.soundEnabled = enabled;
    }
    
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}