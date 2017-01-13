package turtlebotpathing;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static boolean settled = false;
	public static Direction spawnDirection = null;
    public static Direction[] treeDirections = new Direction[5];
    public static final float MULTIPLICITY = 0.333334f;
	
    public static final int MAX_SCOUTS = 20;
    public static final int MAX_LUMBERJACKS = 2;
    
    /*
	public static boolean atTargetLoc = false;
    public static Direction spawnDirection = null;
    public static int numScouts = 0;
    public static int scoutThreshold = 0;
	public static boolean hasBuiltStartingLumberjack = false;
    
    public static final int TRAPPED_THRESHOLD = 10;
    
    public static final int DISTANCE_BETWEEN_GARDENS = 10;
    */
    
    static int roundCounter = 0;
    static int bulletsRequired = 0;
    
    static boolean reportedDeath = false;
    static boolean foundEnemy = false;
    
    public static void turn(RobotController rc) throws GameActionException {
    	BotGardener.rc = rc;
    	
    	foundEnemy = Comms.readFoundEnemy(rc);
    	
    	if(!reportedDeath && rc.getHealth() < 10f) {
    		Comms.writeNumRobots(rc, RobotType.GARDENER, Comms.readNumRobots(rc, RobotType.GARDENER) - 1);
    		reportedDeath = true;
    	}
    	
    	roundCounter--;
    	if(roundCounter <= 0) {
    		bulletsRequired = 0;
    	}
    	

		int scoutsBuilt = Comms.readNumRobots(rc, RobotType.SCOUT);
    	int lumberjacksBuilt = Comms.readNumRobots(rc, RobotType.LUMBERJACK);
    	
    	MapLocation myLocation = rc.getLocation();
    	
    	// Action
    	boolean actioned = false;
    	//int scoutsBuilt = Comms.readNumRobots(rc, RobotType.SCOUT);
    	//int lumberjacksBuilt = Comms.readNumRobots(rc, RobotType.LUMBERJACK);
    	if(settled) {
    		if (spawnDirection == null) setSpawnDirection(myLocation);
    		if(scoutsBuilt < 3 || (rc.getRoundNum() > 100 && Comms.readAttackID(rc) == 0)) {
    			actioned = tryToBuild(RobotType.SCOUT, scoutsBuilt);
    			System.out.println("Trying to build a scout");
    		}
    		if(!foundEnemy && !actioned) {
    			actioned = tryToBuild(RobotType.LUMBERJACK, lumberjacksBuilt);
    		} else {
    			actioned = tryToBuild(RobotType.SOLDIER, 0);
    		}
    		/*
    		if(rc.getRoundNum() < 300) {
    			actioned = tryToBuild(RobotType.SCOUT);
    			//if(actioned) {
    			//	scoutsBuilt++;
    			//	Comms.writeNumRobots(rc, RobotType.SCOUT, scoutsBuilt);
    			//}
    		}
    		if(!actioned && rc.getRoundNum() > 300) {
    			TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
    			RobotType typeToBuild = RobotType.SOLDIER;
    			if(nearbyTrees.length > 2) {
    				typeToBuild = RobotType.LUMBERJACK;
    			}
    			actioned = tryToBuild(typeToBuild);
    			//if(actioned) {
    			//	Comms.writeNumRobots(rc, typeToBuild, lumberjacksBuilt);
    			//}
    		}
    		*/
    		if(!actioned && scoutsBuilt > 0 && rc.getTeamBullets() >= bulletsRequired)
    			actioned = plantTrees();
    		if(!actioned) 
    			actioned = waterTrees();
    	}
    	
    	// Movement
    	boolean moved = false;
    	moved = Nav.avoidBullets(rc, myLocation);
    	if(!moved) {
	    	RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
	    	if(nearbyEnemies.length > 0) {
	    		if(settled) {
	    			moved = Nav.tryMove(rc, spawnDirection.opposite());
	    		} else {
	    			TreeInfo[] nearbyTrees = rc.senseNearbyTrees(2f);
	    			moved = Nav.simpleRunAway(rc, myLocation, nearbyEnemies, nearbyTrees);
	    		}
	    	}
    	}
    	//System.out.format("\nThere are %d enemies and I moved: %b", rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length, moved);
    	if(moved) {
    		settled = false;
    	} else { // We haven't moved yet
    		if(settled) { // We are settled
    			return;
    		} else { // We need to settle
        		spawnDirection = null;
    			settled = goodToSettleHere(myLocation);
    			if(!settled) {
    				moved = Nav.explore(rc);
    			}
    		}
    	}
    	
    	
    	/*
        MapLocation selfLoc = rc.getLocation();
        scoutThreshold = rc.getRoundNum() / 200;
		
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		boolean notJustScoutFlag = false;
		for(int i=0; i < enemies.length; i++) {
			if((enemies[i].type != RobotType.SCOUT && enemies[i].type != RobotType.GARDENER && enemies[i].type != RobotType.ARCHON) 
					|| (enemies[i].type == RobotType.SCOUT && enemies[i].location.distanceTo(selfLoc) < 3f)) {
				notJustScoutFlag = true;
				break;
			}
		}	
		if (notJustScoutFlag && spawnDirection != null && !(Comms.readGardenerUniversalHoldRound(rc) > rc.getRoundNum())) {
			rc.setIndicatorDot(selfLoc, 0, 255, 255);
			Comms.writeGardenerUniversalHold(rc, selfLoc, rc.getRoundNum() + 1);
			Direction direction = new Direction(0f);
			for(int i=0; i<6; i++) {
				if (rc.canBuildRobot(RobotType.LUMBERJACK, direction)) {
					rc.buildRobot(RobotType.LUMBERJACK, direction);
					break;
				}
				direction = direction.rotateLeftRads((float) Math.PI * MULTIPLICITY);
			}		
		}
		else if (!atTargetLoc) {
            //System.out.format("Hm. %d, (%f - %f)\n", trappedCount, targetLoc.x, targetLoc.y);

        	Nav.explore(rc);
        	
        	boolean goodToSettle = true;
        	MapLocation myLocation = rc.getLocation();
        	
        	//RobotInfo[] nearbyBots = rc.senseNearbyRobots(5);
        	//if(nearbyBots.length > 0) {
        	//	goodToSettle = false;
        	//	System.out.format("\n Too many nearby bots to settle");
        	//}
        	
        	if(goodToSettle) {
            	MapLocation[] gardens = Comms.readGardenLocs(rc);
            	for(int i = gardens.length;i-->0;) {
            		MapLocation otherGardenLoc = gardens[i];
            		if(otherGardenLoc != null && myLocation.distanceTo(otherGardenLoc) < DISTANCE_BETWEEN_GARDENS) {
            			goodToSettle = false;
                		//System.out.format("\n Too close to another garden to settle");
            			break;
            		}
            	}
        	}
        	
        	if(goodToSettle) {
        		atTargetLoc = true;
        		//System.out.format("\nWrote garden succesfully: %b", Comms.writeGarden(rc, myLocation));
        	}
        }
        else {
            if(spawnDirection == null) {
                int validPlantCount = 0;
                Direction direction = new Direction(0f);
                for(int i=0; i<6; i++) {
                    if(!rc.isCircleOccupied(selfLoc.add(direction, 2f),0.9f) || rc.isLocationOccupiedByRobot(selfLoc.add(direction, 2f))) {
                        if(spawnDirection == null) {
                            spawnDirection = direction;
                        }
                        else {
                            treeDirections[validPlantCount] = direction;
                            validPlantCount++;
                        }
                    }
                    direction = direction.rotateLeftRads((float) Math.PI * MULTIPLICITY);
                }
                if(spawnDirection == null) {
                    atTargetLoc = false;
                    spawnDirection = new Direction(0);
                }
            }
			//if(rc.canBuildRobot(RobotType.SOLDIER, spawnDirection) && rc.getRoundNum() < 50) {
			//	rc.buildRobot(RobotType.SOLDIER, spawnDirection);
			//}
			if(rc.getRoundNum() < 50) {
				Direction direction = new Direction(0f);
				if(rc.canBuildRobot(RobotType.SCOUT, spawnDirection) && numScouts == 0) {
					rc.buildRobot(RobotType.SCOUT, spawnDirection);
					broadcastUnassignedScout();
					numScouts++;
				}
				if(!hasBuiltStartingLumberjack) {
					for(int i=0; i<6; i++) {
						if (rc.canBuildRobot(RobotType.LUMBERJACK, direction)) {
							rc.buildRobot(RobotType.LUMBERJACK, direction);
							break;
						}
						direction = direction.rotateLeftRads((float) Math.PI * MULTIPLICITY);
						System.out.format("%f\n", direction.radians);
					}	
				}
			}
			if(!(Comms.readGardenerUniversalHoldRound(rc) <= rc.getRoundNum()
					&& Comms.readGardenerUniversalHoldLocation(rc).distanceTo(selfLoc) <= 20f)) {
				for (Direction plantDirection : treeDirections) {
					if (plantDirection != null && rc.canPlantTree(plantDirection) && (rc.getRoundNum() > 50 || rc.getTeamBullets() > 100)) {
						rc.plantTree(plantDirection);
					}
				}
			}
            for (TreeInfo treeInfo : rc.senseNearbyTrees(1.5f, rc.getTeam())) {
                if (treeInfo.health <= 0.9f * treeInfo.maxHealth && rc.canWater(treeInfo.ID)) {
                    rc.water(treeInfo.ID);
                }
            }
            if(rc.getRoundNum() > 300) {
	            RobotType typeToBuild;
	            int lumberjacks = Comms.readNumLumberjacks(rc);
	            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(RobotType.GARDENER.sensorRadius, Team.NEUTRAL);
                    if (numScouts < scoutThreshold) {
                        typeToBuild = RobotType.SCOUT;
                    }
                    else if(nearbyTrees.length > 0) {
	            	if(lumberjacks < 5) {
	            		typeToBuild = RobotType.LUMBERJACK;
	            	} else {
	            		Comms.pushHighPriorityTree(rc, nearbyTrees[0], 5);
	            		typeToBuild = RobotType.SOLDIER;
	            	}
	            } else {
	            	typeToBuild = RobotType.SOLDIER;
	            }
	            if (rc.canBuildRobot(typeToBuild, spawnDirection)) {
	                rc.buildRobot(typeToBuild, spawnDirection);
	                if(typeToBuild == RobotType.LUMBERJACK) {
	                	Comms.writeNumLumberjacks(rc, lumberjacks+1);
	                } else if(typeToBuild == RobotType.SCOUT) {
	                	broadcastUnassignedScout();
                        numScouts++;
	                }
	            }
	            else {
	                //System.out.println(":(");
	            }
            }
        }*/
    }
    
    private static boolean tryToBuild(RobotType typeToBuild, int num) throws GameActionException {
		if(rc.canBuildRobot(typeToBuild, spawnDirection)) {
			rc.buildRobot(typeToBuild, spawnDirection);
			if(typeToBuild == RobotType.SCOUT) {
				broadcastUnassignedScout();
			}
			Comms.writeNumRobots(rc, typeToBuild, num+1);
			return true;
		}
		return false;
	}

	public static void broadcastUnassignedScout() throws GameActionException {
        Comms.writeStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END, rc.getLocation());
    }
    
    public static boolean goodToSettleHere(MapLocation myLocation) throws GameActionException {
    	
    	boolean goodToSettle = true;
    	if(!rc.onTheMap(myLocation, 5f)) {
    		goodToSettle = false;
    	} else {
	    	RobotInfo[] allyBots = rc.senseNearbyRobots(-1, rc.getTeam());
	    	for(int i = allyBots.length;i-->0;) {
	    		if(allyBots[i].getType() == RobotType.GARDENER) {
	    			goodToSettle = false;
	    			break;
	    		}
	    	}
	    	
	    	if(setSpawnDirection(myLocation) < 3) {
	    		goodToSettle = false;
	    	}
    	}
    	
    	return goodToSettle;
    }
    
    public static int setSpawnDirection(MapLocation myLocation) throws GameActionException {
    	int validPlantCount = 0;
        Direction direction = new Direction(0f);
        for(int i=0; i<6; i++) {
            if(!rc.isCircleOccupied(myLocation.add(direction, 2f),0.9f) || rc.isLocationOccupiedByRobot(myLocation.add(direction, 2f))) {
                if(spawnDirection == null) {
                    spawnDirection = direction;
                }
                else {
                    treeDirections[validPlantCount] = direction;
                    validPlantCount++;
                }
            }
            direction = direction.rotateLeftRads((float) Math.PI * MULTIPLICITY);
        }
        if(spawnDirection == null) {
            spawnDirection = new Direction(0);
        }
    	return validPlantCount;
    }
    
    public static boolean plantTrees() throws GameActionException {
    	for (Direction plantDirection : treeDirections) {
			if (plantDirection != null && rc.canPlantTree(plantDirection) && (rc.getRoundNum() > 50 || rc.getTeamBullets() > 100)) {
				rc.plantTree(plantDirection);
				bulletsRequired = 55;
				roundCounter = 20;
				return true;
			}
		}
    	return false;
    }
    
    public static boolean waterTrees() throws GameActionException {
    	for (TreeInfo treeInfo : rc.senseNearbyTrees(1.5f, rc.getTeam())) {
            if (treeInfo.health <= 0.9f * treeInfo.maxHealth && rc.canWater(treeInfo.ID)) {
                rc.water(treeInfo.ID);
                return true;
            }
        }
    	return false;
    }

}
