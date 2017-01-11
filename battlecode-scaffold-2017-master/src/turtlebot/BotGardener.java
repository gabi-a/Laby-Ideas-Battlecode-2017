package turtlebot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	static MapLocation homeLocation;
	static final int DEFENSE_RADIUS = 5;
	static final int TREE_PLANT_RADIUS = 5;
	static int rotation = 90;
	static int turnsSinceChangedRotation = 30;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		MapLocation myLocation = rc.getLocation();
		
		
		if(homeLocation == null) {
			homeLocation = Comms.readHomeLocation(rc);
		}
		
		for(int i = nearbyTrees.length;i-- > 1;) {
			TreeInfo tree= nearbyTrees[i];
			if(tree.getHealth() < 50) {
				if(rc.canWater(tree.getLocation())) {
					rc.water(tree.getLocation());
					if(tree.getHealth() + 5 < 50) {
						return;
					}
				} else if(tree.getHealth() < 30 && Nav.tryMove(rc, myLocation.directionTo(tree.getLocation()))) {
					return;
				}
			}
		}

		turnsSinceChangedRotation++;
		if(homeLocation != null) {
			if(homeLocation.distanceTo(myLocation) < DEFENSE_RADIUS) {
				if(!Nav.tryMove(rc, rc.getLocation().directionTo(homeLocation).opposite())) {
					if(!Nav.tryMove(rc, rc.getLocation().directionTo(homeLocation).rotateLeftDegrees(rotation))) {
						//System.out.println("Turns since changed rotation: "+turnsSinceChangedRotation);
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
					//System.out.println("Turns since changed rotation: "+turnsSinceChangedRotation);
					if(turnsSinceChangedRotation > 15) {
						turnsSinceChangedRotation = 0;
						rotation = -rotation;
					}
				}
			}
		}
		
		
		// TODO: Decide intelligently whether to build lumberjacks
		if(rc.getRoundNum() < 1000) {
			RobotType typeToBuild = null;
			double rand = Math.random();
			if(rand < 0.1) {
				typeToBuild = RobotType.LUMBERJACK;
			} else {
				typeToBuild = RobotType.SOLDIER;
			}
			if(typeToBuild != null) {
				if(rc.isBuildReady() && rc.getTeamBullets() >= typeToBuild.bulletCost) {
					for(int direction = 360; (direction -= 15) > 0;) {
						Direction directionToBuild = new Direction((float)Math.toRadians((double)direction));
						if(rc.canBuildRobot(typeToBuild, directionToBuild)) {
							rc.buildRobot(typeToBuild, directionToBuild);
							break;
						}
					}
				}
			}
		}
	}
}
