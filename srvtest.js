var net = require('net');
// var fs = require('fs');
// var url = require('url');
var dgram = require('dgram');
var express = require('express');
var app = express();
var server = require('http').Server(app);
var io = require('socket.io').listen(server);

app.use(express.static(__dirname));

var playerId;

app.get('/:user', function (req, res) {
    playerId = req.params.user;
    res.sendFile(__dirname + '/index.html');
});

// Ecouteur du broadcast
// var PORT = 2019;

// var client = dgram.createSocket('udp4');

// client.on('listening', function () {
//     var address = client.address();
//     console.log('UDP Client listening on ' + address.address + ":" + address.port);
//     client.setBroadcast(true);
// });
// client.bind(PORT);

var playersConnected = [];

io.on('connection', function (socket) {

    // client.on('message', function (message, remote) {

    //     console.log(message.toString());
    //     var msg = message.toString().replace(/\n/g, '').split('/');

    //     switch (msg[0]) {
    //         case 'NEWPLAYER':
    //             console.log('newplayer : ' + msg[1]);
    //             playersConnected.push(msg[1]);
    //             // io.emit('NEWPLAYER', playersConnected);
    //             break;
    //         case 'SESSION':
    //             console.log("session");
    //             var coords = msg[1].split('|');
    //             coords = coords.slice(0, coords.length - 1);
    //             console.log(coords);
    //             io.emit('SESSION', coords);
    //             break;
    //         default:
    //             break;
    //     }

    // });

    var sock = new net.Socket();
    sock.connect({
        port: 1664
    });

    sock.write('CONNECT/' + playerId + '\r\n');

    sock.on('data', function (data) {
        
        console.log(data.toString());
        var msg = data.toString().replace(/\n/g, '').split('/');

        switch (msg[0]) {
            case 'WELCOME':
                console.log('welcome');
                var cord = msg[3];
                var x = parseInt(cord.substring(cord.indexOf('X') + 1, cord.indexOf('Y')));
                var y = parseInt(cord.substring(cord.indexOf('Y') + 1));
                console.log({ cord: { x: x, y: y } });
                socket.emit('WELCOME', { cord: { x: x, y: y }, playerId: playerId });
                break;
            // case 'SESSION':
            //     console.log('session');

            //     break;

            case 'NEWPLAYER':
                console.log('newplayer : ' + msg[1]);
                playersConnected.push(msg[1]);
                // io.emit('NEWPLAYER', playersConnected);
                break;

            case 'SESSION':
                console.log("session");
                var coords = msg[1].split('|');
                console.log(coords);
                io.emit('SESSION', coords);
                break;

            case 'TICK':
                console.log("tick");
                var vcoords = msg[1].split('|');
                console.log(vcoords);
                io.emit('TICK', vcoords);
            default:
                console.log('default');
                break;
        }
    });


    socket.on('NEWCOM', function (comms) {
        console.log("NEWCOM")
        console.log(comms);
        sock.write('NEWCOM/A' + comms.rotation + 'T' + comms.step + '\r\n');
    });

    socket.on('disconnect', function () {
        delete playersConnected[playerId];
        io.emit('PLAYERLEFT', playerId);
        sock.write('EXIT/' + playerId + '\r\n')
    });

});

server.listen(8081, function () {
    console.log(`Listening on ${server.address().port}`);
});