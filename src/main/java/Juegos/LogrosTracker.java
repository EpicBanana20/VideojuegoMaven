package Juegos;

import java.io.*;
import Elementos.Personaje;

public class LogrosTracker {
    private static final String SAVE_FILE = "logros.dat";
    private boolean[] achievementsUnlocked;
    private static final int TOTAL_ACHIEVEMENTS = 7;
    
    // logros indices
    public static final int LAST_HOPE = 0;
    public static final int CABALLERO_CAIDO = 1;
    public static final int AULLIDO_INTERNO = 2;
    public static final int CURA_FATAL = 3;
    public static final int VELOCIDAD_LETAL = 4;
    public static final int COLECCIONISTA = 5;
    public static final int INTOCABLE = 6;
    

    private boolean hasTakenDamage = false;
    
    public LogrosTracker() {
        achievementsUnlocked = new boolean[TOTAL_ACHIEVEMENTS];
        loadAchievements();
    }
    
    private void loadAchievements() {
        try {
            File file = new File(SAVE_FILE);
            if (!file.exists()) {
                return; 
            }
            
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            achievementsUnlocked = (boolean[]) ois.readObject();
            
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading achievements: " + e.getMessage());
        }
    }
    
    public void saveAchievements() {
        try {
            FileOutputStream fos = new FileOutputStream(SAVE_FILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            oos.writeObject(achievementsUnlocked);
            
            oos.close();
            fos.close();
        } catch (IOException e) {
            System.err.println("Error saving achievements: " + e.getMessage());
        }
    }
    
    public void unlockAchievement(int index) {
        if (index >= 0 && index < TOTAL_ACHIEVEMENTS) {
            achievementsUnlocked[index] = true;
            saveAchievements();
            System.out.println("¡Logro desbloqueado: " + getAchievementName(index) + "!");
        }
    }
    
    public boolean isAchievementUnlocked(int index) {
        if (index >= 0 && index < TOTAL_ACHIEVEMENTS) {
            return achievementsUnlocked[index];
        }
        return false;
    }
    
    public boolean areAllAchievementsUnlocked() {
        for (boolean unlocked : achievementsUnlocked) {
            if (!unlocked) return false;
        }
        return true;
    }
    
    private String getAchievementName(int index) {
        switch (index) {
            case LAST_HOPE: return "The Last Hope";
            case CABALLERO_CAIDO: return "Caballero caído";
            case AULLIDO_INTERNO: return "Aullido interno";
            case CURA_FATAL: return "La cura fatal";
            case VELOCIDAD_LETAL: return "Velocidad Letal";
            case COLECCIONISTA: return "Coleccionista";
            case INTOCABLE: return "Intocable";
            default: return "Desconocido";
        }
    }
    

    public void gameCompleted(Personaje.TipoPersonaje personajeType, long completionTimeSeconds) {
        switch (personajeType) {
            case ECLIPSA: 
                unlockAchievement(AULLIDO_INTERNO);
                break;
            case HALAN:
                unlockAchievement(CURA_FATAL);
                break;
            case VALTHOR:
                unlockAchievement(CABALLERO_CAIDO);
                break;
        }
        
        // VELOCIDAD LETAL
        if (completionTimeSeconds < 600) {
            unlockAchievement(VELOCIDAD_LETAL);
        }
        
        // INTOCABLE
        if (!hasTakenDamage) {
            unlockAchievement(INTOCABLE);
        }
        
        // Last Hope
        if (areAllOthersUnlocked()) {
            unlockAchievement(LAST_HOPE);
        }
    }
    
    private boolean areAllOthersUnlocked() {
        for (int i = 1; i < TOTAL_ACHIEVEMENTS; i++) {
            if (!achievementsUnlocked[i]) {
                return false;
            }
        }
        return true;
    }
    

    public void allWeaponsCollected() {
        unlockAchievement(COLECCIONISTA);
    }

    public void playerTookDamage() {
        hasTakenDamage = true;
    }
    
    public void resetDamageTracking() {
        hasTakenDamage = false;
    }
    
    public void reset() {
        for (int i = 0; i < TOTAL_ACHIEVEMENTS; i++) {
            achievementsUnlocked[i] = false;
        }
        hasTakenDamage = false;
        saveAchievements();
    }
}