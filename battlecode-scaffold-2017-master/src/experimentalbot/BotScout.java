package experimentalbot;

import battlecode.common.*;
import static experimentalbot.BotGardener.rc;

public class BotScout {

	static RobotController rc;

	public static enum MoveState {
		LEFT,
		RIGHT,
		TOWARD_LEFT,
		TOWARD_RIGHT
	}

	static int roundNum;
	static MapLocation myLocation;
	static RobotInfo[] enemyList;
	
	static Team myTeam;
	static boolean startupFlag = true;

	// NAVIGATION VARIABLES
	static MoveState moveState = MoveState.TOWARD_LEFT;
	static float dMin = 10000f;
	static MapLocation goalCache = new MapLocation(-1, -1);

	public static void turn(RobotController rc) throws GameActionException {
		
		// Set variables so we never have to call those functions again
		if (startupFlag) {
			myTeam = rc.getTeam();
			startupFlag = false;
		}
		
		BotScout.rc = rc;
		roundNum = rc.getRoundNum();
		myLocation = rc.getLocation();
		enemyList = rc.senseNearbyRobots(rc.getType().sensorRadius, myTeam.opponent());
		
		pathTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0], new RobotType[]{RobotType.SOLDIER});

	}
	
	private static void pathTo(MapLocation goal) throws GameActionException {
		RobotType[] avoid = new RobotType[0];
		pathTo(goal, avoid); 
	}

	private static void pathTo(MapLocation goal, RobotType[] avoid) throws GameActionException {

		// If this is the first time going here, clear our pathing memory
		if (goal != goalCache) {
			goalCache = goal;
			dMin = 10000f;
			moveState = MoveState.TOWARD_LEFT;
		}

		float degreeOffset = 15f;
		Direction trial;
		float stride = rc.getType().strideRadius;

		// Idea: if we can go closer to the goal than we ever have before, do so.
		for (int i = 0; i < 7; i++) {
			trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
			if (rc.canMove(trial) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(trial, avoid)) {
				rc.move(trial);
				dMin = myLocation.add(trial, rc.getType().strideRadius).distanceTo(goal);
				moveState = chooseMoveState();
				return;
			}
			trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
			if (rc.canMove(trial) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(trial, avoid)) {
				rc.move(trial);
				dMin = myLocation.add(trial, rc.getType().strideRadius).distanceTo(goal);
				moveState = chooseMoveState();
				return;
			}
		}

		// Else, let's start following a wall.
		if (moveState == MoveState.TOWARD_LEFT) {
			moveState = moveState.LEFT;
		} else if (moveState == MoveState.TOWARD_RIGHT) {
			moveState = moveState.RIGHT;
		}

		switch (moveState) {
			case LEFT:
				rc.setIndicatorDot(myLocation, 255, 0, 0);
				for (int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
					if (rc.canMove(trial) && !inEnemySight(trial, avoid)) {
						rc.move(trial);
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return;
					}
				}
				break;
			case RIGHT:
				rc.setIndicatorDot(myLocation, 0, 255, 0);
				for (int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
					if (rc.canMove(trial) && !inEnemySight(trial, avoid)) {
						rc.move(trial);
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return;
					}
				}
				break;
			default:
				System.out.println("PATHING SHOULDN'T GET HERE!");
				break;
		}

		moveState = (moveState == moveState.LEFT) ? moveState.RIGHT : moveState.LEFT;
		return;

	}

	private static MoveState chooseMoveState() {
		if (moveState == MoveState.LEFT || moveState == MoveState.TOWARD_LEFT) {
			return MoveState.TOWARD_LEFT;
		} else {
			return MoveState.TOWARD_RIGHT;
		}
	}
	
	private static boolean inEnemySight(Direction trial, RobotType[] avoid) {
		if (avoid.length == 0) {
			return false;
		}
		for(RobotInfo enemy : enemyList) {
			if(java.util.Arrays.asList(avoid).contains(enemy.type)
					&& enemy.location.distanceTo(myLocation.add(trial, rc.getType().strideRadius)) <= enemy.type.sensorRadius) {
				return true;
			}
		}
		return false;
	}
}
