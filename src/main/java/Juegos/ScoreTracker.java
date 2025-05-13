package Juegos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class ScoreTracker {
    private long startTime;
    private int enemiesKilled;
    private int weaponsCollected;
    private int finalScore;
    private boolean gameCompleted;
    private static final String SCORES_FILE = "scores.txt";
    
    private static final int POINTS_PER_ENEMY = 100;
    private static final int POINTS_PER_WEAPON = 500;
    private static final int TIME_PENALTY_PER_MINUTE = 50;
    private static final int BASE_SCORE = 5000;
    
    private boolean paused = false;
    private long pausedTime = 0;

    public ScoreTracker() {
        startTime = System.currentTimeMillis();
        enemiesKilled = 0;
        weaponsCollected = 0;
        gameCompleted = false;
    }
    
    public void enemyKilled() {
        enemiesKilled++;
    }
    
    public void weaponCollected() {
        weaponsCollected++;
    }
    
    public void gameCompleted() {
        gameCompleted = true;
        calculateFinalScore();
    }
    
    private void calculateFinalScore() {
        if (!gameCompleted) {
            return;
        }
        
        // Calculate time in minutes
        long gameTimeMillis = System.currentTimeMillis() - startTime;
        double gameTimeMinutes = gameTimeMillis / (1000.0 * 60);
        
        // Calculate score components
        int enemyScore = enemiesKilled * POINTS_PER_ENEMY;
        int weaponScore = weaponsCollected * POINTS_PER_WEAPON;
        int timePenalty = (int)(gameTimeMinutes * TIME_PENALTY_PER_MINUTE);
        
        // Calculate final score
        finalScore = BASE_SCORE + enemyScore + weaponScore - timePenalty;
        
        // Ensure score doesn't go negative
        if (finalScore < 0) {
            finalScore = 0;
        }
    }
    
    public void saveScore(String playerName) {
        if (!gameCompleted) {
            return;
        }
        
        try {
            File file = new File(SCORES_FILE);

            if (!file.exists()) {
                file.createNewFile();
            }
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = dateFormat.format(new Date());

            String scoreEntry = String.format("%s,%d,%d,%d,%d,%s\n", 
                playerName, finalScore, enemiesKilled, weaponsCollected, 
                (System.currentTimeMillis() - startTime) / 1000, date);

            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(scoreEntry);
            bw.close();
            
        } catch (IOException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }
    }
    
    public static List<ScoreEntry> getTopScores(int limit) {
        List<ScoreEntry> scores = new ArrayList<>();
        
        try {
            File file = new File(SCORES_FILE);
            if (!file.exists()) {
                return scores;
            }
            
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            
            while ((line = br.readLine()) != null) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        String name = parts[0];
                        int score = Integer.parseInt(parts[1]);
                        int kills = Integer.parseInt(parts[2]);
                        int weapons = Integer.parseInt(parts[3]);
                        long time = Long.parseLong(parts[4]);
                        String date = parts[5];
                        
                        scores.add(new ScoreEntry(name, score, kills, weapons, time, date));
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid lines
                    System.err.println("Invalid score entry: " + line);
                }
            }
            
            br.close();

            Collections.sort(scores, new Comparator<ScoreEntry>() {
                @Override
                public int compare(ScoreEntry s1, ScoreEntry s2) {
                    return Integer.compare(s2.getScore(), s1.getScore());
                }
            });

            if (scores.size() > limit) {
                scores = scores.subList(0, limit);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading scores: " + e.getMessage());
        }
        
        return scores;
    }

    public void pauseTimer() {
        if (!paused) {
            pausedTime = System.currentTimeMillis();
            paused = true;
        }
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }
    
    public int getWeaponsCollected() {
        return weaponsCollected;
    }
    
    public long getElapsedTimeSeconds() {
        if (paused) {
            return (pausedTime - startTime) / 1000;
        } else {
            return (System.currentTimeMillis() - startTime) / 1000;
        }
    }
    
    public int getFinalScore() {
        return finalScore;
    }
    
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public static class ScoreEntry {
        private String playerName;
        private int score;
        private int kills;
        private int weapons;
        private long timeSeconds;
        private String date;
        
        public ScoreEntry(String playerName, int score, int kills, int weapons, long timeSeconds, String date) {
            this.playerName = playerName;
            this.score = score;
            this.kills = kills;
            this.weapons = weapons;
            this.timeSeconds = timeSeconds;
            this.date = date;
        }
        
        public String getPlayerName() { return playerName; }
        public int getScore() { return score; }
        public int getKills() { return kills; }
        public int getWeapons() { return weapons; }
        public long getTimeSeconds() { return timeSeconds; }
        public String getDate() { return date; }
        
        public String getFormattedTime() {
            long minutes = timeSeconds / 60;
            long seconds = timeSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}