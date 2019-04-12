var net = require('net');
var http = require('http');
var fs = require('fs');
var url = require('url');

//var sock = net.createConnection(1664,"localhost");




var server = http.createServer((req, res) => {

	var page = url.parse(req.url).pathname;

	if (page == '/') {

		fs.readFile('./index.html', 'utf-8', function (error, content) {

			res.writeHead(200, {
				"Content-Type": "text/html"
			});

			res.end(content);

		});
	} else if (page == '/app.js') {
		fs.readFile('./app.js', 'utf-8', function (error, content) {

			res.writeHead(200, {
				"Content-Type": "text/javascript"
			});

			res.end(content);

		});
	} else if (page == '/phaser.min.js') {
		fs.readFile('./phaser.min.js', 'utf-8', function (error, content) {

			res.writeHead(200, {
				"Content-Type": "text/javascript"
			});

			res.end(content);

		});
	} else if (page == '/car.png') {
		fs.readFile('./car.png', function (error, content) {
			res.writeHead(200, {
				"Content-Type": "image/png"
			});

			res.end(content);

		});
	} else if (page == '/b.png') {
		fs.readFile('./b.png', function (error, content) {
			res.writeHead(200, {
				"Content-Type": "image/png"
			});

			res.end(content);

		});
	} else if (page == '/car2.png') {
		fs.readFile('./car2.png', function (error, content) {
			res.writeHead(200, {
				"Content-Type": "image/png"
			});

			res.end(content);

		});
	}

});

var io = require('socket.io').listen(server);

io.sockets.on('connection', function (socket) {

	socket.emit('message', 'Vous êtes bien connecté !');

	socket.on('move', function (data) {
		console.log(data);
	});

	/**
	 * Structuration
	 */
	var players = {};

	players[socket.id] = {
		angle: 0,
		x: 0, //Faire un random
		y: 0, //Faire un random
		playerId: socket.id,
		playerPseudo: "yetnehaw_Ga3"
	  };

	// Map de scores intermédiare (Besoin d'envoyer à serv Java ??)
	var scores = [{
		id: "playerId",
		score: 0
	}];

	var award = { // Besoin d'envoyer à serv Java ??
		x: 0, //Faire un random
		y: 0 //Faire un random
	  };

	/**
	 * emit award position && update score
	 */
	socket.on('awardEarned', function () {
		
		award.x = Math.floor(Math.random() * 800) + 50;
		award.y = Math.floor(Math.random() * 550) + 50;
		io.emit('awardPosition', award);
		io.emit('scoresUpdated', scores);
	});

});

server.listen(8000);