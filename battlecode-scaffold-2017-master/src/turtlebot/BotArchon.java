package turtlebot;

import battlecode.common.*;

public class BotArchon {

	public static final float GARDEN_DISTANCE = 10f;
	public static final int EXPLORE_SPOKES = 24;

	public static int count = 0;
	public static boolean startupFlag = true;
	public static Direction[] exploreDirections = new Direction[EXPLORE_SPOKES];
	public static int exploreDirectionsPointer = 0;

	public static void turn(RobotController rc) throws GameActionException {

		if (!Nav.explore(rc)) {
			System.out.format("\nCouldn't move");
		}
		
		// Donate all of our bullets if we can  win or the game's about to end
		if (rc.getTeamBullets() >= 10000 || rc.getRoundNum() == rc.getRoundLimit() - 1) {
			rc.donate(rc.getTeamBullets());
		}

		MapLocation selfLoc = rc.getLocation();

		if (rc.getRoundNum() == 1) {
			Comms.writeGarden(rc, selfLoc);
			Direction exploreDir = new Direction(0);
			for (int i=0; i < EXPLORE_SPOKES; i++) {
				exploreDirections[i] = exploreDir;
				exploreDir = exploreDir.rotateLeftRads(7 * (float) Math.PI / EXPLORE_SPOKES);
			}
		}

		// Nav.avoidBullets(rc, selfLoc);

		if (count <= 7) {
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
        MapLocation exploreLocation = rc.getInitialArchonLocations(rc.getTeam())[0].add(exploreDirections[exploreDirectionsPointer], 100f);
        Comms.writeStack(rc, Comms.ARCHON_SCOUT_DELEGATION_START, Comms.ARCHON_SCOUT_DELEGATION_END, exploreLocation);
        exploreDirectionsPointer++;
        exploreDirectionsPointer %= EXPLORE_SPOKES;
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
