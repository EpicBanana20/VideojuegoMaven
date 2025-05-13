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

        System.out.println(">>> Intentando reproducir música: " + id);
        stopMusic();
        Music music = musicTracks.get(id);
        if (music == null) {
            System.err.println("Música no encontrada: " + id);
            return;
        }

        // Si es la misma música y no está pausada, no hacer nada
        if (currentMusic == music && music.isPlaying()) {
            System.out.println("La música ya está sonando, no se cambiará");
            return;
        }

        // Detener la música actual (sin cerrarla)
        if (currentMusic != null) {
            currentMusic.stop();
        }

        // Iniciar la nueva música
        currentMusic = music;
        currentMusic.setVolume(musicVolume);
        try {
            currentMusic.play();
            System.out.println("Reproduciendo música: " + id);
        } catch (Exception e) {
            System.err.println("Error al reproducir música: " + e.getMessage());
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
            currentMusic.pause();
        }
    }

    public void updateGameState(EstadoJuego state, int currentLevel) {
        System.out.println("Cambiando estado de juego: " + state + " (anterior: " + estadoAnterior + ")");

        if(state == EstadoJuego.PAUSA && estadoAnterior == EstadoJuego.PLAYING){
            currentMusic.resume();
        }

        // MUERTE - solo reproducir efecto de sonido
        if (state == EstadoJuego.MUERTE) {
            playSoundEffect("death");
        }
        // PLAYING
        else if (state == EstadoJuego.PLAYING) {
            String musicId = levelMusicMap.get(currentLevel);
            if (musicId != null) {
                playMusic(musicId);
            }
        }
        else if (state == EstadoJuego.MENU) { // No cambiar música en PAUSA
            String musicId = gameStateMusicMap.get(state);
            if (musicId != null) {
                playMusic(musicId);
            }
        }

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

        if (!enabled && currentMusic != null) {
            stopMusic();
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