package comp771;

import java.io.IOException;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.*;


/**
 * FindingTEAMo:
 * A team in robocode that derives from the code of "circle Bot" taken from here:
 * These robots work as individuals in order to ensnare and circle the enemy. Like sharks, these robots move closer to and circle their prey, whislst killing it at the same time.
 * The first robot "SharkBait" will send info about it's gun heading to "OOHAHA" constantly. If oohaha's gun heading is 180 or equal to SharkBait's, they will
 * not fire. This is to avoid hitting eachother incase the enemy moves. In order to avoid a gun lock up, sharkbait and oohaha will move at different rates.
 */

public class SharkBait extends TeamRobot {

	public void onMessageReceived(MessageEvent e) {
out.println(e.getMessage());
	}


	boolean movingForward; // Is set to true when setAhead is called, set to false on setBack
	boolean inWall; // Is true when robot is near the wall.

	public void run() {
		// Set colors
		setBodyColor(new Color(255, 191, 0, 0));
		setGunColor(new Color(255,255,255, 0));
		setRadarColor(new Color(255,171,10, 0));

		// Every part of the robot moves freely from the others.
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		// Check if the robot is closer than 50px from the wall.
		if (getX() <= 50 || getY() <= 50 || getBattleFieldWidth() - getX() <= 50 || getBattleFieldHeight() - getY() <= 50) {
				inWall = true;
			} else {
			inWall = false;
		}

		setAhead(40000); // go ahead until you get commanded to do differently
		setTurnRadarRight(360); // scan constantly
		movingForward = true; // we called setAhead, so movingForward is true

		while (true) {
			// check if we are in a wall. If we are between 50 pixels of the borders of the battlefield - then we are in a wall
			if (getX() > 50 && getY() > 50 && getBattleFieldWidth() - getX() > 50 && getBattleFieldHeight() - getY() > 50 && inWall == true) {
				inWall = false;
			}
			if (getX() <= 50 || getY() <= 50 || getBattleFieldWidth() - getX() <= 50 || getBattleFieldHeight() - getY() <= 50 ) {
				if ( inWall == false){
					// get out of wall
					reverseDirection();
					inWall = true;
				}
			}
			//Execute all orders
			execute();
		}
	}



	/**
	 * If the robot hits a wall - then it will reverse direction.
	 */
	public void onHitWall(HitWallEvent e) {
		reverseDirection();
	}

	/**
	 * reverseDirection:  Switch from ahead to back & vice versa
	 */
	public void reverseDirection() {
		if (movingForward) {
			setBack(40000);
			movingForward = false;
		} else {
			setAhead(40000);
			movingForward = true;
		}
	}

/*
 *
 */

	public void onScannedRobot(ScannedRobotEvent e) {

	// ignore our team mate - focus on enemy
		if(isTeammate(e.getName())){
			return;
		}
		// Calculate exact location of the robot
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
		double bearingFromRadar = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading());

		//If we are far away, circle closer
		if (movingForward && (e.getDistance()>400)){
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 50));
		} else {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 130));
		}

		//if we are close, circle outwards
		if (movingForward && (e.getDistance()<100)){
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 100));
		} else {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 80));
		}

		//if we are at the perfect distance, circle perprindicular
		if (movingForward && (e.getDistance()==100)){
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 90));
		} else {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 90));
		}


		// If it's close enough, fire!
		if (Math.abs(bearingFromGun) <= 4) {
			setTurnGunRight(bearingFromGun); 	// keep gun focussed on the enemy
			setTurnRadarRight(bearingFromRadar); // keep the radar focussed on the enemy
			// If we are able to fire, then fire dpeending on how far away the enemy is, always keeping 0.1 spare so we don't disable ourselves.
			if (getGunHeat() == 0 && getEnergy() > .2)
				fire(Math.min(4.5 - Math.abs(bearingFromGun) / 2 - e.getDistance() / 250, getEnergy() - .1));

		} // otherwise just set the gun and radar to turn to the enemy
		else {
			setTurnGunRight(bearingFromGun);
			setTurnRadarRight(bearingFromRadar);
		}
		// Generates another scan event if we do not see a robot.
		if (bearingFromGun == 0) {
			scan();
		}

		try {
			// Send enemy position to teammates
			broadcastMessage("Hi im shark bait. Pls get this.");
			out.println("Order sent!");
		} catch (IOException ex) {
			out.println("Unable to send order: ");
			ex.printStackTrace(out);
		}

	}

	/**
	 * The only robot that this is likely to hit, is it's team mate, thus, only one needs to reverse direction.
	 */
	public void onHitRobot(HitRobotEvent e) {
		if (e.isMyFault()) {
			reverseDirection();
		}
	}
}
