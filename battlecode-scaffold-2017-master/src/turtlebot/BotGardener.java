package turtlebot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		
		if(rc.isBuildReady() && rc.getTeamBullets() >= RobotType.LUMBERJACK.bulletCost) {
			for(int direction = 360; (direction -= 15) > 0;) {
				Direction directionToBuild = new Direction((float)Math.toRadians((double)direction));
				if(rc.canBuildRobot(RobotType.LUMBERJACK, directionToBuild)) {
					rc.buildRobot(RobotType.LUMBERJACK, directionToBuild);
					break;
				}
			}
		}
		
	}
}
