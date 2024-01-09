/// 0. WebSocket(Socket.IO) - Client
const io_c = require('socket.io-client'),
    socket = io_c("http://112.150.84.136:5010"),
    readline = require("readline"),
    rl = readline.createInterface({ input: process.stdin, output: process.stdout, });

let userName = "";
socket.on('connection', _ => console.log("connection"));
rl.question("Name : ", (name) => { userName = name; });
rl.on("line", line => {
    // console.log(`😎 ${userName} : ${line}`);
    socket.emit("CHAT", { userName: userName, chat: line });
});
socket.on('CHAT', data => {
    const { userName, chat } = data;
    console.log(`😀 ${userName} : ${chat}`);
});