package turtlegardens;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	static MapLocation myHomeLocation = null;
	static MapLocation gardenLocation;
	static final int MIN_GARDEN_RADIUS = 10;
	static int turnsSinceChangedRotation = 0;
	static int rotation = 90;
	static boolean settled = false;
	static int treesPlanted = 0;
	static int lastState = 0; // 0 - thinking, 1 - watering, 2 - planting
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		MapLocation myLocation = rc.getLocation();
		
		if(myHomeLocation == null) {
			myHomeLocation = Comms.readHomeLocation(rc);
		}
		
		if(myHomeLocation != null) {
			
			if(settled) {
				if(treesPlanted < 10) {
					
					// Plant more trees if none urgently need watering
					// if I was planting, go back to middle
					if(lastState == 2) {
						if(!rc.hasMoved() && myLocation.directionTo(gardenLocation) == null) {
							lastState = 0;
						} else {
							Nav.tryMove(rc, myLocation.directionTo(gardenLocation));
						}
					} else if(lastState == 0) {
						for(float directionRads = 6.283f; (directionRads-=0.6283f) > 0;) {
							Direction plantDirection = new Direction(directionRads);
							if(!rc.hasMoved() && rc.isCircleOccupiedExceptByThisRobot(myLocation.add(plantDirection), 2)) {
								if(Nav.tryMove(rc, plantDirection)) {
									lastState = 2;
									if(rc.canPlantTree(plantDirection)) {
										rc.plantTree(plantDirection);
									}
								}
							}
						}
					}
					
					
				} else {
					
					// Water trees, build other units
					
				}
				
			} else {
				
				// Move and settle
				turnsSinceChangedRotation++;
				if(myHomeLocation.distanceTo(myLocation) < MIN_GARDEN_RADIUS) {
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
					if(nearbyAllies.length > 0) {
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
		
		
		/*
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
		}*/
	}
}
