package testgardener;

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
    	//if(settled) {
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
    	//}
    	
    	// Movement
    	boolean moved = false;
    	moved = Nav.avoidBullets(rc, myLocation);
    	if(!moved) {
	    	RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
	    	if(nearbyEnemies.length > 0) {
	    		//if(settled) {
	    		//	moved = Nav.tryMove(rc, spawnDirection.opposite());
	    		//} else {
	    			TreeInfo[] nearbyTrees = rc.senseNearbyTrees(2f);
	    			moved = Nav.simpleRunAway(rc, myLocation, nearbyEnemies, nearbyTrees);
	    		//}
	    	}
    	}
    	//System.out.format("\nThere are %d enemies and I moved: %b", rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length, moved);
    	if(!moved){ // We haven't moved yet
    		moved = moveToTrees();
    	}
    	if(!moved) {
    		Nav.explore(rc);
    	}
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
    	if(!rc.onTheMap(rc.getLocation(), 5)) {
    		return false;
    	}
    	
    	for (Direction plantDirection : treeDirections) {
    		MapLocation plantLocation = rc.getLocation().add(plantDirection);
			if (plantDirection != null /*&& ((int)plantLocation.x % 8 == 0) && ((int)plantLocation.y % 8 == 0)*/ && rc.canPlantTree(plantDirection) && (rc.getRoundNum() > 50 || rc.getTeamBullets() > 100)) {
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
    
    public static boolean moveToTrees() throws GameActionException {
    	for (TreeInfo treeInfo : rc.senseNearbyTrees(1.5f, rc.getTeam())) {
            
                if(Nav.pathTo(rc, treeInfo.getLocation())) {
                	return true;
                }
        }
    	return false;
    }

}
