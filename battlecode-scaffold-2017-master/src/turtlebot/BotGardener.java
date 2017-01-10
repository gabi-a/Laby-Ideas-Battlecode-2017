package turtlebot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	static MapLocation homeLocation;
	static final int DEFENSE_RADIUS = 10;
	static final int TREE_PLANT_RADIUS = 5;
	static int rotation = 90;
	static int turnsSinceChangedRotation = 30;
	
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

		turnsSinceChangedRotation++;
		if(homeLocation != null) {
			MapLocation myLocation = rc.getLocation();
			if(homeLocation.distanceTo(myLocation) < DEFENSE_RADIUS) {
				if(!Nav.tryMove(rc, rc.getLocation().directionTo(homeLocation).opposite())) {
					if(!Nav.tryMove(rc, rc.getLocation().directionTo(homeLocation).rotateLeftDegrees(rotation))) {
						System.out.println("Turns since changed rotation: "+turnsSinceChangedRotation);
						if(turnsSinceChangedRotation > 15) {
							turnsSinceChangedRotation = 0;
							rotation = -rotation;
						}
					}
				}
			} else {
				if(myLocation.distanceTo(homeLocation) > TREE_PLANT_RADIUS) {
					if(rc.canPlantTree(myLocation.directionTo(homeLocation).opposite())) {
						rc.plantTree(myLocation.directionTo(homeLocation).opposite());
					}
				}
				if(!Nav.tryMove(rc, rc.getLocation().directionTo(homeLocation).rotateLeftDegrees(rotation))) {
					System.out.println("Turns since changed rotation: "+turnsSinceChangedRotation);
					if(turnsSinceChangedRotation > 15) {
						turnsSinceChangedRotation = 0;
						rotation = -rotation;
					}
				}
			}
		}
		
	}
}
