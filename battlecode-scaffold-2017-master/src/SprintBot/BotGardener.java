package SprintBot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static boolean settled = false;
	public static Direction spawnDirection = null;
    public static Direction[] treeDirections = new Direction[5];
    public static final float MULTIPLICITY = 0.333334f;
	
    public static final int MAX_SCOUTS = 20;
    public static final int MAX_LUMBERJACKS = 2;
    
    static int turnsNotSettled = 0;
    static int validTreeSpawnDirections = 0;
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
    	Util.reportDeath(rc);
    	
    	MapLocation myLocation = rc.getLocation();
    	
    	TreeInfo[] trees = rc.senseNearbyTrees(2f, Team.NEUTRAL);
    	
    	
    	if(settled) {
    		validTreeSpawnDirections = setSpawnDirection(myLocation);
    		System.out.println(validTreeSpawnDirections);
        	if(validTreeSpawnDirections < 2) {
        		tryToBuild(RobotType.LUMBERJACK);
        	}
    		plantTrees();
    		waterTrees();
    	} else {
    		turnsNotSettled++;
    	}
    	
    	// Movement
    	boolean moved = false;
    	moved = Nav.avoidBullets(rc, myLocation);
    	if(!moved) {
	    	RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
	    	if(nearbyEnemies.length > 0) {
	    		if(settled) {
	    			moved = Nav.tryMove(rc, spawnDirection.opposite());
	    			settled = false;
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
    }
    
    private static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		if(rc.canBuildRobot(typeToBuild, spawnDirection)) {
			rc.buildRobot(typeToBuild, spawnDirection);
			if(typeToBuild == RobotType.SCOUT) {
				//broadcastUnassignedScout();
			}
			Util.increaseNumBotsByOne(rc, typeToBuild);
			return true;
		}
		
		Direction buildDirection = new Direction(0);
		for (int i = 0; i < 40; i++) {
			if (rc.canBuildRobot(typeToBuild, buildDirection) && rc.onTheMap(rc.getLocation().add(buildDirection, 5f))) {
				rc.buildRobot(typeToBuild, buildDirection);
				return true;
			}
			buildDirection = buildDirection.rotateLeftRads((float) Math.PI * 0.15708f);
		}
		return false;
	}

	//public static void broadcastUnassignedScout() throws GameActionException {
    //   Comms.writeStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END, rc.getLocation());
    //}
    
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
	    	validTreeSpawnDirections = setSpawnDirection(myLocation);
	    	if(validTreeSpawnDirections < 1) {
	    		goodToSettle = false;
	    	}
    	}
    	
    	return goodToSettle;
    }
    
    public static int setSpawnDirection(MapLocation myLocation) throws GameActionException {
    	int validPlantCount = 0;
        Direction direction = new Direction(0f);
        for(int i=0; i<6; i++) {
    		rc.setIndicatorDot(rc.getLocation().add(direction), 20, 200, 20);
            if(!rc.isCircleOccupiedExceptByThisRobot(myLocation.add(direction, 2.1f),1.1f) || rc.isLocationOccupiedByRobot(myLocation.add(direction, 2f))) {
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
			if (plantDirection != null && rc.canPlantTree(plantDirection)) {
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
