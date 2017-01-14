package idkthebot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static boolean settled = false;
	public static Direction spawnDirection = null;
    public static Direction[] treeDirections = new Direction[5];
    public static final float MULTIPLICITY = 0.333334f;
	
    public static final int MAX_SCOUTS = 20;
    public static final int MAX_LUMBERJACKS = 2;
    
    static boolean reportedDeath = false;
    static boolean foundEnemy = false;
    
    static RobotType typeToBuild = null;
    static int prio = 0;
    
    public static void turn(RobotController rc) throws GameActionException {
    	BotGardener.rc = rc;
    	
    	foundEnemy = Comms.readFoundEnemy(rc);
    	
    	if(!reportedDeath && rc.getHealth() < 10f) {
    		Comms.writeNumRobots(rc, RobotType.GARDENER, Comms.readNumRobots(rc, RobotType.GARDENER) - 1);
    		reportedDeath = true;
    	}
    	
    	MapLocation myLocation = rc.getLocation();
    	
    	// Action

    	boolean actioned = false;
    	// If we aren't trying to build anything,
    	// pop the next unit to build
    	// If there are no units to build, plant trees
    	if(typeToBuild != null) System.out.println(typeToBuild);
    	System.out.format("\n1 Actioned: %b\n", actioned);
    	if(typeToBuild == null) {
    		int[] buildQueue = Comms.popBuildStack(rc);
    		if(buildQueue != null) {
        		typeToBuild = RobotType.values()[buildQueue[0]];
        		prio = buildQueue[1];
        		if((rc.getTeamBullets() > typeToBuild.bulletCost)) {
        			actioned = tryToBuild(typeToBuild, Comms.readNumRobots(rc, typeToBuild));
        	    	System.out.format("\n2 Actioned: %b\n", actioned);
        			if(actioned) {
        				typeToBuild = null;
        			}
        		}
    		}
    	} else if((rc.getTeamBullets() > typeToBuild.bulletCost)){
    		actioned = tryToBuild(typeToBuild, Comms.readNumRobots(rc, typeToBuild));
        	System.out.format("\n3 Actioned: %b\n", actioned);
    		if(actioned) {
    			typeToBuild = null;
    		}
    	}
    	System.out.format("\n4 Actioned: %b\n", actioned);
		if(!actioned && !(typeToBuild != null && prio > 5))
			actioned = plantTrees(myLocation);
		if(!actioned) 
			actioned = waterTrees();
    	
    	// Movement
    	boolean moved = false;
    	moved = Nav.avoidBullets(rc, myLocation);
    	if(!moved) {
	    	RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
	    	if(nearbyEnemies.length > 0) {
	    			TreeInfo[] nearbyTrees = rc.senseNearbyTrees(2f);
	    			moved = Nav.simpleRunAway(rc, myLocation, nearbyEnemies, nearbyTrees);
	    	}
    	}
    	//System.out.format("\nThere are %d enemies and I moved: %b", rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length, moved);
    	if(!moved){
    		moved = moveToTrees();
    	}
    	if(!moved) {
    		Nav.explore(rc);
    	}
    }
    
    private static boolean tryToBuild(RobotType typeToBuild, int num) throws GameActionException {
		
    	Direction buildDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canBuildRobot(typeToBuild, buildDirection) && rc.onTheMap(rc.getLocation().add(buildDirection, 5f))) {
				rc.buildRobot(typeToBuild, buildDirection);
				return true;
			}
			buildDirection = buildDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
	}

	public static void broadcastUnassignedScout() throws GameActionException {
        Comms.writeStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END, rc.getLocation());
    }
    /*
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
    */
    public static boolean plantTrees(MapLocation myLocation) throws GameActionException {
    	
    	Direction plantDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canPlantTree(plantDirection) && checkIcanEscape(myLocation, plantDirection)) {
				rc.plantTree(plantDirection);
				return true;
			}
			plantDirection = plantDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
    }
    
    public static boolean checkIcanEscape(MapLocation myLocation, Direction plantDirection) throws GameActionException {
    	plantDirection = plantDirection.rotateLeftRads((float) Math.PI * 0.3333333f);
    	int occupied = 0;
    	for (int i = 0; i < 5; i++) {
    		//rc.setIndicatorDot(myLocation.add(plantDirection, 2f), 255, 0, 0);
    		if(rc.isCircleOccupiedExceptByThisRobot(myLocation.add(plantDirection, 2f), 2f)) {
    			occupied++;
    		}
    		plantDirection = plantDirection.rotateLeftRads((float) Math.PI * 0.3333333f);
    	}
    	//System.out.format("Spaces occupied: %d\n", occupied);
    	return occupied < 4;
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
    	TreeInfo[] closeTrees = rc.senseNearbyTrees(-1, rc.getTeam());
    	if(closeTrees.length == 0) {
    		return false;
    	}
    	float lowestHealth = 1000;
    	TreeInfo lowestHealthTree = null;
    	for (TreeInfo treeInfo : closeTrees) {
            if(treeInfo.getHealth() < lowestHealth) {
            	lowestHealth = treeInfo.getHealth();
            	lowestHealthTree = treeInfo;
            }
        }
        if(Nav.pathTo(rc, lowestHealthTree.getLocation())) {
        	return true;
        }
    	return false;
    }

}
