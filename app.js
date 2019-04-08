
var tick = 2000;
var socket = io.connect('http://localhost:8000');

var posX;
var posY;
var angle;

var config = {
    type: Phaser.AUTO,
    width: 800,
    height: 600,
    physics: {
        default: 'arcade',
        arcade: {
            //gravity: { y: 300 },
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

function preload (){

	this.load.image('ground', '/b.png');
  this.load.image('car','/car.png');
    //this.load.image('car','car_blue_3.png');
	this.load.image('car2','/car2.png');
};

function create (){

	this.add.image(400, 300, 'ground');

	this.mycar = this.physics.add.sprite(200, 300, 'car').setScale(0.3);
	this.therecar = this.add.sprite(150, 150, 'car2').setScale(0.3);

	this.cursor = this.input.keyboard.createCursorKeys();
  posX = this.mycar.body.velocity.x;
  posY = this.mycar.body.velocity.y;
  angle = this.mycar.angle;

};

function update (){
  /*
    New code
  */

  this.mycar.body.velocity.x = 0;
  this.mycar.body.velocity.y = 0;
  this.mycar.body.angularVelocity = 0;

  if (this.cursor.left.isDown && this.cursor.up.isDown) {
    angle -= 200 % 360;
    this.mycar.body.setAngularVelocity(-200);
  } else if (this.cursor.right.isDown && this.cursor.up.isDown) {
    angle += 200 % 360;
    this.mycar.body.setAngularVelocity(200);
  }

  if (this.cursor.up.isDown) {
    this.physics.velocityFromAngle(this.mycar.angle, 200, this.mycar.body.velocity);
    posX = this.mycar.body.velocity.x;
    posY = this.mycar.body.velocity.y;
    angle = this.mycar.angle % 360
  } else if(this.cursor.down.isDown){
	     this.physics.velocityFromAngle(this.mycar.angle-180, 200, this.mycar.body.velocity);
       posX = this.mycar.body.velocity.x;
       posY = this.mycar.body.velocity.y;
       angle = this.mycar.angle % 360
	}

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



	this.physics.world.wrap(this.mycar, 1);

};



setInterval(function(){

  console.log("posX "+posX)
  console.log("posY "+posY)
  console.log("angle "+angle)
  
	socket.emit('move',{
    x : posX,
    y : posY,
    angle : angle
  });

}, tick);





socket.on('message', function(message) {

        console.log('Le serveur a un message pour vous : ' + message);

});

