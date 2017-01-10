package turtlebot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	static MapLocation homeLocation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		
		// TODO: Decide intelligently whether to build lumberjacks
		if(rc.isBuildReady() && rc.getTeamBullets() >= RobotType.LUMBERJACK.bulletCost) {
			for(int direction = 360; (direction -= 15) > 0;) {
				Direction directionToBuild = new Direction((float)Math.toRadians((double)direction));
				if(rc.canBuildRobot(RobotType.LUMBERJACK, directionToBuild)) {
					rc.buildRobot(RobotType.LUMBERJACK, directionToBuild);
					break;
				}
			}
		}
		
		if(homeLocation == null) {
			homeLocation = Comms.readHomeLocation(rc);
		}
		
		if(homeLocation != null) {
			Nav.tryMove(rc, rc.getLocation().directionTo(homeLocation));
		}
		
	}
}
