package com.example.literise.models;



import com.google.gson.annotations.SerializedName;



/**

 * Request model for saving game results

 * Used with save_game_results.php API

 */

public class SaveGameResultRequest {



    @SerializedName("session_id")

    private Integer sessionId;



    @SerializedName("student_id")

    private int studentId;



    @SerializedName("game_type")

    private String gameType;



    @SerializedName("score")

    private int score;



    @SerializedName("accuracy_percentage")

    private float accuracyPercentage;



    @SerializedName("time_completed")

    private int timeCompleted;



    @SerializedName("xp_earned")

    private int xpEarned;



    @SerializedName("streak_achieved")

    private int streakAchieved;



    @SerializedName("lesson_id")

    private Integer lessonId;



    public SaveGameResultRequest() {}



    public SaveGameResultRequest(int studentId, String gameType, int score) {

        this.studentId = studentId;

        this.gameType = gameType;

        this.score = score;

    }



    // Builder pattern for cleaner construction

    public static class Builder {

        private final SaveGameResultRequest request;



        public Builder(int studentId, String gameType, int score) {

            request = new SaveGameResultRequest(studentId, gameType, score);

        }



        public Builder sessionId(Integer sessionId) {

            request.sessionId = sessionId;

            return this;

        }



        public Builder accuracyPercentage(float accuracy) {

            request.accuracyPercentage = accuracy;

            return this;

        }



        public Builder timeCompleted(int timeInSeconds) {

            request.timeCompleted = timeInSeconds;

            return this;

        }



        public Builder xpEarned(int xp) {

            request.xpEarned = xp;

            return this;

        }



        public Builder streakAchieved(int streak) {

            request.streakAchieved = streak;

            return this;

        }



        public Builder lessonId(Integer lessonId) {

            request.lessonId = lessonId;

            return this;

        }



        public SaveGameResultRequest build() {

            return request;

        }

    }



    // Getters and Setters

    public Integer getSessionId() {

        return sessionId;

    }



    public void setSessionId(Integer sessionId) {

        this.sessionId = sessionId;

    }



    public int getStudentId() {

        return studentId;

    }



    public void setStudentId(int studentId) {

        this.studentId = studentId;

    }



    public String getGameType() {

        return gameType;

    }



    public void setGameType(String gameType) {

        this.gameType = gameType;

    }



    public int getScore() {

        return score;

    }



    public void setScore(int score) {

        this.score = score;

    }



    public float getAccuracyPercentage() {

        return accuracyPercentage;

    }



    public void setAccuracyPercentage(float accuracyPercentage) {

        this.accuracyPercentage = accuracyPercentage;

    }



    public int getTimeCompleted() {

        return timeCompleted;

    }



    public void setTimeCompleted(int timeCompleted) {

        this.timeCompleted = timeCompleted;

    }



    public int getXpEarned() {

        return xpEarned;

    }



    public void setXpEarned(int xpEarned) {

        this.xpEarned = xpEarned;

    }



    public int getStreakAchieved() {

        return streakAchieved;

    }



    public void setStreakAchieved(int streakAchieved) {

        this.streakAchieved = streakAchieved;

    }



    public Integer getLessonId() {

        return lessonId;

    }



    public void setLessonId(Integer lessonId) {

        this.lessonId = lessonId;

    }

}