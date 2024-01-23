package com.backtracking.chess;

import org.json.JSONObject;

import java.net.URI;

import io.socket.client.IO;
import io.socket.engineio.client.transports.WebSocket;

public class SocketImpl implements Socket {
    final String URL = "http://192.168.1.2:3000";
    private io.socket.client.Socket socket;
    private JSONObject message;
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
    public void setMessage(JSONObject message) {
        this.message = message;
    }

    public JSONObject getMessage() {
        return message;
    }
}
