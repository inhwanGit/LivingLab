const app = require('express')();
const server = require('http').createServer(app);
const io = require('socket.io')(server);
server.listen(5000);

io.on('connection', (socket) => {
    console.log('User Conncetion');
    socket.on('message', (user) => {
        console.log(user);
        io.emit('message', user);
    });
    socket.on('all', (data) => {
        console.log(data);
        io.emit('all', data);
    });
    socket.on('reflect', (data) => {
        console.log(data);
        io.emit('reflect', data);
    });
    socket.on("user", (data) => {
        console.log(data);
        io.emit('user', data);
    });
    socket.on("location", (data) => {
        console.log(data);
        io.emit('location', data);
    });
});