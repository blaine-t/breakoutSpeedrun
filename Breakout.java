import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

	private static final long serialVersionUID = 5184045631876276977L;
	
	/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

	/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

	/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

	/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

	/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 2;

	/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 2;

	/** Separation between bricks */
	private static final int BRICK_SEP = 4;

	/** Width of a brick */
	private static final int BRICK_WIDTH =
			(WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

	/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

	/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

	/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

	/** Number of turns */
	private int lives = 3;

	/** Paddle Y */
	private static final int PADDLEY = HEIGHT-PADDLE_Y_OFFSET-PADDLE_HEIGHT;

	private static final double INITIAL_VELOCITY = 3;

	private static final double FRAMERATE = 1000/60;

	private GRect paddle;

	private GOval ball;
	
	private GLabel liveLabel = new GLabel(Integer.toString(lives),APPLICATION_WIDTH-25,25);
	
	private GLabel status = new GLabel("",APPLICATION_WIDTH/2,APPLICATION_HEIGHT/2);

	private double vx, vy;
	
	private boolean death = false;
	
	private boolean gameWon = false;

	public void run() {
		// Set initial size of window
		setSize(APPLICATION_WIDTH,APPLICATION_HEIGHT);

		// Allow for mouse input
		addMouseListeners();

		// Initialize RNG
		RandomGenerator rgen = RandomGenerator.getInstance();
		
		// Initialize status message
		add(status);
		add(liveLabel);
		
		// Draws the bricks
		drawBricks();

		// Sets up velocity of ball
		vx = rgen.nextDouble(1.0, 3.0);
		if (rgen.nextBoolean(0.5)) vx = -vx;
		vy = INITIAL_VELOCITY;

		// Define the paddles location
		paddle = new GRect(0,PADDLEY,PADDLE_WIDTH,PADDLE_HEIGHT);
		paddle.setFilled(true);
		add(paddle);

		ball = new GOval((APPLICATION_WIDTH-BALL_RADIUS)/2,(APPLICATION_HEIGHT-BALL_RADIUS)/2,BALL_RADIUS*2,BALL_RADIUS*2);
		ball.setFilled(true);
		add(ball);

		while (lives > 0) {
			ball.setLocation((APPLICATION_WIDTH-BALL_RADIUS)/2,(APPLICATION_HEIGHT-BALL_RADIUS)/2);
			waitForClick();
			while(death == false && gameWon == false) {
				update();
				pause(FRAMERATE);
			}
			if (gameWon) {
				ball.setVisible(false);
			}
			death = false;
		}

	}

	@Override
	public void mouseMoved (MouseEvent m) {
		final double mouseX = m.getX();
		
		double paddleX = mouseX-PADDLE_WIDTH/2;
		
		if (mouseX > WIDTH-(PADDLE_WIDTH/2)) {
			paddleX = WIDTH-(PADDLE_WIDTH);
		}
		else if (mouseX < PADDLE_WIDTH/2) {
			paddleX = 0;
		}
		paddle.setLocation(paddleX, PADDLEY);
	}

	private void bounce() {

		GObject collider = getCollidingObject();

		if (ball.getX() < 0 || ball.getX() > WIDTH-BALL_RADIUS*2) {
			vx *= -1;
		}
		if (ball.getY() < 0) {
			vy *= -1;
		}
		if (collider == paddle && vy > 0) {
			vy *= -1;
		}
		else if (collider != null && collider != paddle && collider != liveLabel && collider != status) {
			remove(collider);
			vy *= -1;
		}
		
		if (ball.getY() > APPLICATION_HEIGHT) {
			lives--;
			liveLabel.setLabel(Integer.toString(lives));
			death = true;
		}
		
	}

	private void update() {
		
		bounce();
		gameStatus();
		ball.move(vx, vy);
	}

	private GObject getCollidingObject() {
		double ballX = ball.getX();
		double ballY = ball.getY();
		
		GObject testObject;
		
		testObject = getElementAt(ballX,ballY);
		if (testObject != null) {
			return testObject;
		}
		
		testObject = getElementAt(ballX+BALL_RADIUS*2,ballY);
		if (testObject != null) {
			return testObject;
		}
		
		testObject = getElementAt(ballX+BALL_RADIUS*2,ballY+BALL_RADIUS*2);
		if (testObject != null) {
			return testObject;
		}
		
		testObject = getElementAt(ballX,ballY+BALL_RADIUS*2);
		if (testObject != null) {
			return testObject;
		}
		
		return testObject;
	}
	
	private void gameStatus() {

		boolean gameLost = false;
		
		if (lives <= 0) {
			gameLost = true;
			status.setLabel("GAME OVER");
		} 
		else if (getElementCount() == 4) {
			gameWon = true;
			status.setLabel("YOU WIN");
		}
		
		if (gameLost | gameWon) {
			status.setLocation((APPLICATION_WIDTH-status.getWidth())/2, (APPLICATION_HEIGHT-status.getHeight())/2);
			status.setVisible(true);
		}
		else {
			status.setVisible(false);
		}
	}
	
	private void drawBricks() {

		for (int i = 0; i < NBRICK_ROWS; i++) {
			
			double brickY = BRICK_Y_OFFSET + (BRICK_HEIGHT + BRICK_SEP) * i;
			Color brickColor;
			
			if (i < NBRICK_ROWS/5) {
				brickColor = Color.RED;
			}
			else if (i < NBRICK_ROWS*2/5) {
				brickColor = Color.ORANGE;
			}
			else if (i < NBRICK_ROWS*3/5) {
				brickColor = Color.YELLOW;
			}
			else if (i < NBRICK_ROWS*4/5) {
				brickColor = Color.GREEN;
			}
			else {
				brickColor = Color.CYAN;
			}
			
			for (int j = 0; j < NBRICKS_PER_ROW; j++) {
				double brickX = (BRICK_WIDTH + BRICK_SEP) * j;
				GRect brick = new GRect(brickX,brickY,BRICK_WIDTH,BRICK_HEIGHT);
				brick.setFilled(true);
				brick.setColor(brickColor);
				
				add(brick);
			}
		}
	}
}