
var net = require('net');
var http = require('http');
var fs = require('fs');
var url = require('url');
var dgram = require('dgram');


// Ecouter du broadcast
var PORT = 2019;

var client = dgram.createSocket('udp4');

client.on('listening', function () {
    var address = client.address();
    console.log('UDP Client listening on ' + address.address + ":" + address.port);
    client.setBroadcast(true);
});

client.on('message', function (message, remote) {

    console.log(message.toString());

});



client.bind(PORT);

// fin du broadCast



// Communication avec le navigateur
var server = http.createServer((req, res) => {

	var page = url.parse(req.url).pathname;

	if (page == '/') {

		fs.readFile('./index.html', 'utf-8', function(error, content) {

		    res.writeHead(200, {"Content-Type": "text/html"});

		    res.end(content);

    	});
    }
    else if (page == '/app.js') {
    	fs.readFile('./app.js', 'utf-8', function(error, content) {

		    res.writeHead(200, {"Content-Type": "text/javascript"});

		    res.end(content);

    	});
    }
    else if (page == '/phaser.min.js') {
    	fs.readFile('./phaser.min.js', 'utf-8', function(error, content) {

		    res.writeHead(200, {"Content-Type": "text/javascript"});

		    res.end(content);

    	});
    }
    else if (page == '/car.png') {
    	fs.readFile('./car.png', function(error, content) {
		    res.writeHead(200, {"Content-Type": "image/png"});

		    res.end(content);

    	});
    }
    else if (page == '/b.png') {
    	fs.readFile('./b.png', function(error, content) {
		    res.writeHead(200, {"Content-Type": "image/png"});

		    res.end(content);

    	});
    }
    else if (page == '/car2.png') {
    	fs.readFile('./car2.png', function(error, content) {
		    res.writeHead(200, {"Content-Type": "image/png"});

		    res.end(content);

    	});
    }
	
});

var io = require('socket.io').listen(server);

io.sockets.on('connection', function (socket) {

    socket.emit('message', 'Vous êtes bien connecté !');

    socket.on('move', function(data){
    	console.log(data);
	});
    
});



server.listen(8000);


// fin de la comm

// var sock = net.createConnection(1664,"localhost");
// // sock.write('CONNECT/riri\r\n');
// sock.write('Hi\r\n');

// console.log(sock.bytesRead)

var sock  = new net.Socket();
sock.connect({
  port:1664
});
sock.write('CONNECT/riri\r\n');
//sock.write('Hi\r\n');
sock.on('data', function(data){
    console.log(data.toString())
})

