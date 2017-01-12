package turtlebot;

import battlecode.common.*;

public class BotArchon {

	public static final float GARDEN_DISTANCE = 10f;
	public static final int EXPLORE_SPOKES = 24;

	public static int count = 0;
	public static boolean startupFlag = true;
	public static MapLocation[] exploreLocations = new MapLocation[EXPLORE_SPOKES];
	public static int exploreLocationsPointer = 0;

	public static void turn(RobotController rc) throws GameActionException {

		MapLocation selfLoc = rc.getLocation();
		
		if (!Nav.avoidBullets(rc, selfLoc) && !Nav.explore(rc)) {
			//System.out.format("\nCouldn't move");
		}
		
		// Donate all of our bullets if we can  win or the game's about to end
		if (rc.getTeamBullets() >= 10000 || rc.getRoundNum() == rc.getRoundLimit() - 1) {
			rc.donate(rc.getTeamBullets());
		}


		if (rc.getRoundNum() == 1) {
			Comms.writeGarden(rc, selfLoc);
			Direction exploreDir = new Direction(0);
			for (int i=0; i < EXPLORE_SPOKES; i++) {
				exploreLocations[i] = selfLoc.add(exploreDir,100f);
				exploreDir = exploreDir.rotateLeftRads(7 * (float) Math.PI / EXPLORE_SPOKES);
			}
		}

		if (count <= 7) {
			//if(rc.onTheMap(selfLoc, RobotType.ARCHON.sensorRadius-3))
				count += BotArchon.tryHireGardener(rc) ? 1 : 0;
		}
		
		while(checkUnassignedScout(rc)) {}

	}

	public static boolean tryHireGardener(RobotController rc) throws GameActionException {
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
	
	public static void delegateScout(RobotController rc) throws GameActionException {
		MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		int numEnemyArchons = enemyArchons.length;
		MapLocation delegationLocation = (exploreLocationsPointer < numEnemyArchons) 
				? enemyArchons[exploreLocationsPointer] : exploreLocations[exploreLocationsPointer - numEnemyArchons];
        Comms.writeStack(rc, Comms.ARCHON_SCOUT_DELEGATION_START, Comms.ARCHON_SCOUT_DELEGATION_END, delegationLocation);
        exploreLocationsPointer++;
        exploreLocationsPointer %= (exploreLocations.length + numEnemyArchons);
    }
    
    public static boolean checkUnassignedScout(RobotController rc) throws GameActionException {
         MapLocation unassignedCheck = Comms.popStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END);
         if(unassignedCheck != null) {
            delegateScout(rc);
            System.out.println("DELEGATED");
            return true;
        }
        return false;
    }
}
