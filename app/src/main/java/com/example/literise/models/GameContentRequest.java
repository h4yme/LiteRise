package com.example.literise.models;

/**
 * Request body for POST /api/generate_game_content.php
 */
public class GameContentRequest {
    public int    node_id;
    public String game_type;
    public String lesson_content;

    public GameContentRequest(int nodeId, String gameType, String lessonContent) {
        this.node_id       = nodeId;
        this.game_type     = gameType;
        this.lesson_content = lessonContent;
    }
}
