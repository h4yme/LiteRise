package com.example.literise.models;

import com.google.gson.JsonObject;

/**
 * Response from POST /api/generate_game_content.php
 * The `content` field contains game-specific JSON that varies by game_type.
 */
public class GameContentResponse {
    public boolean    success;
    public String     game_type;
    public boolean    from_cache;
    public String     message;
    public JsonObject content;   // parsed game-specific payload
}
