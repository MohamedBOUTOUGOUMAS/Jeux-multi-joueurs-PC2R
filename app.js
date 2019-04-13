var socket = io();


//chat
var input = document.getElementById("input");
var button = document.getElementById("sub");
var chatbox = document.getElementById("chatBox");

button.addEventListener('click', function(){
  console.log(input.value)
  socket.emit("ENVOI", {content : input.value});

})

socket.on('RECEPTION', function(msg){

  chatbox.innerHTML += "<p style = 'width : 98%; padding : 5px; background-color : #e1e1e1; border-radius : 2px'>"+msg+"</p>" 

})

var angle = 0;
var clics = 0;
var step = 0;

var turnit = 0.01;
var thrustit = 10;
var refresh_tickrate = 300;

/**
 * for the design :
 * list of the names of the connected players
 */
var playersConnected = [];

socket.on('WELCOME', function (swcoord) {

  /**
   * Config phaser
   */
  var config = {
    type: Phaser.canvas,
    width: 1440,
    height: 895,
    physics: {
      default: 'arcade',
      arcade: {
        gravity: {
          y: 0
        },
        debug: true
      }
    },
    scene: {
      preload: preload,
      create: create,
      update: update
    }
  };

  var game = new Phaser.Game(config);

  function preload() {

    this.load.tilemapTiledJSON("map", "map.json");

    this.load.image("tiles", "tuxmon-sample-32px.png");
    this.load.image('carOther', 'car_red_3.png');
    this.load.image('car', 'car_blue_3.png');
    this.load.image('ship', 'car_blue_3.png');
    this.load.image('bomb', 'bomb.png');
    this.load.image('award', 'award.png');

    this.load.bitmapFont('nGamef', 'nGamef.png', 'nGamef.xml');
    this.load.bitmapFont('tGamef', 'tGamef.png', 'tGamef.xml');

  };

  function create() {

    var self = this;

    this.enemyShips = this.physics.add.group();

    /**
     * lake of death
     */
    this.zone = this.add.zone(1000, 250).setSize(200, 200);
    this.physics.world.enable(this.zone);
    this.zone.body.setAllowGravity(false);
    this.zone.body.moves = false;

    const map = this.make.tilemap({
      key: "map"
    });

    const tileset = map.addTilesetImage("tuxmon-sample-32px", "tiles");
    const ground = map.createStaticLayer("ground", tileset, 0, 0);
    const gras = map.createStaticLayer("gras", tileset, 0, 0);
    const lake = map.createStaticLayer("lake", tileset, 0, 0);
    const fountain = map.createStaticLayer("fountain", tileset, 0, 0);
    const obstacles = map.createStaticLayer("obstacles", tileset, 0, 0);

    obstacles.setCollisionByProperty({
      collides: false
    });

    // this.physics.add.collider(this.ship, this.bomb);

    // this.physics.add.overlap(this.ship, this.zone, () => {
    //   this.ship.disableBody(true, true);
    // });

    this.cursor = this.input.keyboard.createCursorKeys();

    // this.add.text(140, 0, '40', { fontFamily: 'Arial', fontSize: '32px', fill: '#FF0000'});
    this.add.bitmapText(0, 0, 'tGamef', ' SCORE : ', 32);
    this.score = this.add.bitmapText(220, 0, 'nGamef', 0, 32);

    this.add.bitmapText(1170, 0, 'tGamef', ' BOMBS : ', 32);
    this.bombs = this.add.bitmapText(1390, 0, 'nGamef', 5, 32);

    // socket.on('NEWPLAYER',(playerIds)=>{
    //   playersConnected = playerIds;
    // });

    socket.on('SESSION', (sscoord) => {

      self.enemyShips.destroy(true);
      self.enemyShips = self.physics.add.group();

      sscoord.coords.forEach(elt => {

        const allPlayersInfo = elt.split(':');
        const enemyId = allPlayersInfo[0];
        const coord = getXY(allPlayersInfo[1]);
        // console.log(enemyId);

        if (enemyId !== swcoord.playerId) {
          // enemyShip.push({ id: playerInfo[0], x: coord.x, y: coord.y, rot: 180 });
          addEnemyShip(self, [enemyId, coord]);
        } else {
          addNewShip(self, coord);
        }


        // console.log(playerInfo[0]);
        // console.log(playerInfo[1]);

      });

      console.log('session start client');

      socket.on('TICK', (vcoords) => {

        vcoords.forEach(elt => {
  
          const allPlayersInfo = elt.split(':');
          const enemyId = allPlayersInfo[0];
          const vcoord = getXYVXVY(allPlayersInfo[1]);
          // console.log(vcoord);
          self.enemyShips.getChildren().forEach((enemyShip) => {
            if (enemyId === enemyShip.enemyId) {
              enemyShip.setX(vcoord.x);
              enemyShip.setY(vcoord.y);
              enemyShip.setRotation(vcoord.t);
              // enemyShip.setVelocity(vcoord.vx, vcoord.vy);
            }
          });
          if (swcoord.playerId === enemyId) {
            // self.physics.velocityFromRotation(self.ship.rotation, 100, new Phaser.Math.Vector2(vcoord.vx, vcoord.vy));
            self.ship.setX(vcoord.x);
            self.ship.setY(vcoord.y);
            self.ship.setRotation(vcoord.t);
            // self.ship.setVelocity(vcoord.vx, vcoord.vy);
          }
        });
      });

      socket.on('PLAYERLEFT', (enemyId) => {
        console.log('Player : ' + enemyId + ' is gone !');
        console.log(enemyId);
        console.log(self.enemyShips.getLength());
        self.enemyShips.destroy(true);
        self.enemyShips.getChildren().forEach((enemyShip) => {
          if (enemyId === enemyShip.enemyId) {
            console.log('fuck');
            enemyShip.destroy();
          }
        });
      });

      socket.on('NEWOBJ', (awardPosition) => {
        awardPosition = getXY(awardPosition);
        if (self.award) self.award.destroy();
        self.award = self.physics.add.image(awardPosition.x, awardPosition.y, 'award').setDisplaySize(53, 40);
      });

      setInterval(() => {// setInterval(fun(),ms)
        socket.emit('NEWCOM', { angle: angle, step: step });
        // step = 0;
      }, 1000);

    });

    /**  
     * generate event !!
    */

    // this.id = 'toto'; // A initialiser !! 

    // this.socket.on('scoresUpdated', function (scores) {
    //   this.score.setText(scores[this.id]);
    // });

    /**
     * Test : Randomly positioning the award && set score
     * A modifier avec event node !! on.awardEarned
     **/

    var f = () => {
      if (this.award) this.award.destroy();
      this.score.setText(eval(this.score.text) + 1);
      this.award = this.physics.add.image(Math.floor(Math.random() * 600), Math.floor(Math.random() * 450), 'award').setDisplaySize(48, 48);
      this.physics.add.overlap(this.ship, this.award, f);
      this.award.body.bounce.setTo(1);
    }
    this.award = this.physics.add.image(swcoord.cord.x, swcoord.cord.y, 'award').setDisplaySize(48, 48);

    // this.physics.add.overlap(this.ship, this.award, f);

  };


  function update() {
    if (this.ship) {
      /* 
        Using velocityFromRotation
      */
      if (this.cursor.left.isDown) {
        //this.ship.setAngularVelocity(-turnit);
        // console.log(this.ship.rotation);
        anticlok(this.ship);
        angle = this.ship.rotation;
      }
      else if (this.cursor.right.isDown) {
        //this.ship.setAngularVelocity(turnit);
        clock(this.ship);
        angle = this.ship.rotation;
      }
      else {
        //this.ship.setAngularVelocity(0);
      }
      // console.log("Pos after x :" + this.ship.x);
      // console.log("Pos after y :" + this.ship.y);s
      if (this.cursor.space.isDown) {
        clics++;
        if (clics <= 1 && this.bombs.text > 0) {
          this.bomb = this.physics.add.image(this.ship.x, this.ship.y, 'bomb');
          this.bomb.setImmovable(true);
          this.bombs.setText(this.bombs.text - 1);
          angle = this.ship.rotation;
        } else {
          if (clics > 5) {
            clics = 0;
          }
        }
        angle = this.ship.rotation;
      }

      if (this.score.text == '3') {
        this.physics.pause();
        this.add.bitmapText(400, 350, 'tGamef', 'WIN', 64);
      }

      //     // this.physics.add.collider(this.ship, this.therecar);
      //     // this.physics.add.collider(this.therecar, this.bomb, () => { // A revoir !!!

      //     //   this.therecar.disableBody(true, false);
      //     //   this.bomb.disableBody(true, true);

      //     //   let x = this.therecar.x;
      //     //   let y = this.therecar.y;

      //     this.physics.add.collider(this.award, this.zone);
      this.physics.world.wrap(this.ship, 5);
      this.physics.world.wrap(this.enemyShips, 5);

    }
  };

  // var tick = 2000;
  // var i = 0;
  // currentShip = this.ship;
  // setInterval(() => {
  //   socket.emit('NEWCOM', { rotation: currentShip.body.rotation, step: step });
  // }, 5000);

});


