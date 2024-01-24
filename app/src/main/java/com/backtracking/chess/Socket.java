package com.backtracking.chess;

import org.json.JSONObject;

public interface Socket {
    void sendMessage(String message, JSONObject content);
    void connect();
    void disconnect();
    void findMatch();
}
