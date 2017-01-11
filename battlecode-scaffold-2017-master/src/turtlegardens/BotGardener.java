package turtlegardens;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	static final int ANGLES_TO_CHECK = 30;
	static final int ANGLE_BETWEEN_TREES = 12;
	
	static MapLocation myHomeLocation = null;
	static final int MIN_GARDEN_RADIUS_FROM_BASE = 12;
	static final int GARDEN_RADIUS = 4;
	static int turnsSinceChangedRotation = 0;
	static int rotation = Math.random() < 0.5 ? 60 : -60;
	static boolean settled = false;
	static MapLocation gardenLocation;
	
	static MapLocation wateringTree = null;
	static MapLocation plantingTree = null;
	
	static boolean toldTheArchonIamDead = false;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		if(!toldTheArchonIamDead && rc.getHealth() < 10) {
			int numGardeners = Comms.getNumGardeners(rc) - 1;
			Comms.writeNumGardeners(rc, numGardeners);
			toldTheArchonIamDead = true;
		}
		
		MapLocation myLocation = rc.getLocation();
		
		if(myHomeLocation == null) {
			myHomeLocation = Comms.readHomeLocation(rc);
		}
		if(settled) {
			
			TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
			
			// Water trees that need watering
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
			
			// Otherwise attempt to plant trees around the garden
			System.out.println("I want to plant a tree");
			Direction plantingDirection = Direction.getNorth();
			for(int i = ANGLES_TO_CHECK; i-->0;) {
				MapLocation plantingLocation = gardenLocation.add(plantingDirection, GARDEN_RADIUS);
				System.out.println("I can see the planting spot: "+(myLocation.distanceTo(plantingLocation) <= RobotType.GARDENER.sensorRadius));
				if(myLocation.distanceTo(plantingLocation)+1 <= RobotType.GARDENER.sensorRadius && !rc.isCircleOccupied(plantingLocation, 1)) {
					System.out.println("The spot where I want to plant it is clear");
					System.out.println("I am this distance from the spot: "+myLocation.distanceTo(plantingLocation));
					if(myLocation.distanceTo(plantingLocation) < 3) {
						if(rc.canPlantTree(myLocation.directionTo(plantingLocation))) {
							rc.plantTree(myLocation.directionTo(plantingLocation));
							return;
						}
					} else {
						if(Nav.tryMove(rc, myLocation.directionTo(plantingLocation))){
							return;
						}
					}
				}
				plantingDirection = plantingDirection.rotateLeftDegrees(ANGLE_BETWEEN_TREES);
			}
		} else {
			if(myHomeLocation != null) {
				// Move and settle
				turnsSinceChangedRotation++;
				if(myHomeLocation.distanceTo(myLocation) < MIN_GARDEN_RADIUS_FROM_BASE) {
					if(!Nav.tryMove(rc, rc.getLocation().directionTo(myHomeLocation).opposite())) {
						if(!Nav.tryMove(rc, rc.getLocation().directionTo(myHomeLocation).rotateLeftDegrees(rotation))) {
							if(turnsSinceChangedRotation > 15) {
								turnsSinceChangedRotation = 0;
								rotation = -rotation;
							}
						}
					}
				} else {
					// If this is a good spot then settle it
					boolean settleHere = true;
					RobotInfo[] nearbyAllies = rc.senseNearbyRobots(RobotType.GARDENER.sensorRadius, rc.getTeam());
					RobotInfo[] nearbyTrees = rc.senseNearbyRobots(RobotType.GARDENER.sensorRadius, rc.getTeam());
					if(nearbyTrees.length > 0) {
						settleHere = false;
					} else if(nearbyAllies.length > 0) {
						for(int i = nearbyAllies.length; i-- > 0;) {
							if(nearbyAllies[i].getType() == RobotType.GARDENER) {
								settleHere = false;
							}
						}
					}
					
					if(settleHere) {
						gardenLocation = myLocation;
						settled = true;
					} else {
						if(!Nav.tryMove(rc, rc.getLocation().directionTo(myHomeLocation).rotateLeftDegrees(rotation))) {
							if(turnsSinceChangedRotation > 15) {
								turnsSinceChangedRotation = 0;
								rotation = -rotation;
							}
						}
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
