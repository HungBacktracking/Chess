
const express = require('express');
const http = require('http');
const socketIO = require('socket.io');
const app = express();
const server = http.createServer(app);
const IP = require('ip');
const io = socketIO(server, {
    maxHttpBufferSize: 1e7,
    cors: {
        "origin": "*",
        "methods": "GET,HEAD,PUT,PATCH,POST,DELETE",
        "preflightContinue": false,
        "optionsSuccessStatus": 204
    }
});

const SocketMsgModel = require('./msg.model.js');

io.eio.pingTimeout = 120000;
io.eio.pingInterval = 120000;
rooms = new Map();
app.use(express.json())
let availablePlayer = null;

io.on('connection', (socket) => {
    console.log(`User ${socket.id} connected`);

    socket.on('disconnect', () => {
        console.log(`User ${socket.id} disconnected`);
        if(availablePlayer == socket.id){
            availablePlayer = null;
        }
    });
    
    socket.on('find_match', () => {
        console.log(`${socket.id} find_match`);
        if(availablePlayer == socket.id){
            return;
        }
        if (availablePlayer != null) {
            console.log(`match found with ${availablePlayer} and ${socket.id}`);
            const socketMsg = new SocketMsgModel({
                competitorSocketId: socket.id,
                yourTurn: true
            });

            const socketMsg2 = new SocketMsgModel({
                competitorSocketId: availablePlayer,
                yourTurn: false
            });


            io.to(availablePlayer).emit('found_match', socketMsg);
            io.to(socket.id).emit('found_match', socketMsg2);

            availablePlayer = null;
        } else {
            availablePlayer = socket.id;
        }
    });

    socket.on('move', (msg) => {
        const socketMsg = SocketMsgModel.fromJson(msg);
        console.log(`${socket.id} move`);
        console.log(socketMsg);
        socket.to(socketMsg.competitorSocketId).emit('move', msg);
    })

});

// Route handler for GET request to the root URL ("/")
app.get('/', (req, res) => {
    console.log('GET /');
    res.send('Hello, World!');
});

server.listen(3000, () => {
    console.log(`Server is running on http://${IP.address()}:${3000}`);
});


