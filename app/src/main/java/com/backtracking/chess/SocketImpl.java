package com.backtracking.chess;

import org.json.JSONObject;

import java.net.URI;

import io.socket.client.IO;
import io.socket.engineio.client.transports.WebSocket;

public class SocketImpl implements Socket {
    final String URL = "https://android-socket-server-soeq.vercel.app";
    private io.socket.client.Socket socket;
    private JSONObject message;
    private String currentPlayer = null;
    private String typePlayer;
    private SocketImpl() {
        socket = newSocket();
    }

    public static SocketImpl getInstance() {
        return SocketHolder.INSTANCE;
    }

    private io.socket.client.Socket newSocket() {
        IO.Options options = IO.Options.builder()
                .setForceNew(true)
                .setTransports(new String[]{WebSocket.NAME})
                .build();

        io.socket.client.Socket mSocket = IO.socket(URI.create(URL), options);

        mSocket.on("connect", args -> {
            System.out.println("Connected to server");
        });
        return mSocket;
    }

    private static class SocketHolder {
        private static final SocketImpl INSTANCE = new SocketImpl();
    }

    @Override
    public void sendMessage(String message, JSONObject content) {
        socket.emit(message, content);
    }

    @Override
    public void connect() {
        socket.connect();
    }

    @Override
    public void disconnect() {
        socket.disconnect();
    }

    @Override
    public void findMatch() {
        socket.emit("find_match");
    }

    public Boolean isConnected() {
        return socket.connected();
    }

    public void on(String event, io.socket.emitter.Emitter.Listener listener) {
        socket.on(event, listener);
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setTypePlayer(String typePlayer) {
        this.typePlayer = typePlayer;
    }

    public String getTypePlayer() {
        return typePlayer;
    }

    public void setMessage(JSONObject message) {
        this.message = message;
    }

    public JSONObject getMessage() {
        return message;
    }
}
