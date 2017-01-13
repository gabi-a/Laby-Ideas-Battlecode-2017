package experimentalbot;

import battlecode.common.*;

public class BotGardener {
	
	public static enum MoveState {
		LEFT,
		RIGHT,
		TOWARD_LEFT,
		TOWARD_RIGHT
	}
	
	static RobotController rc;
	static int roundNum;
	static MapLocation myLocation;
	static float dMin = 1000f;
	static float dLeave = 1000f;
	static MoveState moveState = MoveState.TOWARD_RIGHT;
	
	public static void turn(RobotController rc) throws GameActionException {
		
		BotGardener.rc = rc;
		roundNum = rc.getRoundNum();
		myLocation = rc.getLocation();
		pathTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
		
	}
	
	private static void pathTo(MapLocation goal) throws GameActionException {
		
		float degreeOffset = 15f;
		Direction trial;
		float stride = rc.getType().strideRadius;
		
		for(int i = 0; i < 7; i++) {
			trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
			if(rc.canMove(trial) && myLocation.add(trial, stride).distanceTo(goal) < dMin) {
				rc.move(trial);
				dMin = myLocation.add(trial, rc.getType().strideRadius).distanceTo(goal);
				moveState = chooseMoveState();
				return;
			}
			trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
			if(rc.canMove(trial) && myLocation.add(trial, stride).distanceTo(goal) < dMin) {
				rc.move(trial);
				dMin = myLocation.add(trial, rc.getType().strideRadius).distanceTo(goal);
				moveState = chooseMoveState();
				return;
			}
		}
		
		if(moveState == MoveState.TOWARD_LEFT) {
			moveState = moveState.LEFT;
		}
		else if (moveState == MoveState.TOWARD_RIGHT) {
			moveState = moveState.RIGHT;
		}
		
		switch (moveState) {
			case LEFT:
				rc.setIndicatorDot(myLocation, 255, 0, 0);
				for(int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
					if(rc.canMove(trial)) {
						rc.move(trial);
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return;
					}
				}
				break;
			case RIGHT:
				rc.setIndicatorDot(myLocation, 0, 255, 0);
				for(int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
					if(rc.canMove(trial)) {
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
		}
		else {
			return MoveState.TOWARD_RIGHT;
		}
	}
}
