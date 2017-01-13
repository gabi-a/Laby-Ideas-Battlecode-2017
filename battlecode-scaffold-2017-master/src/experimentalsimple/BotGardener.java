package experimentalsimple;
import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static boolean settled = false;
	public static Direction spawnDirection = null;
    public static Direction[] treeDirections = new Direction[5];
    public static final float MULTIPLICITY = 0.333334f;
	
    public static final int MAX_SCOUTS = 20;
    public static final int MAX_LUMBERJACKS = 2;
    
    static int roundCounter = 0;
    static int bulletsRequired = 0;
    
    static boolean reportedDeath = false;
    
    public static void turn(RobotController rc) throws GameActionException {
    	BotGardener.rc = rc;
    	
    	if(!reportedDeath && rc.getHealth() < 10f) {
    		Comms.writeNumRobots(rc, RobotType.GARDENER, Comms.readNumRobots(rc, RobotType.GARDENER) - 1);
    		reportedDeath = true;
    	}
    	
    	roundCounter--;
    	if(roundCounter <= 0) {
    		bulletsRequired = 0;
    	}
    	
    	MapLocation myLocation = rc.getLocation();
    	
    	// Action
    	boolean actioned = false;
    	int scoutsBuilt = Comms.readNumRobots(rc, RobotType.SCOUT);
    	int lumberjacksBuilt = Comms.readNumRobots(rc, RobotType.LUMBERJACK);
    	if(settled) {
    		if (spawnDirection == null) setSpawnDirection(myLocation);
    		if(scoutsBuilt < MAX_SCOUTS) {
    			actioned = tryToBuild(RobotType.SCOUT);
    			if(actioned) {
    				scoutsBuilt++;
    				Comms.writeNumRobots(rc, RobotType.SCOUT, scoutsBuilt);
    			}
    		}
    		if(!actioned && lumberjacksBuilt < MAX_LUMBERJACKS) {
    			actioned = tryToBuild(RobotType.LUMBERJACK);
    			if(actioned) {
    				lumberjacksBuilt++;
    				Comms.writeNumRobots(rc, RobotType.LUMBERJACK, lumberjacksBuilt);
    			}
    		}
    		if(!actioned && rc.getTeamBullets() >= bulletsRequired)
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
    	
    	
    }
    
    private static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		if(rc.canBuildRobot(typeToBuild, spawnDirection)) {
			rc.buildRobot(typeToBuild, spawnDirection);
			if(typeToBuild == RobotType.SCOUT) {
				broadcastUnassignedScout();
			}
			return true;
		}
		return false;
	}

	public static void broadcastUnassignedScout() throws GameActionException {
        Comms.writeStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END, rc.getLocation());
    }
    
    public static boolean goodToSettleHere(MapLocation myLocation) throws GameActionException {
    	
    	boolean goodToSettle = true;
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
