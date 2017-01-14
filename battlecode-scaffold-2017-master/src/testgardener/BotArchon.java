package testgardener;

import battlecode.common.*;

public class BotArchon {

	public static RobotController rc;
	
	public static final float GARDEN_DISTANCE = 10f;
	public static final int EXPLORE_SPOKES = 24;

	public static int count = 0;
	public static boolean startupFlag = true;
	public static MapLocation[] exploreLocations = new MapLocation[EXPLORE_SPOKES];
	public static int exploreLocationsPointer = 0;

	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		
		int scoutsBuilt = Comms.readNumRobots(rc, RobotType.SCOUT);
    	int lumberjacksBuilt = Comms.readNumRobots(rc, RobotType.LUMBERJACK);
    	int gardenersBuilt = Comms.readNumRobots(rc, RobotType.GARDENER);
		
		//System.out.format("\nScouts: %d", scoutsBuilt);
		
		MapLocation selfLoc = rc.getLocation();
		
		// Only one archon should set the spawn list
		if(rc.getRoundNum() == 1 && selfLoc == rc.getInitialArchonLocations(rc.getTeam())[0]) {
			Comms.writeBuildStack(rc, RobotType.SCOUT, 0);
			Comms.writeBuildStack(rc, RobotType.SCOUT, 0);
			Comms.writeBuildStack(rc, RobotType.SOLDIER, 0);
			System.out.println(Comms.popBuildStack(rc));
		}

		TreeInfo[] allTrees = rc.senseNearbyTrees(-1);
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		boolean moved = false;
		if (!Nav.avoidBullets(rc, selfLoc)) {
			if(enemies.length > 0) {
				moved = Nav.simpleRunAway(rc, selfLoc, enemies, allTrees);
			}
			if(!moved) {
				TreeInfo[] ourTrees = rc.senseNearbyTrees(-1, rc.getTeam());
				if(ourTrees.length > 0) {
					if(!Nav.pathTo(rc, ourTrees[0].location)) {
						Nav.explore(rc);
					}
				} else {
					Nav.explore(rc);
				}
			}
			//System.out.format("\nCouldn't move");
		}
		
		// Donate all of our bullets if we can  win or the game's about to end
		if (rc.getTeamBullets() >= 10000 || rc.getRoundNum() == rc.getRoundLimit() - 1) {
			rc.donate(rc.getTeamBullets());
		}
    	
		if (rc.getRoundNum() == 1) {
			Direction exploreDir = new Direction(0);
			for (int i=0; i < EXPLORE_SPOKES; i++) {
				exploreLocations[i] = selfLoc.add(exploreDir,100f);
				exploreDir = exploreDir.rotateLeftRads(7 * (float) Math.PI / EXPLORE_SPOKES);
			}
			Comms.writeAttackEnemy(rc, rc.getInitialArchonLocations(rc.getTeam().opponent())[0], 0);
		}

		/*
		if ( (count <= 7
				&& !(Comms.readGardenerUniversalHoldRound(rc) <= rc.getRoundNum()
					&& Comms.readGardenerUniversalHoldLocation(rc).distanceTo(selfLoc) <= 20f)
						&& rc.getTeamBullets() > 60 && rc.getRoundNum() >= 20) 
				|| count < 1
				) {
			//if(rc.onTheMap(selfLoc, RobotType.ARCHON.sensorRadius-3))
				count += BotArchon.tryHireGardener(rc) ? 1 : 0;
		}
		*/
		
		if(gardenersBuilt <= (1 + rc.getRoundNum() / 100)) {
			gardenersBuilt += tryHireGardener() ? 1 : 0;
			Comms.writeNumRobots(rc, RobotType.GARDENER, gardenersBuilt);
		}
		
		while(checkUnassignedScout()) {}

	}

	public static boolean tryHireGardener() throws GameActionException {
		Direction hireDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canHireGardener(hireDirection) && rc.onTheMap(rc.getLocation().add(hireDirection, 5f))) {
				rc.hireGardener(hireDirection);
				return true;
			}
			hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
	}
	
	public static void delegateScout() throws GameActionException {
		MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		int numEnemyArchons = enemyArchons.length;
		MapLocation delegationLocation = (exploreLocationsPointer < numEnemyArchons) 
				? enemyArchons[exploreLocationsPointer] : exploreLocations[exploreLocationsPointer - numEnemyArchons];
        Comms.writeStack(rc, Comms.ARCHON_SCOUT_DELEGATION_START, Comms.ARCHON_SCOUT_DELEGATION_END, delegationLocation);
        exploreLocationsPointer++;
        exploreLocationsPointer %= (exploreLocations.length + numEnemyArchons);
    }
    
    public static boolean checkUnassignedScout() throws GameActionException {
         MapLocation unassignedCheck = Comms.popStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END);
         if(unassignedCheck != null) {
            delegateScout();
            System.out.println("DELEGATED");
            return true;
        }
        return false;
    }
}
