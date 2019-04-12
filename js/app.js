var tick = 2000;

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
      debug: false
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
  this.load.image("tiles", "tuxmon-sample-32px.png");
  this.load.tilemapTiledJSON("map", "map.json");

  this.load.image('carOther', '/users/Etu4/3704134/Documents/Projet/assets/car_red_3.png');
  this.load.image('car', '/users/Etu4/3704134/Documents/Projet/assets/car_blue_3.png');
  this.load.image('bomb', '/users/Etu4/3704134/Documents/Projet/assets/bomb.png');
  this.load.image('award', '/users/Etu4/3704134/Documents/Projet/assets/award.png');

  this.load.bitmapFont('nGamef', '../font/nGamef.png', '../font/nGamef.xml');
  this.load.bitmapFont('tGamef', '../font/tGamef.png', '../font/tGamef.xml');

};

function create() {

  // this.socket = io();

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
  this.obstacles = map.createStaticLayer("obstacles", tileset, 0, 0);

  this.obstacles.setCollisionByProperty({
    collides: true
  });

  this.mycar = this.physics.add.sprite(200, 300, 'car').setScale(0.5);
  this.mycar.setImmovable(false);

  this.therecar = this.physics.add.sprite(150, 150, 'carOther').setScale(0.5);

  this.therecar.body.bounce.setTo(1);

  this.mycar.body.bounce.setTo(1);

  this.physics.add.collider(this.mycar, this.obstacles);

  this.physics.add.collider(this.therecar, this.bomb, () => {

    this.mycar.disableBody(true, false);

    let x = this.mycar.x;
    let y = this.mycar.y;

    setTimeout(() => {
      this.mycar.enableBody(true, x + 2, y + 2, true, true);
    }, 3000);
  });

  this.physics.add.overlap(this.mycar, this.zone, () => {
    this.mycar.disableBody(true, true);
  });

  this.cursor = this.input.keyboard.createCursorKeys();

  // this.add.text(140, 0, '40', { fontFamily: 'Arial', fontSize: '32px', fill: '#FF0000'});
  this.add.bitmapText(0, 0, 'tGamef', ' SCORE : ', 32);
  this.score = this.add.bitmapText(220, 0, 'nGamef', 0, 32);

  this.add.bitmapText(1170, 0, 'tGamef', ' BOMBS : ', 32);
  this.bombs = this.add.bitmapText(1390, 0, 'nGamef', 5, 32);

  if (this.score == 3) {
    console.log(this.score);

    this.physics.pause();
    this.add.bitmapText(500, 400, 'tGamef', 'GAME OVER', 64);
  }

  /**  
   * generate event !!
   */
  /*
  this.id = 'toto'; // A initialiser !! 

  this.socket.on('scoresUpdated', function (scores) {
    this.score.setText(scores[this.id]);
  });

  this.socket.on('awardPosition', function (awardPosition) {
    if (this.award) this.award.destroy();
    this.award = this.physics.add.image(awardPosition.x, awardPosition.y, 'award').setDisplaySize(53, 40);
    this.award.physics.add.overlap(this.mycar, this.award, function () {
      this.socket.emit('awardEarned');
    }, null, this);
  });
  */

  /**
   * Test : Randomly positioning the award && set score
   * A modifier avec event node !! on.awardEarned
   **/

  var f = () => {
    if (this.award) this.award.destroy();
    this.score.setText(eval(this.score.text) + 1);
    this.award = this.physics.add.image(Math.floor(Math.random() * 600), Math.floor(Math.random() * 450), 'award').setDisplaySize(48, 48);
    this.physics.add.overlap(this.mycar, this.award, f);
    this.award.body.bounce.setTo(1);
  }

  this.award = this.physics.add.image(Math.floor(Math.random() * 600), Math.floor(Math.random() * 450), 'award').setDisplaySize(48, 48);
  this.physics.add.overlap(this.mycar, this.award, f);
  this.award.body.bounce.setTo(1);

};

var clics = 0

function update() {
  /*
    New code
  */
  this.mycar.body.velocity.x = 0;
  this.mycar.body.velocity.y = 0;
  this.mycar.body.angularVelocity = 0;

  this.therecar.body.velocity.x = 0;
  this.therecar.body.velocity.y = 0;
  this.therecar.body.angularVelocity = 0;

  if (this.cursor.left.isDown && this.cursor.up.isDown) {
    this.mycar.body.setAngularVelocity(-200);
  } else if (this.cursor.right.isDown && this.cursor.up.isDown) {
    this.mycar.body.setAngularVelocity(200);
  }

  if (this.cursor.up.isDown) {
    this.physics.velocityFromAngle(this.mycar.angle, 250, this.mycar.body.velocity);
  } else if (this.cursor.down.isDown) {
    this.physics.velocityFromAngle(this.mycar.angle - 180, 250, this.mycar.body.velocity);
  }

  if (this.cursor.space.isDown) {
    clics++;
    if (clics <= 1 && this.bombs.text > 0) {
      this.bomb = this.physics.add.image(this.mycar.x, this.mycar.y, 'bomb');
      this.bombs.setText(this.bombs.text - 1);
    } else {
      if (clics > 5) {
        clics = 0;
      }
    }

  }

  this.physics.add.collider(this.mycar, this.therecar);
  this.physics.add.collider(this.therecar, this.bomb);
  this.physics.add.collider(this.award, this.obstacles);
  this.physics.add.collider(this.award, this.zone);


  /*
    old code
  */
  // // this.mycar.body.velocity.x = 0;
  //
  // //this.mycar.setAngularVelocity(0);
  //
  // this.physics.velocityFromRotation(0);
  //
  // if(this.cursor.left.isDown && this.cursor.up.isDown){
  // 	//this.mycar.body.velocity.x = -300;
  // 	acc = this.mycar.body.acceleration*0.1;
  //
  // 	this.mycar.setAcceleration(acc);
  // 	this.mycar.setAngularVelocity(-20);
  // }else if(this.cursor.right.isDown && this.cursor.up.isDown){
  // 	//this.mycar.body.velocity.x = 300;
  // 	acc = this.mycar.body.acceleration*0.1;
  // 	this.mycar.setAcceleration();
  // 	this.mycar.setAngularVelocity(20);
  // }else{
  // 	this.mycar.setAngularVelocity(0);
  // }
  //
  //
  //
  //
  //
  // if(this.cursor.up.isDown){
  //
  // 	this.physics.velocityFromRotation(this.mycar.rotation - 1.5, 40, this.mycar.body.acceleration);
  //
  // }else if(this.cursor.down.isDown){
  // 	this.physics.velocityFromRotation(this.mycar.rotation + 1.5, 40, this.mycar.body.acceleration);
  // }else{
  // 	this.mycar.setAcceleration(0);
  // 	this.mycar.body.velocity.y = 0;
  // 	this.mycar.body.velocity.x = 0;
  // }



  this.physics.world.wrap(this.mycar, 50);
  this.physics.world.wrap(this.therecar, 50);

};


// setInterval(function(){
//
// 	console.log("tick");
//
// }, tick);