window.onkeyup = function (e) {
  var key = e.keyCode ? e.keyCode : e.which;

  if (key == 38) {
    step += thrustit;
  } else if (key == 40) {
    step -= thrustit;
  }
}

function moveAward(game, coord) {

  game.award = game.physics.add.image(x, y, 'award').setDisplaySize(48, 48);

}

function addNewShip(game, coord) {
  if (game.ship) game.ship.destroy();
  game.ship = game.physics.add.sprite(coord.x, coord.y, 'car').setScale(0.5);
  game.ship.setImmovable(false);
  game.ship.setDrag(200);
  game.ship.setMaxVelocity(200);
  game.ship.body.bounce.setTo(1);

  return game.ship;

}

function getXY(coord) {

  var indexX = coord.indexOf('X');
  var indexY = coord.indexOf('Y');
  var x = parseFloat(coord.substring(indexX + 1, indexY));
  var y = parseFloat(coord.substring(indexY + 1));

  return { x: x, y: y };

}

function getXYVXVY(coord) {

  var indexX = coord.indexOf('X');
  var indexY = coord.indexOf('Y');
  var indexVX = coord.indexOf('VX');
  var indexVY = coord.indexOf('VY');
  var indexT = coord.indexOf('T');
  var x = parseFloat(coord.substring(indexX + 1, indexY));
  var y = parseFloat(coord.substring(indexY + 1));
  var vx = parseFloat(coord.substring(indexVX + 2, indexVY));
  var vy = parseFloat(coord.substring(indexVY + 2, indexT));
  var t = parseFloat(coord.substring(indexT + 1));
  return { x: x, y: y, vx: vx, vy: vy, t: t };

}



function addEnemyShip(game, info) {
  const enemyId = info[0];
  const coord = info[1];
  // console.log('Enemy : ' + enemyId + ' coords - x:' + coord.x + ' y:' + coord.y);
  const enemyShip = game.physics.add.sprite(coord.x, coord.y, 'carOther').setScale(0.5);
  enemyShip.setImmovable(false);
  enemyShip.body.bounce.setTo(1);
  enemyShip.enemyId = enemyId;
  game.enemyShips.add(enemyShip);
}

function clock(ship) {
  ship.rotation += turnit;
}
function anticlok(ship) {
  ship.rotation -= turnit;
}