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
		if(roundNum == 2) {
			RobotType type = (rc.getTeam() == Team.A) ? RobotType.SCOUT : RobotType.LUMBERJACK; 
			rc.buildRobot(type, new Direction((rc.getTeam() == Team.A) ? 2.5f : (float) Math.PI * 0.5f));
		}
	}
}
